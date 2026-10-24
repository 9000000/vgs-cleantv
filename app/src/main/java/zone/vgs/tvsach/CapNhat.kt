package zone.vgs.tvsach

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cập nhật qua mạng từ GitHub (9000000/vgs-cleantv/cap-nhat.json):
 *  - dữ liệu (danh sách mẫu tivi, APK Home sạch): tải lặng lẽ vào bộ nhớ app, không cần cài lại;
 *  - bản app mới: tải APK từ GitHub Releases rồi mở hộp cài của Android (người dùng bấm Cài đặt).
 */
object CapNhat {
    const val GITHUB_REPO = "9000000/vgs-cleantv"
    val URL_CAP_NHAT = listOf(
        "https://raw.githubusercontent.com/$GITHUB_REPO/tet/cap-nhat.json",
        "https://raw.githubusercontent.com/$GITHUB_REPO/main/cap-nhat.json",
        "https://raw.githubusercontent.com/$GITHUB_REPO/HEAD/cap-nhat.json"
    )

    data class BanMoi(val versionCode: Int, val versionName: String, val apk: String, val ghiChu: String)

    /** File dữ liệu đã tải về (nếu có) — DuLieu và launcher đọc file này trước assets. */
    fun fileDuLieu(ctx: Context, ten: String) = File(ctx.filesDir, "ota/$ten")

    /**
     * App vừa lên bản mới → dữ liệu OTA tải lúc trước có thể CŨ hơn đồ đóng sẵn trong bản mới.
     * Xoá đi, dùng đồ đóng sẵn cho tới lần tải kế.
     */
    fun donOtaCuNeuDoiBan(ctx: Context) {
        val p = ctx.getSharedPreferences("ota", Context.MODE_PRIVATE)
        if (p.getInt("cho_ban", -1) == BuildConfig.VERSION_CODE) return
        File(ctx.filesDir, "ota").deleteRecursively()
        p.edit().clear().putInt("cho_ban", BuildConfig.VERSION_CODE).apply()
    }

    fun moAsset(ctx: Context, ten: String): java.io.InputStream {
        val f = fileDuLieu(ctx, ten)
        return if (f.isFile && f.length() > 0) f.inputStream() else ctx.assets.open(ten)
    }

    private fun tai(urlBanDau: String, giay: Int = 30): ByteArray {
        var urlHienTai = urlBanDau
        var chuyenHuong = 0
        while (chuyenHuong < 5) {
            val c = URL(urlHienTai).openConnection() as HttpURLConnection
            c.connectTimeout = giay * 1000
            c.readTimeout = giay * 1000
            c.instanceFollowRedirects = true
            c.setRequestProperty("User-Agent", "CleanTV-Android/${BuildConfig.VERSION_NAME}")
            c.setRequestProperty("Cache-Control", "no-cache")
            try {
                val code = c.responseCode
                if (code in 301..308) {
                    val moi = c.getHeaderField("Location") ?: error("Redirect thiếu Location")
                    urlHienTai = if (moi.startsWith("http")) moi else URL(URL(urlHienTai), moi).toString()
                    chuyenHuong++
                    continue
                }
                if (code != 200) error("HTTP $code $urlHienTai")
                return c.inputStream.use { it.readBytes() }
            } finally {
                c.disconnect()
            }
        }
        error("Chuyển hướng quá nhiều lần: $urlBanDau")
    }

    private fun taiJsonCapNhat(): JSONObject {
        var loiCuoi: Exception? = null
        for (u in URL_CAP_NHAT) {
            try {
                val bytes = tai(u, 15)
                return JSONObject(String(bytes, Charsets.UTF_8))
            } catch (e: Exception) {
                loiCuoi = e
            }
        }
        throw loiCuoi ?: error("Không thể tải cap-nhat.json từ GitHub $GITHUB_REPO")
    }

    /**
     * Đọc cap-nhat.json từ GitHub, kéo dữ liệu mới về nếu khác bản đang có.
     * Trả về bản app mới nếu có (versionCode cao hơn bản đang chạy).
     */
    fun kiemTra(ctx: Context): BanMoi? {
        val j = taiJsonCapNhat()
        val prefs = ctx.getSharedPreferences("ota", Context.MODE_PRIVATE)
        j.optJSONObject("duLieu")?.let { dl ->
            for (ten in dl.keys()) {
                val o = dl.getJSONObject(ten)
                if (o.optInt("tuBan", 0) < BuildConfig.VERSION_CODE) { fileDuLieu(ctx, ten).delete(); continue }
                val phienBan = o.getString("phienBan")
                if (prefs.getString(ten, "") == phienBan && fileDuLieu(ctx, ten).isFile) continue
                runCatching {
                    val path = o.getString("url")
                    val otaUrl = if (path.startsWith("http")) path else "https://raw.githubusercontent.com/$GITHUB_REPO/tet/web$path"
                    val bytes = tai(otaUrl, 60)
                    if (bytes.size.toLong() != o.optLong("co", bytes.size.toLong())) error("Sai cỡ $ten")
                    val f = fileDuLieu(ctx, ten).apply { parentFile?.mkdirs() }
                    val tam = File(f.path + ".tam")
                    tam.writeBytes(bytes)
                    if (!tam.renameTo(f)) error("Không ghi được $ten")
                    prefs.edit().putString(ten, phienBan).apply()
                }
            }
        }
        return banApp(j)
    }

    /** Chỉ đọc bản app mới (không kéo dữ liệu) — cho việc kiểm tra chạy nền. */
    fun kiemTraApp(): BanMoi? = banApp(taiJsonCapNhat())

    private fun banApp(j: JSONObject): BanMoi? {
        val app = j.optJSONObject("app") ?: return null
        val code = app.optInt("versionCode")
        val apkRaw = app.optString("apk")
        val apkUrl = when {
            apkRaw.startsWith("http://") || apkRaw.startsWith("https://") -> apkRaw
            apkRaw.startsWith("/") -> "https://raw.githubusercontent.com/$GITHUB_REPO/tet/web$apkRaw"
            else -> "https://raw.githubusercontent.com/$GITHUB_REPO/tet/web/$apkRaw"
        }
        return if (code > BuildConfig.VERSION_CODE)
            BanMoi(code, app.optString("versionName"), apkUrl,
                t(app.optString("ghiChu"), app.optString("ghiChuEn").ifBlank { app.optString("ghiChu") }))
        else null
    }

    /** Người dùng chạm thông báo "có bản mới" để mở app → tải và cài luôn, khỏi hỏi lại. */
    @Volatile var moTuThongBao = false

    /** Tải APK bản mới vào cache rồi mở hộp cài. */
    fun taiVaCai(ctx: Context, ban: BanMoi) {
        val f = File(ctx.cacheDir, "cap-nhat/CleanTV-${ban.versionName}.apk").apply { parentFile?.mkdirs() }
        f.writeBytes(tai(ban.apk, 180))
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.files", f)
        ctx.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}

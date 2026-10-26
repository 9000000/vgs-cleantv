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
    const val BRANCH = "tet"
    val URL_CAP_NHAT = listOf(
        "https://raw.githubusercontent.com/$GITHUB_REPO/$BRANCH/cap-nhat.json",
        "https://raw.githubusercontent.com/$GITHUB_REPO/$BRANCH/web/cap-nhat.json"
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

    const val URL_LATEST_RELEASE = "https://github.com/$GITHUB_REPO/releases/latest"
    const val URL_API_LATEST = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"

    /**
     * So sánh 2 chuỗi phiên bản (Semantic Versioning), ví dụ "3.0.13" vs "3.0.12".
     * Trả về true nếu banMoi lớn hơn banHienTai.
     */
    fun laBanMoiHon(banMoi: String, banHienTai: String): Boolean {
        if (banMoi.isBlank()) return false
        val regex = Regex("[^0-9.]")
        val cleanMoi = regex.replace(banMoi, "").split(".").mapNotNull { it.toIntOrNull() }
        val cleanHienTai = regex.replace(banHienTai, "").split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(cleanMoi.size, cleanHienTai.size)
        for (i in 0 until maxLen) {
            val vMoi = cleanMoi.getOrElse(i) { 0 }
            val vHienTai = cleanHienTai.getOrElse(i) { 0 }
            if (vMoi > vHienTai) return true
            if (vMoi < vHienTai) return false
        }
        return false
    }

    /**
     * Nhận diện bản mới trực tiếp từ https://github.com/9000000/vgs-cleantv/releases/latest.
     * 1. Thử qua GitHub API (lấy tag_name, browser_download_url và changelog chi tiết).
     * 2. Nếu API bị giới hạn (403), đọc chuyển hướng Location từ https://github.com/.../releases/latest.
     */
    fun kiemTraTuGitHubReleases(): BanMoi? {
        // 1. Thử qua GitHub API
        runCatching {
            val c = URL(URL_API_LATEST).openConnection() as HttpURLConnection
            c.connectTimeout = 10000
            c.readTimeout = 10000
            c.setRequestProperty("User-Agent", "CleanTV-Android/${BuildConfig.VERSION_NAME}")
            c.setRequestProperty("Accept", "application/vnd.github.v3+json")
            if (c.responseCode == 200) {
                val j = JSONObject(String(c.inputStream.use { it.readBytes() }, Charsets.UTF_8))
                val tag = j.optString("tag_name", "").trim()
                val versionName = tag.removePrefix("v").removePrefix("V")
                if (laBanMoiHon(versionName, BuildConfig.VERSION_NAME)) {
                    val assets = j.optJSONArray("assets")
                    var downloadUrl = ""
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val a = assets.optJSONObject(i) ?: continue
                            val name = a.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                downloadUrl = a.optString("browser_download_url", "")
                                if (name.contains(versionName)) break
                            }
                        }
                    }
                    if (downloadUrl.isEmpty()) {
                        downloadUrl = "https://github.com/$GITHUB_REPO/releases/download/$tag/CleanTV-$tag.apk"
                    }
                    val body = j.optString("body", "").trim()
                    val firstLine = body.lines().firstOrNull { it.isNotBlank() }?.replace("#", "")?.trim()
                    val ghiChu = if (!firstLine.isNullOrBlank()) firstLine else "CleanTV $tag"
                    return BanMoi(0, versionName, downloadUrl, ghiChu)
                }
            }
        }

        // 2. Thử qua HTTP redirect Location của trang web releases/latest
        runCatching {
            val c = URL(URL_LATEST_RELEASE).openConnection() as HttpURLConnection
            c.connectTimeout = 10000
            c.readTimeout = 10000
            c.instanceFollowRedirects = false
            c.setRequestProperty("User-Agent", "CleanTV-Android/${BuildConfig.VERSION_NAME}")
            val code = c.responseCode
            val targetUrl = if (code in 301..308) {
                c.getHeaderField("Location") ?: ""
            } else {
                c.url.toString()
            }
            if (targetUrl.contains("/tag/")) {
                val tag = targetUrl.substringAfterLast("/tag/").substringBefore("?").substringBefore("#").trim()
                val versionName = tag.removePrefix("v").removePrefix("V")
                if (laBanMoiHon(versionName, BuildConfig.VERSION_NAME)) {
                    val downloadUrl = "https://github.com/$GITHUB_REPO/releases/download/$tag/CleanTV-$tag.apk"
                    return BanMoi(0, versionName, downloadUrl, "CleanTV $tag")
                }
            }
        }

        return null
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
     * Nhận diện bản app mới (ưu tiên GitHub releases/latest, fallback cap-nhat.json),
     * đồng thời kéo dữ liệu OTA mới về nếu có khác biệt.
     */
    fun kiemTra(ctx: Context): BanMoi? {
        val otaJson = runCatching { taiJsonCapNhat() }.getOrNull()
        val prefs = ctx.getSharedPreferences("ota", Context.MODE_PRIVATE)
        otaJson?.optJSONObject("duLieu")?.let { dl ->
            for (ten in dl.keys()) {
                val o = dl.getJSONObject(ten)
                if (o.optInt("tuBan", 0) < BuildConfig.VERSION_CODE) { fileDuLieu(ctx, ten).delete(); continue }
                val phienBan = o.getString("phienBan")
                if (prefs.getString(ten, "") == phienBan && fileDuLieu(ctx, ten).isFile) continue
                runCatching {
                    val path = o.getString("url")
                    val otaUrl = if (path.startsWith("http")) path else "https://raw.githubusercontent.com/$GITHUB_REPO/$BRANCH/web$path"
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
        return kiemTraTuGitHubReleases() ?: otaJson?.let { banApp(it) }
    }

    /** Chỉ đọc bản app mới (không kéo dữ liệu) — cho việc kiểm tra chạy nền. */
    fun kiemTraApp(): BanMoi? {
        return kiemTraTuGitHubReleases() ?: runCatching { banApp(taiJsonCapNhat()) }.getOrNull()
    }

    private fun banApp(j: JSONObject): BanMoi? {
        val app = j.optJSONObject("app") ?: return null
        val code = app.optInt("versionCode")
        val versionName = app.optString("versionName")
        val apkRaw = app.optString("apk")
        val apkUrl = when {
            apkRaw.startsWith("http://") || apkRaw.startsWith("https://") -> apkRaw
            apkRaw.startsWith("/") -> "https://raw.githubusercontent.com/$GITHUB_REPO/$BRANCH/web$apkRaw"
            else -> "https://raw.githubusercontent.com/$GITHUB_REPO/$BRANCH/web/$apkRaw"
        }
        // Bảo vệ độc quyền: Chỉ chấp nhận APK phát hành từ repo của dự án này, chặn hoàn toàn link từ repo gốc
        val tuNguonHopLe = apkUrl.contains("github.com/$GITHUB_REPO/") || apkUrl.contains("raw.githubusercontent.com/$GITHUB_REPO/")
        if (!tuNguonHopLe) return null

        val coBanMoi = code > BuildConfig.VERSION_CODE || (versionName.isNotBlank() && laBanMoiHon(versionName, BuildConfig.VERSION_NAME))
        return if (coBanMoi)
            BanMoi(code, versionName, apkUrl,
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

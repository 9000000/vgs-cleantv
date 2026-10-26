package zone.vgs.tvsach.dulieu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import java.io.ByteArrayOutputStream
import org.json.JSONObject

/**
 * Danh sách kênh của VTVgo (3.0.0, góp ý caicotdien04, xemtivi661 26/10/2026: thêm kênh địa phương như Vĩnh Long 1,
 * HTV9, Quảng Ngãi). Lấy từ API công khai display/v21.0/catalogs/with-channels (không cần đăng nhập), nhớ vào máy để
 * lần sau mở không cần mạng; mất mạng thì chỉ còn VTV1–9 có sẵn.
 */
object KenhVtvGo {
    data class Kenh(val ten: String, val ma: String, val nhom: String, val anh: String, val canDangNhap: Boolean) {
        /** Tên trên ô: bỏ chữ "Truyền hình" cho gọn, tối đa 24 ký tự (giới hạn của link_tiles). */
        val tenO: String get() = tenNgan(ten)
    }

    private const val API = "https://api.vtvdigital.org/display/v21.0/catalogs/with-channels"
    private const val PREF = "kenh_vtvgo"

    fun tenNgan(ten: String): String = ten.removePrefix("Truyền hình ").trim().take(24).trim()

    /** Tên gói giả / tên file ảnh của ô: như LinkTiles của launcher (chữ thường, chỉ a-z 0-9). */
    fun slug(ten: String): String = ten.lowercase(java.util.Locale.ROOT).replace(Regex("[^a-z0-9]"), "")

    fun docNho(ctx: Context): List<Kenh> =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString("ds", null)?.let { runCatching { tach(it) }.getOrNull() } ?: emptyList()

    /** Tải danh sách mới (gọi trên luồng IO); lỗi mạng thì trả bản nhớ. */
    fun tai(ctx: Context): List<Kenh> = runCatching {
        val c = java.net.URL(API).openConnection() as java.net.HttpURLConnection
        c.connectTimeout = 8000; c.readTimeout = 10000
        val chu = c.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        val ds = tach(chu)
        if (ds.isNotEmpty()) ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString("ds", chu).apply()
        ds
    }.getOrElse { docNho(ctx) }

    fun tach(json: String): List<Kenh> {
        val out = mutableListOf<Kenh>(); val da = mutableSetOf<String>()
        val data = JSONObject(json).optJSONArray("data") ?: return out
        for (i in 0 until data.length()) {
            val nhom = data.optJSONObject(i) ?: continue
            val ds = nhom.optJSONArray("channels") ?: continue
            for (k in 0 until ds.length()) {
                val c = ds.optJSONObject(k) ?: continue
                val ma = c.optString("id"); val ten = c.optString("name").trim()
                if (ma.isBlank() || ten.isBlank() || !c.optBoolean("enabled", true) || !da.add(ma)) continue
                if (!Regex("[A-Za-z0-9_-]{1,60}").matches(ma) || slug(tenNgan(ten)).isEmpty()) continue
                out += Kenh(ten, ma, nhom.optString("name").trim(), c.optString("thumbnail").ifBlank { c.optString("logo") }, c.optBoolean("mustLogin", false))
            }
        }
        return out
    }

    /** Ảnh ô kênh 320×180 JPEG từ thumbnail của VTVgo (webp), null nếu không tải được. */
    fun taiAnh(url: String): ByteArray? = runCatching {
        if (!url.startsWith("https://")) return null
        val c = java.net.URL(url).openConnection() as java.net.HttpURLConnection
        c.connectTimeout = 8000; c.readTimeout = 10000
        val goc = c.inputStream.use { BitmapFactory.decodeStream(it) } ?: return null
        val ra = Bitmap.createBitmap(320, 180, Bitmap.Config.ARGB_8888)
        val cv = Canvas(ra); cv.drawColor(Color.WHITE)
        // Vừa khung, giữ tỉ lệ, giữa ô.
        val tl = minOf(320f / goc.width, 180f / goc.height)
        val w = (goc.width * tl).toInt(); val h = (goc.height * tl).toInt()
        cv.drawBitmap(goc, null, Rect((320 - w) / 2, (180 - h) / 2, (320 + w) / 2, (180 + h) / 2), Paint(Paint.FILTER_BITMAP_FLAG))
        ByteArrayOutputStream().also { ra.compress(Bitmap.CompressFormat.JPEG, 90, it) }.toByteArray()
    }.getOrNull()
}

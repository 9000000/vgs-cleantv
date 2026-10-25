package zone.vgs.tvsach

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Số liệu ẩn danh (21/10/2026): mở app, cập nhật, dọn tivi, cài Home sạch, khôi phục, ghim nút máy chiếu.
 * Chỉ gửi một mã máy ngẫu nhiên tự sinh, số bản app và tên mẫu tivi — không email, không tên, không danh sách app.
 * Máy chủ: Cloudflare Worker cleantv-dem (tv-sach/dem). Gửi lỗi thì thôi, không ảnh hưởng gì.
 */
object Dem {
    private const val DIA_CHI = "https://cleantv-dem.vgszone.workers.dev/su-kien"

    private fun maMay(ctx: Context): String {
        val p = ctx.getSharedPreferences("dem", Context.MODE_PRIVATE)
        return p.getString("may", null) ?: UUID.randomUUID().toString().also { p.edit().putString("may", it).apply() }
    }

    fun gui(ctx: Context, loai: String, tivi: String = "", so: Int = 0, them: String = "") {
        // Tắt hoàn toàn việc gửi số liệu phân tích về máy chủ của dự án gốc (vgszone)
        return
    }

    /**
     * Danh sách gói người dùng TỰ BẤM gửi ở màn Xong (23/10/2026): model tivi, gói đang tắt, gói app giữ lại, đánh giá
     * và ghi chú — để làm mẫu cho tivi chưa có trong danh sách.
     */
    fun guiDanhSach(ctx: Context, du: JSONObject): Boolean {
        // Không gửi thông tin tivi lên máy chủ bên thứ ba
        return true
    }

    /** Gọi lúc mở app: "mo" mỗi ngày một lần; "cap-nhat" khi số bản khác lần trước. */
    fun moApp(ctx: Context) {
        val p = ctx.getSharedPreferences("dem", Context.MODE_PRIVATE)
        val banCu = p.getString("ban", null)
        if (banCu != null && banCu != BuildConfig.VERSION_NAME) gui(ctx, "cap-nhat", them = "$banCu→${BuildConfig.VERSION_NAME}")
        val homNay = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        if (p.getString("ngay_mo", "") != homNay) gui(ctx, "mo")
        p.edit().putString("ban", BuildConfig.VERSION_NAME).putString("ngay_mo", homNay).apply()
    }
}

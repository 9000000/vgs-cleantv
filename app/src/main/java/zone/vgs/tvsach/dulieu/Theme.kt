package zone.vgs.tvsach.dulieu

import org.json.JSONObject

/**
 * Giao diện cho Home sạch: các khoá config.json của launcher (upstream themes/README.md)
 * cộng một ảnh nền tuỳ chọn trong assets/theme/.
 */
data class Theme(
    val id: String,
    val tenVi: String,
    val tenEn: String,
    val moTaVi: String,
    val moTaEn: String,
    val cauHinh: Map<String, Any>,
    val anhNen: String? = null,
    val mau: List<Long>,
    /** Thư mục assets chứa BỘ ảnh nền xoay vòng (đẩy vào wallpapers/ của launcher); ảnh xem trước trong app. */
    val boAnh: String? = null,
    val anhXem: String? = null,
) {
    fun ghiVao(o: JSONObject) {
        cauHinh.forEach { (k, v) -> o.put(k, v) }
        o.put("wallpaper", anhNen != null || boAnh != null)
    }
}

val THEMES = listOf(
    // 1. Thương hiệu CleanTV
    Theme("vgs", "VGS", "VGS", "Logo VGS tím, nền đổi sắc theo giờ, dock trong suốt", "Violet VGS logo, sky follows the time of day, transparent dock",
        mapOf("background_color" to "#140A3A", "sky_intensity" to 55, "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false),
        "theme/vgs.jpg", listOf(0xFF0D0A38, 0xFF22168A, 0xFF4C1FD0)),

    // 2. Tối giản đen tuyền Apple TV style
    Theme("toi-gian", "Tối giản", "Minimal", "Nền đen tuyền, kiểu Apple TV, dock trong suốt", "Pure black, Apple TV style, transparent dock",
        mapOf("background_color" to "#000000", "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false),
        null, listOf(0xFF000000, 0xFF111111)),

    // 3. 14 tranh phong cảnh code xoay mỗi giờ
    Theme("phong-canh", "Phong cảnh", "Scenes", "14 tranh phong cảnh, tự đổi mỗi giờ, dock trong suốt", "14 landscapes, a new one every hour, transparent dock",
        mapOf("wallpaper_rotation_minutes" to 60, "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false, "background_color" to "#05060A"),
        null, listOf(0xFF1A2236, 0xFF4A3A5A), boAnh = "theme/phong-canh", anhXem = "theme/phong-canh/02-golden-coast.jpg"),

    // 4. Thụy Điển xoay vòng 6 cột
    Theme("sweden", "Thụy Điển", "Sweden", "3 ảnh phong cảnh đổi mỗi giờ, dock trong suốt 6 cột", "3 rotating landscapes, transparent 6-column dock",
        mapOf("columns" to 6, "rows_below_dock" to 1, "background_color" to "#05070C", "wallpaper_rotation_minutes" to 60,
            "wallpaper_order" to "sequential", "wallpaper_captions" to true, "cinematic_home" to false, "dock_title" to false,
            "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false, "dock_even_padding" to true, "icon_only_buttons" to true,
            "icon_only_inputs" to true, "ambient_glow" to false, "app_glow" to false, "fade_rows" to true,
            "focus_overshoot" to false, "advanced_effects" to true),
        null, listOf(0xFF05070C, 0xFF142233, 0xFF283E55), boAnh = "theme/sweden", anhXem = "theme/sweden/001.jpg"),

    // 5. Trong suốt nguyên bản
    Theme("glass", "Kính trong suốt", "Transparent Glass", "Dock trong suốt không dải nền, thanh trên điện ảnh, nút tròn", "Transparent borderless dock, cinematic top shelf, icon-only buttons",
        mapOf("background_color" to "#090C14", "cinematic_home" to true, "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false, "icon_only_buttons" to true,
            "app_glow" to true, "glow_style" to "horizon", "focus_overshoot" to false, "advanced_effects" to true),
        "theme/glass.jpg", listOf(0xFF090C14, 0xFF141A29, 0xFF2A1C4D)),

    // 6. Cyberpunk Neon Đêm
    Theme("neon-cyber", "Neon Đêm", "Cyberpunk Neon", "Thành phố neon tương lai trong mưa, dock trong suốt, viền sáng horizon", "Rainy cyberpunk neon city, transparent dock, horizon glow",
        mapOf("background_color" to "#080812", "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false, "icon_only_buttons" to true,
            "app_glow" to true, "glow_style" to "horizon", "advanced_effects" to true, "accent_color" to "#00F0FF", "wallpaper_dim" to 15),
        "theme/neon-cyber.jpg", listOf(0xFF080812, 0xFF180B28, 0xFF00E5FF)),

    // 7. Vũ trụ Tinh Vân tím hồng ambient glow
    Theme("tinh-van", "Tinh Vân", "Cosmic Nebula", "Vũ trụ sâu thẳm với bụi sao tím hồng, dock trong suốt, viền sáng nổi bật", "Deep cosmic nebula with violet star dust, transparent dock",
        mapOf("background_color" to "#04030A", "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false, "dock_even_padding" to true,
            "icon_only_buttons" to true, "ambient_glow" to true, "app_glow" to true, "focus_overshoot" to true, "advanced_effects" to true),
        "theme/tinh-van.jpg", listOf(0xFF04030A, 0xFF2A0845, 0xFF6441A5)),

    // 8. Ngọc Bích Emerald Matrix xanh lục bảo
    Theme("ngoc-bich", "Ngọc Bích", "Emerald Matrix", "Kính hình học xanh ngọc lục bảo sang trọng, dock trong suốt, viền sáng horizon", "Luxury emerald geometric dark glass, transparent dock, modern horizon glow",
        mapOf("background_color" to "#030C07", "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false, "dock_even_padding" to true,
            "icon_only_buttons" to true, "icon_only_inputs" to true, "app_glow" to true, "glow_style" to "horizon",
            "accent_color" to "#10B981", "advanced_effects" to true),
        "theme/ngoc-bich.jpg", listOf(0xFF030C07, 0xFF0B291A, 0xFF10B981)),

    // 9. Điện Ảnh Noir đen trắng tương phản cao
    Theme("den-trang", "Điện Ảnh Noir", "Monochrome Noir", "Nhiếp ảnh kiến trúc đen trắng tương phản cao, dock trong suốt, phong cách điện ảnh cổ điển", "High-contrast monochrome cityscape, transparent dock, classic cinematic noir style",
        mapOf("background_color" to "#050505", "cinematic_home" to true, "cinematic_white_focus" to true, "cinematic_scrim" to 80,
            "dock_title" to false, "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false, "ambient_glow" to false, "app_glow" to false,
            "fade_rows" to true, "focus_overshoot" to false),
        "theme/den-trang.jpg", listOf(0xFF050505, 0xFF222222, 0xFF666666)),

    // 10. Băng Giá Bắc Âu 6 cột app
    Theme("bang-gia", "Băng Giá Bắc Âu", "Nordic Winter", "Vịnh hẹp Na Uy tuyết phủ trong hoàng hôn bắc cực, 6 cột app, dock trong suốt", "Snowy Scandinavian fjord at twilight, crisp glacial blue, transparent 6-column dock",
        mapOf("columns" to 6, "rows_below_dock" to 1, "background_color" to "#060A12", "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false,
            "dock_even_padding" to true, "icon_only_buttons" to true, "icon_only_inputs" to true, "fade_rows" to true,
            "app_glow" to true, "focus_overshoot" to false),
        "theme/bang-gia.jpg", listOf(0xFF060A12, 0xFF142436, 0xFF4A7090)),

    // 11. Điện Ảnh Top Shelf 5 cột poster app
    Theme("cinematic-shelf", "Điện Ảnh Top Shelf", "Cinematic Top Shelf", "Poster app lớn làm mờ nửa trên, chấm vị trí dock, dock trong suốt không nền", "Cinematic blurred app artwork shelf, dock position dots, transparent borderless dock",
        mapOf("columns" to 5, "rows_below_dock" to 1, "cinematic_home" to true, "cinematic_art" to 65, "cinematic_scrim" to 75,
            "cinematic_dots" to true, "cinematic_white_focus" to true, "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false,
            "fade_rows" to true, "background_color" to "#060608"),
        "theme/cinematic-shelf.jpg", listOf(0xFF060608, 0xFF1C1A24, 0xFFE5A93C)),

    // 12. Lưới Đa Năng 7 cột full màn hình
    Theme("ultra-dashboard", "Lưới Đa Năng", "Ultra Dashboard", "7 cột ứng dụng trải rộng màn hình, dock trong suốt không dải nền, luôn hiện tên app", "7 columns full-screen dashboard, transparent dock, persistent app labels",
        mapOf("columns" to 7, "rows_below_dock" to 2, "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false, "dock_title" to false, "always_show_names" to true,
            "reorder_button" to true, "fade_rows" to true, "focus_overshoot" to false, "background_color" to "#0A0D14"),
        "theme/ultra-dashboard.jpg", listOf(0xFF0A0D14, 0xFF1E2638, 0xFF3B82F6)),

    // 13. Google TV Tinh Gọn 6 cột bánh răng top menu
    Theme("google-tv-pure", "Google TV Tinh Gọn", "Google TV Pure", "Gom nút đỉnh vào 1 bánh răng, cuộn mờ dần phía trên, dock trong suốt", "Condensed top menu gear, smooth fade-out rows, transparent dock",
        mapOf("columns" to 6, "rows_below_dock" to 2, "top_menu" to true, "fade_rows" to true, "cinematic_white_focus" to true,
            "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false, "focus_overshoot" to true, "background_color" to "#0E1116"),
        "theme/google-tv-pure.jpg", listOf(0xFF0E1116, 0xFF1F2430, 0xFF818CF8)),

    // 14. Bầu Trời Hoàng Kim 4 app trung tâm
    Theme("widgetv-gold", "Bầu Trời Hoàng Kim", "WidgeTV Sunset Gold", "Bầu trời động chuyển sắc theo giờ, viền chọn vàng kim vương giả, dock trong suốt 4 app", "Dynamic time-of-day sky backdrop, royal gold focus ring, transparent 4-app dock",
        mapOf("columns" to 4, "rows_below_dock" to 1, "sky_intensity" to 85, "accent_color" to "#E5A93C", "app_glow" to true,
            "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false, "dock_even_padding" to true, "icon_only_buttons" to true,
            "icon_only_inputs" to true, "background_color" to "#0B101D"),
        "theme/widgetv-gold.jpg", listOf(0xFF0B101D, 0xFF2D1F3D, 0xFFE5A93C)),

    // 15. Trung Tâm Chỉ Huy HUD hiện CPU/RAM
    Theme("kido-tactical", "Trung Tâm Chỉ Huy HUD", "KIDŌ Tactical HUD", "Hiện thông số CPU/RAM/Bộ nhớ dưới đồng hồ, dock trong suốt, viền xanh lục HUD", "Live CPU/RAM/Storage stats under clock, Wi-Fi status color, transparent tactical HUD dock",
        mapOf("columns" to 5, "rows_below_dock" to 1, "show_stats" to true, "wifi_status_color" to true, "dock_band" to false,
            "dock_opacity" to 0, "dock_glass" to false, "dock_title" to true, "app_glow" to true, "accent_color" to "#00FF66",
            "background_color" to "#020406", "wallpaper_dim" to 20),
        "theme/kido-tactical.jpg", listOf(0xFF020406, 0xFF082618, 0xFF00FF66)),

    // 16. Thung Lũng Tĩnh Tại 1 hàng 6 app OLED
    Theme("cosy-zen", "Thung Lũng Tĩnh Tại", "Cosy Valley Zen", "1 hàng 6 app dock trong suốt siêu tĩnh, chuyển động êm chậm, màn hình chờ OLED", "Ultra-minimal 1-row 6 apps, transparent dock, peaceful slow motion, OLED burn-in idle clock",
        mapOf("columns" to 6, "rows_below_dock" to 0, "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false, "dock_even_padding" to true,
            "dock_title" to false, "ambient_glow" to false, "app_glow" to false, "reduced_motion" to true,
            "focus_overshoot" to false, "home_idle_clock_minutes" to 5, "media_bar" to false, "background_color" to "#0D131A"),
        "theme/cosy-zen.jpg", listOf(0xFF0D131A, 0xFF242B28, 0xFFD97706)),
)



/** Thành phố cho dòng thời tiết trên Home sạch (weather_lat/lon/place của launcher). */
data class ThanhPho(val ten: String, val lat: Double, val lon: Double)

/**
 * 3.321 phường/xã sau sáp nhập 2025 kèm toạ độ (assets/xa.json, scripts/lam-xa.py) cho thời tiết theo xã (góp ý
 * 23/10/2026). [timXa] tìm không dấu: "ben thanh", "cua lo hoi an"…
 */
object XaPhuong {
    data class Xa(val ten: String, val tinh: String, val lat: Double, val lon: Double) {
        /** Tên ngắn hiện trên tivi: bỏ chữ "Phường"/"Xã"/"Đặc khu". */
        val tenNgan get() = ten.removePrefix("Phường ").removePrefix("Xã ").removePrefix("Đặc khu ")
        val khoaTim by lazy { boDau("$ten $tinh") }
    }
    @Volatile private var ds: List<Xa>? = null
    fun nap(ctx: android.content.Context): List<Xa> = ds ?: runCatching {
        val o = org.json.JSONObject(ctx.assets.open("xa.json").bufferedReader().readText())
        val t = o.getJSONArray("tinh"); val tinh = (0 until t.length()).map { t.getJSONArray(it).getString(0) }
        val x = o.getJSONArray("xa")
        (0 until x.length()).map { i -> x.getJSONArray(i).let { Xa(it.getString(0), tinh[it.getInt(1)], it.getDouble(2), it.getDouble(3)) } }
    }.getOrDefault(emptyList()).also { ds = it }

    fun boDau(s: String): String = java.text.Normalizer.normalize(s.lowercase(), java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "").replace('đ', 'd')

    fun timXa(ds: List<Xa>, q: String, toiDa: Int = 8): List<Xa> {
        val tu = boDau(q).split(' ').filter { it.isNotBlank() }
        if (tu.isEmpty()) return emptyList()
        return ds.filter { x -> tu.all { x.khoaTim.contains(it) } }
            .sortedBy { x -> if (boDau(x.tenNgan).startsWith(tu.joinToString(" "))) 0 else 1 }.take(toiDa)
    }
}

val THANH_PHO = listOf(
    ThanhPho("Hà Nội", 21.0285, 105.8542), ThanhPho("TP.HCM", 10.7769, 106.7009), ThanhPho("Đà Nẵng", 16.0544, 108.2022),
    ThanhPho("Hải Phòng", 20.8449, 106.6881), ThanhPho("Cần Thơ", 10.0452, 105.7469), ThanhPho("Vinh", 18.6796, 105.6813),
    ThanhPho("Huế", 16.4637, 107.5909), ThanhPho("Nha Trang", 12.2388, 109.1967), ThanhPho("Đà Lạt", 11.9404, 108.4583),
    ThanhPho("Quy Nhơn", 13.7820, 109.2192), ThanhPho("Buôn Ma Thuột", 12.6667, 108.0500), ThanhPho("Vũng Tàu", 10.3460, 107.0843),
    ThanhPho("Biên Hoà", 10.9574, 106.8426), ThanhPho("Thanh Hoá", 19.8067, 105.7852), ThanhPho("Nam Định", 20.4200, 106.1683),
    ThanhPho("Hạ Long", 20.9599, 107.0425), ThanhPho("Thái Nguyên", 21.5942, 105.8482), ThanhPho("Bắc Ninh", 21.1861, 106.0763),
)

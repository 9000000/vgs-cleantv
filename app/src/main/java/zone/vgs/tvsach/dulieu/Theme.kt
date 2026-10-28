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
        cauHinh.forEach { (k, v) ->
            if (v is Map<*, *>) o.put(k, JSONObject(v)) else o.put(k, v)
        }
        o.put("wallpaper", anhNen != null || boAnh != null)
        // Lần sau kết nối lại: mở đúng theme này, và biết khoá nào do theme ghi chứ không phải người dùng bật.
        o.put("vgs_theme", id)
    }
}

val THEMES = listOf(
    // 1. Thương hiệu CleanTV
    Theme(
        "vgs", "VGS", "VGS",
        "Logo VGS tím, nền đổi sắc theo giờ, dock trong suốt",
        "Violet VGS logo, sky follows the time of day, transparent dock",
        mapOf("background_color" to "#140A3A", "sky_intensity" to 55, "dock_band" to false, "dock_opacity" to 0, "dock_glass" to false),
        "theme/vgs.jpg",
        listOf(0xFF0D0A38, 0xFF22168A, 0xFF4C1FD0)
    ),

    // 2. Kiểu Apple TV (CleanTV 2.9.0)
    Theme(
        "apple-tv", "Kiểu Apple TV", "Apple TV style",
        "Nền theo màu app đang chọn, dock kính mờ, ô icon đều",
        "Backdrop in the focused app's colour, frosted dock, even icon tiles",
        mapOf(
            "background_color" to "#000000",
            "cinematic_home" to true,
            "cinematic_dots" to false,
            "cinematic_art" to 70,
            "cinematic_scrim" to 60,
            "cinematic_white_focus" to true,
            "uniform_tiles" to true,
            "dock_glass" to true,
            "show_greeting" to false,
            "header_white" to true,
            "fade_rows" to true,
            "rows_below_dock" to 1,
            "columns" to 5,
            "rows_offset_dp" to 90
        ),
        null,
        listOf(0xFF7A0A14, 0xFF0B3A6E, 0xFF050505)
    ),

    // 3. Kiểu macOS (CleanTV 3.5.14)
    Theme(
        "macos", "Kiểu macOS", "macOS style",
        "Thanh menu mờ, Dock kính sát đáy, nền logo VGS",
        "Translucent menu bar, glass Dock at the bottom, VGS logo wallpaper",
        mapOf(
            "background_color" to "#0B0820", "top_bar_band" to true, "dock_row" to true,
            "single_row" to true, "dock_band_all" to true, "dock_glass" to true,
            "uniform_tiles" to true, "show_greeting" to false, "header_white" to true,
            "icon_only_buttons" to true, "icon_only_inputs" to true, "rows_offset_dp" to 300,
            "columns" to 12, "dock_center" to true, "app_glow" to true, "advanced_effects" to true,
            "header_scale" to 80, "wallpaper_true_color" to true
        ),
        "theme/macos.jpg",
        listOf(0xFF2A1A7A, 0xFF7C3AED, 0xFFDB2777)
    ),


    // 3. Mặc định (Default)
    Theme(
        "default", "Mặc định", "Default",
        "Giao diện gốc dạng JSON, 4 cột, nền đen tuyền",
        "The built-in look written out as JSON, 4 columns, pure black background",
        mapOf(
            "dock_apps" to listOf("com.netflix.ninja", "com.amazon.amazonvideo.livingroom", "com.google.android.youtube.tv", "in.startv.hotstar"),
            "hidden_apps" to listOf("com.mediatek.wwtv.tvcenter", "com.mitv.gallery"),
            "columns" to 4,
            "rows_below_dock" to 0,
            "reduced_motion" to false,
            "background_color" to "#000000",
            "wallpaper" to false
        ),
        null,
        listOf(0xFF000000, 0xFF1A1A1A)
    ),

    // 4. Cực quang (Aurora)
    Theme(
        "aurora", "Cực quang", "Aurora",
        "Hình nền cực quang tối màu (xanh ngọc và tím trên nền navy)",
        "A dark aurora wallpaper (teal and violet on navy, darker at the bottom) with a matching background colour",
        mapOf("background_color" to "#070A14", "wallpaper" to true),
        "theme/aurora.jpg",
        listOf(0xFF070A14, 0xFF0E4F5C, 0xFF3B1E6E)
    ),

    // 5. Kính trong suốt (Glass)
    Theme(
        "glass", "Kính trong suốt", "Glass",
        "Nền xanh navy ánh tím ngọc, thanh trên điện ảnh, dock kính mờ, nút tròn icon",
        "Dark navy wallpaper with soft teal and violet light, cinematic top shelf, frosted-glass dock, icon-only buttons",
        mapOf(
            "dock_apps" to listOf(
                "com.netflix.ninja",
                "com.google.android.youtube.tv",
                "com.amazon.amazonvideo.livingroom",
                "com.disney.disneyplus",
                "com.wbd.stream",
                "com.apple.atve.androidtv.appletv"
            ),
            "background_color" to "#090C14",
            "wallpaper" to true,
            "cinematic_home" to true,
            "dock_glass" to true,
            "icon_only_buttons" to true,
            "app_glow" to true,
            "glow_style" to "horizon",
            "focus_overshoot" to false,
            "advanced_effects" to true
        ),
        "theme/glass.jpg",
        listOf(0xFF090C14, 0xFF141A29, 0xFF2A1C4D)
    ),

    // 6. Thu Phần Lan (Ruska)
    Theme(
        "ruska", "Thu Phần Lan", "Ruska",
        "Hoàng hôn thu Phần Lan bên hồ, dock trong suốt 70%, viền sáng theo màu app",
        "A Finnish autumn (\"ruska\") dusk over a lake, dock 70% opaque, focus glows in app colour without overshoot",
        mapOf(
            "background_color" to "#07080D",
            "wallpaper" to true,
            "ambient_glow" to false,
            "dock_opacity" to 70,
            "app_glow" to true,
            "focus_overshoot" to false
        ),
        "theme/ruska.jpg",
        listOf(0xFF07080D, 0xFF2A1A12, 0xFF8B4513)
    ),

    // 7. Phong cảnh (Scenes)
    Theme(
        "scenes", "Phong cảnh", "Scenes",
        "14 tranh phong cảnh tối vẽ bằng code, tự đổi mỗi giờ",
        "14 dark illustrated landscapes in wallpapers/, a new one every hour",
        mapOf("wallpaper" to true, "wallpaper_rotation_minutes" to 60),
        null,
        listOf(0xFF1A2236, 0xFF4A3A5A),
        boAnh = "theme/phong-canh",
        anhXem = "theme/phong-canh/02-golden-coast.jpg"
    ),

    // 8. 6 cột (Six columns)
    Theme(
        "six-columns", "6 cột", "Six columns",
        "6 ô nhỏ hơn mỗi hàng và thấy thêm 1 hàng app dưới dock",
        "Six smaller tiles per row and one grid row visible under the dock",
        mapOf(
            "columns" to 6,
            "rows_below_dock" to 1,
            "background_color" to "#000000",
            "wallpaper" to false
        ),
        null,
        listOf(0xFF000000, 0xFF111122)
    ),

    // 9. Cổ điển (Retro - CleanYourTV Addons)
    Theme(
        "retro", "Cổ điển", "Retro",
        "Nền đen tuyền, dock không viền dải nền, viền chọn sắc mảnh (rim), 3 ảnh nền đổi theo sáng/chiều/tối",
        "Pure black, a dock with no band or border, rim-only focus highlight, 3 time-of-day wallpapers",
        mapOf(
            "background_color" to "#000000",
            "dock_glass" to false,
            "dock_band" to false,
            "glass_style" to "rim",
            "dock_title" to false,
            "dock_opacity" to 100,
            "fill_dock" to true,
            "columns" to 5,
            "ambient_glow" to false,
            "app_glow" to false,
            "icon_only_buttons" to true,
            "icon_only_inputs" to true,
            "wifi_status_color" to true,
            "tile_menu" to true,
            "wallpaper" to true,
            "wallpaper_rotation_minutes" to 60,
            "wallpaper_moods" to mapOf(
                "morning" to listOf("6am_4pm.jpg"),
                "evening" to listOf("4pm_9pm.jpg"),
                "night" to listOf("9pm_6am.jpg")
            ),
            "wallpaper_dim" to 10,
            "wallpaper_order" to "sequential",
            "reduced_motion" to true,
            "sky_intensity" to 0
        ),
        null,
        listOf(0xFF000000, 0xFF1C1C1E, 0xFF3A3A3C),
        boAnh = "theme/retro",
        anhXem = "theme/retro/screenshot.jpg"
    ),

    // 10. Thụy Điển (Sweden)
    Theme(
        "sweden", "Thụy Điển", "Sweden",
        "3 ảnh Thụy Điển đổi mỗi giờ kèm địa danh và tác giả, dock kính mờ 6 cột",
        "Three photos of Sweden rotating every hour, 6-column frosted-glass dock, round icon-only buttons",
        mapOf(
            "dock_apps" to listOf(
                "com.netflix.ninja",
                "com.google.android.youtube.tv",
                "com.amazon.amazonvideo.livingroom",
                "com.disney.disneyplus",
                "com.apple.atve.androidtv.appletv",
                "com.wbd.stream"
            ),
            "columns" to 6,
            "rows_below_dock" to 1,
            "background_color" to "#05070C",
            "wallpaper" to true,
            "wallpaper_rotation_minutes" to 60,
            "wallpaper_order" to "sequential",
            "wallpaper_captions" to true,
            "cinematic_home" to false,
            "dock_title" to false,
            "dock_glass" to true,
            "dock_opacity" to 50,
            "dock_even_padding" to true,
            "icon_only_buttons" to true,
            "icon_only_inputs" to true,
            "ambient_glow" to false,
            "app_glow" to false,
            "fade_rows" to true,
            "focus_overshoot" to false,
            "advanced_effects" to true
        ),
        null,
        listOf(0xFF05070C, 0xFF142233, 0xFF283E55),
        boAnh = "theme/sweden",
        anhXem = "theme/sweden/001.jpg"
    ),

    // 11. Lễ hội Dussehra (Dussehra)
    Theme(
        "dussehra", "Lễ hội Dussehra", "Dussehra",
        "Giao diện lễ hội Dussehra với vòng hoa cúc vạn thọ và đèn diya vàng",
        "Festive skin for Dussehra: marigold toran, faint gold rangoli, diya and petal shower",
        mapOf(
            "festival" to "dussehra",
            "festival_until" to "2026-10-21",
            "accent_color" to "#FFB23C",
            "background_color" to "#120809",
            "wallpaper" to true
        ),
        "theme/dussehra.jpg",
        listOf(0xFF120809, 0xFF3D1508, 0xFFFFB23C)
    ),

    // 12. Thiên nhiên tĩnh lặng (Nature calm - CleanYourTV Addons)
    Theme(
        "nature-calm", "Thiên nhiên", "Nature calm",
        "12 ảnh thiên nhiên thanh bình, đổi ảnh mỗi 5 phút kèm địa danh và tác giả, dock kính mờ 6 cột",
        "Twelve calm nature photos rotating every 5 minutes with place and photographer, 6-column frosted dock",
        mapOf(
            "dock_apps" to listOf(
                "com.google.android.youtube.tv",
                "com.netflix.ninja",
                "com.amazon.amazonvideo.livingroom",
                "com.apple.atve.androidtv.appletv",
                "com.disney.disneyplus",
                "com.wbd.stream"
            ),
            "fill_dock" to false,
            "columns" to 6,
            "rows_below_dock" to 1,
            "background_color" to "#05070C",
            "wallpaper" to true,
            "wallpaper_rotation_minutes" to 5,
            "wallpaper_order" to "shuffle",
            "wallpaper_captions" to true,
            "wallpaper_true_color" to true,
            "wallpaper_dim" to 14,
            "cinematic_home" to false,
            "dock_title" to false,
            "dock_glass" to true,
            "dock_opacity" to 40,
            "dock_even_padding" to true,
            "icon_only_buttons" to true,
            "icon_only_inputs" to true,
            "ambient_glow" to false,
            "app_glow" to false,
            "fade_rows" to true,
            "focus_overshoot" to false,
            "advanced_effects" to true
        ),
        null,
        listOf(0xFF05070C, 0xFF14241B, 0xFF283B2D),
        boAnh = "theme/nature-calm",
        anhXem = "theme/nature-calm/001.jpg"
    ),
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

    /**
     * Thành phố ngoài Việt Nam (25/10/2026, người dùng ở Indonesia, Nhật xin thời tiết nơi mình ở): hỏi dịch vụ tìm địa
     * danh của Open-Meteo, cùng nơi cấp số thời tiết. Bỏ kết quả ở Việt Nam (đã có danh sách xã). Lỗi mạng thì null
     * (3.5.3: tungtom591 ở Nagoya gõ mãi không ra gì mà không biết vì sao, nên màn hình phải phân biệt lỗi với không thấy).
     */
    fun timTheGioi(q: String, tiengViet: Boolean, toiDa: Int = 6, choMs: Int = 5000): List<Xa>? = runCatching {
        val url = java.net.URL("https://geocoding-api.open-meteo.com/v1/search?count=$toiDa&format=json&language=${if (tiengViet) "vi" else "en"}&name=" +
            java.net.URLEncoder.encode(q.trim(), "UTF-8"))
        val c = url.openConnection() as java.net.HttpURLConnection
        c.connectTimeout = choMs; c.readTimeout = choMs
        val j = c.inputStream.bufferedReader().use { org.json.JSONObject(it.readText()) }
        val ds = j.optJSONArray("results") ?: return@runCatching emptyList()
        (0 until ds.length()).map { ds.getJSONObject(it) }.filter { it.optString("country_code") != "VN" }.map { r ->
            Xa(r.optString("name"), listOf(r.optString("admin1"), r.optString("country")).filter { it.isNotBlank() }.joinToString(", "),
                r.optDouble("latitude"), r.optDouble("longitude"))
        }
    }.getOrNull()

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

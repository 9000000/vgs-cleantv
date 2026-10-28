package zone.vgs.tvsach.dulieu

/**
 * Một ô mở thẳng nội dung trên Home sạch (link_tiles của launcher): kênh VTVgo, kênh / sự kiện FPT Play, hay kênh IPTV
 * mở bằng trình phát video (3.1.0). ten là chữ trên ô và cũng là tên gói giả / tên ảnh (slug), anh là ảnh logo để app
 * tải rồi gửi sang tivi (launcher không có mạng).
 */
data class OKenh(val ten: String, val goi: String, val uri: String, val mau: String = "#C62828", val anh: String? = null) {
    val slug: String get() = KenhVtvGo.slug(ten)
    fun dong(): String = "$ten|$goi|$uri|$mau"

    companion object {
        /** Đọc lại một dòng link_tiles ("Tên|gói|uri|#màu"). */
        fun tuDong(d: String): OKenh? {
            val p = d.split('|')
            if (p.size < 3 || p[0].isBlank() || p[1].isBlank() || !p[2].contains(':')) return null
            return OKenh(p[0], p[1], p[2], p.getOrNull(3)?.takeIf { it.startsWith("#") } ?: "#C62828")
        }

        /** Tên trên ô: gọn, tối đa 24 ký tự (giới hạn của link_tiles), còn ít nhất một chữ a-z / số để làm slug. */
        fun tenGon(ten: String): String = ten.replace('|', ' ').trim().take(24).trim()
    }
}

/**
 * FPT Play trên tivi (gói net.fptplay.ottbox, "FPT Play for Android TV") nhận link web fptplay.vn: /xem-truyen-hinh/<kênh>,
 * /su-kien/<sự kiện>, /xem-video/<phim> (đọc intent-filter bản 7.15.9, thử VTV1 HD mở thẳng không cần đăng nhập,
 * 26/10/2026). FPT không có danh sách kênh công khai, nên: vài kênh có sẵn + người dùng dán link chia sẻ từ app / web.
 */
object FptPlay {
    const val GOI = "net.fptplay.ottbox"
    /** (tên, slug trang /xem-truyen-hinh/, nhóm): 61 kênh, slug dò từng trang (og:title có tên kênh) ngày 27/10/2026 vì
     * API của FPT ký từng lượt gọi; góp ý xemtivi661: dán link từng kênh mất thời gian. */
    val KENH = listOf(
        Triple("VTV1 HD", "vtv1-hd", "qg"),
        Triple("VTV2 HD", "vtv2-hd", "qg"),
        Triple("VTV3 HD", "vtv3-hd", "qg"),
        Triple("VTV4 HD", "vtv4-hd", "qg"),
        Triple("VTV5 HD", "vtv5-hd", "qg"),
        Triple("VTV5 Tây Nam Bộ", "vtv5-tay-nam-bo", "qg"),
        Triple("VTV5 Tây Nguyên", "vtv5-tay-nguyen", "qg"),
        Triple("VTV6 HD", "vtv6-hd", "qg"),
        Triple("VTV7 HD", "vtv7-hd", "qg"),
        Triple("VTV8 HD", "vtv8-hd", "qg"),
        Triple("VTV9 HD", "vtv9-hd", "qg"),
        Triple("ANTV HD", "antv", "qg"),
        Triple("HTV1", "htv1", "qg"),
        Triple("HTV2 HD", "htv2", "qg"),
        Triple("HTV3", "htv3", "qg"),
        Triple("HTV4", "htv4", "qg"),
        Triple("HTV7 HD", "htv7-hd", "qg"),
        Triple("HTV9 HD", "htv9-hd", "qg"),
        Triple("HTVC Phim", "htvc-phim", "qg"),
        Triple("HTVC Thuần Việt", "htvc-thuan-viet-hd", "qg"),
        Triple("HTVC Du Lịch", "htvc-du-lich", "qg"),
        Triple("HTVC Gia Đình", "htvc-gia-dinh", "qg"),
        Triple("HTVC Phụ Nữ", "htvc-phu-nu", "qg"),
        Triple("HTVC Plus", "htvc-plus", "qg"),
        Triple("Vĩnh Long 1", "thvl1", "dp"),
        Triple("Vĩnh Long 2", "vinh-long-2", "dp"),
        Triple("Vĩnh Long 3", "vinh-long-3", "dp"),
        Triple("Cần Thơ 1", "can-tho", "dp"),
        Triple("Hải Phòng", "hai-phong", "dp"),
        Triple("Quảng Ninh 1", "quang-ninh-1", "dp"),
        Triple("Quảng Ninh 3", "quang-ninh-3", "dp"),
        Triple("Hưng Yên", "hung-yen", "dp"),
        Triple("Ninh Bình", "ninh-binh", "dp"),
        Triple("Phú Thọ", "phu-tho", "dp"),
        Triple("Thái Nguyên", "thai-nguyen", "dp"),
        Triple("Tuyên Quang", "tuyen-quang", "dp"),
        Triple("Lào Cai", "lao-cai", "dp"),
        Triple("Lai Châu", "lai-chau", "dp"),
        Triple("Điện Biên", "dien-bien", "dp"),
        Triple("Sơn La", "son-la", "dp"),
        Triple("Lạng Sơn", "lang-son", "dp"),
        Triple("Cao Bằng", "cao-bang", "dp"),
        Triple("Thanh Hóa", "thanh-hoa", "dp"),
        Triple("Nghệ An", "nghe-an", "dp"),
        Triple("Hà Tĩnh", "ha-tinh", "dp"),
        Triple("Quảng Trị", "quang-tri", "dp"),
        Triple("Thừa Thiên Huế", "thua-thien-hue", "dp"),
        Triple("Đà Nẵng 1", "da-nang-1", "dp"),
        Triple("Đà Nẵng 2", "da-nang-2", "dp"),
        Triple("Quảng Ngãi", "quang-ngai-1", "dp"),
        Triple("Khánh Hòa", "khanh-hoa", "dp"),
        Triple("Đắk Lắk", "dak-lak", "dp"),
        Triple("Lâm Đồng 1", "lam-dong", "dp"),
        Triple("Lâm Đồng 2", "lam-dong-2", "dp"),
        Triple("Tây Ninh", "tay-ninh", "dp"),
        Triple("Đồng Nai 1", "dong-nai-1", "dp"),
        Triple("Đồng Nai 2", "dong-nai-2", "dp"),
        Triple("Đồng Tháp 1", "dong-thap", "dp"),
        Triple("An Giang 1", "an-giang-1", "dp"),
        Triple("An Giang 2", "an-giang", "dp"),
        Triple("Cà Mau", "ca-mau", "dp"))
    fun linkKenh(slug: String) = "https://fptplay.vn/xem-truyen-hinh/$slug"

    /** Link fptplay.vn chuẩn hoá về https, bỏ tham số; null nếu không phải link nội dung FPT Play. */
    fun chuanHoa(link: String): String? {
        val m = Regex("""(?:https?://)?(?:www\.)?fptplay\.vn(/(?:xem-truyen-hinh|su-kien|xem-video|playlist)/[A-Za-z0-9._~%-]+(?:/[A-Za-z0-9._~%-]+)?)""")
            .find(link.trim()) ?: return null
        return "https://fptplay.vn" + m.groupValues[1]
    }

    /** Tên và ảnh của trang (og:title, og:image); trang không có nội dung thì tên chung "FPT Play - …" → null. */
    fun docTrang(url: String): Pair<String, String?>? = runCatching {
        val c = java.net.URL(url).openConnection() as java.net.HttpURLConnection
        c.connectTimeout = 8000; c.readTimeout = 10000; c.setRequestProperty("User-Agent", "Mozilla/5.0")
        val html = c.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        val tieuDe = Regex("""property="og:title" content="([^"]+)"""").find(html)?.groupValues?.get(1)?.let { android.text.Html.fromHtml(it, 0).toString() }
            ?: return null
        if (tieuDe.startsWith("FPT Play")) return null
        val anh = Regex("""property="og:image" content="([^"]+)"""").find(html)?.groupValues?.get(1)
        tenNgan(tieuDe) to anh
    }.getOrNull()

    /** "VTV1 HD Online - Xem Trực Tiếp…" → "VTV1 HD": phần trước dấu gạch / dấu |, bỏ chữ quảng cáo. */
    fun tenNgan(tieuDe: String): String {
        var t = tieuDe.split(" - ", " | ", " – ").first().trim()
        t = t.replace(Regex("""\s+(Online|Trực Tiếp|Trực Tuyến)\b.*$""", RegexOption.IGNORE_CASE), "").trim()
        return OKenh.tenGon(t)
    }
}

/**
 * TV360 của Viettel trên tivi (gói com.viettel.tv360.tv, "TV360 SmartTV"): từ bản 6.4 nhận tv360://recommendation?type=LIVE&id=<mã
 * kênh> (link Android TV dùng cho ô gợi ý ở màn chính), mở thẳng màn xem kênh. Bản 4.3 chưa có (đọc manifest + dịch ngược,
 * 28/10/2026; góp ý quangptq và 2 phiếu trước). Mã kênh lấy từ trang tv360.vn/tv/<slug>?ch=<mã>; kênh khoa = isFree 0, TV360 bắt
 * đăng nhập (VTV1 phát ngay, VTV3 hiện màn đăng nhập, thử trên tivi ảo).
 */
object Tv360 {
    const val GOI = "com.viettel.tv360.tv"
    data class K(val ten: String, val id: Int, val slug: String, val nhom: String, val khoa: Boolean = false)
    /** 101 kênh trong nước (bỏ kênh quốc tế, kênh riêng của TV360, radio), rút từ dữ liệu trang tv360.vn ngày 28/10/2026. */
    val KENH = listOf(
        K("VTV1", 2, "vtv1", "qg"),
        K("VTV2", 3, "vtv2", "qg", true),
        K("VTV3", 4, "vtv3", "qg", true),
        K("VTV4", 108, "vtv4", "qg", true),
        K("VTV5", 110, "vtv5", "qg"),
        K("VTV5 Tây Nam Bộ", 157, "vtv5-tay-nam-bo", "qg", true),
        K("VTV5 Tây Nguyên", 207, "vtv5-tay-nguyen", "qg", true),
        K("VTV6", 10043, "vtv6", "qg", true),
        K("VTV7", 6, "vtv7", "qg", true),
        K("VTV8", 115, "vtv8", "qg", true),
        K("VTV9", 8, "vtv9", "qg", true),
        K("VTV10", 98, "vtv10", "qg", true),
        K("ANTV", 20, "antv", "qg"),
        K("QPVN HD", 19, "qpvn-hd", "qg"),
        K("Vietnam Today", 9951, "vietnam-today-hd", "qg"),
        K("HTV1", 190, "htv1", "qg"),
        K("HTV2", 191, "htv2", "qg"),
        K("HTV3", 192, "htv3", "qg"),
        K("HTV4", 9, "htv4", "qg"),
        K("HTV7 HD", 193, "htv7-hd", "qg"),
        K("HTV9 HD", 194, "htv9-hd", "qg"),
        K("HTV Thể Thao", 195, "htv-the-thao", "qg", true),
        K("HTVC Du Lịch", 133, "htvc-du-lich-cuoc-song", "qg"),
        K("HTVC Phim Truyện", 15, "htvc-phim-truyen-hd", "qg"),
        K("HTVC Phụ Nữ", 12, "htvc-phu-nu", "qg"),
        K("HTVC Thuần Việt", 13, "htvc-thuan-viet", "qg"),
        K("HTVC+ Channel B", 132, "htvc", "qg"),
        K("VTVcab 1 Vie Giải Trí HD", 180, "vtvcab-1-vie-giai-tri-hd", "qg"),
        K("VTVcab 2 Phim Việt HD", 175, "vtvcab-2-phim-viet-hd", "qg"),
        K("VTVcab 3 On Sports", 173, "vtvcab-3-on-sports", "qg", true),
        K("VTVcab 4 On Movies", 181, "vtvcab-4-on-movies", "qg"),
        K("VTVcab 5 E Channel HD", 182, "vtvcab-5-e-channel-hd", "qg"),
        K("VTVcab 6 On Sports +", 183, "vtvcab-6-on-sports", "qg", true),
        K("VTVcab 8 BiBi", 178, "vtvcab-8-bi-bi-sd", "qg"),
        K("VTVcab 9 InfoTV HD", 189, "vtvcab-9-infotv-hd", "qg"),
        K("VTVcab 10 On Cine HD", 176, "vtvcab10-on-cine-hd", "qg"),
        K("VTVcab 12 StyleTV HD", 184, "vtvcab-12-styletv-hd", "qg"),
        K("VTVcab 15 On Music", 185, "vtvcab15-on-music", "qg"),
        K("VTVcab 16 On Football HD", 174, "vtvcab-16-hd", "qg", true),
        K("VTVcab 17 Trending TV", 186, "vtvcab17-trending-tv", "qg"),
        K("VTVcab 18 On Sports News", 170, "vtvcab-18-on-sports-news", "qg", true),
        K("VTVcab 19 Vie Dramas", 177, "vtvcab19-vie-dramas", "qg"),
        K("VTVcab 20 V Family HD", 187, "vtvcab-20-v-family-hd", "qg"),
        K("VTVcab 21 Cartoon Kids H", 179, "vtvcab-21-cartoon-kids-hd", "qg"),
        K("VTVcab 22 Life TV HD", 188, "vtvcab-22-life-tv-hd", "qg"),
        K("VTVcab 23 Golf Channel", 169, "vtvcab-23-golf-channel", "qg", true),
        K("An Viên HD", 151, "an-vien-hd", "qg"),
        K("BTV6", 9838, "btv6", "qg"),
        K("HITV", 32, "hitv", "qg"),
        K("O2TV", 136, "o2tv", "qg"),
        K("SCTV2 Today TV", 201, "sctv2-today-tv", "qg"),
        K("SCTV6", 232, "sctv6", "qg"),
        K("YouTV", 31, "youtv", "qg"),
        K("An Giang 1", 66, "an-giang-1", "dp"),
        K("An Giang 2", 35, "an-giang-2", "dp"),
        K("Bắc Ninh", 39, "bac-ninh", "dp"),
        K("Cao Bằng", 48, "cao-bang", "dp"),
        K("Cà Mau", 46, "ca-mau", "dp"),
        K("Cần Thơ 1", 47, "can-tho-1", "dp"),
        K("Cần Thơ 2", 61, "can-tho-2", "dp"),
        K("Cần Thơ 3", 84, "can-tho-3", "dp"),
        K("Gia Lai", 55, "gia-lai", "dp"),
        K("Huế", 63, "hue", "dp"),
        K("Hà Nội 1", 33, "ha-noi-1", "dp"),
        K("Hà Nội 2", 34, "ha-noi-2", "dp"),
        K("Hà Tĩnh", 58, "ha-tinh", "dp"),
        K("Hưng Yên", 64, "hung-yen", "dp"),
        K("Hải Phòng", 60, "hai-phong", "dp"),
        K("Hải Phòng 3", 59, "hai-phong-3", "dp"),
        K("Khánh Hòa", 65, "khanh-hoa", "dp"),
        K("Khánh Hòa 1", 76, "khanh-hoa-1", "dp"),
        K("Lai Châu", 68, "lai-chau", "dp"),
        K("Lào Cai", 71, "lao-cai", "dp"),
        K("Lâm Đồng 1", 69, "lam-dong-1", "dp"),
        K("Lâm Đồng 2", 45, "lam-dong-2", "dp"),
        K("Lạng Sơn", 70, "lang-son", "dp"),
        K("Nghệ An", 74, "nghe-an", "dp"),
        K("Ninh Bình", 75, "ninh-binh", "dp"),
        K("Phú Thọ", 77, "phu-tho", "dp"),
        K("Quảng Ngãi", 81, "quang-ngai", "dp"),
        K("Quảng Ninh 1", 82, "quang-ninh-1", "dp"),
        K("Quảng Ninh 3", 134, "quang-ninh-3", "dp"),
        K("Quảng Trị", 83, "quang-tri", "dp"),
        K("Sơn La", 85, "son-la", "dp"),
        K("Thanh Hóa", 89, "thanh-hoa", "dp"),
        K("Thái Nguyên", 88, "thai-nguyen", "dp"),
        K("Tuyên Quang", 92, "tuyen-quang", "dp"),
        K("Tây Ninh 1", 72, "tay-ninh-1", "dp"),
        K("Vĩnh Long 1", 25, "vinh-long-1", "dp"),
        K("Vĩnh Long 2", 26, "vinh-long-2", "dp"),
        K("Vĩnh Long 3", 219, "vinh-long-3", "dp"),
        K("Vĩnh Long 4", 220, "vinh-long-4", "dp"),
        K("Vĩnh Long 5", 91, "vinh-long-5", "dp"),
        K("Điện Biên", 52, "dien-bien", "dp"),
        K("Đà Nẵng 1", 49, "da-nang-1", "dp"),
        K("Đà Nẵng 2", 80, "da-nang-2", "dp"),
        K("Đắk Lắk", 51, "dak-lak", "dp"),
        K("Đồng Nai", 255, "dong-nai", "dp"),
        K("Đồng Nai 1", 53, "dong-nai-1", "dp"),
        K("Đồng Tháp 1", 54, "dong-thap-1", "dp"),
        K("Đồng Tháp 2", 90, "dong-thap-2", "dp")
    )
    fun uri(id: Int) = "tv360://recommendation?type=LIVE&id=$id"

    /** Ảnh thẻ logo kênh (og:image của trang kênh); null nếu không đọc được. */
    fun anh(k: K): String? = runCatching {
        val c = java.net.URL("https://tv360.vn/tv/${k.slug}?ch=${k.id}").openConnection() as java.net.HttpURLConnection
        c.connectTimeout = 8000; c.readTimeout = 10000; c.setRequestProperty("User-Agent", "Mozilla/5.0")
        val html = c.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        Regex("""property="og:image" content="([^"]+)"""").find(html)?.groupValues?.get(1)
    }.getOrNull()
}

/**
 * Danh sách IPTV cá nhân (m3u / m3u8, góp ý 26/10/2026): mỗi kênh thành một ô, bấm là mở link luồng trong trình phát video
 * có trên tivi (VLC, MX Player, Just Player, Kodi). Launcher thử ACTION_VIEW, không ai nhận thì gửi kèm kiểu video.
 */
object Iptv {
    data class Kenh(val ten: String, val url: String, val logo: String?, val nhom: String)

    /** Trình phát hay gặp trên Android TV, theo thứ tự ưu tiên. */
    val TRINH_PHAT = listOf("org.videolan.vlc" to "VLC", "com.mxtech.videoplayer.pro" to "MX Player Pro",
        "com.mxtech.videoplayer.ad" to "MX Player", "com.brouken.player" to "Just Player", "org.xbmc.kodi" to "Kodi")

    /** Đọc danh sách từ link (http/https) hoặc nội dung dán vào. */
    fun doc(nguon: String): List<Kenh> {
        val n = nguon.trim()
        val chu = if (Regex("^https?://\\S+$").matches(n)) {
            val c = java.net.URL(n).openConnection() as java.net.HttpURLConnection
            c.connectTimeout = 10000; c.readTimeout = 20000; c.setRequestProperty("User-Agent", "Mozilla/5.0")
            c.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        } else n
        return tach(chu)
    }

    fun tach(m3u: String): List<Kenh> {
        val out = mutableListOf<Kenh>(); var ten: String? = null; var logo: String? = null; var nhom = ""
        for (dong in m3u.lines().map { it.trim() }) {
            when {
                dong.startsWith("#EXTINF", ignoreCase = true) -> {
                    ten = dong.substringAfterLast(',').trim().ifBlank { null }
                    logo = Regex("""tvg-logo="([^"]+)"""").find(dong)?.groupValues?.get(1)?.takeIf { it.startsWith("http") }
                    nhom = Regex("""group-title="([^"]*)"""").find(dong)?.groupValues?.get(1) ?: ""
                }
                dong.isEmpty() || dong.startsWith("#") -> {}
                Regex("^(https?|rtmp|rtsp|udp)://\\S+$", RegexOption.IGNORE_CASE).matches(dong) && !dong.contains('|') -> {
                    out += Kenh(ten ?: dong.substringAfterLast('/').take(24), dong, logo, nhom)
                    ten = null; logo = null
                }
            }
        }
        return out.distinctBy { it.url }
    }
}

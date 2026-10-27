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

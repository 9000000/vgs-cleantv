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
    val KENH = listOf("VTV1 HD" to "vtv1-hd", "VTV2 HD" to "vtv2-hd", "VTV3 HD" to "vtv3-hd", "VTV4 HD" to "vtv4-hd",
        "VTV5 HD" to "vtv5-hd", "VTV6 HD" to "vtv6-hd", "VTV7 HD" to "vtv7-hd", "VTV8 HD" to "vtv8-hd", "VTV9 HD" to "vtv9-hd",
        "HTV7 HD" to "htv7-hd", "HTV9 HD" to "htv9-hd")
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

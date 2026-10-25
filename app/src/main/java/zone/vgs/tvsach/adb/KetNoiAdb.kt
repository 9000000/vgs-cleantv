package zone.vgs.tvsach.adb

import android.content.Context
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Một kết nối ADB tới tivi. Khoá RSA tạo một lần rồi giữ trong bộ nhớ app, để tivi đã bấm
 * "Luôn cho phép" thì lần sau không hỏi lại.
 */
class KetNoiAdb private constructor(
    private val khoa: PrivateKey,
    private val chungChi: Certificate,
) : AbsAdbConnectionManager() {

    init {
        setTimeout(20, TimeUnit.SECONDS)
    }

    override fun getPrivateKey(): PrivateKey = khoa
    override fun getCertificate(): Certificate = chungChi
    override fun getDeviceName(): String = "CleanTV"

    /** Địa chỉ của lần kết nối thành công gần nhất (cả Gỡ lỗi không dây, connectTls cũng đi qua đây). */
    @Volatile var diemCuoi: Pair<String, Int>? = null
        private set

    override fun connect(host: String, port: Int): Boolean {
        val ok = super.connect(host, port)
        if (ok) { diemCuoi = host to port; coHeadC = null; coDd = null }
        return ok
    }

    /** Nối lại đúng địa chỉ vừa dùng khi kết nối rớt giữa chừng (Wi-Fi chập chờn, tivi khởi động lại). */
    fun ketNoiLai(): Boolean {
        val d = diemCuoi ?: return false
        runCatching { disconnect() }
        return runCatching { super.connect(d.first, d.second) }.getOrDefault(false)
    }

    /**
     * Chạy một lệnh shell, trả về chữ in ra (stdout + stderr gộp).
     *
     * Đọc tới dấu kết thúc tự chèn chứ không chờ luồng đóng: libadb 3.1.1 có lỗi đua, khi tivi
     * đóng luồng lúc còn dữ liệu chưa đọc thì read() chờ mãi.
     */
    fun shell(lenh: String): String {
        val dau = "__TVS_" + java.lang.Long.toHexString(System.nanoTime()) + "__"
        val chu = docToiKhi(openStream("shell:$lenh; echo $dau")) { it.contains(dau) }
        return chu.substringBefore(dau)
    }

    /** Ảnh PNG màn hình tivi (qua base64 để đi được đường shell có dấu kết thúc). */
    fun chupPng(): ByteArray {
        val b64 = shell("screencap -p | base64").filter { it.isLetterOrDigit() || it == '+' || it == '/' || it == '=' }
        return android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
    }

    /** Đọc luồng tới khi [xong] đúng với phần đã nhận, hoặc luồng đóng, hoặc quá [giay] giây. */
    private fun docToiKhi(stream: io.github.muntashirakon.adb.AdbStream, giay: Int = 120, xong: (String) -> Boolean): String {
        val out = ByteArrayOutputStream()
        val hetHan = System.currentTimeMillis() + giay * 1000L
        val luong = Thread {
            runCatching {
                val input = stream.openInputStream()
                val buf = ByteArray(16 * 1024)
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    synchronized(out) { out.write(buf, 0, n) }
                    if (xong(synchronized(out) { out.toString("UTF-8") })) break
                }
            }
        }.apply { isDaemon = true; start() }
        while (luong.isAlive && System.currentTimeMillis() < hetHan) luong.join(200)
        runCatching { stream.close() }
        if (luong.isAlive) {
            luong.interrupt()
            throw java.io.IOException("Tivi không trả lời sau $giay giây")
        }
        return synchronized(out) { out.toString("UTF-8") }.replace("\r", "")
    }

    /**
     * Cài APK theo kiểu `adb install` mới: `cmd package install -S <cỡ>` rồi đổ byte vào stdin.
     * Trả về chữ máy in ra ("Success" hoặc "Failure [...]").
     */
    fun caiApk(apk: ByteArray): String {
        val stream = openStream("exec:cmd package install -r -S ${apk.size}")
        val o = stream.openOutputStream()
        var i = 0
        while (i < apk.size) {
            val n = minOf(4000, apk.size - i)
            o.write(apk, i, n)
            i += n
        }
        o.flush()
        return docToiKhi(stream, 180) { it.contains("Success") || it.contains("Failure") || it.contains("Error") }
    }

    /**
     * Ghi một file lên tivi. Đường nhanh: đẩy thẳng byte vào `head -c <cỡ>` như caiApk (24/10/2026: đường base64 cũ mỗi
     * 3000 ký tự một lệnh shell, theme 14 ảnh thành vài trăm lệnh — TCL 65Q646 chạy CleanTV trên chính tivi kẹt ở "Đang
     * gửi 14 ảnh"). Cỡ file sai thì quay về đường base64 (toybox có base64 từ Android 6).
     */
    fun ghiFile(duongDan: String, noiDung: ByteArray) {
        if (coHeadC() && runCatching { ghiNhanh(duongDan, noiDung, "head -c ${noiDung.size}") }.getOrDefault(false)) return
        // Android 8 không có head -c; dd đọc từng byte thì chậm hơn nhưng có sẵn từ rất lâu (25/10/2026, Sony KDL-43W800C:
        // gửi ảnh nền "xong" mà hình nền không đổi — file trên tivi rỗng).
        if (coDd() && runCatching { ghiNhanh(duongDan, noiDung, "dd bs=1 count=${noiDung.size} 2>/dev/null") }.getOrDefault(false)) return
        shell("rm -f $duongDan $duongDan.b64")
        val b64 = android.util.Base64.encodeToString(noiDung, android.util.Base64.NO_WRAP)
        b64.chunked(3000).forEach { manh -> shell("printf '%s' '$manh' >> $duongDan.b64") }
        shell("base64 -d $duongDan.b64 > $duongDan; rm -f $duongDan.b64")
        if (shell("wc -c < $duongDan").trim().toIntOrNull() != noiDung.size) throw java.io.IOException("Tivi không nhận file ${duongDan.substringAfterLast('/')}")
    }

    /** Tivi có `head -c` không, hỏi một lần mỗi kết nối. */
    @Volatile private var coHeadC: Boolean? = null

    /**
     * toybox của Android 8 chưa có `head -c` (24/10/2026, Sony BRAVIA 2015 Android 8 kẹt ở "Đang gửi ảnh nền"): lệnh
     * báo lỗi ngay, phía tivi đóng luồng giữa lúc app còn đẩy byte nên kẹt. Không có thì thử dd, rồi mới tới base64.
     */
    private fun coHeadC(): Boolean = coHeadC ?: (runCatching { shell("echo abc | head -c 2 2>/dev/null").trim() == "ab" }.getOrDefault(false)).also { coHeadC = it }

    @Volatile private var coDd: Boolean? = null
    private fun coDd(): Boolean = coDd ?: (runCatching { shell("echo abc | dd bs=1 count=2 2>/dev/null").trim() == "ab" }.getOrDefault(false)).also { coDd = it }

    /** Đẩy byte vào [lenh] (đọc đúng cỡ file từ stdin) rồi ghi ra [duongDan]; đúng cỡ thì true. */
    private fun ghiNhanh(duongDan: String, noiDung: ByteArray, lenh: String): Boolean {
        val dau = "__TVS_" + java.lang.Long.toHexString(System.nanoTime()) + "__"
        val stream = openStream("exec:sh -c '$lenh > $duongDan; echo $dau'")
        val o = stream.openOutputStream()
        var i = 0
        while (i < noiDung.size) {
            val n = minOf(4000, noiDung.size - i)
            o.write(noiDung, i, n)
            i += n
        }
        o.flush()
        docToiKhi(stream, 120) { it.contains(dau) }
        return shell("wc -c < $duongDan").trim().toIntOrNull() == noiDung.size
    }

    companion object {
        @Volatile private var hienTai: KetNoiAdb? = null

        fun lay(context: Context): KetNoiAdb = hienTai ?: synchronized(this) {
            hienTai ?: taoMoi(context).also { hienTai = it }
        }

        private fun taoMoi(context: Context): KetNoiAdb {
            val thuMuc = File(context.filesDir, "adb").apply { mkdirs() }
            val fKhoa = File(thuMuc, "khoa.der")
            val fCert = File(thuMuc, "cert.der")
            if (fKhoa.exists() && fCert.exists()) {
                runCatching {
                    val khoa = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(fKhoa.readBytes()))
                    val cert = CertificateFactory.getInstance("X.509").generateCertificate(fCert.inputStream())
                    return KetNoiAdb(khoa, cert)
                }
            }
            val gen = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }
            val cap = gen.generateKeyPair()
            val ten = X500Name("CN=CleanTV")
            val tu = Date()
            val den = Date(tu.time + 3650L * 24 * 3600 * 1000)
            val builder = JcaX509v3CertificateBuilder(ten, BigInteger.valueOf(tu.time), tu, den, ten, cap.public)
            val signer = JcaContentSignerBuilder("SHA256withRSA").build(cap.private)
            val cert = JcaX509CertificateConverter().getCertificate(builder.build(signer))
            fKhoa.writeBytes(cap.private.encoded)
            fCert.writeBytes(cert.encoded)
            return KetNoiAdb(cap.private, cert)
        }
    }
}

package zone.vgs.tvsach.dulieu

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Gợi ý mẫu gần giống khi không khớp đúng (23/10/2026: Sony BRAVIA_VU3_4K, khi đó danh sách mới có VU31). */
class MauGanGiongTest {
    private val du = DuLieu.docJson(File("src/main/assets/du-lieu.json").readText())
    private fun tv(brand: String, model: String, device: String) =
        ThongTinTv(brand, brand, model, device, device, device, "14", 34, "", "1")

    // 25/10/2026: upstream có danh sách riêng cho VU3 (#1433) — VU3 giờ khớp đúng, gợi ý gần giống thử bằng VU32.
    @Test fun sonyVu3_khopDung() =
        assertEquals("sony-bravia-vu3", du.khopMau(tv("Sony", "BRAVIA VU3", "BRAVIA_VU3_4K")).firstOrNull()?.first?.id)

    @Test fun sonyVu32_goiYVu3() {
        val t = tv("Sony", "BRAVIA VU32", "BRAVIA_VU32_4K")
        assertTrue(du.khopMau(t).isEmpty())
        assertTrue(du.ganGiong(t).firstOrNull()?.id?.startsWith("sony-bravia-vu3") == true)
    }

    @Test fun khacHang_khongGoiY() = assertTrue(du.ganGiong(tv("Acme", "VU3", "VU3_4K")).isEmpty())

    @Test fun boQuaMauDaKhop() =
        assertTrue(du.ganGiong(tv("Sony", "BRAVIA VU32", "BRAVIA_VU32_4K"), setOf("sony-bravia-vu31")).none { it.id == "sony-bravia-vu31" })
}

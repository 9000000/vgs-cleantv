package zone.vgs.tvsach.dulieu

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ThemeTest {
    @Test
    fun danhSachThemeHopLe() {
        assertTrue("Có ít nhất 10 giao diện", THEMES.size >= 10)
        val ids = THEMES.map { it.id }
        assertEquals("Không trùng id theme", ids.size, ids.toSet().size)

        val assetRoot = File("src/main/assets")
        THEMES.forEach { th ->
            assertTrue("Có tên tiếng Việt: ${th.id}", th.tenVi.isNotBlank())
            assertTrue("Có tên tiếng Anh: ${th.id}", th.tenEn.isNotBlank())
            assertTrue("Có màu đại diện: ${th.id}", th.mau.isNotEmpty())

            th.anhNen?.let { path ->
                val f = File(assetRoot, path)
                assertTrue("File ảnh nền tồn tại: $path (${th.id})", f.exists() && f.isFile && f.length() > 0)
            }

            th.boAnh?.let { path ->
                val dir = File(assetRoot, path)
                assertTrue("Thư mục bộ ảnh tồn tại: $path (${th.id})", dir.exists() && dir.isDirectory)
                val files = dir.listFiles()
                assertTrue("Thư mục bộ ảnh không rỗng: $path (${th.id})", files != null && files.isNotEmpty())
            }

            th.anhXem?.let { path ->
                val f = File(assetRoot, path)
                assertTrue("File ảnh xem trước tồn tại: $path (${th.id})", f.exists() && f.isFile && f.length() > 0)
            }
        }
    }
}

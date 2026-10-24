package zone.vgs.tvsach.dulieu

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ThemeTest {
    @Test
    fun danhSachThemeHopLe() {
        assertTrue("Có ít nhất 26 giao diện", THEMES.size >= 26)
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

    @Test
    fun kiemTraChuyenDoiGiaTriNangCao() {
        assertEquals("true", NangCao.chuTuGiaTri(true))
        assertEquals("false", NangCao.chuTuGiaTri(false))
        assertEquals("42", NangCao.chuTuGiaTri(42))
        assertEquals("#ffffff", NangCao.chuTuGiaTri("#ffffff"))
        assertEquals("a, b, c", NangCao.chuTuGiaTri(listOf("a", "b", "c")))
        assertEquals(null, NangCao.chuTuGiaTri(null))
    }

    @Test
    fun kiemTraThemeCauHinhCoTheChuyenDoi() {
        THEMES.forEach { th ->
            th.cauHinh.forEach { (k, v) ->
                val chu = NangCao.chuTuGiaTri(v)
                assertTrue("Giá trị cấu hình của ${th.id}.$k không được null sau chuyển đổi", chu != null)
            }
        }
    }

    @Test
    fun kiemTraCauTrucMultiThemeConfigsJson() {
        val json = org.json.JSONObject()
        val mockThemeConfigs = mapOf(
            "minimal-light" to mapOf("background_color" to "#ffffff", "dock_glass" to "true"),
            "cyberpunk-neon" to mapOf("accent_color" to "#ff00ff", "dock_glass" to "false")
        )
        val themeConfigsObj = org.json.JSONObject()
        mockThemeConfigs.forEach { (thId, map) ->
            val o = org.json.JSONObject()
            map.forEach { (k, v) -> o.put(k, v) }
            themeConfigsObj.put(thId, o)
        }
        json.put("vgs_theme_configs", themeConfigsObj)
        json.put("vgs_theme", "cyberpunk-neon")

        // Deserialize
        val restoredObj = json.optJSONObject("vgs_theme_configs")
        assertTrue(restoredObj != null)
        val minimalLight = restoredObj!!.optJSONObject("minimal-light")
        assertEquals("#ffffff", minimalLight!!.optString("background_color"))
        assertEquals("true", minimalLight.optString("dock_glass"))
        assertEquals("cyberpunk-neon", json.optString("vgs_theme"))
    }
}

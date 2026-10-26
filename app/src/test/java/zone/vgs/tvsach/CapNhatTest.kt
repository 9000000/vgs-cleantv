package zone.vgs.tvsach

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CapNhatTest {

    @Test
    fun testLaBanMoiHon() {
        // Bản mới hơn
        assertTrue(CapNhat.laBanMoiHon("3.0.13", "3.0.12"))
        assertTrue(CapNhat.laBanMoiHon("v3.0.13", "3.0.12"))
        assertTrue(CapNhat.laBanMoiHon("3.0.14", "3.0.13"))
        assertTrue(CapNhat.laBanMoiHon("3.1.0", "3.0.13"))
        assertTrue(CapNhat.laBanMoiHon("4.0.0", "3.99.99"))
        assertTrue(CapNhat.laBanMoiHon("3.0.13", "2.10.14"))

        // Bản bằng nhau
        assertFalse(CapNhat.laBanMoiHon("3.0.13", "3.0.13"))
        assertFalse(CapNhat.laBanMoiHon("v3.0.13", "3.0.13"))
        assertFalse(CapNhat.laBanMoiHon("3.0.13", "v3.0.13"))

        // Bản cũ hơn
        assertFalse(CapNhat.laBanMoiHon("3.0.12", "3.0.13"))
        assertFalse(CapNhat.laBanMoiHon("2.10.14", "3.0.13"))
        assertFalse(CapNhat.laBanMoiHon("", "3.0.13"))
    }
}

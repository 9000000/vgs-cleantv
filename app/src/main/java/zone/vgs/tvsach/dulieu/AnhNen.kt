package zone.vgs.tvsach.dulieu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream

/** Ảnh nền riêng từ điện thoại (góp ý 23/10/2026): cắt giữa theo khung 16:9 (hoặc vừa khung, viền mờ), thu về 1920×1080, JPEG 95. Giao diện tivi vẽ ở 1080p nên ảnh 4K không nét hơn. */
object AnhNen {
    private const val RONG = 1920
    private const val CAO = 1080
    /** 95 thay vì 85 (25/10/2026, góp ý ảnh riêng kém nét): ảnh 1080p chỉ to thêm vài trăm KB. */
    private const val CHAT_LUONG = 95

    /** Như chuanBi, cho ảnh đọc thẳng thành byte (ảnh trên tivi / USB, ThaoTacTv.docFileTv). */
    fun chuanBiByte(du: ByteArray, vuaKhung: Boolean = false): ByteArray? = runCatching {
        val bien = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(du, 0, du.size, bien)
        val chia = maxOf(1, minOf(bien.outWidth / RONG, bien.outHeight / CAO))
        val goc = BitmapFactory.decodeByteArray(du, 0, du.size, BitmapFactory.Options().apply { inSampleSize = chia }) ?: return null
        veKhung(goc, vuaKhung)
    }.getOrNull()

    /** Ảnh cao hơn rộng: chỉ đọc kích thước, tính cả xoay EXIF (ảnh điện thoại chụp dọc thường lưu ngang + cờ xoay). */
    fun laAnhDoc(ctx: Context, uri: Uri): Boolean = runCatching {
        val bien = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bien) }
        val xoay = ctx.contentResolver.openInputStream(uri)?.use { xoay90(android.media.ExifInterface(it)) } ?: false
        if (xoay) bien.outWidth > bien.outHeight else bien.outHeight > bien.outWidth
    }.getOrElse { false }

    fun laAnhDocByte(du: ByteArray): Boolean = runCatching {
        val bien = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(du, 0, du.size, bien)
        val xoay = xoay90(android.media.ExifInterface(du.inputStream()))
        if (xoay) bien.outWidth > bien.outHeight else bien.outHeight > bien.outWidth
    }.getOrElse { false }

    private fun xoay90(exif: android.media.ExifInterface) =
        exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, 1) in 5..8

    fun chuanBi(ctx: Context, uri: Uri, vuaKhung: Boolean = false): ByteArray? = runCatching {
        val goc: Bitmap = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(ctx.contentResolver, uri)) { dec, info, _ ->
                val chia = maxOf(1, minOf(info.size.width / RONG, info.size.height / CAO))
                dec.setTargetSampleSize(chia)
                dec.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            val bien = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bien) }
            val chia = maxOf(1, minOf(bien.outWidth / RONG, bien.outHeight / CAO))
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = chia }) }
                ?: return null
        }
        veKhung(goc, vuaKhung)
    }.getOrNull()

    /** Phủ kín khung 16:9 rồi cắt phần thừa ở giữa. */
    private fun phuKin(goc: Bitmap): Bitmap {
        val ty = maxOf(RONG.toFloat() / goc.width, CAO.toFloat() / goc.height)
        val w = (goc.width * ty).toInt().coerceAtLeast(RONG); val h = (goc.height * ty).toInt().coerceAtLeast(CAO)
        val phu = Bitmap.createScaledBitmap(goc, w, h, true)
        return Bitmap.createBitmap(phu, (w - RONG) / 2, (h - CAO) / 2, RONG, CAO)
    }

    /**
     * vuaKhung (3.0.0, góp ý hieuinter): cả ảnh nằm trọn trong khung, không cắt; phần trống hai bên / trên dưới là chính
     * ảnh đó phóng to, làm mờ (thu nhỏ 1/24 rồi phóng lại) và tối đi, như khung ảnh dọc trên điện thoại.
     */
    private fun veKhung(goc: Bitmap, vuaKhung: Boolean): ByteArray {
        // Công tắc quyết định cả với ảnh dọc (góp ý dungduongmobile89 30/10/2026: muốn ảnh dọc phủ kín như trước 3.5.1);
        // chọn ảnh dọc thì app tự bật vừa khung (laAnhDoc), như góp ý build.dng 27/10.
        val ra = if (!vuaKhung) phuKin(goc) else {
            val nen = phuKin(goc)
            val mo = Bitmap.createScaledBitmap(Bitmap.createScaledBitmap(nen, RONG / 24, CAO / 24, true), RONG, CAO, true)
            val kq = mo.copy(Bitmap.Config.ARGB_8888, true)
            val cv = android.graphics.Canvas(kq)
            cv.drawColor(0x66000000)
            val ty = minOf(RONG.toFloat() / goc.width, CAO.toFloat() / goc.height)
            val w = (goc.width * ty).toInt(); val h = (goc.height * ty).toInt()
            cv.drawBitmap(goc, null, android.graphics.Rect((RONG - w) / 2, (CAO - h) / 2, (RONG + w) / 2, (CAO + h) / 2),
                android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG))
            kq
        }
        return ByteArrayOutputStream().use { o -> ra.compress(Bitmap.CompressFormat.JPEG, CHAT_LUONG, o); o.toByteArray() }
    }
}

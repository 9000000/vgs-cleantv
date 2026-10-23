package zone.vgs.tvsach.dulieu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream

/** Ảnh nền riêng từ điện thoại (góp ý 23/10/2026): cắt giữa theo khung 16:9, thu về 1920×1080, JPEG ~85. */
object AnhNen {
    private const val RONG = 1920
    private const val CAO = 1080

    fun chuanBi(ctx: Context, uri: Uri): ByteArray? = runCatching {
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
        // Phủ kín khung 16:9 rồi cắt phần thừa ở giữa.
        val ty = maxOf(RONG.toFloat() / goc.width, CAO.toFloat() / goc.height)
        val w = (goc.width * ty).toInt().coerceAtLeast(RONG); val h = (goc.height * ty).toInt().coerceAtLeast(CAO)
        val phu = Bitmap.createScaledBitmap(goc, w, h, true)
        val cat = Bitmap.createBitmap(phu, (w - RONG) / 2, (h - CAO) / 2, RONG, CAO)
        ByteArrayOutputStream().use { o -> cat.compress(Bitmap.CompressFormat.JPEG, 85, o); o.toByteArray() }
    }.getOrNull()
}

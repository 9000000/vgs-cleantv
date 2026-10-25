package zone.vgs.tvsach.dulieu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream

/** Ảnh nền riêng từ điện thoại (góp ý 23/10/2026): cắt giữa theo khung 16:9, thu về 1920×1080, JPEG 95. Giao diện tivi vẽ ở 1080p nên ảnh 4K không nét hơn. */
object AnhNen {
    private const val RONG = 1920
    private const val CAO = 1080
    /** 95 thay vì 85 (25/10/2026, góp ý ảnh riêng kém nét): ảnh 1080p chỉ to thêm vài trăm KB. */
    private const val CHAT_LUONG = 95

    /** Như chuanBi, cho ảnh đọc thẳng thành byte (ảnh trên tivi / USB, ThaoTacTv.docFileTv). */
    fun chuanBiByte(du: ByteArray): ByteArray? = runCatching {
        val bien = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(du, 0, du.size, bien)
        val chia = maxOf(1, minOf(bien.outWidth / RONG, bien.outHeight / CAO))
        val goc = BitmapFactory.decodeByteArray(du, 0, du.size, BitmapFactory.Options().apply { inSampleSize = chia }) ?: return null
        val ty = maxOf(RONG.toFloat() / goc.width, CAO.toFloat() / goc.height)
        val w = (goc.width * ty).toInt().coerceAtLeast(RONG); val h = (goc.height * ty).toInt().coerceAtLeast(CAO)
        val phu = Bitmap.createScaledBitmap(goc, w, h, true)
        val cat = Bitmap.createBitmap(phu, (w - RONG) / 2, (h - CAO) / 2, RONG, CAO)
        ByteArrayOutputStream().use { o -> cat.compress(Bitmap.CompressFormat.JPEG, CHAT_LUONG, o); o.toByteArray() }
    }.getOrNull()

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
        ByteArrayOutputStream().use { o -> cat.compress(Bitmap.CompressFormat.JPEG, CHAT_LUONG, o); o.toByteArray() }
    }.getOrNull()
}

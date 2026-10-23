package zone.vgs.khoa

import android.content.Context

/** Trạng thái lưu trên tivi: cấu hình, mở tạm tới lúc nào, số lần nhập sai. */
class Kho(ctx: Context) {
    private val p = ctx.applicationContext.getSharedPreferences("khoa", Context.MODE_PRIVATE)
    var json: String? get() = p.getString("cau_hinh", null); set(v) = p.edit().putString("cau_hinh", v).apply()
    var moTamDen: Long get() = p.getLong("mo_tam_den", 0); set(v) = p.edit().putLong("mo_tam_den", v).apply()
    var sai: Int get() = p.getInt("sai", 0); set(v) = p.edit().putInt("sai", v).apply()
    var chanDen: Long get() = p.getLong("chan_den", 0); set(v) = p.edit().putLong("chan_den", v).apply()
    val cauHinh: CauHinh? get() = Luat.docJson(json)
}

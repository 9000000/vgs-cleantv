# ERRORS.md - Danh sách lỗi và bài học kinh nghiệm

## [2026-11-01 11:50] - Widget Ngày này năm xưa không hoạt động, bị treo hiển thị mãi 1 câu

- **Type**: Logic
- **Severity**: High
- **File**: `upstream/launcher/app/src/main/java/com/gathin/tvhome/HistoryProvider.java` & `MainActivity.java`
- **Agent**: mon
- **Root Cause**: 
  1. `CAROUSEL_INTERVAL_MS` đặt 3 phút (180 giây) là quá dài cho màn hình TV Launcher; mỗi khi người dùng mở ứng dụng hoặc mở cài đặt rồi quay về Home, `onResume()` lại huỷ và reset timer 3 phút từ đầu nên widget không bao giờ kịp chuyển câu.
  2. `historyEventIndex` luôn khởi tạo cố định bằng 0, khiến lần nào mở launcher cũng chỉ lấy câu đầu tiên.
  3. Khi TV khởi động (boot), mạng WiFi chưa kịp kết nối Internet khiến request Wikipedia thất bại ngay lập tức và nạp 2 câu fallback cứng (1945, 1975); sau khi có mạng, `NetworkCallback` không hề thử tải lại.
  4. Thiếu bộ nhớ đệm đĩa (disk persistence), khiến mỗi lần app khởi động lại đều phụ thuộc 100% vào mạng tại đúng thời điểm đó.
  5. `todayCard` không có thuộc tính `android:focusable="true"` và selector highlight, khiến điều khiển TV remote (D-pad) không thể focus và click để đổi câu tiếp theo.
- **Error Message**: Người dùng phản ánh: "wiget ngày này năm xưa lỗi không hoạt động chỉ thấy hiển thị mãi 1 câu".
- **Fix Applied**: 
  1. Rút ngắn chu kỳ xoay vòng `CAROUSEL_INTERVAL_MS` từ 3 phút xuống 15 giây (`15 * 1000L`) chuẩn nhịp TV, tự động chuyển luân phiên 1:1 mượt mà giữa Danh ngôn và Sự kiện lịch sử.
  2. Ngẫu nhiên hóa `historyEventIndex` khi khởi tạo (`System.currentTimeMillis() % 1000`).
  3. Thêm bộ nhớ đệm đĩa SharedPreferences `vgs_history_cache` trong `HistoryProvider`, hỗ trợ offline-first nạp ngay sự kiện lịch sử trong ngày kể cả khi chưa có WiFi.
  4. Lắng nghe `onCapabilitiesChanged` khi mạng online để tự động tải dữ liệu Wikipedia mới nhất và cập nhật widget.
  5. Bổ sung tập dữ liệu dự phòng (fallback) phong phú hơn 20 sự kiện lịch sử tiêu biểu, hào hùng của Việt Nam và nhân loại.
  6. Thêm `android:focusable="true"`, `clickable="true"` và selector `bg_today_card.xml` viền vàng hổ phách `#FFD479` khi focus, cho phép người dùng bấm remote (phím OK/Enter) để chuyển ngay câu tiếp theo theo ý muốn.
- **Prevention**: Luôn thiết kế cơ chế offline-first lưu disk cache cho widget thông tin phòng khách, đảm bảo chu kỳ xoay vòng phù hợp với thói quen dùng tivi (15-20s), và hỗ trợ điều khiển bằng Remote TV (D-pad focus & click).
- **Status**: Fixed

---


## [2026-11-03 21:30] - Đồng hồ khi để yên (home_idle_clock_minutes) không hoạt động sau khi áp dụng cài đặt

- **Type**: Logic
- **Severity**: High
- **File**: `app/src/main/java/zone/vgs/tvsach/ui/PhienLamViec.kt:423`, `upstream/launcher/app/src/main/java/com/gathin/tvhome/MainActivity.java`
- **Agent**: mon
- **Root Cause**:
  1. `PhienLamViec.kt`: Trong `dongBoCongTacTuTheme()`, `dongHoKhiYen` bị gán `?: 0` khi theme hoặc `nangCao` không có khoá, đồng thời `napTuTivi()` không lưu lại `home_idle_clock_minutes` sau khi gọi `dongBoCongTacTuTheme()`. Do đó mỗi khi kết nối tới TV hoặc đổi theme, giá trị người dùng chọn bị reset về 0 (tắt) và ở lần áp dụng tiếp theo, `home_idle_clock_minutes` bị loại khỏi cấu hình.
  2. `MainActivity.java` (Launcher): Launcher chỉ tính `idleClockMs` 1 lần duy nhất trong `onCreate()`. Khi CleanTV gửi cấu hình mới và gọi `am start -W -f 0x10008000 -n $cmpHome`, do Launcher là `singleTask`, Activity không được tạo lại mà chỉ đi qua `onNewIntent()` và `onResume()`. Launcher không lắng nghe broadcast `zone.vgs.cleantv.RELOAD_CONFIG` và không kiểm tra thay đổi file `config.json` trên đĩa, khiến giá trị `idleClockMs` trong bộ nhớ vẫn bằng 0 cho đến khi TV khởi động lại.
- **Error Message**: Người dùng phản ánh: "mục đồng hồ khi để yên tôi thấy không hoạt động", "sửa ngay lỗi Đồng hồ khi để yên".
- **Fix Applied**:
  1. `PhienLamViec.kt`: Bảo toàn `dongHoKhiYen` khi đổi theme (chỉ cập nhật nếu theme/nâng cao chỉ định rõ). Sau khi nạp từ TV, đồng bộ lại `dongHoKhiYen` từ JSON vào `nangCao["home_idle_clock_minutes"]` nếu có.
  2. `ThaoTacTv.kt`: Bổ sung cờ `--ez reload true` trong lệnh `am start` khi đẩy cấu hình xuống TV.
  3. `Config.java`: Bổ sung `themeChanged(Context)` kiểm tra xem file `config.json` hoặc `theme_revision` trên đĩa có thay đổi signature so với bản đã lưu hay không.
  4. `MainActivity.java`:
     - Lắng nghe broadcast `zone.vgs.cleantv.RELOAD_CONFIG` (`RECEIVER_EXPORTED`) để gọi `recreate()`.
     - Trong `onNewIntent()`, kiểm tra extra `reload == true` để gọi `recreate()`.
     - Trong `onResume()`, kiểm tra `Config.themeChanged(this)` để tự động `recreate()` nạp lại cấu hình mới ngay lập tức.
  5. Biên dịch lại toàn bộ `tvhome.apk` và `tvhome-xiaomi.apk` trong `app/src/main/assets/`, đồng bộ patch `launcher-vgs.patch`.
- **Prevention**: Khi thêm tuỳ chọn launcher điều khiển runtime bằng file cấu hình, luôn đảm bảo luồng đồng bộ 2 chiều (load từ TV <-> UI CleanTV) không bị ghi đè ngầm bởi theme defaults, và Activity Launcher phải có cơ chế hot-reload hoặc restartActivity khi nhận tín hiệu cấu hình mới.
- **Status**: Fixed

---

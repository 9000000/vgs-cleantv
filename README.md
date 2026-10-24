# CleanTV by VGS

**Tivi hết quảng cáo.** App Android dọn quảng cáo, theo dõi và app rác của hãng trên Android TV / Google TV qua Wi-Fi, rồi thay màn Home đầy quảng cáo bằng một Home sạch. Không root, không gỡ gì (chỉ tắt), khôi phục một chạm.

**No more TV ads.** An Android app that switches off the maker's ads, tracking and junk on Android TV / Google TV over Wi-Fi and swaps the ad-filled Home for a clean one. No root, nothing uninstalled (only disabled), one-tap restore.

[Tải APK / Download](https://vgs.zone/cleantv) · [English page](https://cleantv.alexvgs.tech/en) · [Nhật ký cập nhật / Changelog](https://vgs.zone/cleantv#cap-nhat) · [Góp ý / Feedback](https://vgs.zone/gop-y)

<p>
  <img src="web/anh/tv-truoc.webp" width="49%" alt="Trước / before">
  <img src="web/anh/tv-sau.webp" width="49%" alt="Sau / after">
</p>
<p>
  <img src="web/anh/app-tim.webp" width="24%" alt="Tìm tivi">
  <img src="web/anh/app-chon.webp" width="24%" alt="Chọn gói">
  <img src="web/anh/app-en.webp" width="24%" alt="Theme">
</p>

---

## Tiếng Việt

### App làm gì
**Dọn tivi**
- **Kết nối tivi qua Wi-Fi** bằng ADB: quét mạng hoặc gõ IP; Google TV đời mới ghép bằng mã 6 số (Gỡ lỗi không dây), app tự dò cổng. Hướng dẫn 7 bước có hình trong app.
- **So với 248 danh sách đã thử** của cộng đồng CleanYourTV (74 hãng: TCL, Sony, Xiaomi, Philips, Hisense, Skyworth, onn, Chromecast, máy chiếu…). Chưa khớp mẫu thì gợi ý mẫu gần giống; tivi lạ dùng luật chung an toàn.
- **Chỉ tắt, không gỡ** quảng cáo, theo dõi, app rác. Không đụng cổng HDMI, remote, bàn phím, CH Play, gói đang giữ Wi-Fi, gói lõi Android và Home đang dùng. Google Home / cast và tìm kiếm giọng nói không bao giờ tích sẵn.

**Home sạch** (launcher CleanYourTV, bản vá VGS)
- Đồng hồ, lịch âm có can chi, thời tiết theo 3.321 phường/xã, lời chào, dock 8 app, cụm nút Máy chiếu · Mạng · Màn hình chờ · Cài đặt.
- 10 giao diện (VGS, Cực quang, Bầu trời theo giờ, Điện ảnh, Kính mờ, Thu Phần Lan, Phong cảnh, Thụy Điển, Tối giản, Nhiều app), ảnh nền riêng từ điện thoại hoặc từ tivi / USB, 73 tuỳ chọn Nâng cao.
- **Nút Home về đúng Home sạch**: app bấm thử nút Home thật. Tivi không cho tắt Home gốc (Google TV còn trình cài đặt, một số box nhà mạng) thì Home sạch tự mở đè lên — không phải quên Wi-Fi.
- **Nút trên remote vẫn chạy**: YouTube, Netflix, Prime, bánh răng Cài đặt theo bảng phím của từng mẫu tivi; TCL Google TV giữ âm lượng vẫn tăng giảm liên tục.
- Máy chiếu (Formovie, XGIMI, JMGO…): ghim màn lấy nét / chỉnh hình vào nút Máy chiếu. Xiaomi bản Trung Quốc có Home sạch riêng. Bo mạch CVTE giữ Home hãng.

**An toàn**
- **Sao lưu trước mọi thay đổi**, trên điện thoại và trên tivi; Khôi phục một chạm, đổi điện thoại vẫn khôi phục được.
- **Tivi tự canh gác**: đang thay Home mà rớt Wi-Fi / hết pin / tắt app thì 3 phút sau tivi tự bật lại Home gốc.
- **Kịch bản khôi phục** cho máy tính (file lệnh adb chạy trên Windows / Mac / Linux), **xem trước lệnh** sẽ chạy trên tivi.
- Xong việc rời app thì **tivi tự tắt ADB** (có tuỳ chọn giữ cho lần sau).

**Còn nữa**
- **Khoá trẻ em**: hẹn giờ khoá tivi theo ngày trong tuần, mã PIN 4 số mở 60 phút, che cả HDMI và chặn remote.
- DNS chặn quảng cáo AdGuard (tuỳ chọn), xem màn hình tivi trên điện thoại, nhớ danh sách tivi đã dọn.
- Chạy trên **điện thoại Android** hoặc **ngay trên tivi** (cho người dùng iPhone). Tự cập nhật, song ngữ Việt / Anh.

### Dựng từ mã nguồn
Cần JDK 17 và Android SDK (compileSdk 35).

```bash
scripts/dung-launcher.sh          # tải CleanYourTV, áp patches/launcher-vgs.patch, dựng Home sạch + dữ liệu 248 mẫu vào assets
./gradlew assembleRelease         # thiếu ky.properties thì APK ký bằng khoá debug
./gradlew :app:testReleaseUnitTest
```

`app/src/main/assets/tvhome.apk` và `du-lieu.json` đã có sẵn trong repo, nên chỉ dựng app thì không cần bước đầu.

### Kiểm tra bản tải về
Mọi APK trên [Releases](https://github.com/vgscity/vgs-cleantv/releases) và vgs.zone/cleantv đều ký bằng cùng một khoá. Dấu SHA-256 của chứng chỉ:

```
ab94764d40109789e5448ab6539ed960bafb2e5c51c4a8ebd100e524ad8b3046
```

```bash
apksigner verify --print-certs CleanTV-x.y.z.apk     # phải ra đúng dấu trên
shasum -a 256 CleanTV-x.y.z.apk                       # so với file .sha256 trên Releases
scripts/so-ban-dung.sh CleanTV-x.y.z.apk              # dựng lại từ mã nguồn ở tag vx.y.z rồi so: mọi file bên trong phải trùng
```

Bản dựng tái tạo được: hai lần dựng sạch cùng mã nguồn ra cùng một APK. Bạn tự dựng thì ký bằng khoá của bạn, nên `so-ban-dung.sh` so từng file bên trong (kể cả `khoa.apk` lồng trong) và bỏ qua chữ ký.

### Cấu trúc
| Thư mục | Nội dung |
|---|---|
| `app/` | App (Kotlin + Jetpack Compose). Package `zone.vgs.cleantv`, mã ở `zone.vgs.tvsach` (tên biến tiếng Việt) |
| `app/.../adb/` | Kết nối ADB (libadb-android), các thao tác trên tivi: sao lưu, tắt gói, cài Home sạch, thử nút Home, khôi phục |
| `app/.../dulieu/` | Danh sách mẫu tivi, luật chọn gói (`KeHoach.kt`), theme, hướng dẫn |
| `patches/launcher-vgs.patch` | Phần VGS sửa trên launcher CleanYourTV (thời tiết, font Lexend, cụm nút, máy chiếu, màn chờ…) |
| `khoa/` | App nhỏ "Khoá trẻ em" cài lên tivi (dịch vụ trợ năng che màn hình theo giờ, mở bằng mã PIN) |
| `scripts/` | Dựng launcher, dựng dữ liệu mẫu tivi, phát hành, so bản dựng (`so-ban-dung.sh`) |
| `dem/` | Cloudflare Worker đếm ẩn danh (lượt tải, lượt dùng) |
| `web/` | Trang tiếng Anh, file cập nhật (`cap-nhat.json`), nhật ký (`nhat-ky.json`) |

### Dữ liệu thu thập
Chỉ số liệu ẩn danh: một mã máy ngẫu nhiên do app tự sinh, số bản app, tên mẫu tivi, số gói đã tắt, gửi khi mở app (mỗi ngày một lần), cập nhật, dọn xong, cài Home sạch. **Không** gửi email, tên, danh sách app, IP hay vị trí. Riêng nút "Gửi danh sách gói cho VGS" ở màn cuối chỉ chạy khi bạn tự bấm (model tivi, các gói đang tắt, đánh giá, ghi chú). Xem `app/src/main/java/zone/vgs/tvsach/Dem.kt` và `dem/src/index.js`.

### Đóng góp
Báo lỗi, xin hỗ trợ tivi mới: [vgs.zone/gop-y](https://vgs.zone/gop-y) (mục "App CleanTV") hoặc mở Issue. Danh sách tivi mới nên gửi thẳng cho [CleanYourTV](https://github.com/Gathin23/cleanyourtv) để mọi người cùng dùng; CleanTV tự lấy về.

---

## English

### What it does
**Cleaning**
- **Connects over Wi-Fi** with ADB: scan or type the IP; newer Google TVs pair with a 6-digit code and the app finds the port. A 7-step picture guide is built in.
- **Matches 248 tested lists** from the CleanYourTV community (74 brands); suggests close models; unknown TVs get safe general rules.
- **Disables, never uninstalls** ads, tracking and junk. Leaves HDMI inputs, the remote, keyboards, Play Store, the Wi-Fi owner, core Android packages and the Home in use alone. Google Home / casting and voice search are never pre-ticked.

**Clean Home** (CleanYourTV launcher with VGS patches)
- Clock, Vietnamese lunar date, ward-level weather, greeting, an 8-app dock, a Projector · Network · Screensaver · Settings cluster.
- 10 themes (VGS, Aurora, Sky, Cinematic, Glass, Ruska, Scenes, Sweden, Minimal, More apps), your own wallpaper from the phone or the TV / a USB stick, 73 Advanced options.
- **Home lands on the clean Home**: the app tests the real Home key; where the stock Home can't be disabled (Google TV with its setup app, some operator boxes) the clean Home opens over it — no forgetting Wi-Fi.
- **Remote keys keep working**: YouTube, Netflix, Prime and the Settings gear from each model's key map; holding volume still ramps on TCL Google TV.
- Projectors get focus / keystone shortcuts; Chinese Xiaomi TVs get an allowed Home build; CVTE boards keep their stock Home.

**Safety**
- **Backs up before any change**, on the phone and on the TV; one-tap Restore, even from a new phone.
- **The TV guards itself**: if the connection drops while Home is being replaced, it restores its stock Home after 3 minutes.
- **Restore script** for a computer (plain adb commands for Windows / macOS / Linux) and a **command preview** before anything runs.
- **ADB switches off** on the TV once you leave the app (optional keep-on).

**Also**
- **Kids lock**: weekday lock periods, a 4-digit PIN unlocks for 60 minutes, covers HDMI and blocks the remote.
- Optional AdGuard DNS, view the TV screen on the phone, remembers every TV you cleaned.
- Runs on an **Android phone** or **on the TV itself** (for iPhone users). Self-updating, Vietnamese / English.

### Build
JDK 17 + Android SDK (compileSdk 35): `scripts/dung-launcher.sh`, then `./gradlew assembleRelease`. Without `ky.properties` the release APK is signed with the debug key. Code identifiers are in Vietnamese.

### Verify a download
Every APK on Releases and vgs.zone/cleantv is signed with the same key, certificate SHA-256
`ab94764d40109789e5448ab6539ed960bafb2e5c51c4a8ebd100e524ad8b3046` (`apksigner verify --print-certs`). Builds are
reproducible: `scripts/so-ban-dung.sh CleanTV-x.y.z.apk` rebuilds from the matching tag and checks that every file inside
(the nested `khoa.apk` too) is identical, ignoring the signature, since yours uses your own key.

### Privacy
Anonymous counts only (a random app-generated ID, app version, TV model, number of packages disabled). No email, name, app list, IP or location. The "Send package list to VGS" button on the last screen only sends when you tap it (TV model, disabled packages, your rating and note). See `Dem.kt` and `dem/src/index.js`.

---

## Ghi công / Credits
CleanTV is built on **[CleanYourTV](https://github.com/Gathin23/cleanyourtv)** by Gathin and its contributors (MIT): the clean Home launcher and the TV lists come from that project. Also libadb-android, Conscrypt, Bouncy Castle, Lexend (OFL) and Open-Meteo weather data. See [NOTICE](NOTICE).

## Giấy phép / License
Source code: [MIT](LICENSE). The names **CleanTV** and **VGS**, the VGS and CleanTV logos and the VGS wallpaper are not covered by the licence: modified builds should use their own name and artwork.

Made by [VGS — Vietnam Gaming Setup](https://vgs.zone).

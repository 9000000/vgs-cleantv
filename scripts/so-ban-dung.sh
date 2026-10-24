#!/usr/bin/env bash
# So một APK CleanTV đã phát hành với bản tự dựng từ mã nguồn: mọi file bên trong (kể cả khoa.apk lồng trong) phải
# giống hệt; chỉ chữ ký được khác (bạn ký bằng khoá của bạn). Dùng:
#   scripts/so-ban-dung.sh CleanTV-2.6.6.apk              # tự dựng :app:assembleRelease rồi so
#   scripts/so-ban-dung.sh CleanTV-2.6.6.apk ban-cua-ban.apk
set -euo pipefail
cd "$(dirname "$0")/.."
[ $# -ge 1 ] || { echo "Dùng: $0 <apk-phat-hanh> [apk-tu-dung]"; exit 2; }
PH="$(cd "$(dirname "$1")" && pwd)/$(basename "$1")"
if [ $# -ge 2 ]; then TU="$(cd "$(dirname "$2")" && pwd)/$(basename "$2")"
else
  export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17}"
  bash scripts/dung-khoa.sh >/dev/null
  ./gradlew -q :app:assembleRelease --console=plain
  TU="$PWD/app/build/outputs/apk/release/app-release.apk"
fi
TAM="$(mktemp -d)"; trap 'rm -rf "$TAM"' EXIT
giai() {  # giải nén APK, khoa.apk lồng bên trong cũng giải ra, bỏ file chữ ký v1 nếu có
  mkdir -p "$2"; (cd "$2" && unzip -q "$1")
  if [ -f "$2/assets/khoa.apk" ]; then mkdir "$2/assets/khoa.apk.d"; (cd "$2/assets/khoa.apk.d" && unzip -q ../khoa.apk); rm "$2/assets/khoa.apk"; fi
  find "$2" -path '*/META-INF/*' \( -name '*.SF' -o -name '*.RSA' -o -name '*.EC' -o -name 'MANIFEST.MF' \) -delete
}
giai "$PH" "$TAM/a"; giai "$TU" "$TAM/b"
if diff -r "$TAM/a" "$TAM/b" >/dev/null; then echo "GIỐNG: mọi file trong hai APK trùng nhau, chỉ khác chữ ký (nếu có)."
else echo "KHÁC:"; diff -rq "$TAM/a" "$TAM/b"; exit 1; fi

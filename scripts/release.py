#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
CleanTV - Script tu dong dong goi va phat hanh Release APK len GitHub Releases
Trỏ toàn bộ cấu hình, Gradle task, package và OTA metadata về dự án vgs-cleantv.
"""

import os
import sys

# Ensure UTF-8 output on Windows console
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
        sys.stderr.reconfigure(encoding='utf-8')
    except Exception:
        pass

import argparse
import datetime
import json
import re
import shutil
import subprocess
import urllib.request
import urllib.error
from pathlib import Path

# Auto-detect ROOT path (whether run from root or from scripts/)
CURRENT_DIR = Path(__file__).resolve().parent
ROOT = CURRENT_DIR.parent if (CURRENT_DIR.parent / "app").exists() else CURRENT_DIR

BUILD_GRADLE = ROOT / "app" / "build.gradle.kts"
LOCAL_PROPS = ROOT / "local.properties"
RELEASE_NOTES_FILE = ROOT / "RELEASE_NOTES.md"
WEB_CAP_NHAT = ROOT / "web" / "cap-nhat.json"
ROOT_CAP_NHAT = ROOT / "cap-nhat.json"

def read_config_property(key: str, default: str = "") -> str:
    val = os.environ.get(key)
    if val:
        return val.strip()
    if LOCAL_PROPS.exists():
        with open(LOCAL_PROPS, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line.startswith(f"{key}="):
                    return line.split("=", 1)[1].strip()
    return default

def get_git_branch() -> str:
    try:
        res = subprocess.run(
            ["git", "rev-parse", "--abbrev-ref", "HEAD"],
            cwd=ROOT,
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace",
            check=True
        )
        branch = res.stdout.strip()
        if branch and branch != "HEAD":
            return branch
    except Exception:
        pass
    return "tet"

def get_git_repo_info() -> tuple[str | None, str | None]:
    try:
        res = subprocess.run(
            ["git", "config", "--get", "remote.origin.url"],
            cwd=ROOT,
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace"
        )
        url = res.stdout.strip()
        m = re.search(r"github\.com[/:]([^/]+)/([^/\.]+)(?:\.git)?", url)
        if m:
            return m.group(1), m.group(2)
    except Exception:
        pass
    return None, None

auto_owner, auto_repo = get_git_repo_info()
GITHUB_OWNER = read_config_property("GITHUB_OWNER", auto_owner or "9000000")
configured_repo = read_config_property("GITHUB_REPO", "")
GITHUB_REPO = configured_repo or auto_repo or "vgs-cleantv"
TARGET_BRANCH = get_git_branch()

def read_version() -> str:
    if not BUILD_GRADLE.exists():
        return "2.6.12"
    try:
        with open(BUILD_GRADLE, "r", encoding="utf-8") as f:
            content = f.read()
            m = re.search(r'versionName\s*=\s*["\']([^"\']+)["\']', content)
            if m:
                return m.group(1).strip()
    except Exception as e:
        print(f"[WARN] Khong doc duoc versionName tu app/build.gradle.kts: {e}")
    return "2.6.12"

def read_version_code() -> int | None:
    if not BUILD_GRADLE.exists():
        return None
    try:
        with open(BUILD_GRADLE, "r", encoding="utf-8") as f:
            content = f.read()
            m = re.search(r"versionCode\s*=\s*(\d+)", content)
            if m:
                return int(m.group(1).strip())
    except Exception:
        pass
    return None

def update_gradle_version(version_name: str, version_code: int = None) -> bool:
    if not BUILD_GRADLE.exists():
        return False
    try:
        content = BUILD_GRADLE.read_text(encoding="utf-8")
        new_content = re.sub(r'(versionName\s*=\s*["\'])[^"\']+(["\'])', rf'\g<1>{version_name}\g<2>', content)
        if version_code is not None:
            new_content = re.sub(r"(versionCode\s*=\s*)\d+", rf"\g<1>{version_code}", new_content)
        if new_content != content:
            BUILD_GRADLE.write_text(new_content, encoding="utf-8")
            details = [f"versionName = '{version_name}'"]
            if version_code is not None:
                details.append(f"versionCode = {version_code}")
            print(f"[OK] Da cap nhat app/build.gradle.kts: {', '.join(details)}")
            return True
    except Exception as e:
        print(f"[WARN] Khong the cap nhat phien ban vao app/build.gradle.kts: {e}")
    return False

def is_prerelease_version(version: str) -> bool:
    v_lower = version.lower()
    return any(keyword in v_lower for keyword in ["beta", "alpha", "rc", "preview", "dev"])

def read_token() -> str | None:
    token = os.environ.get("GITHUB_TOKEN") or os.environ.get("GH_TOKEN")
    if token:
        return token.strip()
    token = read_config_property("GITHUB_TOKEN") or read_config_property("GH_TOKEN")
    return token if token else None

def get_java_env() -> dict:
    env = os.environ.copy()
    if not env.get("JAVA_HOME"):
        candidates = [
            r"C:\Users\NongTuan\.jdks\jbr-21.0.11",
            r"C:\Program Files\Android\Android Studio\jbr",
            r"C:\Program Files\Java\jdk-21",
            r"C:\Program Files\Java\jdk-17",
            "/opt/homebrew/opt/openjdk@17",
            "/usr/lib/jvm/java-17-openjdk-amd64"
        ]
        for candidate in candidates:
            if os.path.exists(candidate):
                env["JAVA_HOME"] = candidate
                break
    return env

def build_apk():
    print("\n[1/3] Dang bien dich cac ban Release APK cua CleanTV...")
    gradle_cmd = "gradlew.bat" if os.name == "nt" else "./gradlew"
    gradle_path = ROOT / gradle_cmd

    # Neu co module khoa va chua co khoa.apk thi build khoa truoc
    khoa_apk = ROOT / "app" / "src" / "main" / "assets" / "khoa.apk"
    if (ROOT / "khoa").exists() and not khoa_apk.exists():
        print("   -> Dang dung module Khoa tre em (:khoa:assembleRelease)...")
        cmd_khoa = [str(gradle_path), ":khoa:assembleRelease"]
        res_k = subprocess.run(cmd_khoa, cwd=ROOT, env=get_java_env())
        if res_k.returncode == 0:
            khoa_build_apk = ROOT / "khoa" / "build" / "outputs" / "apk" / "release" / "khoa-release.apk"
            if khoa_build_apk.exists():
                shutil.copy2(khoa_build_apk, khoa_apk)
                print(f"   [OK] Da tao khoa.apk trong assets ({khoa_apk.stat().st_size / 1024:.1f} KB)")

    cmd = [str(gradle_path), ":app:testReleaseUnitTest", ":app:assembleRelease"]
    res = subprocess.run(cmd, cwd=ROOT, env=get_java_env())
    if res.returncode != 0:
        print("[ERROR] Loi khi bien dich APK Release!")
        sys.exit(res.returncode)
    print("[OK] Bien dich APK Release thanh cong!")

def get_release_apks(version: str) -> list[Path]:
    """
    Thu thap file APK phat hanh chinh thuc cho CleanTV.
    Doi ten truc tiep file app-release.apk thanh CleanTV-v{version}.apk tai app/build/outputs/apk/release/
    Khong tao hoac copy file ra thu muc goc.
    """
    release_dir = ROOT / "app" / "build" / "outputs" / "apk" / "release"
    target_apk = release_dir / f"CleanTV-v{version}.apk"
    default_apk = release_dir / "app-release.apk"

    if default_apk.exists():
        if target_apk.exists():
            target_apk.unlink()
        default_apk.rename(target_apk)
    elif not target_apk.exists() and release_dir.exists():
        # Neu khong co app-release.apk, tim file apk bat ky hop le trong thu muc release
        any_apk = next((p for p in release_dir.glob("*.apk") if p.stat().st_size > 1000000), None)
        if any_apk:
            any_apk.rename(target_apk)

    if target_apk.exists() and target_apk.stat().st_size > 1000000:
        return [target_apk]

    return []

def get_previous_tag(target_version: str = None) -> str | None:
    try:
        res = subprocess.run(
            ["git", "tag", "--sort=-creatordate"],
            cwd=ROOT,
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace"
        )
        tags = [t.strip() for t in res.stdout.splitlines() if t.strip()]
        target_tags = {f"v{target_version}", target_version} if target_version else set()
        for tag in tags:
            if tag not in target_tags:
                return tag
    except Exception:
        pass
    return None

def get_git_commit_notes(previous_tag: str | None = None, limit: int = 20) -> list[str]:
    try:
        cmd = ["git", "log", "--no-merges", "--pretty=format:%s"]
        if previous_tag:
            cmd.append(f"{previous_tag}..HEAD")
        else:
            cmd.extend(["-n", str(limit)])

        res = subprocess.run(cmd, cwd=ROOT, capture_output=True, text=True, encoding="utf-8", errors="replace")
        if res.returncode != 0:
            return []

        raw_commits = [line.strip() for line in res.stdout.splitlines() if line.strip()]
        cleaned = []
        seen = set()
        drop_prefixes = ("merge ", "revert ", "bump version", "update version")

        for commit in raw_commits:
            c_lower = commit.lower()
            if any(c_lower.startswith(p) for p in drop_prefixes):
                continue
            if c_lower in seen:
                continue
            seen.add(c_lower)

            item = commit
            if item and item[0].islower() and not item.startswith(("feat", "fix", "chore", "docs", "perf", "refactor", "ci", "build", "style", "test")):
                item = item[0].upper() + item[1:]

            cleaned.append(item)
            if len(cleaned) >= limit:
                break

        return cleaned
    except Exception:
        return []

def get_default_release_notes(version: str) -> str:
    if RELEASE_NOTES_FILE.exists():
        try:
            with open(RELEASE_NOTES_FILE, "r", encoding="utf-8") as f:
                content = f.read().strip()
                if content:
                    return content
        except Exception:
            pass

    prev_tag = get_previous_tag(version)
    commits = get_git_commit_notes(prev_tag, limit=20)
    if commits:
        commit_bullets = "\n".join(f"- {c}" for c in commits)
        compare_url = ""
        if prev_tag and GITHUB_OWNER and GITHUB_REPO:
            curr_tag = f"v{version}" if not version.startswith("v") else version
            compare_url = f"\n\n**Full Changelog**: https://github.com/{GITHUB_OWNER}/{GITHUB_REPO}/compare/{prev_tag}...{curr_tag}"

        return f"""## CleanTV v{version}

### Có gì mới & Cải tiến:
{commit_bullets}{compare_url}
"""

    return f"""## CleanTV v{version}

### Có gì mới & Cải tiến:
- 🎨 Hỗ trợ 26 Giao diện (Themes) phong phú với hình nền & màu sắc độc bản.
- ⚙️ Tự động nạp cấu hình đi theo từng giao diện; tùy biến riêng lưu độc lập theo từng theme.
- 🔄 Nút "Khôi phục mặc định giao diện" giúp hoàn tác về cấu hình gốc nguyên bản bất cứ lúc nào.
- 📁 Xuất / Nhập file cấu hình chung đa theme toàn diện (`vgs_theme_configs`).
- 🎛️ Gom 6 cụm Nâng cao chuẩn CleanYourTV: Look, Dock & apps, Remote & buttons, Clock & info, Behaviour, More.
- 📺 Tối ưu nút điều khiển từ xa, phím Home lạ, và bảo vệ chống treo máy trên tivi bo mạch CVTE.
"""

def sync_ota_metadata(version: str, target_code: int, apk_file: Path, tag: str, notes: str):
    """
    Dong bo file cap-nhat.json (web/cap-nhat.json va cap-nhat.json) cho he thong OTA.
    """
    today_str = datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=7))).strftime("%Y-%m-%d")
    apk_url = f"https://github.com/{GITHUB_OWNER}/{GITHUB_REPO}/releases/download/{tag}/{apk_file.name}"
    apk_size = apk_file.stat().st_size if apk_file.exists() else 0

    first_line_notes = notes.strip().splitlines()[0].replace("#", "").strip() if notes else f"CleanTV {tag}"

    base_ota = {
        "app": {
            "versionCode": target_code,
            "versionName": version,
            "apk": apk_url,
            "co": apk_size,
            "ghiChu": first_line_notes,
            "ghiChuEn": f"CleanTV {tag} release",
            "ngay": today_str
        }
    }

    # Giu lai duLieu neu file cu da co san
    if WEB_CAP_NHAT.exists():
        try:
            with open(WEB_CAP_NHAT, "r", encoding="utf-8") as f:
                old_data = json.load(f)
                if "duLieu" in old_data:
                    base_ota["duLieu"] = old_data["duLieu"]
        except Exception:
            pass

    for target_path in [WEB_CAP_NHAT, ROOT_CAP_NHAT]:
        target_path.parent.mkdir(parents=True, exist_ok=True)
        with open(target_path, "w", encoding="utf-8") as f:
            json.dump(base_ota, f, ensure_ascii=False, indent=2)
        print(f"[OK] Da dong bo {target_path.relative_to(ROOT)} theo GitHub Release {tag}")

def upload_via_gh(tag: str, title: str, notes: str, apks: list[Path], is_prerelease: bool = False, is_draft: bool = False) -> bool:
    print(f"\n[3/3] Dang tai len GitHub qua GitHub CLI (gh) vao {GITHUB_OWNER}/{GITHUB_REPO}...")
    cmd = [
        "gh", "release", "create", tag,
        *[str(apk) for apk in apks],
        "--repo", f"{GITHUB_OWNER}/{GITHUB_REPO}",
        "--target", TARGET_BRANCH,
        "--title", title,
        "--notes", notes,
    ]
    if is_prerelease:
        cmd.append("--prerelease")
    else:
        cmd.append("--latest")
    if is_draft:
        cmd.append("--draft")

    res = subprocess.run(cmd, cwd=ROOT)
    if res.returncode == 0:
        print(f"\n[SUCCESS] Da phat hanh Release {tag} tai:")
        print(f"-> https://github.com/{GITHUB_OWNER}/{GITHUB_REPO}/releases/tag/{tag}\n")
        return True
    return False

def upload_via_api(token: str, tag: str, title: str, notes: str, apks: list[Path], is_prerelease: bool = False, is_draft: bool = False) -> bool:
    print(f"\n[3/3] Dang tao Release va tai len APK qua GitHub API vao {GITHUB_OWNER}/{GITHUB_REPO}...")
    api_base = f"https://api.github.com/repos/{GITHUB_OWNER}/{GITHUB_REPO}"
    create_url = f"{api_base}/releases"

    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
        "User-Agent": "CleanTV-Release-Script",
    }

    payload = json.dumps({
        "tag_name": tag,
        "target_commitish": TARGET_BRANCH,
        "name": title,
        "body": notes,
        "draft": is_draft,
        "prerelease": is_prerelease,
        "make_latest": "true" if not is_prerelease and not is_draft else "false"
    }).encode("utf-8")

    req_headers = {**headers, "Content-Type": "application/json"}
    req = urllib.request.Request(create_url, data=payload, headers=req_headers, method="POST")

    release_data = None
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            release_data = json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode("utf-8", errors="replace")
        if e.code == 422:
            print(f"[WARN] Release {tag} da ton tai tren GitHub, dang lay thong tin de bo sung/cap nhat APK...")
            try:
                get_url = f"{api_base}/releases/tags/{tag}"
                get_req = urllib.request.Request(get_url, headers=headers, method="GET")
                with urllib.request.urlopen(get_req, timeout=60) as get_resp:
                    release_data = json.loads(get_resp.read().decode("utf-8"))

                if release_data:
                    try:
                        patch_url = f"{api_base}/releases/{release_data.get('id')}"
                        patch_payload = json.dumps({
                            "name": title,
                            "body": notes,
                            "draft": is_draft,
                            "prerelease": is_prerelease,
                            "make_latest": "true" if not is_prerelease and not is_draft else "false"
                        }).encode("utf-8")
                        patch_req = urllib.request.Request(patch_url, data=patch_payload, headers=req_headers, method="PATCH")
                        with urllib.request.urlopen(patch_req, timeout=30) as p_resp:
                            release_data = json.loads(p_resp.read().decode("utf-8"))
                            print(f"   [SYNC] Da dong bo thong tin release (Pre-release: {'Co' if is_prerelease else 'Khong'})")
                    except Exception as p_ex:
                        print(f"   [WARN] Khong the cap nhat thong tin release ton tai: {p_ex}")
            except Exception as ex:
                print(f"[ERROR] Khong the lay thong tin release ton tai: {ex}")
                return False
        else:
            print(f"[ERROR] Loi tao release qua GitHub API: HTTP {e.code} - {err_msg}")
            return False
    except Exception as e:
        print(f"[ERROR] Loi ket noi GitHub API: {e}")
        return False

    if not release_data:
        print("[ERROR] Khong nhan duoc du lieu Release hop le tu GitHub.")
        return False

    upload_url_template = release_data.get("upload_url", "")
    html_url = release_data.get("html_url", "")
    upload_url_base = upload_url_template.split("{")[0]
    release_id = release_data.get("id")

    existing_assets = {}
    if release_id:
        try:
            assets_url = f"{api_base}/releases/{release_id}/assets"
            assets_req = urllib.request.Request(assets_url, headers=headers, method="GET")
            with urllib.request.urlopen(assets_req, timeout=60) as a_resp:
                assets_list = json.loads(a_resp.read().decode("utf-8"))
                for a in assets_list:
                    existing_assets[a["name"]] = a["id"]
        except Exception as e:
            print(f"[WARN] Khong lay duoc danh sach asset cu: {e}")

    for apk in apks:
        size_mb = apk.stat().st_size / (1024 * 1024)

        if apk.name in existing_assets:
            old_id = existing_assets[apk.name]
            print(f"   [OVERWRITE] Phat hien {apk.name} da co san tren Release. Dang xoa asset cu (ID: {old_id})...")
            try:
                del_url = f"{api_base}/releases/assets/{old_id}"
                del_req = urllib.request.Request(del_url, headers=headers, method="DELETE")
                with urllib.request.urlopen(del_req, timeout=60) as del_resp:
                    pass
            except Exception as e:
                print(f"   [WARN] Khong the xoa asset cu {apk.name}: {e}")

        print(f"   [UPLOAD] Dang tai len {apk.name} ({size_mb:.2f} MB)...")
        with open(apk, "rb") as f:
            apk_bytes = f.read()

        target_url = f"{upload_url_base}?name={apk.name}"
        up_headers = {
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github+json",
            "X-GitHub-Api-Version": "2022-11-28",
            "User-Agent": "CleanTV-Release-Script",
            "Content-Type": "application/vnd.android.package-archive",
            "Content-Length": str(len(apk_bytes)),
        }
        up_req = urllib.request.Request(target_url, data=apk_bytes, headers=up_headers, method="POST")
        try:
            with urllib.request.urlopen(up_req, timeout=300) as up_resp:
                print(f"   [DONE] Da tai xong: {apk.name}")
        except urllib.error.HTTPError as e:
            print(f"   [FAIL] Loi tai len {apk.name}: HTTP {e.code} - {e.read().decode('utf-8', errors='replace')}")
            return False
        except Exception as e:
            print(f"   [FAIL] Loi truyen file {apk.name}: {e}")
            return False

    print(f"\n[SUCCESS] Da phat hanh Release {tag} thanh cong tai:")
    print(f"-> {html_url}\n")
    return True

def parse_arguments():
    parser = argparse.ArgumentParser(
        description="Script tu dong dong goi va phat hanh Release APK cua CleanTV len GitHub Releases."
    )
    parser.add_argument(
        "version_pos",
        nargs="?",
        default=None,
        metavar="VERSION",
        help="So phien ban release tuy chon (vi du: 2.6.12 hoac 2.6.13-beta)."
    )
    parser.add_argument(
        "-v", "--version",
        type=str,
        default=None,
        dest="version_flag",
        metavar="VERSION",
        help="Tuy chon so phien ban release (vi du: 2.6.12)."
    )
    parser.add_argument(
        "--version-code",
        type=int,
        default=None,
        help="Tuy chon versionCode trong app/build.gradle.kts (vi du: 59)."
    )
    parser.add_argument(
        "-y", "--yes",
        action="store_true",
        default=False,
        help="Tu dong xac nhan va tien hanh (bo qua cac cau hoi tuong tac)."
    )
    parser.add_argument(
        "--skip-build",
        action="store_true",
        help="Bo qua buoc bien dich Gradle, su dung cac file APK da co san."
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Chay thu nghiem kiem tra phien ban, nhanh va danh sach APKs ma khong upload len GitHub."
    )
    parser.add_argument(
        "--tag",
        type=str,
        default=None,
        help="Tuy chinh Release Tag (mac dinh: vX.Y.Z)."
    )
    parser.add_argument(
        "--title",
        type=str,
        default=None,
        help="Tuy chinh tieu de Release (mac dinh: 'CleanTV v<version>')."
    )
    parser.add_argument(
        "--notes",
        type=str,
        default=None,
        help="Noi dung Release Notes dang text/markdown."
    )
    parser.add_argument(
        "--notes-file",
        type=Path,
        default=None,
        help="Duong dan toi file markdown chua noi dung Release Notes."
    )
    parser.add_argument(
        "--release",
        action="store_true",
        default=False,
        help="Bat buoc phat hanh duoi dang ban Release chinh thuc (Latest), khong danh dau Pre-release."
    )
    parser.add_argument(
        "--prerelease",
        action="store_true",
        default=None,
        help="Danh dau release nay la Pre-release (tu dong bat neu versionName co beta/alpha/rc)."
    )
    parser.add_argument(
        "--draft",
        action="store_true",
        default=False,
        help="Tao release o che do nhap (Draft)."
    )
    return parser.parse_args()

def main():
    args = parse_arguments()

    current_gradle_version = read_version()
    current_gradle_code = read_version_code()

    specified_version = args.version_flag or args.version_pos
    if specified_version:
        version = specified_version.strip().lstrip("v").lstrip("V")
    elif not args.yes:
        try:
            print("==================================================")
            print("CleanTV - Trinh phat hanh Release APK")
            print("==================================================")
            code_info = f" (versionCode: {current_gradle_code})" if current_gradle_code else ""
            print(f"Phien ban hien tai : {current_gradle_version}{code_info}")
            prompt_text = f"Nhap so phien ban release [Enter de giu '{current_gradle_version}']: "
            user_input = input(prompt_text).strip()
            if user_input:
                version = user_input.lstrip("v").lstrip("V")
            else:
                version = current_gradle_version
        except EOFError:
            version = current_gradle_version
        except KeyboardInterrupt:
            print("\n[INFO] Da huy thao tac.")
            sys.exit(0)
    else:
        version = current_gradle_version

    is_version_changed = (version != current_gradle_version)
    if args.version_code is not None:
        target_code = args.version_code
    elif is_version_changed and current_gradle_code is not None:
        target_code = current_gradle_code + 1
    else:
        target_code = current_gradle_code

    if not args.dry_run:
        if is_version_changed or (target_code != current_gradle_code):
            update_gradle_version(version, target_code)
    else:
        if is_version_changed:
            print(f"[DRY-RUN] Se cap nhat app/build.gradle.kts -> versionName = '{version}'")
        if target_code != current_gradle_code:
            print(f"[DRY-RUN] Se cap nhat app/build.gradle.kts -> versionCode = {target_code}")

    tag = args.tag if args.tag else f"v{version}"
    title = args.title if args.title else f"CleanTV {tag}"

    if args.notes:
        notes = args.notes
    elif args.notes_file and args.notes_file.exists():
        notes = args.notes_file.read_text(encoding="utf-8")
    else:
        notes = get_default_release_notes(version)

    if args.release:
        is_prerelease = False
    elif args.prerelease is not None:
        is_prerelease = args.prerelease
    elif not args.yes:
        default_is_pre = is_prerelease_version(version)
        default_choice = "2" if default_is_pre else "1"
        try:
            choice_prompt = f"Loai phat hanh [1: Release chinh thuc (Latest), 2: Pre-release] [Enter de chon '{default_choice}']: "
            user_choice = input(choice_prompt).strip()
            if user_choice == "1":
                is_prerelease = False
            elif user_choice == "2":
                is_prerelease = True
            else:
                is_prerelease = default_is_pre
        except (EOFError, KeyboardInterrupt):
            is_prerelease = default_is_pre
    else:
        is_prerelease = is_prerelease_version(version)

    print(f"\n==================================================")
    print(f"CleanTV - Tu dong phat hanh ban Release")
    print(f"Phien ban: {tag} ({title})")
    if target_code:
        print(f"Version Code: {target_code}")
    print(f"Pre-release: {'Co' if is_prerelease else 'Khong'} | Draft: {'Co' if args.draft else 'Khong'}")
    print(f"Nhanh nguon (Target Branch): {TARGET_BRANCH}")
    print(f"Kho chua (Repository): {GITHUB_OWNER}/{GITHUB_REPO}")
    if args.dry_run:
        print(f"Che do: DRY-RUN (Kiem tra thu nghiem, khong upload)")
    print(f"==================================================")

    # 1. Build
    if args.skip_build or args.dry_run:
        print("\n[1/3] Bo qua buoc dong goi APK. Su dung cac file APK da co san.")
    else:
        build_apk()

    # 2. Collect APKs
    apks = get_release_apks(version)
    if not apks:
        print(f"[ERROR] Khong tim thay file APK nao cho phien ban {version} trong build output!")
        print(f"Vui long build truoc: ./gradlew :app:assembleRelease")
        sys.exit(1)

    print(f"\n[2/3] Danh sach file APK Release ({len(apks)} tep):")
    total_size = 0
    for a in apks:
        size_mb = a.stat().st_size / (1024 * 1024)
        total_size += size_mb
        print(f"   - {a.name} ({size_mb:.2f} MB)")
    print(f"   => Tong dung luong: {total_size:.2f} MB")

    # Dong bo cap-nhat.json cho he thong OTA
    apk_main = apks[0]
    sync_ota_metadata(version, target_code or 58, apk_main, tag, notes)

    # Dry-run check
    if args.dry_run:
        print(f"\n[3/3] DRY-RUN thanh cong! Tat ca thong tin da san sang.")
        print(f"\n--- NOI DUNG RELEASE NOTES DU KIEN ---")
        print(notes.strip())
        print(f"--------------------------------------\n")
        return

    # 3. Upload
    gh_available = shutil.which("gh") is not None
    uploaded = False
    if gh_available:
        if upload_via_gh(tag, title, notes, apks, is_prerelease=is_prerelease, is_draft=args.draft):
            uploaded = True

    if not uploaded:
        token = read_token()
        if token:
            if upload_via_api(token, tag, title, notes, apks, is_prerelease=is_prerelease, is_draft=args.draft):
                uploaded = True
        else:
            print("\n[WARN] Chua cau hinh xac thuc GitHub!")
            print("Cai dat GitHub CLI (gh auth login) hoac them Token vao file local.properties:")
            print("   GITHUB_TOKEN=ghp_your_personal_access_token")
            sys.exit(1)

    if uploaded and not args.draft:
        try:
            files_to_commit = ["app/build.gradle.kts"]
            if WEB_CAP_NHAT.exists():
                files_to_commit.append("web/cap-nhat.json")
            if ROOT_CAP_NHAT.exists():
                files_to_commit.append("cap-nhat.json")

            subprocess.run(["git", "add", *files_to_commit], cwd=ROOT, check=True)
            status = subprocess.run(["git", "diff", "--cached", "--name-only"], cwd=ROOT, capture_output=True, text=True)
            if any(f in status.stdout for f in ["cap-nhat.json", "build.gradle"]):
                subprocess.run(["git", "commit", "-m", f"chore: phát hành CleanTV {tag} và cập nhật OTA metadata"], cwd=ROOT, check=True)
                subprocess.run(["git", "push", "origin", TARGET_BRANCH], cwd=ROOT, check=True)
                print(f"[OK] Da tu dong day cap-nhat.json & build.gradle.kts len nhanh {TARGET_BRANCH} de phuc vu cap nhat OTA.")
        except Exception as e:
            print(f"[WARN] Khong the tu dong push commit len git: {e}")

if __name__ == "__main__":
    main()

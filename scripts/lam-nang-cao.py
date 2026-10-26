#!/usr/bin/env python3
"""Danh sách tuỳ chọn ("switches") của launcher cho mục Nâng cao trong app, đọc bảng ở upstream themes/README.md.
Ghi assets/nang-cao.json: [{khoa, kieu (bool|so|chon|chu|ds|ds-so), min, max, chon[], moTa, canThan}]."""
import json, re, sys
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
readme = sys.argv[1] if len(sys.argv) > 1 else "upstream/themes/README.md"
out = sys.argv[2] if len(sys.argv) > 2 else "app/src/main/assets/nang-cao.json"
# App đã có công tắc / ô nhập riêng cho các khoá này (hoặc khoá không hợp nhập tay) — không lặp ở Nâng cao.
APP_CO_SAN = {"dock_apps", "greeting_name", "lunar_date", "always_show_names", "app_library", "library_button",
    "handle_view_inputs", "wallpaper", "race_calendar"}
# Đụng tới remote, nguồn vào, app của hãng: sai là mất HDMI / remote hết ăn — nhóm "cẩn thận".
CAN_THAN = {"live_tv_package", "live_tv_action", "live_tv_input_id", "source_panel_service", "source_panel_action",
    "source_panel_extra", "coocaa_sources", "input_action", "input_uri_scheme", "launch_actions", "app_banners",
    "settings_key_keycodes", "settings_key_panel", "input_key_keycodes", "app_keys", "key_filter", "stock_home_redirect",
    "boot_to_live_tv", "vpn_app", "module_cards", "hidden_apps", "app_labels", "festival", "festival_until", "greeting",
    "accent_color", "show_home_settings_shortcut"}
ds, da = [], set()
for dong in open(readme, encoding="utf-8"):
    if not dong.startswith("| `"): continue
    o = [x.strip() for x in dong.strip().strip("|").split("|")]
    if len(o) < 3: continue
    khoas = re.findall(r"`([a-z_]+)`", o[0]); kieu, mo = o[1], " | ".join(o[2:])
    if "config.xml" in kieu: continue
    m = re.match(r"(-?\d+) to (\d+)", kieu); lua = re.findall(r"`([a-z]+)`", kieu)
    if kieu == "bool": k = {"kieu": "bool"}
    elif m: k = {"kieu": "so", "min": int(m.group(1)), "max": int(m.group(2))}
    elif " or " in kieu and len(lua) >= 2: k = {"kieu": "chon", "chon": lua}
    elif kieu.startswith("list of keycodes"): k = {"kieu": "ds-so"}
    elif kieu.startswith("list") and "races" not in kieu: k = {"kieu": "ds"}
    elif kieu.startswith("list"): continue
    else: k = {"kieu": "chu"}
    for khoa in khoas:
        if khoa in APP_CO_SAN or khoa in da: continue
        da.add(khoa)
        cur_k = dict(k)
        if khoa == "columns" and cur_k.get("max") == 8:
            cur_k["max"] = 9
        ds.append({"khoa": khoa, **cur_k, "moTa": re.sub(r"`", "", mo)[:400], "canThan": khoa in CAN_THAN})

VGS_THEM = [
    {"khoa": "today_carousel", "kieu": "bool", "moTa": "Today Carousel (Quotes, History)", "canThan": False},
    {"khoa": "countdown_events", "kieu": "bool", "moTa": "Holiday & Tet countdown", "canThan": False},
    {"khoa": "show_events", "kieu": "bool", "moTa": "Show events row", "canThan": False},
    {"khoa": "show_today", "kieu": "bool", "moTa": "Show today card", "canThan": False},
    {"khoa": "show_weather", "kieu": "bool", "moTa": "Show weather card", "canThan": False},
    {"khoa": "weather_corner", "kieu": "bool", "moTa": "Show simple weather above settings button", "canThan": False},
    {"khoa": "settings_fallback_components", "kieu": "ds", "moTa": "Vendor settings components tried in order", "canThan": True},
    {"khoa": "race_calendar", "kieu": "ds", "moTa": "F1 race-weekend mode", "canThan": False},
    {"khoa": "rows_offset_dp", "kieu": "so", "min": -300, "max": 300, "moTa": "Offset the dock and grid rows up (negative) or down (positive) in dp within the screen (0 by default)", "canThan": False},
    {"khoa": "icon_tile_color", "kieu": "bool", "moTa": "Phone apps without a TV banner get a tile filled with their icon's colour and a larger icon instead of a grey card (true by default)", "canThan": False},
    {"khoa": "dream_button", "kieu": "bool", "moTa": "Screensaver button in the top bar to quickly launch the screensaver/clock (false by default)", "canThan": False},
    {"khoa": "top_compact", "kieu": "bool", "moTa": "Compact icon-only cluster for top header buttons, dropping text labels (false by default)", "canThan": False},
    {"khoa": "media_bar", "kieu": "bool", "moTa": "Show active music playback controls (Spotify, YouTube Music...) on the top bar (true by default)", "canThan": False},
]
for them in VGS_THEM:
    if them["khoa"] not in da:
        da.add(them["khoa"])
        ds.append(them)

# Tên + mô tả tiếng Việt (scripts/nang-cao-vi.json, 22/10/2026): người không biết tiếng Anh vẫn dùng được.
import os
vi_tep = os.path.join(os.path.dirname(os.path.abspath(__file__)), "nang-cao-vi.json")
vi = json.load(open(vi_tep, encoding="utf-8")) if os.path.exists(vi_tep) else {}
thieu = []
for x in ds:
    v = vi.get(x["khoa"])
    if not v: thieu.append(x["khoa"]); continue
    x["ten"] = v["ten"]; x["moTaVi"] = v["moTa"]
    if "chon" in v: x["chonVi"] = v["chon"]
if thieu: print("CHƯA DỊCH (thêm vào scripts/nang-cao-vi.json):", ", ".join(thieu))
json.dump(ds, open(out, "w", encoding="utf-8"), ensure_ascii=False, indent=0)
print(f"{len(ds)} tuỳ chọn ({sum(x['canThan'] for x in ds)} cẩn thận) -> {out}")

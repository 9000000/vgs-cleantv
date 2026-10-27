/* Trang giới thiệu CleanTV tĩnh (cleantv.alexvgs.tech, 27/10/2026). Số mẫu tivi, bản đang phát, nhật ký đọc từ file
   cùng nguồn /tivi.json, /cap-nhat.json, /nhat-ky.json (scripts/phat-hanh.sh + lam-ds-tivi.py ghi) — ra bản mới không
   phải sửa trang. Không thư viện. */
(function () {
  "use strict";

  var LINK_APK = "https://cleantv-dem.vgszone.workers.dev/tai?tu=vgszone";
  /** Bộ đếm người dùng: Worker cleantv-dem, có Access-Control-Allow-Origin: *. */
  var NGUON_SO_NGUOI = "https://cleantv-dem.vgszone.workers.dev/so-nguoi";
  var CHU_KY_SO_NGUOI = 30000;
  /** Góp ý vào cùng hộp của vgs.zone (phần "cleantv"). */
  var API_GOP_Y = "https://vgs.zone/api/gop-y";
  /**
   * /api/gop-y chưa trả header CORS cho cleantv.alexvgs.tech (soát 27/10/2026), nên trình duyệt không cho đọc câu trả
   * lời: gửi kiểu no-cors (multipart là "simple request", máy chủ vẫn nhận và lưu), không đọc được lỗi máy chủ.
   * Khi máy chủ thêm Access-Control-Allow-Origin: https://cleantv.alexvgs.tech thì đổi thành true để hiện đúng lỗi.
   */
  var GOP_Y_DOC_DUOC = false;

  function $(sel, goc) { return (goc || document).querySelector(sel); }
  function $$(sel, goc) { return Array.prototype.slice.call((goc || document).querySelectorAll(sel)); }
  function tao(the, lop, chu) {
    var el = document.createElement(the);
    if (lop) el.className = lop;
    if (chu != null) el.textContent = chu;
    return el;
  }
  function soVn(n) { return Number(n).toLocaleString("vi-VN"); }
  function ngayVn(s) {
    var p = String(s || "").split("-");
    return p.length === 3 ? Number(p[2]) + "/" + Number(p[1]) + "/" + p[0] : String(s || "");
  }
  function layJson(url) {
    return fetch(url, { cache: "no-cache" }).then(function (r) {
      if (!r.ok) throw new Error(url + " " + r.status);
      return r.json();
    });
  }

  /* Dữ liệu đổ về sau khi trình duyệt đã cuộn tới #cap-nhat / #tivi: cuộn lại đúng chỗ một lần. */
  var daCuonLai = false;
  function cuonLaiTheoNeo() {
    if (daCuonLai || !location.hash || location.hash.length < 2) return;
    var dich;
    try { dich = document.getElementById(decodeURIComponent(location.hash.slice(1))); } catch (e) { return; }
    if (!dich) return;
    var y = dich.getBoundingClientRect().top + window.pageYOffset - 76;
    window.scrollTo({ top: Math.max(0, y), behavior: "instant" });
  }
  var choDuLieu = [];
  window.addEventListener("wheel", function () { daCuonLai = true; }, { passive: true, once: true });
  window.addEventListener("touchmove", function () { daCuonLai = true; }, { passive: true, once: true });

  /* ── Bản đang phát ── */
  function chuBan(b) {
    var p = String(b.ngay || "").split("-");
    var ngay = p.length === 3 ? " · " + Number(p[2]) + "/" + Number(p[1]) + "/" + p[0] : "";
    var mb = typeof b.co === "number" ? " · " + (b.co / 1048576).toFixed(1).replace(".", ",") + " MB" : "";
    return "Bản " + b.versionName + ngay + mb;
  }
  layJson("/cap-nhat.json").then(function (j) {
    var b = j && j.app;
    if (!b || !b.versionName) return;
    $$("[data-ban]").forEach(function (el) {
      el.textContent = "";
      el.appendChild(tao("b", "", chuBan(b)));
      if (el.hasAttribute("data-ghi-chu") && b.ghiChu) el.appendChild(document.createTextNode(" — mới: " + b.ghiChu));
      el.hidden = false;
    });
  }).catch(function () { /* không có thì để ẩn */ });

  /* ── Danh sách tivi đã thử ── */
  var LOAI_CHU = { box: "Box", "may-chieu": "Máy chiếu" };
  function boDau(s) { return String(s).normalize("NFD").replace(/[̀-ͯ]/g, "").replace(/đ/gi, "d").toLowerCase(); }

  function dungDsTivi(ds) {
    var khung = $("#ds-tivi");
    if (!khung) return;
    var o = $(".dsTim", khung), dem = $(".dsDem", khung), cuon = $(".dsCuon", khung);
    var loai = "";
    var chiMuc = ds.map(function (m) { return boDau([m.hang, m.ten, m.chiTiet, m.android].join(" ")); });

    function ve() {
      var tu = boDau(o.value.trim());
      var theoHang = new Map();
      ds.forEach(function (m, i) {
        if (loai && m.loai !== loai) return;
        if (tu && chiMuc[i].indexOf(tu) < 0) return;
        if (!theoHang.has(m.hang)) theoHang.set(m.hang, []);
        theoHang.get(m.hang).push(m);
      });
      var nhom = Array.from(theoHang.entries()).sort(function (a, b) {
        return b[1].length - a[1].length || a[0].localeCompare(b[0]);
      });
      var so = nhom.reduce(function (n, x) { return n + x[1].length; }, 0);
      dem.textContent = so + " mẫu · " + nhom.length + " hãng";
      var frag = document.createDocumentFragment();
      if (!nhom.length) {
        frag.appendChild(tao("p", "dsTrong", "Chưa có mẫu này trong danh sách. Tivi chạy Android TV / Google TV vẫn dùng được: app áp luật chung an toàn, chỉ tích sẵn gói đã tắt ổn trên nhiều mẫu khác."));
      }
      nhom.forEach(function (x) {
        var sec = tao("section", "dsHang");
        var h = tao("h3", "", x[0] + " ");
        h.appendChild(tao("span", "", String(x[1].length)));
        sec.appendChild(h);
        var ul = tao("ul");
        x[1].forEach(function (m) {
          var li = tao("li");
          var dong = tao("div", "dsTen");
          dong.appendChild(tao("b", "", m.ten));
          if (m.loai && m.loai !== "tivi") dong.appendChild(tao("em", "", LOAI_CHU[m.loai] || m.loai));
          if (m.android) dong.appendChild(tao("span", "", m.android));
          li.appendChild(dong);
          if (m.chiTiet) li.appendChild(tao("small", "", m.chiTiet));
          ul.appendChild(li);
        });
        sec.appendChild(ul);
        frag.appendChild(sec);
      });
      cuon.textContent = "";
      cuon.appendChild(frag);
      cuon.scrollTop = 0;
    }

    var hen;
    o.addEventListener("input", function () { clearTimeout(hen); hen = setTimeout(ve, 80); });
    $$(".dsChip button", khung).forEach(function (nut) {
      nut.addEventListener("click", function () {
        loai = nut.getAttribute("data-loai") || "";
        $$(".dsChip button", khung).forEach(function (b) { b.setAttribute("aria-pressed", b === nut ? "true" : "false"); });
        ve();
      });
    });
    ve();
  }

  choDuLieu.push(layJson("/tivi.json").then(function (j) {
    var ds = j && Array.isArray(j.ds) ? j.ds.filter(function (m) { return m && m.hang && m.ten; }) : [];
    if (!ds.length) throw new Error("tivi.json rỗng");
    var soMau = ds.length;
    var soHang = new Set(ds.map(function (m) { return m.hang; })).size;
    $$("[data-so-mau]").forEach(function (el) { el.textContent = soVn(soMau); });
    $$("[data-so-hang]").forEach(function (el) { el.textContent = soVn(soHang); });
    dungDsTivi(ds);
  }).catch(function () {
    // Như bản Next: không tải được danh sách thì bỏ hẳn khối, số giữ mức 243 mẫu / 70 hãng (bản 2.3).
    var k = $("#tivi");
    if (k) k.hidden = true;
    $$('a[href="#tivi"]').forEach(function (a) { a.hidden = true; });
  }));

  /* ── Nhật ký cập nhật ── */
  function theNhatKy(d, moiNhat, dangDung) {
    var li = tao("li", moiNhat ? "nkMoi" : "");
    li.id = "ban-" + d.ban;
    var dau = tao("div", "nkDau");
    dau.appendChild(tao("b", "", "Bản " + d.ban));
    if (moiNhat) dau.appendChild(tao("span", "nkNhan", "Mới nhất"));
    if (dangDung) dau.appendChild(tao("span", "nkDangDung", "Bạn đang dùng"));
    dau.appendChild(tao("span", "", ngayVn(d.ngay)));
    li.appendChild(dau);
    var ul = tao("ul");
    d.vi.forEach(function (y) { ul.appendChild(tao("li", "", y)); });
    li.appendChild(ul);
    return li;
  }

  choDuLieu.push(layJson("/nhat-ky.json").then(function (ds) {
    ds = Array.isArray(ds) ? ds.filter(function (d) { return d && d.ban && Array.isArray(d.vi); }) : [];
    if (!ds.length) throw new Error("nhat-ky.json rỗng");
    // App mở /?b=<bản đang dùng>#cap-nhat: đánh dấu bản đó.
    var b = null;
    try { b = new URLSearchParams(location.search).get("b"); } catch (e) { /* */ }
    var dau = $("#nhat-ky");
    ds.slice(0, 3).forEach(function (d, i) { dau.appendChild(theNhatKy(d, i === 0, d.ban === b)); });
    if (ds.length > 3) {
      var cu = $("#nhat-ky-cu");
      $("summary", cu).textContent = "Xem các bản trước (" + (ds.length - 3) + ")";
      var ol = $("ol", cu);
      ds.slice(3).forEach(function (d) { ol.appendChild(theNhatKy(d, false, d.ban === b)); });
      if (b && ds.slice(3).some(function (d) { return d.ban === b; })) cu.open = true;
      cu.hidden = false;
    }
  }).catch(function () {
    var k = $("#cap-nhat");
    if (k) k.hidden = true;
  }));
  // Hai khối #tivi và #cap-nhat dựng từ JSON làm trang dài ra: xong cả hai thì cuộn lại đúng neo trong link.
  Promise.all(choDuLieu).then(cuonLaiTheoNeo);

  /* ── Số người đã dùng app (chữ số cuộn dọc) — hỏi lại 30 giây một lần, CHỈ khi tab đang mở ── */
  (function () {
    var khung = $("#so-nguoi");
    if (!khung) return;
    var dem = $(".soDem", khung);
    var n = 0;

    function ve() {
      var chu = soVn(n).split("");
      var cu = {};
      $$("[data-vt]", dem).forEach(function (o) { cu[o.getAttribute("data-vt")] = o; });
      var frag = document.createDocumentFragment();
      chu.forEach(function (k, i) {
        var vt = String(chu.length - i);
        var o;
        if (!/\d/.test(k)) {
          o = tao("span", "soPhay", k);
          o.setAttribute("data-vt", "p" + vt);
        } else {
          o = cu["d" + vt];
          if (!o) {
            o = tao("span", "soO");
            o.setAttribute("data-vt", "d" + vt);
            var cot = tao("span", "soCot");
            for (var d = 0; d <= 9; d++) cot.appendChild(tao("span", "", String(d)));
            o.appendChild(cot);
          }
          var c = o.firstChild;
          // Lần đầu: đặt ở 0 rồi trượt tới số đúng ở khung hình sau.
          if (!c.style.transform) {
            c.style.transform = "translateY(0)";
            requestAnimationFrame(function () { requestAnimationFrame(function () { c.style.transform = "translateY(" + -Number(k) + "em)"; }); });
          } else {
            c.style.transform = "translateY(" + -Number(k) + "em)";
          }
        }
        frag.appendChild(o);
      });
      dem.textContent = "";
      dem.appendChild(frag);
      khung.setAttribute("aria-label", soVn(n) + " người đã dùng app");
      khung.hidden = false;
    }

    function hoi() {
      if (document.visibilityState !== "visible") return Promise.resolve();
      return fetch(NGUON_SO_NGUOI, { cache: "no-store" })
        .then(function (r) { return r.json(); })
        .then(function (j) {
          if (j && typeof j.n === "number" && j.n > n) { n = j.n; ve(); }
        })
        .catch(function () { /* lỗi thì giữ số cũ, chưa có số thì để ẩn */ });
    }
    var hen;
    function lap() { hen = setTimeout(function () { hoi().then(lap); }, CHU_KY_SO_NGUOI); }
    hoi();
    lap();
    document.addEventListener("visibilitychange", function () { if (document.visibilityState === "visible") hoi(); });
  })();

  /* ── Trước & sau: kéo thanh giữa ── */
  (function () {
    var khung = $("#so-sanh");
    if (!khung) return;
    var o = $("input", khung);
    var dat = function () { khung.style.setProperty("--x", o.value + "%"); };
    o.addEventListener("input", dat);
    dat();
  })();

  /* ── Mời vào cộng đồng VGS sau khi bấm Tải APK (một lần mỗi phiên) ── */
  (function () {
    var the = $("#moi-the");
    if (!the) return;
    var daHien = false;
    try { daHien = sessionStorage.getItem("cleantv-moi") === "1"; } catch (e) { /* */ }
    document.addEventListener("click", function (e) {
      var a = e.target && e.target.closest ? e.target.closest("a") : null;
      if (!a || a.getAttribute("href") !== LINK_APK || daHien) return;
      daHien = true;
      try { sessionStorage.setItem("cleantv-moi", "1"); } catch (e2) { /* */ }
      setTimeout(function () { the.hidden = false; }, 900);
    });
    $(".moiDong", the).addEventListener("click", function () { the.hidden = true; });
  })();

  /* ── Góp ý: chỉ email + nội dung (luật 25/10/2026) ── */
  (function () {
    var form = $("#form-gop-y");
    if (!form) return;
    var bao = $(".gopYBao", form);
    var nut = $("button[type=submit]", form);
    var trang = "CleanTV · cleantv.alexvgs.tech";
    var EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

    function datBao(chu, kieu) { bao.textContent = chu; bao.className = "gopYBao" + (kieu ? " " + kieu : ""); }
    function loaiDangChon() { var r = $("input[name=loai]:checked", form); return r ? r.value : "loi"; }
    function doiChuNut() { nut.textContent = loaiDangChon() === "loi" ? "Gửi báo lỗi" : "Gửi góp ý"; }
    $$("input[name=loai]", form).forEach(function (r) { r.addEventListener("change", doiChuNut); });

    // Link "báo ở đây" của mục Cài thẳng lên tivi: ghi rõ nguồn như ?tu= của bản cũ.
    $$("[data-gop-y-tu]").forEach(function (a) {
      a.addEventListener("click", function () { trang = a.getAttribute("data-gop-y-tu") + " · cleantv.alexvgs.tech"; });
    });

    form.addEventListener("submit", function (e) {
      e.preventDefault();
      var chu = form.chu.value.trim();
      var lienHe = form.lienHe.value.trim();
      if (chu.length < 10) { datBao("Kể rõ hơn chút nhé — ít nhất một câu.", "loi"); form.chu.focus(); return; }
      if (chu.length > 2000) { datBao("Dài quá rồi — gọn lại dưới 2.000 ký tự nhé.", "loi"); form.chu.focus(); return; }
      if (!EMAIL.test(lienHe)) { datBao("Góp ý CleanTV được trả lời qua email, bạn để lại địa chỉ email giúp mình nhé.", "loi"); form.lienHe.focus(); return; }

      var f = new FormData();
      f.append("loai", loaiDangChon());
      f.append("chu", chu);
      f.append("trang", trang);
      f.append("lienHe", lienHe);
      f.append("nen", "web");
      f.append("phan", "cleantv");

      nut.disabled = true;
      datBao("Đang gửi…");
      fetch(API_GOP_Y, GOP_Y_DOC_DUOC ? { method: "POST", body: f } : { method: "POST", body: f, mode: "no-cors" })
        .then(function (r) {
          if (!GOP_Y_DOC_DUOC) return { ok: true };
          return r.json().catch(function () { return {}; }).then(function (j) {
            return r.ok ? { ok: true } : { ok: false, loi: j.error || "Gửi chưa được, thử lại sau nhé." };
          });
        })
        .then(function (kq) {
          if (!kq.ok) { datBao(kq.loi, "loi"); return; }
          form.chu.value = "";
          datBao("Đã gửi, cảm ơn bạn! CleanTV sẽ trả lời qua email " + lienHe + ".", "xong");
        })
        .catch(function () {
          var tieuDe = encodeURIComponent((loaiDangChon() === "loi" ? "Báo lỗi" : "Góp ý") + " CleanTV");
          datBao("", "loi");
          bao.appendChild(document.createTextNode("Chưa gửi được (mạng lỗi). Thử lại, hoặc gửi email tới "));
          var a = tao("a", "", "cleantv@alexvgs.tech");
          a.href = "mailto:cleantv@alexvgs.tech?subject=" + tieuDe + "&body=" + encodeURIComponent(chu);
          bao.appendChild(a);
          bao.appendChild(document.createTextNode("."));
        })
        .then(function () { nut.disabled = false; });
    });
  })();
})();

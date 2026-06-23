package com.govautofill.utils

import com.govautofill.model.UserProfile

object JsFormFiller {

    fun buildScript(profile: UserProfile): String {
        val p = profile
        fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
        val careOfVal = if (p.careOf.isNotBlank()) p.careOf else p.fatherNameEn.ifBlank { p.fatherNameBn }

        return """
(function() {
  // ── Profile Data ──────────────────────────────────────────────────────────
  var fullNameEn   = "${esc(p.fullNameEn)}";
  var fullNameBn   = "${esc(p.fullNameBn)}";
  var fatherEn     = "${esc(p.fatherNameEn)}";
  var fatherBn     = "${esc(p.fatherNameBn)}";
  var motherEn     = "${esc(p.motherNameEn)}";
  var motherBn     = "${esc(p.motherNameBn)}";
  var spouseEn     = "${esc(p.spouseNameEn)}";
  var spouseBn     = "${esc(p.spouseNameBn)}";
  var careOf       = "${esc(careOfVal)}";
  var dob          = "${esc(p.dateOfBirth)}";
  var nid          = "${esc(p.nidNo)}";
  var birthCertNo  = "${esc(p.birthCertificateNo)}";
  var passport     = "${esc(p.passportNo)}";
  var mobile       = "${esc(p.mobileNo)}";
  var email        = "${esc(p.email)}";
  var religion     = "${esc(p.religion)}";
  var gender       = "${esc(p.gender)}";
  var blood        = "${esc(p.bloodGroup)}";
  var nationality  = "${esc(p.nationality)}";
  var marital      = "${esc(p.maritalStatus)}";
  var quota        = "${esc(p.quota)}";
  var pVillage     = "${esc(p.presentVillage)}";
  var pPostOffice  = "${esc(p.presentPostOffice)}";
  var pUpazila     = "${esc(p.presentUpazila)}";
  var pDistrict    = "${esc(p.presentDistrict)}";
  var pDivision    = "${esc(p.presentDivision)}";
  var pPostCode    = "${esc(p.presentPostCode)}";
  var sVillage     = "${esc(p.permanentVillage)}";
  var sPostOffice  = "${esc(p.permanentPostOffice)}";
  var sUpazila     = "${esc(p.permanentUpazila)}";
  var sDistrict    = "${esc(p.permanentDistrict)}";
  var sDivision    = "${esc(p.permanentDivision)}";
  var sPostCode    = "${esc(p.permanentPostCode)}";
  var sscBoard     = "${esc(p.sscBoard)}";
  var sscRoll      = "${esc(p.sscRollNo)}";
  var sscReg       = "${esc(p.sscRegNo)}";
  var sscYear      = "${esc(p.sscYear)}";
  var sscGpa       = "${esc(p.sscGpa)}";
  var sscGroup     = "${esc(p.sscGroup)}";
  var hscBoard     = "${esc(p.hscBoard)}";
  var hscRoll      = "${esc(p.hscRollNo)}";
  var hscReg       = "${esc(p.hscRegNo)}";
  var hscYear      = "${esc(p.hscYear)}";
  var hscGpa       = "${esc(p.hscGpa)}";
  var hscGroup     = "${esc(p.hscGroup)}";
  var gradDegree   = "${esc(p.graduationDegree)}";
  var gradSubject  = "${esc(p.graduationSubject)}";
  var gradInst     = "${esc(p.graduationInstitution)}";
  var gradYear     = "${esc(p.graduationYear)}";
  var gradResult   = "${esc(p.graduationResult)}";

  // ── নাম/আইডি-ভিত্তিক rules (যেখানে field-এর নিজের name/id/label-এই যথেষ্ট তথ্য থাকে) ──
  var rules = [
    { pat: /applicant.*name.*bn|applicantnameben|namebn|namebengali|আবেদনকারী.*নাম|বাংলা.*নাম/i, val: fullNameBn },
    { pat: /father.*name.*bn|fathernameben|piternamebn|পিতার.*নাম|father.*bangla/i,              val: fatherBn },
    { pat: /mother.*name.*bn|mothernameben|maternamebn|মাতার.*নাম|mother.*bangla/i,              val: motherBn },
    { pat: /spouse.*bn|husband.*bn|wife.*bn|স্বামী|স্ত্রী/i,                                   val: spouseBn },
    { pat: /applicant.*name(?!.*bn)|applicantnameen|applicantname$|^name$/i,                     val: fullNameEn },
    { pat: /father.*name(?!.*bn)|fathernameen|father_name$/i,                                    val: fatherEn },
    { pat: /mother.*name(?!.*bn)|mothernameen|mother_name$/i,                                    val: motherEn },
    { pat: /spouse.*en|spouse.*name$|husband.*name$|wife.*name$/i,                               val: spouseEn },
    { pat: /date.*birth|birth.*date|^dob$|জন্ম.*তারিখ/i,                                        val: dob },

    { pat: /national.*id(?!.*type)|nid(?!.*card)(?!.*type)|জাতীয়.*পরিচয়/i,                     val: nid },
    { pat: /birth.*reg(?!.*type)|জন্ম.*নিবন্ধন/i,                                                val: birthCertNo },
    { pat: /passport(?!.*type)/i,                                                                 val: passport },
    { pat: /mobile|phone|cell|মোবাইল/i,                                                          val: mobile },
    { pat: /email/i,                                                                              val: email },
    { pat: /religion|ধর্ম/i,                                                                     val: religion },
    { pat: /gender|sex|লিঙ্গ/i,                                                                  val: gender },
    { pat: /blood/i,                                                                              val: blood },
    { pat: /nationality|জাতীয়তা/i,                                                               val: nationality },
    { pat: /marital|বৈবাহিক/i,                                                                   val: marital },
    { pat: /quota|কোটা/i,                                                                        val: quota },

    // District/Upazila/Division/PostCode-এ সাধারণত present/permanent prefix থাকে (যেমন ddl_present_district)
    { pat: /present.*upazila|p.*upazila/i,                  val: pUpazila },
    { pat: /present.*district|p.*district|p.*zila/i,        val: pDistrict },
    { pat: /present.*division|p.*division/i,                val: pDivision },
    { pat: /present.*post.*code|p.*post.*code|present.*zip/i, val: pPostCode },
    { pat: /permanent.*upazila|s.*upazila/i,                 val: sUpazila },
    { pat: /permanent.*district|s.*district|s.*zila/i,       val: sDistrict },
    { pat: /permanent.*division|s.*division/i,               val: sDivision },
    { pat: /permanent.*post.*code|s.*post.*code|permanent.*zip/i, val: sPostCode },

    // SSC/HSC — Board/Roll/Reg/Year/Gpa-তে সাধারণত ssc/hsc prefix থাকে
    { pat: /ssc.*board|ssc.*bord/i,       val: sscBoard },
    { pat: /ssc.*roll/i,                  val: sscRoll },
    { pat: /ssc.*reg/i,                   val: sscReg },
    { pat: /ssc.*year|ssc.*pass/i,        val: sscYear },
    { pat: /ssc.*gpa|ssc.*result|ssc.*grade/i, val: sscGpa },
    { pat: /hsc.*board/i,                 val: hscBoard },
    { pat: /hsc.*roll/i,                  val: hscRoll },
    { pat: /hsc.*reg/i,                   val: hscReg },
    { pat: /hsc.*year|hsc.*pass/i,        val: hscYear },
    { pat: /hsc.*gpa|hsc.*result/i,       val: hscGpa },

    // Graduation
    { pat: /degree|graduation.*degree/i,  val: gradDegree },
    { pat: /grad.*subject|graduation.*subject/i, val: gradSubject },
    { pat: /university|institution|college/i, val: gradInst },
    { pat: /grad.*year|graduation.*year|passing.*year/i, val: gradYear },
    { pat: /cgpa|grad.*result|graduation.*result/i, val: gradResult }
  ];

  // ── Bangla ↔ English synonym groups ────────────────────────────────────────
  var SYN_GROUPS = [
    ['male', 'm', 'পুরুষ'], ['female', 'f', 'মহিলা', 'নারী'],
    ['islam', 'muslim', 'ইসলাম'], ['hindu', 'হিন্দু'], ['christian', 'খ্রিস্টান'], ['buddhist', 'বৌদ্ধ'],
    ['married', 'বিবাহিত'], ['unmarried', 'single', 'অবিবাহিত'],
    ['bangladeshi', 'বাংলাদেশী', 'bangladesh'], ['none', 'no', 'না', 'নাই', 'n/a']
  ];
  var SHORT_SYN_WORDS = [].concat.apply([], SYN_GROUPS);

  function inSameSynGroup(a, b) {
    for (var g = 0; g < SYN_GROUPS.length; g++) {
      var grp = SYN_GROUPS[g];
      if (grp.indexOf(a) !== -1 && grp.indexOf(b) !== -1) return true;
    }
    return false;
  }

  // ── Section-context detection ────────────────────────────────────────────
  // অনেক field-এর (Examination/Result/Group-Subject, Village/Post Office) নিজের নামে কোনো
  // পরিচয় থাকে না — কোন কার্ড/সেকশনের ভেতরে আছে (যেমন "SSC/Equivalent Level" বা
  // "Present Address") সেটা দেখেই বুঝতে হয়। তাই পেইজের সব সেকশন-হেডার আগে থেকে খুঁজে রাখি,
  // তারপর প্রতিটা field-এর জন্য DOM-order-এ তার ঠিক আগের header-টা context হিসেবে ধরি।
  var SECTION_PATTERNS = [
    { tag: 'permanent',  re: /permanent[\s\S]{0,20}address|স্থায়ী[\s\S]{0,10}ঠিকানা/i },
    { tag: 'present',    re: /present[\s\S]{0,20}address|বর্তমান[\s\S]{0,10}ঠিকানা/i },
    { tag: 'hsc',        re: /h\.?\s*s\.?\s*c[\s\S]{0,20}(equivalent|level)/i },
    { tag: 'ssc',        re: /s\.?\s*s\.?\s*c[\s\S]{0,20}(equivalent|level)/i },
    { tag: 'graduation', re: /graduation[\s\S]{0,20}(equivalent|level)/i }
  ];
  var sectionHeaderCache = null;
  function getSectionHeaders() {
    if (sectionHeaderCache) return sectionHeaderCache;
    var found = [];
    var all = document.querySelectorAll('div, h1, h2, h3, h4, h5, legend, span, td, th, p');
    for (var i = 0; i < all.length; i++) {
      var txt = (all[i].innerText || all[i].textContent || '').trim();
      if (!txt || txt.length > 60) continue; // section header সাধারণত ছোট টেক্সট হয়
      for (var j = 0; j < SECTION_PATTERNS.length; j++) {
        if (SECTION_PATTERNS[j].re.test(txt)) { found.push({ el: all[i], tag: SECTION_PATTERNS[j].tag }); break; }
      }
    }
    sectionHeaderCache = found;
    return found;
  }
  function getSectionContext(el) {
    var headers = getSectionHeaders();
    var best = null;
    for (var i = 0; i < headers.length; i++) {
      // headers[i].el যদি el-এর আগে (DOM order এ) থাকে, candidate — শেষ পর্যন্ত সবচেয়ে কাছের টা থাকবে
      var rel = headers[i].el.compareDocumentPosition(el);
      if (rel & Node.DOCUMENT_POSITION_FOLLOWING) best = headers[i].tag;
    }
    return best;
  }

  // context অনুযায়ী generic keyword (village/post office/care of/examination/result/group-subject)
  // resolve করার rule গুলো — এগুলোর নিজের নামে present/permanent/ssc/hsc কিছু লাগবে না
  var CONTEXT_KEYWORD_RULES = [
    { pat: /village|road|house|flat|town|গ্রাম|মহল্লা/i, key: 'village' },
    { pat: /post.*office|পোস্ট.*অফিস/i, key: 'postOffice' },
    { pat: /care.*of/i, key: 'careOf' },
    { pat: /examination/i, key: 'examination' },
    { pat: /^result$|result\s*\/?\s*grade/i, key: 'result' },
    { pat: /group.*subject|subject.*group|subject.*degree/i, key: 'groupSubject' }
  ];
  function contextValue(context, key) {
    if (context === 'present') {
      if (key === 'village') return pVillage;
      if (key === 'postOffice') return pPostOffice;
      if (key === 'careOf') return careOf;
    }
    if (context === 'permanent') {
      if (key === 'village') return sVillage;
      if (key === 'postOffice') return sPostOffice;
      if (key === 'careOf') return careOf;
    }
    if (context === 'ssc') {
      if (key === 'examination') return 'S.S.C';
      if (key === 'result') return sscGpa;
      if (key === 'groupSubject') return sscGroup;
    }
    if (context === 'hsc') {
      if (key === 'examination') return 'H.S.C';
      if (key === 'result') return hscGpa;
      if (key === 'groupSubject') return hscGroup;
    }
    if (context === 'graduation') {
      if (key === 'examination') return gradDegree;
      if (key === 'result') return gradResult;
      if (key === 'groupSubject') return gradSubject;
    }
    return null;
  }

  // ── Identifiers collector — নাম/আইডি/placeholder/label + আশেপাশের visible টেক্সট ────
  function getIdentifiers(el) {
    var ids = [
      el.getAttribute('name') || '', el.getAttribute('id') || '',
      el.getAttribute('placeholder') || '', el.getAttribute('aria-label') || ''
    ];
    if (el.id) {
      var lbl = document.querySelector('label[for="' + el.id + '"]');
      if (lbl) ids.push(lbl.innerText || lbl.textContent || '');
    }
    var parentLabel = el.closest('label');
    if (parentLabel) ids.push(parentLabel.innerText || '');

    // Bootstrap/Card layout-এ label আর input আলাদা div/td হিসেবে থাকে, <label for=""> ছাড়াও —
    // কয়েক লেভেল উপরে গিয়ে কাছাকাছি টেক্সট খুঁজি (deep nesting handle করার জন্য)
    var node = el;
    for (var depth = 0; depth < 4 && node; depth++) {
      var prevSib = node.previousElementSibling;
      if (prevSib && prevSib.tagName !== 'SELECT' && prevSib.tagName !== 'INPUT' && prevSib.tagName !== 'TEXTAREA') {
        var t = (prevSib.innerText || prevSib.textContent || '').trim();
        if (t && t.length < 60) ids.push(t);
      }
      node = node.parentElement;
    }
    return ids.filter(function (s) { return s.trim().length > 0; });
  }

  function matchRule(identifiers) {
    for (var r = 0; r < rules.length; r++) {
      var rule = rules[r];
      if (!rule.val) continue;
      for (var i = 0; i < identifiers.length; i++) {
        if (rule.pat.test(identifiers[i])) return rule.val;
      }
    }
    return null;
  }

  function matchContextKey(identifiers) {
    for (var r = 0; r < CONTEXT_KEYWORD_RULES.length; r++) {
      for (var i = 0; i < identifiers.length; i++) {
        if (CONTEXT_KEYWORD_RULES[r].pat.test(identifiers[i])) return CONTEXT_KEYWORD_RULES[r].key;
      }
    }
    return null;
  }

  // ── তারিখ ফরম্যাট কনভার্ট (DD/MM/YYYY → YYYY-MM-DD, type="date" input-এর জন্য) ─────────
  function pad2(n) { n = n.toString(); return n.length < 2 ? '0' + n : n; }
  function toIsoDate(ddmmyyyy) {
    var m = ddmmyyyy.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/);
    if (!m) return null;
    return m[3] + '-' + pad2(m[1]) + '-' + pad2(m[2]);
  }

  // ── Text input / textarea fill helper ───────────────────────────────────────
  function setVal(el, val) {
    if (!val || el.readOnly || el.disabled) return false;
    if (el.tagName === 'INPUT' && el.type === 'date') {
      var iso = toIsoDate(val);
      if (iso) val = iso; else return false; // ফরম্যাট না মিললে আর কিছু করার নেই
    }
    try {
      var desc = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value')
               || Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value');
      if (desc && desc.set) desc.set.call(el, val);
      else el.value = val;
      el.dispatchEvent(new Event('input',  { bubbles: true }));
      el.dispatchEvent(new Event('change', { bubbles: true }));
      el.dispatchEvent(new Event('blur',   { bubbles: true }));
      return true;
    } catch (e) { return false; }
  }

  // ── SELECT/dropdown matching — multi-stage ──────────────────────────────────
  function findOptionIndex(selectEl, val) {
    if (!val) return -1;
    var target = val.toString().trim().toLowerCase();
    if (!target) return -1;
    var opts = selectEl.options;
    var i;
    for (i = 0; i < opts.length; i++) {
      var t = (opts[i].text || '').trim().toLowerCase();
      var v = (opts[i].value || '').trim().toLowerCase();
      if (t === target || v === target) return i;
    }
    for (i = 0; i < opts.length; i++) {
      var t2 = (opts[i].text || '').trim().toLowerCase();
      if (t2 && inSameSynGroup(target, t2)) return i;
    }
    for (i = 0; i < opts.length; i++) {
      var t3 = (opts[i].text || '').trim().toLowerCase();
      if (!t3 || t3 === 'select' || t3.indexOf('select') === 0) continue;
      if (target.length < 4 || t3.length < 4) continue;
      if (t3.indexOf(target) !== -1) return i;
      if (target.indexOf(t3) !== -1 && SHORT_SYN_WORDS.indexOf(t3) === -1) return i;
    }
    return -1;
  }

  function setSelectVal(el, val) {
    if (!val || el.disabled) return false;
    var idx = findOptionIndex(el, val);
    if (idx === -1) return false;
    try {
      var desc = Object.getOwnPropertyDescriptor(window.HTMLSelectElement.prototype, 'value');
      if (desc && desc.set) desc.set.call(el, el.options[idx].value);
      el.selectedIndex = idx;
      el.dispatchEvent(new Event('input',  { bubbles: true }));
      el.dispatchEvent(new Event('change', { bubbles: true }));
      el.dispatchEvent(new Event('blur',   { bubbles: true }));
      return true;
    } catch (e) { return false; }
  }

  // ── Cascading dropdown (District → Upazila AJAX দিয়ে populate হয়) ──────────
  function waitAndFillDependentSelect(el, val, triesLeft) {
    if (!el || !val) return;
    function attempt(left) {
      if (el.options.length > 1 && setSelectVal(el, val)) return;
      if (left <= 0) return;
      setTimeout(function () { attempt(left - 1); }, 250);
    }
    attempt(triesLeft);
  }

  // ── ID-type dropdown (National ID / Birth Registration / Passport — Yes/No স্টাইল) ─
  function idTypeCandidates(identifiers) {
    var joined = identifiers.join(' ').toLowerCase();
    if (/national.*id|^nid|জাতীয়/.test(joined)) {
      return nid ? ['yes', 'national', 'national id', 'হ্যাঁ', 'আছে'] : ['no', 'না', 'নাই'];
    }
    if (/birth.*reg|জন্ম.*নিবন্ধন/.test(joined)) {
      return birthCertNo ? ['yes', 'birth', 'birth registration', 'হ্যাঁ', 'আছে'] : ['no', 'না', 'নাই'];
    }
    if (/passport/.test(joined)) {
      return passport ? ['yes', 'passport', 'হ্যাঁ', 'আছে'] : ['no', 'না', 'নাই'];
    }
    return null;
  }
  function tryCandidates(el, candidates) {
    for (var i = 0; i < candidates.length; i++) if (setSelectVal(el, candidates[i])) return true;
    return false;
  }

  // ── মূল লুপ: input + textarea + select + date সব একসাথে ─────────────────────
  var fields = document.querySelectorAll(
    'input[type="text"], input[type="email"], input[type="tel"], input[type="date"], ' +
    'input[type="number"], input:not([type]), textarea, select'
  );

  var filled = 0;
  var deferredUpazila = [];

  fields.forEach(function (el) {
    var tag = el.tagName.toLowerCase();
    var ids = getIdentifiers(el);

    if (tag === 'select') {
      var isUpazila = /upazila|উপজেলা|thana|থানা|p\.?\s*s\.?$/i.test(ids.join(' '));
      var val = matchRule(ids);

      if (isUpazila && val) { deferredUpazila.push({ el: el, val: val }); return; }
      if (val && setSelectVal(el, val)) { filled++; return; }

      // নাম-ভিত্তিক rule কাজ না করলে — context (কোন সেকশনে আছে) দেখে resolve করি
      var ctxKey = matchContextKey(ids);
      if (ctxKey) {
        var ctx = getSectionContext(el);
        var ctxVal = ctx ? contextValue(ctx, ctxKey) : null;
        if (ctxVal && setSelectVal(el, ctxVal)) { filled++; return; }
        // Result field-এ exact GPA number না মিললে categorical "GPA" option try করি
        if (ctxKey === 'result' && ctxVal && setSelectVal(el, 'GPA')) { filled++; return; }
      }

      var idCandidates = idTypeCandidates(ids);
      if (idCandidates && tryCandidates(el, idCandidates)) filled++;
      return;
    }

    // input[text/date-like] / textarea
    var textVal = matchRule(ids);
    if (textVal) { if (setVal(el, textVal)) filled++; return; }

    var ctxKey2 = matchContextKey(ids);
    if (ctxKey2) {
      var ctx2 = getSectionContext(el);
      var ctxVal2 = ctx2 ? contextValue(ctx2, ctxKey2) : null;
      if (ctxVal2 && setVal(el, ctxVal2)) filled++;
    }
  });

  deferredUpazila.forEach(function (item) {
    waitAndFillDependentSelect(item.el, item.val, 20);
  });

  return "✅ " + filled + " টি field পূরণ হয়েছে! (Upazila ড্রপডাউন কিছুক্ষণ পর auto-আপডেট হবে)";
})();
""".trimIndent()
    }
}

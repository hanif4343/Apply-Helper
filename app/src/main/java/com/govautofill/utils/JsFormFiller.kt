package com.govautofill.utils

import com.govautofill.model.UserProfile

object JsFormFiller {

    fun buildScript(profile: UserProfile): String {
        val p = profile

        // সরাসরি JS এ value embed করো
        fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

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

  // ── সঠিক keyword mapping (exact match priority) ───────────────────────────
  // key = regex pattern to match against field name/id/placeholder/label
  // value = data to fill (works for both <input> and <select>)
  var rules = [
    // Bengali name fields — bn আগে চেক করতে হবে
    { pat: /applicant.*name.*bn|applicantnameben|namebn|namebengali|আবেদনকারী.*নাম|বাংলা.*নাম/i, val: fullNameBn },
    { pat: /father.*name.*bn|fathernameben|piternamebn|পিতার.*নাম|father.*bangla/i,              val: fatherBn },
    { pat: /mother.*name.*bn|mothernameben|maternamebn|মাতার.*নাম|mother.*bangla/i,              val: motherBn },
    { pat: /spouse.*bn|husband.*bn|wife.*bn|স্বামী|স্ত্রী/i,                                   val: spouseBn },

    // English name fields
    { pat: /applicant.*name(?!.*bn)|applicantnameen|applicantname$|^name$/i,                     val: fullNameEn },
    { pat: /father.*name(?!.*bn)|fathernameen|father_name$/i,                                    val: fatherEn },
    { pat: /mother.*name(?!.*bn)|mothernameen|mother_name$/i,                                    val: motherEn },
    { pat: /spouse.*en|spouse.*name$|husband.*name$|wife.*name$/i,                               val: spouseEn },

    // Personal info
    { pat: /date.*birth|birth.*date|dob|জন্ম.*তারিখ/i,                                          val: dob },
    { pat: /national.*id(?!.*type)|nid(?!.*card)(?!.*type)|জাতীয়.*পরিচয়/i,                     val: nid },
    { pat: /birth.*reg(?!.*type)|জন্ম.*নিবন্ধন/i,                                                val: birthCertNo },
    { pat: /passport(?!.*type)/i,                                                                 val: passport },
    { pat: /mobile|phone|cell|মোবাইল/i,                                                          val: mobile },
    { pat: /email/i,                                                                              val: email },
    { pat: /religion|ধর্ম/i,                                                                     val: religion },
    // gender — আগে missing ছিল, এটাই dropdown না-ফিল-হওয়ার একটা কারণ
    { pat: /gender|sex|লিঙ্গ/i,                                                                  val: gender },
    { pat: /blood/i,                                                                              val: blood },
    { pat: /nationality|জাতীয়তা/i,                                                               val: nationality },
    { pat: /marital|বৈবাহিক/i,                                                                   val: marital },
    { pat: /quota|কোটা/i,                                                                        val: quota },

    // Present address
    { pat: /present.*village|village.*present|p.*vill/i,    val: pVillage },
    { pat: /present.*post.*office|p.*post.*office/i,        val: pPostOffice },
    { pat: /present.*upazila|p.*upazila/i,                  val: pUpazila },
    { pat: /present.*district|p.*district|p.*zila/i,        val: pDistrict },
    { pat: /present.*division|p.*division/i,                val: pDivision },
    { pat: /present.*post.*code|p.*post.*code|present.*zip/i, val: pPostCode },

    // Permanent address
    { pat: /permanent.*village|village.*permanent|s.*vill/i, val: sVillage },
    { pat: /permanent.*post.*office|s.*post.*office/i,       val: sPostOffice },
    { pat: /permanent.*upazila|s.*upazila/i,                 val: sUpazila },
    { pat: /permanent.*district|s.*district|s.*zila/i,       val: sDistrict },
    { pat: /permanent.*division|s.*division/i,               val: sDivision },
    { pat: /permanent.*post.*code|s.*post.*code|permanent.*zip/i, val: sPostCode },

    // SSC
    { pat: /ssc.*board|ssc.*bord/i,       val: sscBoard },
    { pat: /ssc.*roll/i,                  val: sscRoll },
    { pat: /ssc.*reg/i,                   val: sscReg },
    { pat: /ssc.*year|ssc.*pass/i,        val: sscYear },
    { pat: /ssc.*gpa|ssc.*result|ssc.*grade/i, val: sscGpa },
    { pat: /ssc.*group|ssc.*subject/i,    val: sscGroup },

    // HSC
    { pat: /hsc.*board/i,                 val: hscBoard },
    { pat: /hsc.*roll/i,                  val: hscRoll },
    { pat: /hsc.*reg/i,                   val: hscReg },
    { pat: /hsc.*year|hsc.*pass/i,        val: hscYear },
    { pat: /hsc.*gpa|hsc.*result/i,       val: hscGpa },
    { pat: /hsc.*group|hsc.*subject/i,    val: hscGroup },

    // Graduation
    { pat: /degree|graduation.*degree/i,  val: gradDegree },
    { pat: /grad.*subject|graduation.*subject/i, val: gradSubject },
    { pat: /university|institution|college/i, val: gradInst },
    { pat: /grad.*year|graduation.*year|passing.*year/i, val: gradYear },
    { pat: /cgpa|grad.*result|graduation.*result/i, val: gradResult }
  ];

  // ── Bangla ↔ English synonym groups (select option matching-এর জন্য) ──────
  // (key-based map এর বদলে group-based, যাতে দুই দিক থেকেই (bn->en, en->bn) মিল খুঁজে পায়)
  var SYN_GROUPS = [
    ['male', 'm', 'পুরুষ'],
    ['female', 'f', 'মহিলা', 'নারী'],
    ['islam', 'muslim', 'ইসলাম'],
    ['hindu', 'হিন্দু'],
    ['christian', 'খ্রিস্টান'],
    ['buddhist', 'বৌদ্ধ'],
    ['married', 'বিবাহিত'],
    ['unmarried', 'single', 'অবিবাহিত'],
    ['bangladeshi', 'বাংলাদেশী', 'bangladesh'],
    ['none', 'no', 'না', 'নাই', 'n/a']
  ];
  // flatten — substring-matching ধাপে এই short words গুলোর জন্য বিশেষ সতর্কতা লাগবে
  // (যেমন "female" স্ট্রিং-এর ভেতরেই আক্ষরিকভাবে "male" আছে — ওইটা false-positive ধরবে না)
  var SHORT_SYN_WORDS = [].concat.apply([], SYN_GROUPS);

  function inSameSynGroup(a, b) {
    for (var g = 0; g < SYN_GROUPS.length; g++) {
      var grp = SYN_GROUPS[g];
      if (grp.indexOf(a) !== -1 && grp.indexOf(b) !== -1) return true;
    }
    return false;
  }

  // ── Identifiers collector (নাম/আইডি/placeholder/label + আশেপাশের visible টেক্সট) ──
  function getIdentifiers(el) {
    var ids = [
      el.getAttribute('name') || '',
      el.getAttribute('id') || '',
      el.getAttribute('placeholder') || '',
      el.getAttribute('aria-label') || ''
    ];
    if (el.id) {
      var lbl = document.querySelector('label[for="' + el.id + '"]');
      if (lbl) ids.push(lbl.innerText || lbl.textContent || '');
    }
    var parentLabel = el.closest('label');
    if (parentLabel) ids.push(parentLabel.innerText || '');

    // Bootstrap/Card layout-এ label আর input আলাদা div/column হিসেবে থাকে,
    // <label for=""> না থাকলেও কাছাকাছি টেক্সট ধরার fallback
    var prevSib = el.previousElementSibling;
    if (prevSib && prevSib.tagName !== 'SELECT' && prevSib.tagName !== 'INPUT') {
      ids.push(prevSib.innerText || prevSib.textContent || '');
    }
    if (el.parentElement) {
      var parentPrev = el.parentElement.previousElementSibling;
      if (parentPrev) ids.push((parentPrev.innerText || parentPrev.textContent || '').slice(0, 60));
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

  // ── Text input / textarea fill helper (অপরিবর্তিত) ────────────────────────
  function setVal(el, val) {
    if (!val || el.readOnly || el.disabled) return false;
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

  // ── SELECT/dropdown matching — multi-stage ─────────────────────────────────
  function findOptionIndex(selectEl, val) {
    if (!val) return -1;
    var target = val.toString().trim().toLowerCase();
    if (!target) return -1;
    var opts = selectEl.options;
    var i;

    // 1) exact match — option text বা value এর সাথে
    for (i = 0; i < opts.length; i++) {
      var t = (opts[i].text || '').trim().toLowerCase();
      var v = (opts[i].value || '').trim().toLowerCase();
      if (t === target || v === target) return i;
    }

    // 2) synonym match (বাংলা ⇄ ইংরেজি, group-based)
    for (i = 0; i < opts.length; i++) {
      var t2 = (opts[i].text || '').trim().toLowerCase();
      if (t2 && inSameSynGroup(target, t2)) return i;
    }

    // 3) contains match — placeholder ("Select") বাদ দিয়ে, short-word collision guarded
    //    (যেমন "Female" এর ভেতরে আক্ষরিকভাবে "male" সাবস্ট্রিং আছে — তাই reverse-direction
    //    ম্যাচ করার সময় t3 যদি কোনো পরিচিত ছোট synonym word হয়, সেটা স্কিপ করি)
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
  // District সিলেক্ট করার পর Upazila-র option list load হতে সময় লাগে,
  // তাই option আসা পর্যন্ত poll করে তারপর value সেট করি (best-effort, max ~5s)
  function waitAndFillDependentSelect(el, val, triesLeft) {
    if (!el || !val) return;
    function attempt(left) {
      if (el.options.length > 1 && setSelectVal(el, val)) return;
      if (left <= 0) return;
      setTimeout(function () { attempt(left - 1); }, 250);
    }
    attempt(triesLeft);
  }

  // ── ID-type dropdown (National ID / Birth Registration / Passport) ────────
  // অনেক govt form-এ এগুলো নিজেই "Select" dropdown — exact data না, বরং
  // "আছে/নাই" বা ডকুমেন্টের নাম সিলেক্ট করতে হয়। Best-effort guess:
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
    for (var i = 0; i < candidates.length; i++) {
      if (setSelectVal(el, candidates[i])) return true;
    }
    return false;
  }

  // ── মূল লুপ: input + textarea + select সব একসাথে ───────────────────────────
  var fields = document.querySelectorAll(
    'input[type="text"], input[type="email"], input[type="tel"], ' +
    'input[type="number"], input:not([type]), textarea, select'
  );

  var filled = 0;
  var deferredUpazila = []; // {el, val} — District-এর পরে fill হবে

  fields.forEach(function (el) {
    var tag = el.tagName.toLowerCase();
    var ids = getIdentifiers(el);

    if (tag === 'select') {
      var isUpazila = /upazila|উপজেলা|thana|থানা|p\.?\s*s\.?$/i.test(ids.join(' '));
      var val = matchRule(ids);

      if (isUpazila && val) {
        deferredUpazila.push({ el: el, val: val }); // District আগে set হোক, তারপর এটা
        return;
      }
      if (val) {
        if (setSelectVal(el, val)) { filled++; return; }
      }
      // generic match কাজ না করলে, ID-type dropdown best-effort
      var idCandidates = idTypeCandidates(ids);
      if (idCandidates && tryCandidates(el, idCandidates)) filled++;
      return;
    }

    // input[text-like] / textarea
    var textVal = matchRule(ids);
    if (textVal && setVal(el, textVal)) filled++;
  });

  // District সেট হওয়ার পর Upazila-র option populate হতে সময় লাগতে পারে (AJAX) —
  // তাই poll করে wait করি, page reload না হলে এটা silently background-এ কাজ করবে
  deferredUpazila.forEach(function (item) {
    waitAndFillDependentSelect(item.el, item.val, 20);
  });

  return "✅ " + filled + " টি field পূরণ হয়েছে! (Upazila ড্রপডাউন কিছুক্ষণ পর auto-আপডেট হবে)";
})();
""".trimIndent()
    }
}

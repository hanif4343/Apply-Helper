package com.govautofill.utils

import com.govautofill.model.UserProfile

object JsFormFiller {

    fun buildScript(profile: UserProfile): String {
        val p = profile
        fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

        // NID/Birth Reg/Passport status strings
        val nidStatus = if (p.nidNo.isNotBlank()) "Yes" else "No"
        val bregStatus = if (p.birthCertificateNo.isNotBlank()) "Yes" else "No"
        val passportStatus = if (p.passportNo.isNotBlank()) "Yes" else "No"

        // Care of fallback
        val careOfVal = if (p.careOf.isNotBlank()) p.careOf else p.fatherNameEn.ifBlank { p.fatherNameBn }
        val permCareOfVal = if (p.permanentCareOf.isNotBlank()) p.permanentCareOf else careOfVal

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
  var permCareOf   = "${esc(permCareOfVal)}";
  var dob          = "${esc(p.dateOfBirth)}";
  var nid          = "${esc(p.nidNo)}";
  var nidStatus    = "${esc(nidStatus)}";
  var birthCertNo  = "${esc(p.birthCertificateNo)}";
  var bregStatus   = "${esc(bregStatus)}";
  var passport     = "${esc(p.passportNo)}";
  var passportStatus = "${esc(passportStatus)}";
  var mobile       = "${esc(p.mobileNo)}";
  var email        = "${esc(p.email)}";
  var religion     = "${esc(p.religion)}";
  var gender       = "${esc(p.gender)}";
  var blood        = "${esc(p.bloodGroup)}";
  var nationality  = "${esc(p.nationality)}";
  var marital      = "${esc(p.maritalStatus)}";
  var quota        = "${esc(p.quota)}";
  var depStatus    = "${esc(p.departmentalStatus)}";

  var pCareOf      = "${esc(careOfVal)}";
  var pVillage     = "${esc(p.presentVillage)}";
  var pPostOffice  = "${esc(p.presentPostOffice)}";
  var pUpazila     = "${esc(p.presentUpazila)}";
  var pDistrict    = "${esc(p.presentDistrict)}";
  var pDivision    = "${esc(p.presentDivision)}";
  var pPostCode    = "${esc(p.presentPostCode)}";

  var sCareOf      = "${esc(permCareOfVal)}";
  var sVillage     = "${esc(p.permanentVillage)}";
  var sPostOffice  = "${esc(p.permanentPostOffice)}";
  var sUpazila     = "${esc(p.permanentUpazila)}";
  var sDistrict    = "${esc(p.permanentDistrict)}";
  var sDivision    = "${esc(p.permanentDivision)}";
  var sPostCode    = "${esc(p.permanentPostCode)}";

  var jscExam      = "${esc(p.jscExam)}";
  var jscBoard     = "${esc(p.jscBoard)}";
  var jscRoll      = "${esc(p.jscRollNo)}";
  var jscGpa       = "${esc(p.jscGpa)}";
  var jscResultType = "${esc(p.jscResultType)}";
  var jscYear      = "${esc(p.jscYear)}";

  var sscExam      = "${esc(p.sscExam)}";
  var sscBoard     = "${esc(p.sscBoard)}";
  var sscRoll      = "${esc(p.sscRollNo)}";
  var sscReg       = "${esc(p.sscRegNo)}";
  var sscYear      = "${esc(p.sscYear)}";
  var sscGpa       = "${esc(p.sscGpa)}";
  var sscGroup     = "${esc(p.sscGroup)}";
  var sscResultType = "${esc(p.sscResultType)}";

  var hscExam      = "${esc(p.hscExam)}";
  var hscBoard     = "${esc(p.hscBoard)}";
  var hscRoll      = "${esc(p.hscRollNo)}";
  var hscReg       = "${esc(p.hscRegNo)}";
  var hscYear      = "${esc(p.hscYear)}";
  var hscGpa       = "${esc(p.hscGpa)}";
  var hscGroup     = "${esc(p.hscGroup)}";
  var hscResultType = "${esc(p.hscResultType)}";

  var gradDegree   = "${esc(p.graduationDegree)}";
  var gradSubject  = "${esc(p.graduationSubject)}";
  var gradInst     = "${esc(p.graduationInstitution)}";
  var gradYear     = "${esc(p.graduationYear)}";
  var gradResult   = "${esc(p.graduationResult)}";
  var gradResultType = "${esc(p.graduationResultType)}";
  var gradDuration = "${esc(p.graduationDuration)}";

  // ── নাম/আইডি-ভিত্তিক rules ──
  var rules = [
    // Names — Bengali
    { pat: /applicant.*name.*bn|applicantnameben|namebn|namebengali|আবেদনকারী.*নাম|বাংলা.*নাম/i, val: fullNameBn },
    { pat: /father.*name.*bn|fathernameben|piternamebn|পিতার.*নাম|father.*bangla/i,              val: fatherBn },
    { pat: /mother.*name.*bn|mothernameben|maternamebn|মাতার.*নাম|mother.*bangla/i,              val: motherBn },
    { pat: /spouse.*bn|husband.*bn|wife.*bn|স্বামী|স্ত্রী/i,                                   val: spouseBn },
    // Names — English
    { pat: /applicant.*name(?!.*bn)|applicantnameen|applicantname$|^name$/i,                     val: fullNameEn },
    { pat: /father.*name(?!.*bn)|fathernameen|father_name$/i,                                    val: fatherEn },
    { pat: /mother.*name(?!.*bn)|mothernameen|mother_name$/i,                                    val: motherEn },
    { pat: /spouse.*en|spouse.*name$|husband.*name$|wife.*name$/i,                               val: spouseEn },
    // DOB
    { pat: /date.*birth|birth.*date|^dob$|জন্ম.*তারিখ/i,                                        val: dob },
    // IDs — number fields (value boxes, not the Yes/No dropdown)
    { pat: /national.*id.*number|nid.*number|nid.*no\b|জাতীয়.*পরিচয়.*নং/i,                    val: nid },
    { pat: /birth.*reg.*number|birth.*cert.*number|birth.*reg.*no\b|জন্ম.*নিবন্ধন.*নং/i,       val: birthCertNo },
    { pat: /passport.*number|passport.*no\b/i,                                                    val: passport },
    // Contact
    { pat: /confirm.*mobile|confirm.*phone|mobile.*confirm/i,                                    val: mobile },
    { pat: /mobile|phone|cell|মোবাইল/i,                                                          val: mobile },
    { pat: /email/i,                                                                              val: email },
    // Identity dropdowns
    { pat: /^nationality$|nationality/i,                                                         val: nationality },
    { pat: /^religion$|religion/i,                                                               val: religion },
    { pat: /^gender$|gender|sex\b/i,                                                             val: gender },
    { pat: /^blood/i,                                                                            val: blood },
    { pat: /^marital|marital.*status/i,                                                         val: marital },
    { pat: /^quota$|quota/i,                                                                     val: quota },
    { pat: /dep.*status|departmental.*status|dep_status/i,                                       val: depStatus },

    // District/Upazila — prefixed (website uses present_district / permanent_district as id)
    { pat: /present.*upazila|present_upazila/i,                       val: pUpazila },
    { pat: /present.*district|present_district/i,                     val: pDistrict },
    { pat: /present.*division|p.*division/i,                          val: pDivision },
    { pat: /present.*post.*code|present.*zip/i,                       val: pPostCode },
    { pat: /permanent.*upazila|permanent_upazila/i,                   val: sUpazila },
    { pat: /permanent.*district|permanent_district/i,                 val: sDistrict },
    { pat: /permanent.*division|s.*division/i,                        val: sDivision },
    { pat: /permanent.*post.*code|permanent.*zip/i,                   val: sPostCode },

    // JSC
    { pat: /jsc.*board|jdc.*board/i,        val: jscBoard },
    { pat: /jsc.*roll|jdc.*roll/i,          val: jscRoll },
    { pat: /jsc.*year|jdc.*year/i,          val: jscYear },
    { pat: /jsc.*gpa|jsc.*result/i,         val: jscGpa },

    // SSC
    { pat: /ssc.*board/i,                   val: sscBoard },
    { pat: /ssc.*roll/i,                    val: sscRoll },
    { pat: /ssc.*reg/i,                     val: sscReg },
    { pat: /ssc.*year|ssc.*pass/i,          val: sscYear },
    { pat: /ssc.*gpa|ssc.*result|ssc.*grade/i, val: sscGpa },

    // HSC
    { pat: /hsc.*board/i,                   val: hscBoard },
    { pat: /hsc.*roll/i,                    val: hscRoll },
    { pat: /hsc.*reg/i,                     val: hscReg },
    { pat: /hsc.*year|hsc.*pass/i,          val: hscYear },
    { pat: /hsc.*gpa|hsc.*result/i,         val: hscGpa },

    // Graduation
    { pat: /gra.*year|grad.*year|graduation.*year/i,   val: gradYear },
    { pat: /cgpa|grad.*result|graduation.*result/i,    val: gradResult },
    { pat: /gra.*institute|university|institution/i,   val: gradInst },
    { pat: /gra.*subject|subject.*degree|grad.*subject/i, val: gradSubject },
    { pat: /gra.*exam|grad.*exam|graduation.*exam/i,   val: gradDegree }
  ];

  // ── Synonym groups ────────────────────────────────────────────────────────
  var SYN_GROUPS = [
    ['male', 'm', 'পুরুষ'],
    ['female', 'f', 'মহিলা', 'নারী'],
    ['islam', 'muslim', 'ইসলাম'],
    ['hindu', 'hinduism', 'হিন্দু'],
    ['christian', 'christianity', 'খ্রিস্টান'],
    ['buddhist', 'buddhism', 'বৌদ্ধ'],
    ['married', 'বিবাহিত'],
    ['single', 'unmarried', 'অবিবাহিত'],
    ['bangladeshi', 'বাংলাদেশী', 'bangladesh'],
    ['not applicable', 'none', 'no', 'না', 'নাই', 'n/a'],
    ['yes', 'হ্যাঁ', 'আছে'],
    ['s.s.c', 'ssc', 'secondary'],
    ['h.s.c', 'hsc', 'higher secondary'],
    ['honors', 'honour', 'b.a.', 'b.sc.', 'b.com.'],
    ['gpa(out of 5)', 'gpa (out of 5)', 'gpa out of 5'],
    ['gpa(out of 4)', 'gpa (out of 4)', 'gpa out of 4'],
    ['cgpa(out of 4)', 'cgpa (out of 4)', 'cgpa out of 4'],
    ['1st class', 'first class'],
    ['2nd class', 'second class']
  ];
  var SHORT_SYN_WORDS = [].concat.apply([], SYN_GROUPS);

  function inSameSynGroup(a, b) {
    for (var g = 0; g < SYN_GROUPS.length; g++) {
      var grp = SYN_GROUPS[g];
      if (grp.indexOf(a) !== -1 && grp.indexOf(b) !== -1) return true;
    }
    return false;
  }

  // ── Section-context detection ─────────────────────────────────────────────
  var SECTION_PATTERNS = [
    { tag: 'permanent',  re: /permanent[\s\S]{0,20}address|স্থায়ী[\s\S]{0,10}ঠিকানা/i },
    { tag: 'present',    re: /present[\s\S]{0,20}address|বর্তমান[\s\S]{0,10}ঠিকানা/i },
    { tag: 'jsc',        re: /j\.?\s*s\.?\s*c|jdc|junior|class.*eight/i },
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
      if (!txt || txt.length > 80) continue;
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
      var rel = headers[i].el.compareDocumentPosition(el);
      if (rel & Node.DOCUMENT_POSITION_FOLLOWING) best = headers[i].tag;
    }
    return best;
  }

  // Context keyword rules — generic labels that need section context
  var CONTEXT_KEYWORD_RULES = [
    { pat: /village|road|house|flat|town|গ্রাম|মহল্লা/i,       key: 'village' },
    { pat: /post.*office|পোস্ট.*অফিস/i,                          key: 'postOffice' },
    { pat: /care.*of/i,                                           key: 'careOf' },
    { pat: /^examination$|^exam$/i,                               key: 'examination' },
    { pat: /^result$|result\s*\/?(\s*grade|type)?$/i,            key: 'result' },
    { pat: /group.*subject|subject.*group/i,                     key: 'groupSubject' },
    { pat: /^board$/i,                                           key: 'board' },
    { pat: /passing.*year|^year$/i,                              key: 'year' },
    { pat: /roll.*no|^roll$/i,                                   key: 'roll' },
    { pat: /course.*duration|duration/i,                         key: 'duration' }
  ];

  function contextValue(context, key) {
    if (context === 'present') {
      if (key === 'village') return pVillage;
      if (key === 'postOffice') return pPostOffice;
      if (key === 'careOf') return pCareOf;
    }
    if (context === 'permanent') {
      if (key === 'village') return sVillage;
      if (key === 'postOffice') return sPostOffice;
      if (key === 'careOf') return sCareOf;
    }
    if (context === 'jsc') {
      if (key === 'examination') return jscExam;
      if (key === 'board') return jscBoard;
      if (key === 'roll') return jscRoll;
      if (key === 'result') return jscGpa;
      if (key === 'year') return jscYear;
    }
    if (context === 'ssc') {
      if (key === 'examination') return sscExam;
      if (key === 'board') return sscBoard;
      if (key === 'roll') return sscRoll;
      if (key === 'result') return sscGpa;
      if (key === 'groupSubject') return sscGroup;
      if (key === 'year') return sscYear;
    }
    if (context === 'hsc') {
      if (key === 'examination') return hscExam;
      if (key === 'board') return hscBoard;
      if (key === 'roll') return hscRoll;
      if (key === 'result') return hscGpa;
      if (key === 'groupSubject') return hscGroup;
      if (key === 'year') return hscYear;
    }
    if (context === 'graduation') {
      if (key === 'examination') return gradDegree;
      if (key === 'result') return gradResult;
      if (key === 'groupSubject') return gradSubject;
      if (key === 'year') return gradYear;
      if (key === 'duration') return gradDuration;
    }
    return null;
  }

  // ── Identifiers collector ─────────────────────────────────────────────────
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
    var node = el;
    for (var depth = 0; depth < 4 && node; depth++) {
      var prevSib = node.previousElementSibling;
      if (prevSib && prevSib.tagName !== 'SELECT' && prevSib.tagName !== 'INPUT' && prevSib.tagName !== 'TEXTAREA') {
        var t = (prevSib.innerText || prevSib.textContent || '').trim();
        if (t && t.length < 80) ids.push(t);
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

  // ── Date format convert ───────────────────────────────────────────────────
  function pad2(n) { n = n.toString(); return n.length < 2 ? '0' + n : n; }
  function toIsoDate(ddmmyyyy) {
    var m = ddmmyyyy.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/);
    if (!m) return null;
    return m[3] + '-' + pad2(m[1]) + '-' + pad2(m[2]);
  }

  // ── Text input fill ───────────────────────────────────────────────────────
  function setVal(el, val) {
    if (!val || el.readOnly || el.disabled) return false;
    if (el.tagName === 'INPUT' && el.type === 'date') {
      var iso = toIsoDate(val);
      if (iso) val = iso; else return false;
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

  // ── SELECT matching ───────────────────────────────────────────────────────
  function findOptionIndex(selectEl, val) {
    if (!val) return -1;
    var target = val.toString().trim().toLowerCase();
    if (!target) return -1;
    var opts = selectEl.options;
    var i;
    // Exact match
    for (i = 0; i < opts.length; i++) {
      var t = (opts[i].text || '').trim().toLowerCase();
      var v = (opts[i].value || '').trim().toLowerCase();
      if (t === target || v === target) return i;
    }
    // Synonym group
    for (i = 0; i < opts.length; i++) {
      var t2 = (opts[i].text || '').trim().toLowerCase();
      if (t2 && inSameSynGroup(target, t2)) return i;
    }
    // Partial
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

  // ── Cascading dropdown (District → Upazila AJAX) ──────────────────────────
  function waitAndFillDependentSelect(el, val, triesLeft) {
    if (!el || !val) return;
    function attempt(left) {
      if (el.options.length > 1 && setSelectVal(el, val)) return;
      if (left <= 0) return;
      setTimeout(function () { attempt(left - 1); }, 300);
    }
    attempt(triesLeft);
  }

  // ── ID-type Yes/No dropdown ───────────────────────────────────────────────
  function idTypeCandidates(identifiers) {
    var joined = identifiers.join(' ').toLowerCase();
    // These match the website's dropdown IDs: nid, breg, passport
    if (/\bnid\b|national.*id(?!.*number)/.test(joined)) {
      return nid ? ['yes', 'হ্যাঁ', 'আছে'] : ['no', 'না', 'নাই'];
    }
    if (/\bbreg\b|birth.*reg(?!.*number)/.test(joined)) {
      return birthCertNo ? ['yes', 'হ্যাঁ', 'আছে'] : ['no', 'না', 'নাই'];
    }
    if (/\bpassport\b(?!.*number)/.test(joined)) {
      return passport ? ['yes', 'হ্যাঁ', 'আছে'] : ['no', 'না', 'নাই'];
    }
    return null;
  }

  function tryCandidates(el, candidates) {
    for (var i = 0; i < candidates.length; i++) if (setSelectVal(el, candidates[i])) return true;
    return false;
  }

  // ── Result-type dropdown helper ───────────────────────────────────────────
  // Website has separate Result-type dropdown (GPA out of 5 / 1st Division etc.)
  // and then a GPA value input. We try to fill the type dropdown first.
  function tryResultTypeDropdown(el, context) {
    var rt = '';
    if (context === 'jsc') rt = jscResultType;
    else if (context === 'ssc') rt = sscResultType;
    else if (context === 'hsc') rt = hscResultType;
    else if (context === 'graduation') rt = gradResultType;
    if (rt && setSelectVal(el, rt)) return true;
    // Fallback: if GPA value exists, select "GPA(out of 5)"
    var gpaVal = '';
    if (context === 'ssc') gpaVal = sscGpa;
    else if (context === 'hsc') gpaVal = hscGpa;
    else if (context === 'jsc') gpaVal = jscGpa;
    else if (context === 'graduation') gpaVal = gradResult;
    if (gpaVal) {
      return setSelectVal(el, 'GPA(out of 5)') || setSelectVal(el, 'GPA(out of 4)') ||
             setSelectVal(el, 'CGPA(out of 4)') || setSelectVal(el, 'CGPA(out of 5)');
    }
    return false;
  }

  // ── Main loop ─────────────────────────────────────────────────────────────
  var fields = document.querySelectorAll(
    'input[type="text"], input[type="email"], input[type="tel"], input[type="date"], ' +
    'input[type="number"], input:not([type]), textarea, select'
  );

  var filled = 0;
  var deferredUpazila = [];

  fields.forEach(function (el) {
    var tag = el.tagName.toLowerCase();
    var ids = getIdentifiers(el);
    var joinedIds = ids.join(' ').toLowerCase();

    if (tag === 'select') {
      var isUpazila = /upazila|উপজেলা|thana|থানা|p\.?\s*s\.?$/i.test(joinedIds);

      // Named-rule match first
      var val = matchRule(ids);
      if (val) {
        if (isUpazila) { deferredUpazila.push({ el: el, val: val }); return; }
        if (setSelectVal(el, val)) { filled++; return; }
      }

      // Context-based
      var ctxKey = matchContextKey(ids);
      if (ctxKey) {
        var ctx = getSectionContext(el);

        // Result-type dropdown — special handling
        if (ctxKey === 'result' && ctx) {
          if (tryResultTypeDropdown(el, ctx)) { filled++; return; }
          var ctxVal2 = contextValue(ctx, ctxKey);
          if (ctxVal2 && setSelectVal(el, ctxVal2)) { filled++; return; }
          return;
        }

        var ctxVal = ctx ? contextValue(ctx, ctxKey) : null;
        if (ctxVal) {
          if (isUpazila) { deferredUpazila.push({ el: el, val: ctxVal }); return; }
          if (setSelectVal(el, ctxVal)) { filled++; return; }
        }
      }

      // ID-type Yes/No dropdown
      var idCandidates = idTypeCandidates(ids);
      if (idCandidates && tryCandidates(el, idCandidates)) { filled++; return; }

      // Departmental status — try direct
      if (/dep.*status|departmental/i.test(joinedIds)) {
        if (setSelectVal(el, depStatus)) { filled++; return; }
      }
      return;
    }

    // Text / textarea inputs
    var textVal = matchRule(ids);
    if (textVal) { if (setVal(el, textVal)) { filled++; return; } }

    var ctxKey2 = matchContextKey(ids);
    if (ctxKey2) {
      var ctx2 = getSectionContext(el);
      var ctxVal3 = ctx2 ? contextValue(ctx2, ctxKey2) : null;
      if (ctxVal3 && setVal(el, ctxVal3)) { filled++; return; }
    }
  });

  // Deferred upazila fills (wait for AJAX cascade)
  deferredUpazila.forEach(function (item) {
    waitAndFillDependentSelect(item.el, item.val, 24);
  });

  return "✅ " + filled + " টি field পূরণ হয়েছে! (Upazila ড্রপডাউন কিছুক্ষণ পর auto-আপডেট হবে)";
})();
""".trimIndent()
    }
}

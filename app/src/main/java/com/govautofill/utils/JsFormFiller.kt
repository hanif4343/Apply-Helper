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
  var fullNameEn  = "${esc(p.fullNameEn)}";
  var fullNameBn  = "${esc(p.fullNameBn)}";
  var fatherEn    = "${esc(p.fatherNameEn)}";
  var fatherBn    = "${esc(p.fatherNameBn)}";
  var motherEn    = "${esc(p.motherNameEn)}";
  var motherBn    = "${esc(p.motherNameBn)}";
  var spouseEn    = "${esc(p.spouseNameEn)}";
  var spouseBn    = "${esc(p.spouseNameBn)}";
  var dob         = "${esc(p.dateOfBirth)}";
  var nid         = "${esc(p.nidNo)}";
  var passport    = "${esc(p.passportNo)}";
  var mobile      = "${esc(p.mobileNo)}";
  var email       = "${esc(p.email)}";
  var religion    = "${esc(p.religion)}";
  var gender      = "${esc(p.gender)}";
  var blood       = "${esc(p.bloodGroup)}";
  var nationality = "${esc(p.nationality)}";
  var marital     = "${esc(p.maritalStatus)}";
  var quota       = "${esc(p.quota)}";
  var pVillage    = "${esc(p.presentVillage)}";
  var pPostOffice = "${esc(p.presentPostOffice)}";
  var pUpazila    = "${esc(p.presentUpazila)}";
  var pDistrict   = "${esc(p.presentDistrict)}";
  var pDivision   = "${esc(p.presentDivision)}";
  var pPostCode   = "${esc(p.presentPostCode)}";
  var sVillage    = "${esc(p.permanentVillage)}";
  var sPostOffice = "${esc(p.permanentPostOffice)}";
  var sUpazila    = "${esc(p.permanentUpazila)}";
  var sDistrict   = "${esc(p.permanentDistrict)}";
  var sDivision   = "${esc(p.permanentDivision)}";
  var sPostCode   = "${esc(p.permanentPostCode)}";
  var sscBoard    = "${esc(p.sscBoard)}";
  var sscRoll     = "${esc(p.sscRollNo)}";
  var sscReg      = "${esc(p.sscRegNo)}";
  var sscYear     = "${esc(p.sscYear)}";
  var sscGpa      = "${esc(p.sscGpa)}";
  var hscBoard    = "${esc(p.hscBoard)}";
  var hscRoll     = "${esc(p.hscRollNo)}";
  var hscReg      = "${esc(p.hscRegNo)}";
  var hscYear     = "${esc(p.hscYear)}";
  var hscGpa      = "${esc(p.hscGpa)}";
  var gradDegree  = "${esc(p.graduationDegree)}";
  var gradSubject = "${esc(p.graduationSubject)}";
  var gradInst    = "${esc(p.graduationInstitution)}";
  var gradYear    = "${esc(p.graduationYear)}";
  var gradResult  = "${esc(p.graduationResult)}";

  // ── সঠিক keyword mapping (exact match priority) ───────────────────────────
  // key = regex pattern to match against field name/id/placeholder/label
  // value = data to fill
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
    { pat: /national.*id|nid(?!.*card)|জাতীয়.*পরিচয়/i,                                        val: nid },
    { pat: /passport/i,                                                                           val: passport },
    { pat: /mobile|phone|cell|মোবাইল/i,                                                          val: mobile },
    { pat: /email/i,                                                                              val: email },
    { pat: /religion|ধর্ম/i,                                                                     val: religion },
    { pat: /blood/i,                                                                              val: blood },
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

    // HSC
    { pat: /hsc.*board/i,                 val: hscBoard },
    { pat: /hsc.*roll/i,                  val: hscRoll },
    { pat: /hsc.*reg/i,                   val: hscReg },
    { pat: /hsc.*year|hsc.*pass/i,        val: hscYear },
    { pat: /hsc.*gpa|hsc.*result/i,       val: hscGpa },

    // Graduation
    { pat: /degree|graduation.*degree/i,  val: gradDegree },
    { pat: /subject|grad.*subject/i,      val: gradSubject },
    { pat: /university|institution|college/i, val: gradInst },
    { pat: /grad.*year|graduation.*year|passing.*year/i, val: gradYear },
    { pat: /cgpa|grad.*result|graduation.*result/i, val: gradResult }
  ];

  // ── Input fill helper ─────────────────────────────────────────────────────
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
    } catch(e) { return false; }
  }

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
    // parent label
    var parent = el.closest('label');
    if (parent) ids.push(parent.innerText || '');
    return ids.filter(function(s){ return s.trim().length > 0; });
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

  // ── Fill all inputs ───────────────────────────────────────────────────────
  var inputs = document.querySelectorAll(
    'input[type="text"], input[type="email"], input[type="tel"], ' +
    'input[type="number"], input:not([type]), textarea'
  );
  var filled = 0;
  inputs.forEach(function(el) {
    var ids = getIdentifiers(el);
    var val = matchRule(ids);
    if (val && setVal(el, val)) filled++;
  });

  return "✅ " + filled + " টি field পূরণ হয়েছে!";
})();
""".trimIndent()
    }
}

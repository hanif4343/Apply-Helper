package com.govautofill.utils

import com.govautofill.model.UserProfile

object JsFormFiller {

    /**
     * Generates JavaScript that fills all detectable input fields on the page.
     * Uses multiple strategies: name, id, placeholder, label text matching.
     */
    fun buildScript(profile: UserProfile): String {
        val fields = buildFieldMap(profile)
        val jsonFields = fields.entries.joinToString(",\n") { (k, v) ->
            val escaped = v.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
            "  \"$k\": \"$escaped\""
        }

        return """
(function() {
  var profileData = {
$jsonFields
  };

  var keywordMap = {
    // Full name Bengali
    "fullname_bn": ["fullnameben", "fullnamebn", "namebn", "namebengali", "applicantnameben",
                    "পূর্ণনাম", "বাংলানাম", "full_name_bn", "full_name_bengali"],
    // Full name English
    "fullname_en": ["fullnameen", "fullnameenglish", "nameen", "applicantnameen",
                    "full_name_en", "fullname", "applicantname", "name"],
    // Father
    "father_bn":   ["fathername", "fathernameben", "fathernamebn", "pitarnam", "father_name_bn"],
    "father_en":   ["fathernameen", "fathernameenglish", "father_name_en", "father_name"],
    // Mother
    "mother_bn":   ["mothername", "mothernameben", "mothernamebn", "matanam", "mother_name_bn"],
    "mother_en":   ["mothernameen", "mothernameenglish", "mother_name_en", "mother_name"],
    // Spouse
    "spouse_bn":   ["spousename", "husbandname", "wifename", "spouse_name_bn"],
    "spouse_en":   ["spousenameen", "husband_name", "wife_name", "spouse_name_en"],
    // DOB
    "dob":         ["dob", "dateofbirth", "birthdate", "date_of_birth", "birth_date", "janmotarikh"],
    // NID
    "nid":         ["nid", "nationalid", "nidno", "nid_no", "national_id", "nidnumber"],
    // Passport
    "passport":    ["passport", "passportno", "passport_no", "passport_number"],
    // Mobile
    "mobile":      ["mobile", "phone", "cellphone", "mobileno", "mobile_no", "phonenumber", "contactno"],
    // Email
    "email":       ["email", "emailaddress", "email_address"],
    // Religion
    "religion":    ["religion", "dharm", "dhormo"],
    // Blood
    "blood":       ["bloodgroup", "blood_group", "blood"],
    // Gender
    "gender":      ["gender", "sex", "lingo"],
    // Nationality
    "nationality": ["nationality", "jatiyota"],
    // Marital
    "marital":     ["maritalstatus", "marital_status", "marital", "boibaik"],
    // Quota
    "quota":       ["quota", "kota"],
    // Present address
    "p_village":   ["presentvillage", "present_village", "pvillage", "vill_p", "village_p"],
    "p_postoffice":["presentpostoffice", "present_postoffice", "ppostoffice", "postoffice_p"],
    "p_upazila":   ["presentupazila", "present_upazila", "pupazila", "upazila_p"],
    "p_district":  ["presentdistrict", "present_district", "pdistrict", "district_p", "zila_p"],
    "p_division":  ["presentdivision", "present_division", "pdivision", "division_p"],
    "p_postcode":  ["presentpostcode", "present_postcode", "ppostcode", "postcode_p", "pzip"],
    // Permanent address
    "s_village":   ["permanentvillage", "permanent_village", "svillage", "vill_s", "village_s"],
    "s_postoffice":["permanentpostoffice", "permanent_postoffice", "spostoffice", "postoffice_s"],
    "s_upazila":   ["permanentupazila", "permanent_upazila", "supazila", "upazila_s"],
    "s_district":  ["permanentdistrict", "permanent_district", "sdistrict", "district_s", "zila_s"],
    "s_division":  ["permanentdivision", "permanent_division", "sdivision", "division_s"],
    "s_postcode":  ["permanentpostcode", "permanent_postcode", "spostcode", "postcode_s", "szip"],
    // SSC
    "ssc_board":   ["sscboard", "ssc_board", "sscbord"],
    "ssc_roll":    ["sscroll", "ssc_roll", "sscrollno", "ssc_roll_no"],
    "ssc_reg":     ["sscreg", "ssc_reg", "sscregno", "ssc_reg_no", "sscregistration"],
    "ssc_year":    ["sscyear", "ssc_year", "sscpassyear", "ssc_passing_year"],
    "ssc_gpa":     ["sscgpa", "ssc_gpa", "sscresult", "ssc_result", "sscgrade"],
    // HSC
    "hsc_board":   ["hscboard", "hsc_board"],
    "hsc_roll":    ["hscroll", "hsc_roll", "hscrollno"],
    "hsc_reg":     ["hscreg", "hsc_reg", "hscregno", "hscregistration"],
    "hsc_year":    ["hscyear", "hsc_year", "hscpassyear"],
    "hsc_gpa":     ["hscgpa", "hsc_gpa", "hscresult"],
    // Graduation
    "grad_degree": ["degree", "graddegree", "graduation_degree", "grad_degree"],
    "grad_subject":["subject", "gradsubject", "graduation_subject"],
    "grad_inst":   ["institution", "university", "college", "gradinstitution"],
    "grad_year":   ["gradyear", "grad_year", "graduationyear", "passingyear"],
    "grad_result": ["gradresult", "grad_result", "cgpa", "graduationresult"]
  };

  function normalize(str) {
    return (str || "").toLowerCase().replace(/[^a-z0-9\u0980-\u09ff]/g, "");
  }

  function getValueForField(identifier) {
    var norm = normalize(identifier);
    for (var key in keywordMap) {
      var keywords = keywordMap[key];
      for (var i = 0; i < keywords.length; i++) {
        if (norm.indexOf(normalize(keywords[i])) !== -1 || normalize(keywords[i]).indexOf(norm) !== -1) {
          return profileData[key] || null;
        }
      }
    }
    return null;
  }

  function fillInput(el) {
    var identifiers = [
      el.getAttribute("name"),
      el.getAttribute("id"),
      el.getAttribute("placeholder"),
      el.getAttribute("aria-label"),
      el.getAttribute("data-field")
    ];

    // Also try to find associated label
    if (el.id) {
      var label = document.querySelector('label[for="' + el.id + '"]');
      if (label) identifiers.push(label.textContent);
    }

    for (var i = 0; i < identifiers.length; i++) {
      if (!identifiers[i]) continue;
      var val = getValueForField(identifiers[i]);
      if (val) {
        // Set value
        var nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value');
        if (nativeInputValueSetter) {
          nativeInputValueSetter.set.call(el, val);
        } else {
          el.value = val;
        }
        // Trigger React/Vue/Angular events
        el.dispatchEvent(new Event('input', { bubbles: true }));
        el.dispatchEvent(new Event('change', { bubbles: true }));
        el.dispatchEvent(new Event('blur', { bubbles: true }));
        return true;
      }
    }
    return false;
  }

  // Fill all text inputs and textareas
  var inputs = document.querySelectorAll('input[type="text"], input[type="email"], input[type="tel"], input[type="number"], input:not([type]), textarea');
  var filled = 0;
  inputs.forEach(function(el) {
    if (!el.readOnly && !el.disabled && fillInput(el)) filled++;
  });

  return "✅ " + filled + " টি field পূরণ হয়েছে!";
})();
""".trimIndent()
    }

    private fun buildFieldMap(p: UserProfile): Map<String, String> = mapOf(
        "fullname_bn" to p.fullNameBn,
        "fullname_en" to p.fullNameEn,
        "father_bn"   to p.fatherNameBn,
        "father_en"   to p.fatherNameEn,
        "mother_bn"   to p.motherNameBn,
        "mother_en"   to p.motherNameEn,
        "spouse_bn"   to p.spouseNameBn,
        "spouse_en"   to p.spouseNameEn,
        "dob"         to p.dateOfBirth,
        "nid"         to p.nidNo,
        "passport"    to p.passportNo,
        "mobile"      to p.mobileNo,
        "email"       to p.email,
        "religion"    to p.religion,
        "blood"       to p.bloodGroup,
        "gender"      to p.gender,
        "nationality" to p.nationality,
        "marital"     to p.maritalStatus,
        "quota"       to p.quota,
        "p_village"   to p.presentVillage,
        "p_postoffice" to p.presentPostOffice,
        "p_upazila"   to p.presentUpazila,
        "p_district"  to p.presentDistrict,
        "p_division"  to p.presentDivision,
        "p_postcode"  to p.presentPostCode,
        "s_village"   to p.permanentVillage,
        "s_postoffice" to p.permanentPostOffice,
        "s_upazila"   to p.permanentUpazila,
        "s_district"  to p.permanentDistrict,
        "s_division"  to p.permanentDivision,
        "s_postcode"  to p.permanentPostCode,
        "ssc_board"   to p.sscBoard,
        "ssc_roll"    to p.sscRollNo,
        "ssc_reg"     to p.sscRegNo,
        "ssc_year"    to p.sscYear,
        "ssc_gpa"     to p.sscGpa,
        "hsc_board"   to p.hscBoard,
        "hsc_roll"    to p.hscRollNo,
        "hsc_reg"     to p.hscRegNo,
        "hsc_year"    to p.hscYear,
        "hsc_gpa"     to p.hscGpa,
        "grad_degree" to p.graduationDegree,
        "grad_subject" to p.graduationSubject,
        "grad_inst"   to p.graduationInstitution,
        "grad_year"   to p.graduationYear,
        "grad_result" to p.graduationResult
    ).filter { it.value.isNotEmpty() }
}

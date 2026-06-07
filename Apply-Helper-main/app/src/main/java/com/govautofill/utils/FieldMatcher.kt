package com.govautofill.utils

import com.govautofill.model.UserProfile

object FieldMatcher {

    /**
     * Given a field hint/label/id text, returns the matching profile value.
     * Handles both Bengali and English keywords found in BD govt sites.
     */
    fun matchField(fieldText: String, profile: UserProfile): String? {
        val t = fieldText.lowercase().trim()

        // Full name
        if (t.containsAny("পূর্ণ নাম", "নামের বাংলা", "full name (bn", "applicant name bn", "name in bengali")) return profile.fullNameBn
        if (t.containsAny("name in english", "full name (en", "applicant name en", "name_en", "fullname_en")) return profile.fullNameEn

        // Father
        if (t.containsAny("পিতার নাম", "father name bn", "father_name_bn")) return profile.fatherNameBn
        if (t.containsAny("father name en", "father_name_en", "father name (english")) return profile.fatherNameEn

        // Mother
        if (t.containsAny("মাতার নাম", "mother name bn", "mother_name_bn")) return profile.motherNameBn
        if (t.containsAny("mother name en", "mother_name_en")) return profile.motherNameEn

        // Spouse
        if (t.containsAny("স্বামীর নাম", "স্ত্রীর নাম", "spouse name bn")) return profile.spouseNameBn
        if (t.containsAny("spouse name en", "husband name", "wife name")) return profile.spouseNameEn

        // NID
        if (t.containsAny("nid", "national id", "জাতীয় পরিচয়", "nid no", "nid_no")) return profile.nidNo

        // Date of birth
        if (t.containsAny("date of birth", "dob", "জন্ম তারিখ", "birth date", "birth_date")) return profile.dateOfBirth

        // Mobile
        if (t.containsAny("mobile", "phone", "cell", "মোবাইল", "contact no")) return profile.mobileNo

        // Email
        if (t.containsAny("email", "ই-মেইল", "e-mail")) return profile.email

        // Religion
        if (t.containsAny("religion", "ধর্ম")) return profile.religion

        // Blood group
        if (t.containsAny("blood", "রক্তের গ্রুপ")) return profile.bloodGroup

        // Gender
        if (t.containsAny("gender", "লিঙ্গ", "sex")) return profile.gender

        // Nationality
        if (t.containsAny("nationality", "জাতীয়তা")) return profile.nationality

        // Marital
        if (t.containsAny("marital", "বৈবাহিক")) return profile.maritalStatus

        // Present address
        if (t.containsAny("present village", "বর্তমান গ্রাম", "present_village")) return profile.presentVillage
        if (t.containsAny("present post office", "বর্তমান পোস্ট অফিস")) return profile.presentPostOffice
        if (t.containsAny("present upazila", "বর্তমান উপজেলা")) return profile.presentUpazila
        if (t.containsAny("present district", "বর্তমান জেলা")) return profile.presentDistrict
        if (t.containsAny("present division", "বর্তমান বিভাগ")) return profile.presentDivision
        if (t.containsAny("present post code", "বর্তমান পোস্ট কোড")) return profile.presentPostCode

        // Permanent address
        if (t.containsAny("permanent village", "স্থায়ী গ্রাম", "perm_village")) return profile.permanentVillage
        if (t.containsAny("permanent post office", "স্থায়ী পোস্ট অফিস")) return profile.permanentPostOffice
        if (t.containsAny("permanent upazila", "স্থায়ী উপজেলা")) return profile.permanentUpazila
        if (t.containsAny("permanent district", "স্থায়ী জেলা")) return profile.permanentDistrict
        if (t.containsAny("permanent division", "স্থায়ী বিভাগ")) return profile.permanentDivision
        if (t.containsAny("permanent post code", "স্থায়ী পোস্ট কোড")) return profile.permanentPostCode

        // SSC
        if (t.containsAny("ssc roll", "এসএসসি রোল")) return profile.sscRollNo
        if (t.containsAny("ssc reg", "এসএসসি রেজি")) return profile.sscRegNo
        if (t.containsAny("ssc year", "ssc passing year")) return profile.sscYear
        if (t.containsAny("ssc gpa", "ssc result", "ssc grade")) return profile.sscGpa
        if (t.containsAny("ssc board")) return profile.sscBoard

        // HSC
        if (t.containsAny("hsc roll", "এইচএসসি রোল")) return profile.hscRollNo
        if (t.containsAny("hsc reg", "এইচএসসি রেজি")) return profile.hscRegNo
        if (t.containsAny("hsc year", "hsc passing year")) return profile.hscYear
        if (t.containsAny("hsc gpa", "hsc result")) return profile.hscGpa
        if (t.containsAny("hsc board")) return profile.hscBoard

        // Graduation
        if (t.containsAny("degree", "graduation degree", "স্নাতক")) return profile.graduationDegree
        if (t.containsAny("institution", "university", "college", "বিশ্ববিদ্যালয়")) return profile.graduationInstitution
        if (t.containsAny("passing year", "graduation year")) return profile.graduationYear

        // Quota
        if (t.containsAny("quota", "কোটা")) return profile.quota

        return null
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it.lowercase()) }
    }
}

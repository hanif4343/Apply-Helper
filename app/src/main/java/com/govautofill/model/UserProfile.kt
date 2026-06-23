package com.govautofill.model

import com.google.gson.Gson

data class UserProfile(
    // Personal Info
    var fullNameBn: String = "",         // বাংলায় পূর্ণ নাম
    var fullNameEn: String = "",         // ইংরেজিতে পূর্ণ নাম
    var fatherNameBn: String = "",
    var fatherNameEn: String = "",
    var motherNameBn: String = "",
    var motherNameEn: String = "",
    var spouseNameBn: String = "",
    var spouseNameEn: String = "",
    var careOf: String = "",

    // Birth Info
    var dateOfBirth: String = "",        // DD/MM/YYYY
    var placeOfBirth: String = "",
    var birthCertificateNo: String = "", // Birth Registration Number
    var nidNo: String = "",
    var passportNo: String = "",

    // Contact
    var mobileNo: String = "",
    var confirmMobileNo: String = "",
    var alternatePhone: String = "",
    var email: String = "",

    // Identity
    var religion: String = "",
    var nationality: String = "Bangladeshi",
    var gender: String = "",
    var maritalStatus: String = "",
    var quota: String = "Not Applicable",
    var departmentalStatus: String = "Not Applicable",
    var bloodGroup: String = "",

    // Address - Present
    var presentCareOf: String = "",
    var presentVillage: String = "",
    var presentPostOffice: String = "",
    var presentUpazila: String = "",
    var presentDistrict: String = "",
    var presentDivision: String = "",
    var presentPostCode: String = "",

    // Address - Permanent
    var permanentCareOf: String = "",
    var permanentVillage: String = "",
    var permanentPostOffice: String = "",
    var permanentUpazila: String = "",
    var permanentDistrict: String = "",
    var permanentDivision: String = "",
    var permanentPostCode: String = "",

    // Education - JSC
    var jscExam: String = "",
    var jscBoard: String = "",
    var jscRollNo: String = "",
    var jscRegNo: String = "",
    var jscYear: String = "",
    var jscGpa: String = "",
    var jscResultType: String = "",

    // Education - SSC
    var sscExam: String = "S.S.C",
    var sscBoard: String = "",
    var sscRollNo: String = "",
    var sscRegNo: String = "",
    var sscYear: String = "",
    var sscGpa: String = "",
    var sscGroup: String = "",
    var sscResultType: String = "GPA(out of 5)",

    // Education - HSC
    var hscExam: String = "H.S.C",
    var hscBoard: String = "",
    var hscRollNo: String = "",
    var hscRegNo: String = "",
    var hscYear: String = "",
    var hscGpa: String = "",
    var hscGroup: String = "",
    var hscResultType: String = "GPA(out of 5)",

    // Education - Graduation
    var graduationDegree: String = "",
    var graduationSubject: String = "",
    var graduationInstitution: String = "",
    var graduationYear: String = "",
    var graduationResult: String = "",
    var graduationResultType: String = "",
    var graduationDuration: String = "",

    // Others
    var physicalDisability: String = "না",
    var freedomFighterQuota: String = "না",

    // Photo path (local)
    var photoPath: String = "",
    var signaturePath: String = ""
) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): UserProfile = Gson().fromJson(json, UserProfile::class.java)
    }
}

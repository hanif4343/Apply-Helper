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

    // Birth Info
    var dateOfBirth: String = "",        // DD/MM/YYYY
    var placeOfBirth: String = "",
    var birthCertificateNo: String = "",
    var nidNo: String = "",
    var passportNo: String = "",

    // Contact
    var mobileNo: String = "",
    var alternatePhone: String = "",
    var email: String = "",

    // Address - Present
    var presentVillage: String = "",
    var presentPostOffice: String = "",
    var presentUpazila: String = "",
    var presentDistrict: String = "",
    var presentDivision: String = "",
    var presentPostCode: String = "",

    // Address - Permanent
    var permanentVillage: String = "",
    var permanentPostOffice: String = "",
    var permanentUpazila: String = "",
    var permanentDistrict: String = "",
    var permanentDivision: String = "",
    var permanentPostCode: String = "",

    // Identity
    var religion: String = "",
    var nationality: String = "বাংলাদেশী",
    var gender: String = "",
    var maritalStatus: String = "",
    var quota: String = "",

    // Education - SSC
    var sscBoard: String = "",
    var sscRollNo: String = "",
    var sscRegNo: String = "",
    var sscYear: String = "",
    var sscGpa: String = "",
    var sscGroup: String = "",

    // Education - HSC
    var hscBoard: String = "",
    var hscRollNo: String = "",
    var hscRegNo: String = "",
    var hscYear: String = "",
    var hscGpa: String = "",
    var hscGroup: String = "",

    // Education - Graduation
    var graduationDegree: String = "",
    var graduationSubject: String = "",
    var graduationInstitution: String = "",
    var graduationYear: String = "",
    var graduationResult: String = "",

    // Others
    var bloodGroup: String = "",
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

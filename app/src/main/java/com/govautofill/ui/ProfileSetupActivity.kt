package com.govautofill.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.govautofill.databinding.ActivityProfileSetupBinding
import com.govautofill.model.UserProfile
import com.govautofill.utils.ProfileRepository

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSetupBinding
    private lateinit var profileRepo: ProfileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        profileRepo = ProfileRepository(this)
        loadProfile()
        binding.btnSave.setOnClickListener { saveProfile() }
        binding.btnBack.setOnClickListener { finish() }
    }

    fun onSaveBottomClicked(view: View) { saveProfile() }

    private fun loadProfile() {
        val p = profileRepo.getProfile()
        with(binding) {
            etFullNameBn.setText(p.fullNameBn); etFullNameEn.setText(p.fullNameEn)
            etFatherNameBn.setText(p.fatherNameBn); etFatherNameEn.setText(p.fatherNameEn)
            etMotherNameBn.setText(p.motherNameBn); etMotherNameEn.setText(p.motherNameEn)
            etSpouseNameBn.setText(p.spouseNameBn); etSpouseNameEn.setText(p.spouseNameEn)
            etDob.setText(p.dateOfBirth); etNid.setText(p.nidNo); etPassport.setText(p.passportNo)
            etMobile.setText(p.mobileNo); etEmail.setText(p.email)
            etReligion.setText(p.religion); etBloodGroup.setText(p.bloodGroup)
            etGender.setText(p.gender); etNationality.setText(p.nationality)
            etMarital.setText(p.maritalStatus); etQuota.setText(p.quota)
            etPresentVillage.setText(p.presentVillage); etPresentPostOffice.setText(p.presentPostOffice)
            etPresentUpazila.setText(p.presentUpazila); etPresentDistrict.setText(p.presentDistrict)
            etPresentDivision.setText(p.presentDivision); etPresentPostCode.setText(p.presentPostCode)
            etPermanentVillage.setText(p.permanentVillage); etPermanentPostOffice.setText(p.permanentPostOffice)
            etPermanentUpazila.setText(p.permanentUpazila); etPermanentDistrict.setText(p.permanentDistrict)
            etPermanentDivision.setText(p.permanentDivision); etPermanentPostCode.setText(p.permanentPostCode)
            etSscBoard.setText(p.sscBoard); etSscRoll.setText(p.sscRollNo); etSscReg.setText(p.sscRegNo)
            etSscYear.setText(p.sscYear); etSscGpa.setText(p.sscGpa)
            etHscBoard.setText(p.hscBoard); etHscRoll.setText(p.hscRollNo); etHscReg.setText(p.hscRegNo)
            etHscYear.setText(p.hscYear); etHscGpa.setText(p.hscGpa)
            etGradDegree.setText(p.graduationDegree); etGradSubject.setText(p.graduationSubject)
            etGradInstitution.setText(p.graduationInstitution); etGradYear.setText(p.graduationYear)
            etGradResult.setText(p.graduationResult)
        }
    }

    private fun saveProfile() {
        val p = UserProfile().apply {
            with(binding) {
                fullNameBn = etFullNameBn.text.toString().trim(); fullNameEn = etFullNameEn.text.toString().trim()
                fatherNameBn = etFatherNameBn.text.toString().trim(); fatherNameEn = etFatherNameEn.text.toString().trim()
                motherNameBn = etMotherNameBn.text.toString().trim(); motherNameEn = etMotherNameEn.text.toString().trim()
                spouseNameBn = etSpouseNameBn.text.toString().trim(); spouseNameEn = etSpouseNameEn.text.toString().trim()
                dateOfBirth = etDob.text.toString().trim(); nidNo = etNid.text.toString().trim()
                passportNo = etPassport.text.toString().trim(); mobileNo = etMobile.text.toString().trim()
                email = etEmail.text.toString().trim(); religion = etReligion.text.toString().trim()
                bloodGroup = etBloodGroup.text.toString().trim(); gender = etGender.text.toString().trim()
                nationality = etNationality.text.toString().trim(); maritalStatus = etMarital.text.toString().trim()
                quota = etQuota.text.toString().trim()
                presentVillage = etPresentVillage.text.toString().trim(); presentPostOffice = etPresentPostOffice.text.toString().trim()
                presentUpazila = etPresentUpazila.text.toString().trim(); presentDistrict = etPresentDistrict.text.toString().trim()
                presentDivision = etPresentDivision.text.toString().trim(); presentPostCode = etPresentPostCode.text.toString().trim()
                permanentVillage = etPermanentVillage.text.toString().trim(); permanentPostOffice = etPermanentPostOffice.text.toString().trim()
                permanentUpazila = etPermanentUpazila.text.toString().trim(); permanentDistrict = etPermanentDistrict.text.toString().trim()
                permanentDivision = etPermanentDivision.text.toString().trim(); permanentPostCode = etPermanentPostCode.text.toString().trim()
                sscBoard = etSscBoard.text.toString().trim(); sscRollNo = etSscRoll.text.toString().trim()
                sscRegNo = etSscReg.text.toString().trim(); sscYear = etSscYear.text.toString().trim(); sscGpa = etSscGpa.text.toString().trim()
                hscBoard = etHscBoard.text.toString().trim(); hscRollNo = etHscRoll.text.toString().trim()
                hscRegNo = etHscReg.text.toString().trim(); hscYear = etHscYear.text.toString().trim(); hscGpa = etHscGpa.text.toString().trim()
                graduationDegree = etGradDegree.text.toString().trim(); graduationSubject = etGradSubject.text.toString().trim()
                graduationInstitution = etGradInstitution.text.toString().trim(); graduationYear = etGradYear.text.toString().trim()
                graduationResult = etGradResult.text.toString().trim()
            }
        }
        profileRepo.saveProfile(p)
        Toast.makeText(this, "✅ প্রোফাইল সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
        finish()
    }
}

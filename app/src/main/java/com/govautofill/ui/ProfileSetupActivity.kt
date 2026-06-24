package com.govautofill.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.govautofill.R
import com.govautofill.databinding.ActivityProfileSetupBinding
import com.govautofill.model.ProfileEntry
import com.govautofill.model.UserProfile
import com.govautofill.utils.AdManager
import com.govautofill.utils.ProfileRepository

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSetupBinding
    private lateinit var profileRepo: ProfileRepository
    private var editingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        profileRepo = ProfileRepository(this)

        editingId = intent.getStringExtra("profile_id")
        binding.tvSetupTitle.text = if (editingId != null) "প্রোফাইল এডিট করুন" else "নতুন প্রোফাইল"

        loadProfile()
        binding.btnSave.setOnClickListener { saveProfile() }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnOpenMediaVault.setOnClickListener {
            startActivity(Intent(this, MediaVaultActivity::class.java))
        }
        AdManager.loadBanner(this, binding.adContainerProfile)
    }

    fun onSaveBottomClicked(view: View) = saveProfile()

    // ── Helper: set a Spinner to the item matching the given value ────────────
    private fun Spinner.setSelection(value: String) {
        if (value.isBlank()) return
        val v = value.trim()
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString().equals(v, ignoreCase = true)) {
                setSelection(i); return
            }
        }
    }

    // ── Helper: get selected Spinner value (skip "Select") ───────────────────
    private fun Spinner.value(): String {
        val sel = selectedItem?.toString() ?: ""
        return if (sel == "Select") "" else sel
    }

    private fun loadProfile() {
        val entry = editingId?.let { profileRepo.getById(it) }
        val p = entry?.profile ?: UserProfile()

        binding.etProfileLabel.setText(entry?.label ?: "")

        with(binding) {
            // Basic info — text fields
            etFullNameEn.setText(p.fullNameEn)
            etFullNameBn.setText(p.fullNameBn)
            etFatherNameEn.setText(p.fatherNameEn)
            etFatherNameBn.setText(p.fatherNameBn)
            etMotherNameEn.setText(p.motherNameEn)
            etMotherNameBn.setText(p.motherNameBn)
            etDob.setText(p.dateOfBirth)

            // Basic info — spinners
            spinnerNationality.setSelection(p.nationality.ifBlank { "Bangladeshi" })
            spinnerReligion.setSelection(p.religion)
            spinnerGender.setSelection(p.gender)
            spinnerNid.setSelection(if (p.nidNo.isNotBlank()) "Yes" else "No")
            etNid.setText(p.nidNo)
            spinnerBreg.setSelection(if (p.birthCertificateNo.isNotBlank()) "Yes" else "No")
            etBirthRegNo.setText(p.birthCertificateNo)
            spinnerPassport.setSelection(if (p.passportNo.isNotBlank()) "Yes" else "No")
            etPassport.setText(p.passportNo)
            spinnerMarital.setSelection(p.maritalStatus)
            etSpouseNameEn.setText(p.spouseNameEn)
            etSpouseNameBn.setText(p.spouseNameBn)
            etMobile.setText(p.mobileNo)
            etConfirmMobile.setText(p.confirmMobileNo)
            etEmail.setText(p.email)
            spinnerQuota.setSelection(p.quota.ifBlank { "Not Applicable" })
            spinnerDepStatus.setSelection(p.departmentalStatus.ifBlank { "Not Applicable" })
            spinnerBlood.setSelection(p.bloodGroup)

            // Present Address
            etCareOf.setText(p.careOf)
            etPresentVillage.setText(p.presentVillage)
            spinnerPresentDistrict.setSelection(p.presentDistrict)
            etPresentUpazila.setText(p.presentUpazila)
            etPresentPostOffice.setText(p.presentPostOffice)
            etPresentPostCode.setText(p.presentPostCode)
            etPresentDivision.setText(p.presentDivision)

            // Permanent Address
            etPermanentCareOf.setText(p.permanentCareOf)
            etPermanentVillage.setText(p.permanentVillage)
            spinnerPermanentDistrict.setSelection(p.permanentDistrict)
            etPermanentUpazila.setText(p.permanentUpazila)
            etPermanentPostOffice.setText(p.permanentPostOffice)
            etPermanentPostCode.setText(p.permanentPostCode)
            etPermanentDivision.setText(p.permanentDivision)

            // JSC
            spinnerJscExam.setSelection(p.jscExam)
            spinnerJscBoard.setSelection(p.jscBoard)
            etJscRoll.setText(p.jscRollNo)
            spinnerJscResult.setSelection(p.jscResultType)
            etJscGpa.setText(p.jscGpa)
            spinnerJscYear.setSelection(p.jscYear)

            // SSC
            spinnerSscExam.setSelection(p.sscExam)
            spinnerSscBoard.setSelection(p.sscBoard)
            etSscRoll.setText(p.sscRollNo)
            spinnerSscResult.setSelection(p.sscResultType)
            etSscGroup.setText(p.sscGroup)
            spinnerSscYear.setSelection(p.sscYear)
            etSscReg.setText(p.sscRegNo)
            etSscGpa.setText(p.sscGpa)

            // HSC
            spinnerHscExam.setSelection(p.hscExam)
            spinnerHscBoard.setSelection(p.hscBoard)
            etHscRoll.setText(p.hscRollNo)
            spinnerHscResult.setSelection(p.hscResultType)
            etHscGroup.setText(p.hscGroup)
            spinnerHscYear.setSelection(p.hscYear)
            etHscReg.setText(p.hscRegNo)
            etHscGpa.setText(p.hscGpa)

            // Graduation
            spinnerGradExam.setSelection(p.graduationDegree)
            etGradSubject.setText(p.graduationSubject)
            etGradInstitution.setText(p.graduationInstitution)
            spinnerGradYear.setSelection(p.graduationYear)
            spinnerGradResult.setSelection(p.graduationResultType)
            etGradResult.setText(p.graduationResult)
            spinnerGradDuration.setSelection(p.graduationDuration)
        }
    }

    private fun saveProfile() {
        val p = UserProfile().apply {
            with(binding) {
                // Text fields
                fullNameEn        = etFullNameEn.text.toString().trim()
                fullNameBn        = etFullNameBn.text.toString().trim()
                fatherNameEn      = etFatherNameEn.text.toString().trim()
                fatherNameBn      = etFatherNameBn.text.toString().trim()
                motherNameEn      = etMotherNameEn.text.toString().trim()
                motherNameBn      = etMotherNameBn.text.toString().trim()
                dateOfBirth       = etDob.text.toString().trim()
                spouseNameEn      = etSpouseNameEn.text.toString().trim()
                spouseNameBn      = etSpouseNameBn.text.toString().trim()
                mobileNo          = etMobile.text.toString().trim()
                confirmMobileNo   = etConfirmMobile.text.toString().trim()
                email             = etEmail.text.toString().trim()
                nidNo             = etNid.text.toString().trim()
                birthCertificateNo = etBirthRegNo.text.toString().trim()
                passportNo        = etPassport.text.toString().trim()

                // Spinner fields
                nationality       = spinnerNationality.value()
                religion          = spinnerReligion.value()
                gender            = spinnerGender.value()
                maritalStatus     = spinnerMarital.value()
                quota             = spinnerQuota.value()
                departmentalStatus = spinnerDepStatus.value()
                bloodGroup        = spinnerBlood.value()

                // Present Address
                careOf            = etCareOf.text.toString().trim()
                presentVillage    = etPresentVillage.text.toString().trim()
                presentDistrict   = spinnerPresentDistrict.value()
                presentUpazila    = etPresentUpazila.text.toString().trim()
                presentPostOffice = etPresentPostOffice.text.toString().trim()
                presentPostCode   = etPresentPostCode.text.toString().trim()
                presentDivision   = etPresentDivision.text.toString().trim()

                // Permanent Address
                permanentCareOf      = etPermanentCareOf.text.toString().trim()
                permanentVillage     = etPermanentVillage.text.toString().trim()
                permanentDistrict    = spinnerPermanentDistrict.value()
                permanentUpazila     = etPermanentUpazila.text.toString().trim()
                permanentPostOffice  = etPermanentPostOffice.text.toString().trim()
                permanentPostCode    = etPermanentPostCode.text.toString().trim()
                permanentDivision    = etPermanentDivision.text.toString().trim()

                // JSC
                jscExam       = spinnerJscExam.value()
                jscBoard      = spinnerJscBoard.value()
                jscRollNo     = etJscRoll.text.toString().trim()
                jscResultType = spinnerJscResult.value()
                jscGpa        = etJscGpa.text.toString().trim()
                jscYear       = spinnerJscYear.value()

                // SSC
                sscExam       = spinnerSscExam.value()
                sscBoard      = spinnerSscBoard.value()
                sscRollNo     = etSscRoll.text.toString().trim()
                sscResultType = spinnerSscResult.value()
                sscGroup      = etSscGroup.text.toString().trim()
                sscYear       = spinnerSscYear.value()
                sscRegNo      = etSscReg.text.toString().trim()
                sscGpa        = etSscGpa.text.toString().trim()

                // HSC
                hscExam       = spinnerHscExam.value()
                hscBoard      = spinnerHscBoard.value()
                hscRollNo     = etHscRoll.text.toString().trim()
                hscResultType = spinnerHscResult.value()
                hscGroup      = etHscGroup.text.toString().trim()
                hscYear       = spinnerHscYear.value()
                hscRegNo      = etHscReg.text.toString().trim()
                hscGpa        = etHscGpa.text.toString().trim()

                // Graduation
                graduationDegree     = spinnerGradExam.value()
                graduationSubject    = etGradSubject.text.toString().trim()
                graduationInstitution = etGradInstitution.text.toString().trim()
                graduationYear       = spinnerGradYear.value()
                graduationResultType = spinnerGradResult.value()
                graduationResult     = etGradResult.text.toString().trim()
                graduationDuration   = spinnerGradDuration.value()
            }
        }

        var label = binding.etProfileLabel.text.toString().trim()
        if (label.isEmpty()) label = p.fullNameEn.ifEmpty { p.fullNameBn.ifEmpty { "নামহীন প্রোফাইল" } }

        val entry = if (editingId != null)
            ProfileEntry(id = editingId!!, label = label, profile = p)
        else
            ProfileEntry(label = label, profile = p)

        profileRepo.save(entry)
        Toast.makeText(this, "✅ প্রোফাইল সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
        finish()
    }
}

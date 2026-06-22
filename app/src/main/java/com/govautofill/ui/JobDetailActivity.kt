package com.govautofill.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.govautofill.databinding.ActivityJobDetailBinding
import com.govautofill.model.*

class JobDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobDetailBinding
    private lateinit var repo: JobApplicationRepository
    private var existingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = JobApplicationRepository(this)

        existingId = intent.getStringExtra("job_id")
        existingId?.let { id ->
            repo.getById(id)?.let { loadJob(it) }
        }

        setupSpinners()
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSaveJob.setOnClickListener { saveJob() }
    }

    private fun setupSpinners() {
        // Payment Status spinner
        val payStatuses = PaymentStatus.values().map { "${it.emoji} ${it.label}" }
        binding.spinnerPayment.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, payStatuses)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Application Status spinner
        val appStatuses = ApplicationStatus.values().map { "${it.emoji} ${it.label}" }
        binding.spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, appStatuses)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun loadJob(job: JobApplication) {
        binding.etPostName.setText(job.postName)
        binding.etOrganization.setText(job.organization)
        binding.etCircularNo.setText(job.circularNo)
        binding.etApplicationDate.setText(job.applicationDate)
        binding.etDeadline.setText(job.deadline)
        binding.etExamDate.setText(job.examDate)
        binding.etApplicationUrl.setText(job.applicationUrl)
        binding.etTrackingId.setText(job.trackingId)
        binding.etUserId.setText(job.userIdOnSite)
        binding.etPassword.setText(job.passwordOnSite)
        binding.etPaymentAmount.setText(job.paymentAmount)
        binding.etPaymentMethod.setText(job.paymentMethod)
        binding.etPaymentDate.setText(job.paymentDate)
        binding.etTransactionId.setText(job.transactionId)
        binding.etExamResult.setText(job.examResult)
        binding.etNotes.setText(job.notes)
        binding.spinnerPayment.setSelection(job.paymentStatus.ordinal)
        binding.spinnerStatus.setSelection(job.applicationStatus.ordinal)
    }

    private fun saveJob() {
        val postName = binding.etPostName.text.toString().trim()
        val org = binding.etOrganization.text.toString().trim()
        if (postName.isEmpty() || org.isEmpty()) {
            Toast.makeText(this, "পদের নাম ও প্রতিষ্ঠান আবশ্যক", Toast.LENGTH_SHORT).show()
            return
        }

        val job = JobApplication(
            id = existingId ?: System.currentTimeMillis().toString(),
            postName = postName,
            organization = org,
            circularNo = binding.etCircularNo.text.toString().trim(),
            applicationDate = binding.etApplicationDate.text.toString().trim(),
            deadline = binding.etDeadline.text.toString().trim(),
            examDate = binding.etExamDate.text.toString().trim(),
            applicationUrl = binding.etApplicationUrl.text.toString().trim(),
            trackingId = binding.etTrackingId.text.toString().trim(),
            userIdOnSite = binding.etUserId.text.toString().trim(),
            passwordOnSite = binding.etPassword.text.toString().trim(),
            paymentStatus = PaymentStatus.values()[binding.spinnerPayment.selectedItemPosition],
            paymentAmount = binding.etPaymentAmount.text.toString().trim(),
            paymentMethod = binding.etPaymentMethod.text.toString().trim(),
            paymentDate = binding.etPaymentDate.text.toString().trim(),
            transactionId = binding.etTransactionId.text.toString().trim(),
            applicationStatus = ApplicationStatus.values()[binding.spinnerStatus.selectedItemPosition],
            examResult = binding.etExamResult.text.toString().trim(),
            notes = binding.etNotes.text.toString().trim()
        )

        repo.save(job)
        Toast.makeText(this, "✅ সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}

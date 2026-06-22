package com.govautofill.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.govautofill.R
import com.govautofill.databinding.ActivityJobHistoryBinding
import com.govautofill.model.ApplicationStatus
import com.govautofill.model.JobApplication
import com.govautofill.model.JobApplicationRepository
import com.govautofill.model.PaymentStatus
import com.govautofill.utils.AdManager
import com.govautofill.utils.PdfGenerator
import com.govautofill.utils.ProfileRepository

class JobHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobHistoryBinding
    private lateinit var repo: JobApplicationRepository
    private lateinit var profileRepo: ProfileRepository
    private var filterStatus: String = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = JobApplicationRepository(this)
        profileRepo = ProfileRepository(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddNew.setOnClickListener {
            startActivityForResult(Intent(this, JobDetailActivity::class.java), 100)
        }

        setupFilterChips()
        AdManager.loadBanner(this, binding.adContainer)
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupFilterChips() {
        val chips = listOf(
            binding.chipAll, binding.chipUnpaid, binding.chipPaid,
            binding.chipPending, binding.chipPassed
        )
        val filters = listOf("ALL", "UNPAID", "PAID", "PENDING", "PASSED")

        chips.forEachIndexed { i, chip ->
            chip.setOnClickListener {
                chips.forEach { it.isSelected = false }
                chip.isSelected = true
                filterStatus = filters[i]
                loadData()
            }
        }
        chips[0].isSelected = true
    }

    private fun loadData() {
        val all = repo.getAll()
        val stats = repo.getStats()

        // Stats row
        binding.tvTotal.text = "মোট: ${stats.total}"
        binding.tvPaid.text = "পেমেন্ট: ${stats.paid}"
        binding.tvUnpaid.text = "বাকি: ${stats.unpaid}"
        binding.tvPassed.text = "পাস: ${stats.passed}"

        val filtered = when (filterStatus) {
            "UNPAID"  -> all.filter { it.paymentStatus == PaymentStatus.UNPAID }
            "PAID"    -> all.filter { it.paymentStatus == PaymentStatus.PAID }
            "PENDING" -> all.filter { it.applicationStatus == ApplicationStatus.APPLIED || it.applicationStatus == ApplicationStatus.PENDING }
            "PASSED"  -> all.filter { it.applicationStatus == ApplicationStatus.PASSED }
            else      -> all
        }

        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = JobAdapter(filtered, repo, profileRepo, this) { loadData() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) loadData()
    }
}

// ── RecyclerView Adapter ─────────────────────────────────────────────────────
class JobAdapter(
    private val items: List<JobApplication>,
    private val repo: JobApplicationRepository,
    private val profileRepo: ProfileRepository,
    private val activity: JobHistoryActivity,
    private val onRefresh: () -> Unit
) : RecyclerView.Adapter<JobAdapter.VH>() {

    inner class VH(val root: View) : RecyclerView.ViewHolder(root) {
        val tvTitle: TextView = root.findViewById(R.id.tvJobTitle)
        val tvOrg: TextView = root.findViewById(R.id.tvOrganization)
        val tvDate: TextView = root.findViewById(R.id.tvApplicationDate)
        val tvPayment: TextView = root.findViewById(R.id.tvPaymentBadge)
        val tvStatus: TextView = root.findViewById(R.id.tvStatusBadge)
        val tvTracking: TextView = root.findViewById(R.id.tvTrackingId)
        val btnEdit: Button = root.findViewById(R.id.btnEditJob)
        val btnPdf: Button = root.findViewById(R.id.btnGeneratePdf)
        val btnDelete: Button = root.findViewById(R.id.btnDeleteJob)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_job_application, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val job = items[pos]
        h.tvTitle.text = "${job.postName.ifEmpty { "পদ অজানা" }} — ${job.circularNo}"
        h.tvOrg.text = job.organization.ifEmpty { "প্রতিষ্ঠান অজানা" }
        h.tvDate.text = "📅 ${job.applicationDate.ifEmpty { "তারিখ নেই" }}  •  শেষ: ${job.deadline.ifEmpty { "—" }}"
        h.tvTracking.text = if (job.trackingId.isNotEmpty()) "🔖 Tracking: ${job.trackingId}" else ""
        h.tvTracking.visibility = if (job.trackingId.isNotEmpty()) View.VISIBLE else View.GONE

        // Payment badge
        h.tvPayment.text = "${job.paymentStatus.emoji} ${job.paymentStatus.label}"
        h.tvPayment.setTextColor(when (job.paymentStatus) {
            PaymentStatus.PAID -> android.graphics.Color.rgb(46, 125, 50)
            PaymentStatus.UNPAID -> android.graphics.Color.rgb(198, 40, 40)
            else -> android.graphics.Color.rgb(230, 81, 0)
        })

        // Status badge
        h.tvStatus.text = "${job.applicationStatus.emoji} ${job.applicationStatus.label}"

        h.btnEdit.setOnClickListener {
            val intent = Intent(activity, JobDetailActivity::class.java)
            intent.putExtra("job_id", job.id)
            activity.startActivityForResult(intent, 100)
        }

        h.btnPdf.setOnClickListener {
            try {
                val profile = profileRepo.getProfile()
                val file = PdfGenerator.generateApplicationPdf(activity, job, profile)
                val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", file)
                val shareIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                activity.startActivity(Intent.createChooser(shareIntent, "PDF খুলুন"))
            } catch (e: Exception) {
                Toast.makeText(activity, "PDF Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        h.btnDelete.setOnClickListener {
            AlertDialog.Builder(activity)
                .setTitle("মুছে ফেলবেন?")
                .setMessage("\"${job.postName}\" আবেদনটি মুছে যাবে।")
                .setPositiveButton("হ্যাঁ, মুছুন") { _, _ ->
                    repo.delete(job.id)
                    onRefresh()
                }
                .setNegativeButton("না", null)
                .show()
        }
    }
}

package com.govautofill.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class JobApplication(
    val id: String = System.currentTimeMillis().toString(),
    var jobTitle: String = "",
    var organization: String = "",
    var postName: String = "",          // পদের নাম
    var circularNo: String = "",        // বিজ্ঞপ্তি নম্বর
    var applicationDate: String = "",   // DD/MM/YYYY
    var deadline: String = "",          // আবেদনের শেষ তারিখ
    var examDate: String = "",          // পরীক্ষার তারিখ (জানা থাকলে)
    var applicationUrl: String = "",
    var trackingId: String = "",        // Teletalk tracking / roll
    var userIdOnSite: String = "",
    var passwordOnSite: String = "",

    // Payment
    var paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    var paymentMethod: String = "",     // Teletalk/bKash/Card
    var paymentAmount: String = "",
    var paymentDate: String = "",
    var transactionId: String = "",

    // Status
    var applicationStatus: ApplicationStatus = ApplicationStatus.APPLIED,
    var examResult: String = "",        // Pass/Fail/Waiting
    var notes: String = "",
    var isFavorite: Boolean = false
)

enum class PaymentStatus(val label: String, val emoji: String) {
    UNPAID("পেমেন্ট বাকি", "🔴"),
    PAID("পেমেন্ট সম্পন্ন", "🟢"),
    FREE("বিনামূল্যে", "🟡")
}

enum class ApplicationStatus(val label: String, val emoji: String) {
    APPLIED("আবেদন সম্পন্ন", "📋"),
    PENDING("অপেক্ষারত", "⏳"),
    ADMIT_CARD("প্রবেশপত্র প্রস্তুত", "🎫"),
    EXAM_DONE("পরীক্ষা দেওয়া হয়েছে", "✍️"),
    PASSED("উত্তীর্ণ", "✅"),
    FAILED("অনুত্তীর্ণ", "❌"),
    CANCELLED("বাতিল", "🚫")
}

class JobApplicationRepository(context: android.content.Context) {
    private val prefs = context.getSharedPreferences("job_applications", android.content.Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getAll(): List<JobApplication> {
        val json = prefs.getString("applications", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<JobApplication>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) { emptyList() }
    }

    fun save(app: JobApplication) {
        val list = getAll().toMutableList()
        val idx = list.indexOfFirst { it.id == app.id }
        if (idx >= 0) list[idx] = app else list.add(0, app)
        prefs.edit().putString("applications", gson.toJson(list)).apply()
    }

    fun delete(id: String) {
        val list = getAll().toMutableList()
        list.removeAll { it.id == id }
        prefs.edit().putString("applications", gson.toJson(list)).apply()
    }

    fun getById(id: String): JobApplication? = getAll().find { it.id == id }

    fun getStats(): AppStats {
        val all = getAll()
        return AppStats(
            total = all.size,
            paid = all.count { it.paymentStatus == PaymentStatus.PAID },
            unpaid = all.count { it.paymentStatus == PaymentStatus.UNPAID },
            passed = all.count { it.applicationStatus == ApplicationStatus.PASSED },
            pending = all.count { it.applicationStatus == ApplicationStatus.PENDING || it.applicationStatus == ApplicationStatus.APPLIED }
        )
    }

    data class AppStats(val total: Int, val paid: Int, val unpaid: Int, val passed: Int, val pending: Int)
}

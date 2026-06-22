package com.govautofill.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.govautofill.model.JobApplication
import com.govautofill.model.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    fun generateApplicationPdf(
        context: Context,
        job: JobApplication,
        profile: UserProfile
    ): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        var y = 50f
        val leftMargin = 50f
        val rightMargin = 545f
        val lineHeight = 22f

        // ── Title ────────────────────────────────────────────────────────────
        val titlePaint = Paint().apply {
            color = Color.rgb(21, 101, 192)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText("চাকরির আবেদন — সারসংক্ষেপ", leftMargin, y, titlePaint)
        y += 8f

        // Divider
        val divPaint = Paint().apply { color = Color.rgb(21, 101, 192); strokeWidth = 2f }
        canvas.drawLine(leftMargin, y, rightMargin, y, divPaint)
        y += 20f

        // ── Section header paint ─────────────────────────────────────────────
        val secPaint = Paint().apply {
            color = Color.rgb(21, 101, 192)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val bgPaint = Paint().apply { color = Color.rgb(232, 245, 253) }
        val grayBg = Paint().apply { color = Color.rgb(245, 245, 245) }

        fun section(title: String) {
            y += 8f
            canvas.drawRect(leftMargin, y - 14f, rightMargin, y + 4f, bgPaint)
            canvas.drawText(title, leftMargin + 4f, y, secPaint)
            y += lineHeight
        }

        fun row(label: String, value: String, zebra: Boolean = false) {
            if (value.isBlank()) return
            if (zebra) canvas.drawRect(leftMargin, y - 14f, rightMargin, y + 4f, grayBg)
            canvas.drawText("$label:", leftMargin + 4f, y, labelPaint)
            canvas.drawText(value, leftMargin + 160f, y, valuePaint)
            y += lineHeight
        }

        // ── Job Info ─────────────────────────────────────────────────────────
        section("চাকরির তথ্য")
        row("পদের নাম", job.postName, false)
        row("প্রতিষ্ঠান", job.organization, true)
        row("বিজ্ঞপ্তি নম্বর", job.circularNo, false)
        row("আবেদনের তারিখ", job.applicationDate, true)
        row("শেষ তারিখ", job.deadline, false)
        row("পরীক্ষার তারিখ", job.examDate, true)
        row("Tracking ID", job.trackingId, false)
        row("User ID", job.userIdOnSite, true)
        row("আবেদনের URL", job.applicationUrl, false)

        // ── Payment ───────────────────────────────────────────────────────────
        section("পেমেন্ট তথ্য")
        val statusPaint = Paint().apply {
            color = when (job.paymentStatus.name) {
                "PAID" -> Color.rgb(46, 125, 50)
                "UNPAID" -> Color.rgb(198, 40, 40)
                else -> Color.rgb(230, 81, 0)
            }
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText("${job.paymentStatus.emoji} ${job.paymentStatus.label}", leftMargin + 4f, y, statusPaint)
        y += lineHeight
        row("পরিমাণ", if (job.paymentAmount.isNotEmpty()) "${job.paymentAmount} টাকা" else "", true)
        row("পদ্ধতি", job.paymentMethod, false)
        row("লেনদেন ID", job.transactionId, true)
        row("পেমেন্টের তারিখ", job.paymentDate, false)

        // ── Application Status ───────────────────────────────────────────────
        section("আবেদনের অবস্থা")
        canvas.drawText("${job.applicationStatus.emoji} ${job.applicationStatus.label}", leftMargin + 4f, y, statusPaint)
        y += lineHeight
        row("পরীক্ষার ফলাফল", job.examResult, true)
        row("নোট", job.notes, false)

        // ── Applicant Info ───────────────────────────────────────────────────
        section("আবেদনকারীর তথ্য")
        row("নাম (বাংলা)", profile.fullNameBn, false)
        row("Name (English)", profile.fullNameEn, true)
        row("NID", profile.nidNo, false)
        row("মোবাইল", profile.mobileNo, true)
        row("ইমেইল", profile.email, false)

        // Try to add photo
        try {
            val photoFile = File(context.filesDir, "media_vault/photo.jpg")
            if (photoFile.exists()) {
                val bmp = BitmapFactory.decodeFile(photoFile.absolutePath)
                canvas.drawBitmap(bmp, null, RectF(450f, 55f, 545f, 155f), null)
                val framePaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
                canvas.drawRect(450f, 55f, 545f, 155f, framePaint)
            }
        } catch (e: Exception) { /* no photo */ }

        // ── Footer ────────────────────────────────────────────────────────────
        y += 10f
        divPaint.color = Color.LTGRAY
        canvas.drawLine(leftMargin, y, rightMargin, y, divPaint)
        y += 15f
        val footerPaint = Paint().apply { color = Color.GRAY; textSize = 9f; isAntiAlias = true }
        val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("Generated by সরকারি চাকরি অটোফিল • $now", leftMargin, y, footerPaint)

        doc.finishPage(page)

        val dir = File(context.getExternalFilesDir(null), "job_applications").also { it.mkdirs() }
        val safe = job.organization.replace("[^a-zA-Z0-9]".toRegex(), "_").take(20)
        val outFile = File(dir, "Job_${safe}_${job.id.takeLast(6)}.pdf")
        FileOutputStream(outFile).use { doc.writeTo(it) }
        doc.close()
        return outFile
    }
}

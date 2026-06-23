package com.govautofill.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.govautofill.R
import com.govautofill.databinding.ActivityProfileListBinding
import com.govautofill.model.ProfileEntry
import com.govautofill.utils.ProfileRepository
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileListBinding
    private lateinit var repo: ProfileRepository

    companion object {
        private const val REQ_CREATE_BACKUP = 501
        private const val REQ_OPEN_BACKUP = 502
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = ProfileRepository(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddProfile.setOnClickListener {
            startActivity(Intent(this, ProfileSetupActivity::class.java))
        }
        binding.btnBackup.setOnClickListener { startBackupFlow() }
        binding.btnRestore.setOnClickListener { startRestoreFlow() }

        loadList()
    }

    override fun onResume() {
        super.onResume()
        loadList()
    }

    private fun loadList() {
        val entries = repo.getAll()
        binding.tvEmpty.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = ProfileAdapter(entries, repo, this) { loadList() }
    }

    // ── Local file backup (Storage Access Framework — কোনো special permission লাগে না) ──
    private fun startBackupFlow() {
        if (repo.getAll().isEmpty()) {
            Toast.makeText(this, "কোনো প্রোফাইল নেই, ব্যাকআপ করার কিছু নেই", Toast.LENGTH_SHORT).show()
            return
        }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "GovAutofillBackup_$stamp.json")
        }
        try {
            startActivityForResult(intent, REQ_CREATE_BACKUP)
        } catch (e: Exception) {
            Toast.makeText(this, "ফাইল ম্যানেজার খোলা যাচ্ছে না: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun startRestoreFlow() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            // কিছু ফাইল ম্যানেজার .json কে ঠিকমতো application/json ট্যাগ করে না, তাই */* রাখলাম
            type = "*/*"
        }
        try {
            startActivityForResult(intent, REQ_OPEN_BACKUP)
        } catch (e: Exception) {
            Toast.makeText(this, "ফাইল ম্যানেজার খোলা যাচ্ছে না: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return

        when (requestCode) {
            REQ_CREATE_BACKUP -> writeBackupToUri(uri)
            REQ_OPEN_BACKUP -> confirmAndRestoreFromUri(uri)
        }
    }

    private fun writeBackupToUri(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { out ->
                OutputStreamWriter(out).use { writer ->
                    writer.write(repo.exportBackupJson())
                }
            }
            Toast.makeText(this, "✅ ব্যাকআপ সেভ হয়েছে! এই ফাইলটা মুছে ফেলবেন না।", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "ব্যাকআপ Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun confirmAndRestoreFromUri(uri: Uri) {
        val json = try {
            contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input)).readText()
            }
        } catch (e: Exception) { null }

        if (json.isNullOrBlank()) {
            Toast.makeText(this, "ফাইল পড়া যাচ্ছে না, এটা কি সঠিক ব্যাকআপ ফাইল?", Toast.LENGTH_LONG).show()
            return
        }

        val hasExisting = repo.getAll().isNotEmpty()
        if (!hasExisting) {
            // এখন কিছু নেই, তাই সরাসরি restore করে দেই — extra dialog দেখানোর দরকার নেই
            doImport(json, replaceExisting = true)
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Restore করবেন?")
            .setMessage("এখন যেসব প্রোফাইল আছে, তার সাথে ব্যাকআপ থেকে নতুনগুলো যোগ করবেন? নাকি বর্তমান সব মুছে শুধু ব্যাকআপ থেকে রাখবেন?")
            .setPositiveButton("যোগ করুন (Merge)") { _, _ -> doImport(json, replaceExisting = false) }
            .setNegativeButton("সব মুছে Restore") { _, _ -> doImport(json, replaceExisting = true) }
            .setNeutralButton("বাতিল", null)
            .show()
    }

    private fun doImport(json: String, replaceExisting: Boolean) {
        try {
            val count = repo.importBackupJson(json, replaceExisting)
            Toast.makeText(this, "✅ $count টি প্রোফাইল Restore হয়েছে!", Toast.LENGTH_LONG).show()
            loadList()
        } catch (e: Exception) {
            Toast.makeText(this, "Restore Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

// ── RecyclerView Adapter ─────────────────────────────────────────────────────
class ProfileAdapter(
    private val items: List<ProfileEntry>,
    private val repo: ProfileRepository,
    private val activity: ProfileListActivity,
    private val onRefresh: () -> Unit
) : RecyclerView.Adapter<ProfileAdapter.VH>() {

    inner class VH(val root: View) : RecyclerView.ViewHolder(root) {
        val tvLabel: TextView = root.findViewById(R.id.tvProfileLabel)
        val tvDetail: TextView = root.findViewById(R.id.tvProfileDetail)
        val tvActiveBadge: TextView = root.findViewById(R.id.tvActiveBadge)
        val btnUse: Button = root.findViewById(R.id.btnUseProfile)
        val btnEdit: Button = root.findViewById(R.id.btnEditProfileItem)
        val btnDelete: Button = root.findViewById(R.id.btnDeleteProfileItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_entry, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val entry = items[pos]
        val isActive = repo.getActiveProfileId() == entry.id

        h.tvLabel.text = entry.label.ifEmpty { "নামহীন প্রোফাইল" }
        val nameStr = entry.profile.fullNameEn.ifEmpty { entry.profile.fullNameBn }
        h.tvDetail.text = listOf(nameStr, entry.profile.mobileNo)
            .filter { it.isNotEmpty() }
            .joinToString("  •  ")
        h.tvActiveBadge.visibility = if (isActive) View.VISIBLE else View.GONE
        h.btnUse.text = if (isActive) "✓ ব্যবহৃত হচ্ছে" else "এটা ব্যবহার করুন"
        h.btnUse.isEnabled = !isActive

        h.btnUse.setOnClickListener {
            repo.setActiveProfileId(entry.id)
            Toast.makeText(activity, "\"${entry.label}\" এখন active হয়েছে", Toast.LENGTH_SHORT).show()
            onRefresh()
        }
        h.btnEdit.setOnClickListener {
            val intent = Intent(activity, ProfileSetupActivity::class.java)
            intent.putExtra("profile_id", entry.id)
            activity.startActivity(intent)
        }
        h.btnDelete.setOnClickListener {
            AlertDialog.Builder(activity)
                .setTitle("মুছে ফেলবেন?")
                .setMessage("\"${entry.label}\" প্রোফাইলটি মুছে যাবে।")
                .setPositiveButton("হ্যাঁ, মুছুন") { _, _ ->
                    repo.delete(entry.id)
                    onRefresh()
                }
                .setNegativeButton("না", null)
                .show()
        }
    }
}

package com.govautofill.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.govautofill.R
import com.govautofill.databinding.ActivityMainBinding
import com.govautofill.service.AutoFillAccessibilityService
import com.govautofill.utils.ProfileRepository

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var profileRepo: ProfileRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileRepo = ProfileRepository(this)

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, ProfileSetupActivity::class.java))
        }

        binding.btnFillNow.setOnClickListener {
            if (isAccessibilityEnabled()) {
                AutoFillAccessibilityService.instance?.triggerFill()
                Toast.makeText(this, "✅ তথ্য পূরণ শুরু হয়েছে!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "⚠️ Accessibility Service চালু করুন", Toast.LENGTH_LONG).show()
                openAccessibilitySettings()
            }
        }

        binding.btnAccessibility.setOnClickListener {
            openAccessibilitySettings()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val profile = profileRepo.getProfile()
        val hasProfile = profileRepo.hasProfile()
        val accessEnabled = isAccessibilityEnabled()

        // Profile status
        if (hasProfile) {
            binding.tvProfileStatus.text = "✅ প্রোফাইল সেট করা আছে"
            binding.tvProfileStatus.setTextColor(getColor(R.color.green))
            binding.tvProfileName.text = profile.fullNameEn.ifEmpty { profile.fullNameBn }
            binding.tvProfileName.visibility = View.VISIBLE
        } else {
            binding.tvProfileStatus.text = "❌ প্রোফাইল সেট করা নেই"
            binding.tvProfileStatus.setTextColor(getColor(R.color.red))
            binding.tvProfileName.visibility = View.GONE
        }

        // Accessibility status
        if (accessEnabled) {
            binding.tvAccessibilityStatus.text = "✅ Accessibility Service চালু আছে"
            binding.tvAccessibilityStatus.setTextColor(getColor(R.color.green))
            binding.btnAccessibility.visibility = View.GONE
        } else {
            binding.tvAccessibilityStatus.text = "❌ Accessibility Service বন্ধ আছে"
            binding.tvAccessibilityStatus.setTextColor(getColor(R.color.red))
            binding.btnAccessibility.visibility = View.VISIBLE
        }

        // Fill button state
        binding.btnFillNow.isEnabled = hasProfile
        binding.btnFillNow.alpha = if (hasProfile) 1.0f else 0.5f
    }

    private fun isAccessibilityEnabled(): Boolean {
        val service = "${packageName}/${AutoFillAccessibilityService::class.java.canonicalName}"
        return try {
            val enabled = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabled)
            colonSplitter.any { it.equals(service, ignoreCase = true) }
        } catch (e: Exception) {
            false
        }
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        Toast.makeText(this, "'Gov Job Autofill' খুঁজে চালু করুন", Toast.LENGTH_LONG).show()
    }
}

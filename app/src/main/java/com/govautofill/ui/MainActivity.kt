package com.govautofill.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDex
import android.content.Context
import com.govautofill.R
import com.govautofill.databinding.ActivityMainBinding
import com.govautofill.utils.AdManager
import com.govautofill.utils.ProfileRepository

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var profileRepo: ProfileRepository

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        profileRepo = ProfileRepository(this)

        try {
            AdManager.initialize(this)
            AdManager.loadBanner(this, binding.adContainerMain)
            AdManager.loadInterstitial(this)
        } catch (e: Exception) { /* Ad init failure should not crash app */ }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, ProfileSetupActivity::class.java))
        }
        binding.btnOpenBrowser.setOnClickListener {
            if (!profileRepo.hasProfile()) {
                Toast.makeText(this, "প্রথমে প্রোফাইল সেট করুন!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, BrowserActivity::class.java))
        }
        binding.btnJobHistory.setOnClickListener {
            startActivity(Intent(this, JobHistoryActivity::class.java))
        }
        binding.btnMediaVault.setOnClickListener {
            startActivity(Intent(this, MediaVaultActivity::class.java))
        }
        binding.btnBpsc.setOnClickListener    { openSite("https://bpsc.teletalk.com.bd") }
        binding.btnTeletalk.setOnClickListener { openSite("https://career.teletalk.com.bd") }
        binding.btnEjobs.setOnClickListener   { openSite("https://ejobs.gov.bd") }
        binding.btnBb.setOnClickListener      { openSite("https://erecruitment.bb.org.bd") }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuHistory -> { startActivity(Intent(this, JobHistoryActivity::class.java)); true }
            R.id.menuBrowser -> { startActivity(Intent(this, BrowserActivity::class.java)); true }
            R.id.menuProfile -> { startActivity(Intent(this, ProfileSetupActivity::class.java)); true }
            R.id.menuMedia   -> { startActivity(Intent(this, MediaVaultActivity::class.java)); true }
            R.id.menuPrivacy -> { startActivity(Intent(this, PolicyActivity::class.java).putExtra("type", "privacy")); true }
            R.id.menuTerms   -> { startActivity(Intent(this, PolicyActivity::class.java).putExtra("type", "terms")); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openSite(url: String) {
        if (!profileRepo.hasProfile()) {
            Toast.makeText(this, "প্রথমে প্রোফাইল সেট করুন!", Toast.LENGTH_LONG).show()
            return
        }
        startActivity(Intent(this, BrowserActivity::class.java).putExtra(BrowserActivity.EXTRA_URL, url))
    }

    override fun onResume() {
        super.onResume()
        val profile = profileRepo.getProfile()
        val hasProfile = profileRepo.hasProfile()
        if (hasProfile) {
            binding.tvProfileStatus.text = "প্রোফাইল সেট করা আছে"
            binding.tvProfileStatus.setTextColor(getColor(R.color.green))
            binding.tvProfileName.text = profile.fullNameEn.ifEmpty { profile.fullNameBn }
            binding.tvProfileName.visibility = View.VISIBLE
        } else {
            binding.tvProfileStatus.text = "প্রোফাইল সেট করা নেই"
            binding.tvProfileStatus.setTextColor(getColor(R.color.red))
            binding.tvProfileName.visibility = View.GONE
        }
        binding.btnOpenBrowser.isEnabled = hasProfile
        binding.btnOpenBrowser.alpha = if (hasProfile) 1.0f else 0.5f
    }
}

package com.govautofill.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.govautofill.R
import com.govautofill.databinding.ActivityBrowserBinding
import com.govautofill.utils.BookmarkManager
import com.govautofill.utils.JsFormFiller
import com.govautofill.utils.ProfileRepository

class BrowserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrowserBinding
    private lateinit var profileRepo: ProfileRepository
    private lateinit var bookmarkManager: BookmarkManager

    companion object {
        const val EXTRA_URL = "extra_url"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileRepo = ProfileRepository(this)
        bookmarkManager = BookmarkManager(this)

        setupWebView()
        setupUrlBar()
        setupButtons()

        val startUrl = intent.getStringExtra(EXTRA_URL) ?: "https://ejobs.gov.bd"
        binding.webView.loadUrl(startUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        with(binding.webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                binding.progressBar.isVisible = true
                binding.etUrl.setText(url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.isVisible = false
                binding.etUrl.setText(url)
                // Show fill button if page has input fields
                detectFormAndShowButton(url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progressBar.progress = newProgress
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                supportActionBar?.title = title ?: "Browser"
            }
        }
    }

    private fun detectFormAndShowButton(url: String?) {
        binding.webView.evaluateJavascript(
            "document.querySelectorAll('input[type=\"text\"], input[type=\"email\"], input[type=\"tel\"], input[type=\"number\"], input:not([type])').length"
        ) { result ->
            val count = result?.trim()?.toIntOrNull() ?: 0
            runOnUiThread {
                binding.fabFill.isVisible = count > 0
            }
        }
    }

    private fun setupUrlBar() {
        binding.etUrl.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                loadUserUrl()
                true
            } else false
        }

        binding.btnGo.setOnClickListener { loadUserUrl() }
    }

    private fun loadUserUrl() {
        var url = binding.etUrl.text.toString().trim()
        if (url.isEmpty()) return
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = if (url.contains(".")) "https://$url"
            else "https://www.google.com/search?q=$url"
        }
        binding.webView.loadUrl(url)
        hideKeyboard()
    }

    private fun setupButtons() {
        // Back
        binding.btnBack.setOnClickListener {
            if (binding.webView.canGoBack()) binding.webView.goBack()
            else finish()
        }

        // Forward
        binding.btnForward.setOnClickListener {
            if (binding.webView.canGoForward()) binding.webView.goForward()
        }

        // Refresh
        binding.btnRefresh.setOnClickListener {
            binding.webView.reload()
        }

        // Bookmarks
        binding.btnBookmarks.setOnClickListener {
            showBookmarksDialog()
        }

        // Add bookmark
        binding.btnAddBookmark.setOnClickListener {
            val url = binding.webView.url ?: return@setOnClickListener
            val title = binding.webView.title ?: url
            bookmarkManager.addBookmark(com.govautofill.utils.Bookmark(title, url))
            Toast.makeText(this, "✅ Bookmark যোগ হয়েছে!", Toast.LENGTH_SHORT).show()
        }

        // Floating Fill Button
        binding.fabFill.setOnClickListener {
            fillForm()
        }

        // Home
        binding.btnHome.setOnClickListener {
            finish()
        }
    }

    private fun fillForm() {
        val profile = profileRepo.getProfile()
        if (profile.fullNameEn.isEmpty() && profile.nidNo.isEmpty()) {
            Toast.makeText(this, "⚠️ প্রথমে প্রোফাইল সেট করুন!", Toast.LENGTH_LONG).show()
            return
        }

        val js = JsFormFiller.buildScript(profile)
        binding.webView.evaluateJavascript(js) { result ->
            runOnUiThread {
                val msg = result?.replace("\"", "") ?: "পূরণ সম্পন্ন"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

                // Flash the fill button to confirm
                binding.fabFill.setBackgroundColor(Color.parseColor("#2E7D32"))
                binding.fabFill.postDelayed({
                    binding.fabFill.setBackgroundColor(Color.parseColor("#1565C0"))
                }, 800)
            }
        }
    }

    private fun showBookmarksDialog() {
        val bookmarks = bookmarkManager.getBookmarks()
        val titles = bookmarks.map { "${it.emoji} ${it.title}\n${it.url}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("📌 Bookmarks")
            .setItems(titles) { _, which ->
                binding.webView.loadUrl(bookmarks[which].url)
            }
            .setNegativeButton("বন্ধ করুন", null)
            .setNeutralButton("Edit") { _, _ -> showEditBookmarksDialog() }
            .show()
    }

    private fun showEditBookmarksDialog() {
        val bookmarks = bookmarkManager.getBookmarks()
        val titles = bookmarks.map { "❌ ${it.emoji} ${it.title}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Bookmark মুছুন")
            .setItems(titles) { _, which ->
                bookmarkManager.removeBookmark(bookmarks[which].url)
                Toast.makeText(this, "Bookmark মুছে গেছে", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("বন্ধ করুন", null)
            .show()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etUrl.windowToken, 0)
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) binding.webView.goBack()
        else super.onBackPressed()
    }
}

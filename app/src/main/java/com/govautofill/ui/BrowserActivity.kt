package com.govautofill.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.govautofill.databinding.ActivityBrowserBinding
import com.govautofill.utils.Bookmark
import com.govautofill.utils.BookmarkManager
import com.govautofill.utils.JsFormFiller
import com.govautofill.utils.ProfileRepository

class BrowserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrowserBinding
    private lateinit var profileRepo: ProfileRepository
    private lateinit var bookmarkManager: BookmarkManager
    private var webViewReady = false

    companion object {
        const val EXTRA_URL = "extra_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding inflate — try/catch যাতে inflate error-এও crash না হয়
        try {
            binding = ActivityBrowserBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } catch (e: Exception) {
            Toast.makeText(this, "Browser load error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

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
        try {
            with(binding.webView.settings) {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        } catch (e: Exception) { /* ignore settings errors */ }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                try {
                    webViewReady = false
                    binding.progressBar.visibility = View.VISIBLE
                    binding.etUrl.setText(url)
                } catch (e: Exception) { }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                try {
                    webViewReady = true
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.etUrl.setText(url)
                    detectFormAndShowButton()
                } catch (e: Exception) { }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = false

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                try { binding.progressBar.visibility = View.INVISIBLE } catch (e: Exception) { }
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                try { binding.progressBar.progress = newProgress } catch (e: Exception) { }
            }
        }
    }

    private fun detectFormAndShowButton() {
        if (!webViewReady) return
        try {
            binding.webView.evaluateJavascript(
                "(function(){ try { return document.querySelectorAll('input[type=\"text\"],input[type=\"email\"],input[type=\"tel\"],input[type=\"number\"],input:not([type])').length; } catch(e){ return 0; } })()"
            ) { result ->
                runOnUiThread {
                    try {
                        val count = result?.trim()?.toIntOrNull() ?: 0
                        binding.fabFill.visibility = if (count > 0) View.VISIBLE else View.GONE
                    } catch (e: Exception) { }
                }
            }
        } catch (e: Exception) { }
    }

    private fun setupUrlBar() {
        // Text change listener to preserve typed URL even if focus is lost
        binding.etUrl.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etUrl.tag = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.etUrl.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                loadUserUrl(); true
            } else false
        }

        binding.btnGo.setOnClickListener {
            // Restore text from tag if focus loss cleared it
            val saved = (binding.etUrl.tag as? String) ?: ""
            if (saved.isNotEmpty() && binding.etUrl.text.toString().trim().isEmpty()) {
                binding.etUrl.setText(saved)
            }
            loadUserUrl()
        }
    }

    private fun loadUserUrl() {
        try {
            var url = binding.etUrl.text.toString().trim()
            if (url.isEmpty()) {
                url = (binding.etUrl.tag as? String)?.trim() ?: ""
            }
            if (url.isEmpty()) return
            url = when {
                url.startsWith("http://") || url.startsWith("https://") -> url
                url.contains(".") -> "https://$url"
                else -> "https://www.google.com/search?q=${url.replace(" ", "+")}"
            }
            binding.webView.loadUrl(url)
            hideKeyboard()
        } catch (e: Exception) { }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            try {
                if (binding.webView.canGoBack()) binding.webView.goBack() else finish()
            } catch (e: Exception) { finish() }
        }
        binding.btnForward.setOnClickListener {
            try { if (binding.webView.canGoForward()) binding.webView.goForward() } catch (e: Exception) { }
        }
        binding.btnRefresh.setOnClickListener {
            try { binding.webView.reload() } catch (e: Exception) { }
        }
        binding.btnHome.setOnClickListener { finish() }
        binding.btnBookmarks.setOnClickListener { showBookmarksDialog() }
        binding.btnAddBookmark.setOnClickListener {
            try {
                val url = binding.webView.url ?: return@setOnClickListener
                val title = binding.webView.title ?: url
                bookmarkManager.addBookmark(Bookmark(title, url))
                Toast.makeText(this, "Bookmark যোগ হয়েছে", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) { }
        }
        binding.fabFill.setOnClickListener { fillForm() }
    }

    private fun fillForm() {
        try {
            val profile = profileRepo.getProfile()
            if (profile.fullNameEn.isEmpty() && profile.nidNo.isEmpty()) {
                Toast.makeText(this, "প্রথমে প্রোফাইল সেট করুন", Toast.LENGTH_LONG).show()
                return
            }
            val js = JsFormFiller.buildScript(profile)
            binding.webView.evaluateJavascript(js) { result ->
                runOnUiThread {
                    try {
                        val msg = result?.trim()?.replace("\"", "") ?: "পূরণ সম্পন্ন"
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) { }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showBookmarksDialog() {
        try {
            val bookmarks = bookmarkManager.getBookmarks()
            if (bookmarks.isEmpty()) {
                Toast.makeText(this, "কোনো bookmark নেই", Toast.LENGTH_SHORT).show()
                return
            }
            val titles = bookmarks.map { it.title }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Bookmarks")
                .setItems(titles) { _, which ->
                    try { binding.webView.loadUrl(bookmarks[which].url) } catch (e: Exception) { }
                }
                .setNegativeButton("বন্ধ", null)
                .setNeutralButton("Edit") { _, _ ->
                    val editTitles = bookmarks.map { "X  ${it.title}" }.toTypedArray()
                    AlertDialog.Builder(this)
                        .setTitle("Bookmark মুছুন")
                        .setItems(editTitles) { _, i ->
                            bookmarkManager.removeBookmark(bookmarks[i].url)
                            Toast.makeText(this, "মুছে গেছে", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("বন্ধ", null).show()
                }
                .show()
        } catch (e: Exception) { }
    }

    private fun hideKeyboard() {
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etUrl.windowToken, 0)
        } catch (e: Exception) { }
    }

    override fun onBackPressed() {
        try {
            if (binding.webView.canGoBack()) binding.webView.goBack()
            else super.onBackPressed()
        } catch (e: Exception) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        try {
            binding.webView.stopLoading()
            binding.webView.loadUrl("about:blank")
            binding.webView.destroy()
        } catch (e: Exception) { }
        super.onDestroy()
    }
}

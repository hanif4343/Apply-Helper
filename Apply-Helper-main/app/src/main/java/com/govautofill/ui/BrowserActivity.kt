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
                binding.progressBar.visibility = View.VISIBLE
                binding.etUrl.setText(url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.visibility = View.INVISIBLE
                binding.etUrl.setText(url)
                detectFormAndShowButton()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = false

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                binding.progressBar.visibility = View.INVISIBLE
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                try { binding.progressBar.progress = newProgress } catch (e: Exception) { }
            }
        }
    }

    private fun detectFormAndShowButton() {
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
        binding.etUrl.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                loadUserUrl(); true
            } else false
        }
        binding.btnGo.setOnClickListener { loadUserUrl() }
    }

    private fun loadUserUrl() {
        var url = binding.etUrl.text.toString().trim()
        if (url.isEmpty()) return
        url = when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.contains(".") -> "https://$url"
            else -> "https://www.google.com/search?q=${url.replace(" ", "+")}"
        }
        binding.webView.loadUrl(url)
        hideKeyboard()
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            if (binding.webView.canGoBack()) binding.webView.goBack() else finish()
        }
        binding.btnForward.setOnClickListener {
            if (binding.webView.canGoForward()) binding.webView.goForward()
        }
        binding.btnRefresh.setOnClickListener { binding.webView.reload() }
        binding.btnHome.setOnClickListener { finish() }
        binding.btnBookmarks.setOnClickListener { showBookmarksDialog() }
        binding.btnAddBookmark.setOnClickListener {
            val url = binding.webView.url ?: return@setOnClickListener
            val title = binding.webView.title ?: url
            bookmarkManager.addBookmark(Bookmark(title, url))
            Toast.makeText(this, "✅ Bookmark যোগ হয়েছে!", Toast.LENGTH_SHORT).show()
        }
        binding.fabFill.setOnClickListener { fillForm() }
    }

    private fun fillForm() {
        val profile = profileRepo.getProfile()
        if (profile.fullNameEn.isEmpty() && profile.nidNo.isEmpty()) {
            Toast.makeText(this, "⚠️ প্রথমে প্রোফাইল সেট করুন!", Toast.LENGTH_LONG).show()
            return
        }
        try {
            val js = JsFormFiller.buildScript(profile)
            binding.webView.evaluateJavascript(js) { result ->
                runOnUiThread {
                    val msg = result?.trim()?.replace("\"", "") ?: "পূরণ সম্পন্ন"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showBookmarksDialog() {
        val bookmarks = bookmarkManager.getBookmarks()
        if (bookmarks.isEmpty()) {
            Toast.makeText(this, "কোনো bookmark নেই", Toast.LENGTH_SHORT).show()
            return
        }
        val titles = bookmarks.map { "${it.emoji} ${it.title}" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("📌 Bookmarks")
            .setItems(titles) { _, which -> binding.webView.loadUrl(bookmarks[which].url) }
            .setNegativeButton("বন্ধ", null)
            .setNeutralButton("Edit") { _, _ ->
                val editTitles = bookmarks.map { "❌ ${it.emoji} ${it.title}" }.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle("Bookmark মুছুন")
                    .setItems(editTitles) { _, i ->
                        bookmarkManager.removeBookmark(bookmarks[i].url)
                        Toast.makeText(this, "মুছে গেছে", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("বন্ধ", null).show()
            }
            .show()
    }

    private fun hideKeyboard() {
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etUrl.windowToken, 0)
        } catch (e: Exception) { }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) binding.webView.goBack()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        try { binding.webView.stopLoading(); binding.webView.destroy() } catch (e: Exception) { }
        super.onDestroy()
    }
}

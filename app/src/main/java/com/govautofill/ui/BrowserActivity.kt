package com.govautofill.ui

import android.annotation.SuppressLint
import android.content.Context
import android.net.http.SslError
import android.os.Bundle
import android.view.View
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
    private var lastTypedUrl = ""

    companion object {
        const val EXTRA_URL = "extra_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setupSearchBar()
        setupButtons()

        val startUrl = intent.getStringExtra(EXTRA_URL) ?: "https://www.google.com"
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
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            // Chrome UA — govt site গুলো এতে ঠিকমতো respond করে
            userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
        }

        binding.webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                webViewReady = false
                binding.progressBar.visibility = View.VISIBLE
                // user typing না করলেই URL bar আপডেট করো
                if (lastTypedUrl.isEmpty()) {
                    binding.etUrl.setText(url)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                webViewReady = true
                lastTypedUrl = ""
                binding.progressBar.visibility = View.INVISIBLE
                binding.etUrl.setText(url)
                detectFormAndShowButton()
            }

            // সব link একই WebView এ খোলো
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                view?.loadUrl(url)
                return true
            }

            // SSL certificate error — govt site গুলোর জন্য proceed
            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                binding.progressBar.visibility = View.INVISIBLE
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progressBar.progress = newProgress
            }
        }
    }

    private fun setupSearchBar() {
        // EditText এ click করলে সব text select
        binding.etUrl.setOnClickListener {
            binding.etUrl.selectAll()
        }

        // Keyboard এর Go বাটন
        binding.etUrl.setOnEditorActionListener { _, _, _ ->
            navigateTo(binding.etUrl.text.toString())
            true
        }

        // Go বাটন
        binding.btnGo.setOnClickListener {
            navigateTo(binding.etUrl.text.toString())
        }
    }

    private fun navigateTo(input: String) {
        val text = input.trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "কিছু লিখুন", Toast.LENGTH_SHORT).show()
            return
        }

        val url = when {
            text.startsWith("http://") || text.startsWith("https://") -> text
            text.contains(".") && !text.contains(" ") -> "https://$text"
            else -> "https://www.google.com/search?q=${text.replace(" ", "+")}"
        }

        lastTypedUrl = url
        hideKeyboard()
        binding.webView.loadUrl(url)
    }

    private fun detectFormAndShowButton() {
        if (!webViewReady) return
        try {
            binding.webView.evaluateJavascript(
                "(function(){ try { return document.querySelectorAll('input[type=\"text\"],input[type=\"email\"],input[type=\"tel\"],input[type=\"number\"],input:not([type])').length; } catch(e){ return 0; } })()"
            ) { result ->
                runOnUiThread {
                    val count = result?.trim()?.toIntOrNull() ?: 0
                    binding.fabFill.visibility = if (count > 0) View.VISIBLE else View.GONE
                }
            }
        } catch (e: Exception) { }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            if (binding.webView.canGoBack()) binding.webView.goBack() else finish()
        }
        binding.btnForward.setOnClickListener {
            if (binding.webView.canGoForward()) binding.webView.goForward()
        }
        binding.btnRefresh.setOnClickListener {
            binding.webView.reload()
        }
        binding.btnHome.setOnClickListener { finish() }
        binding.btnBookmarks.setOnClickListener { showBookmarksDialog() }
        binding.btnAddBookmark.setOnClickListener {
            val url = binding.webView.url ?: return@setOnClickListener
            val title = binding.webView.title ?: url
            bookmarkManager.addBookmark(Bookmark(title, url))
            Toast.makeText(this, "Bookmark যোগ হয়েছে", Toast.LENGTH_SHORT).show()
        }
        binding.fabFill.setOnClickListener { fillForm() }
    }

    private fun fillForm() {
        val profile = profileRepo.getProfile()
        if (profile.fullNameEn.isEmpty() && profile.nidNo.isEmpty()) {
            Toast.makeText(this, "প্রথমে প্রোফাইল সেট করুন", Toast.LENGTH_LONG).show()
            return
        }
        val js = JsFormFiller.buildScript(profile)
        binding.webView.evaluateJavascript(js) { result ->
            runOnUiThread {
                val msg = result?.trim()?.replace("\"", "") ?: "পূরণ সম্পন্ন"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBookmarksDialog() {
        val bookmarks = bookmarkManager.getBookmarks()
        if (bookmarks.isEmpty()) {
            Toast.makeText(this, "কোনো bookmark নেই", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Bookmarks")
            .setItems(bookmarks.map { it.title }.toTypedArray()) { _, which ->
                binding.webView.loadUrl(bookmarks[which].url)
            }
            .setNegativeButton("বন্ধ", null)
            .setNeutralButton("Edit") { _, _ ->
                val bm = bookmarkManager.getBookmarks()
                AlertDialog.Builder(this)
                    .setTitle("Bookmark মুছুন")
                    .setItems(bm.map { "❌ ${it.title}" }.toTypedArray()) { _, i ->
                        bookmarkManager.removeBookmark(bm[i].url)
                        Toast.makeText(this, "মুছে গেছে", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("বন্ধ", null).show()
            }
            .show()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etUrl.windowToken, 0)
        binding.etUrl.clearFocus()
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) binding.webView.goBack()
        else super.onBackPressed()
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

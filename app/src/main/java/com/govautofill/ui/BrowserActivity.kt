package com.govautofill.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.Button
import android.widget.EditText
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
        } catch (e: Exception) { }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                webViewReady = false
                binding.progressBar.visibility = View.VISIBLE
                // শুধু page load এর সময় URL bar আপডেট করো — user typing এর সময় না
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                webViewReady = true
                binding.progressBar.visibility = View.INVISIBLE
                if (!binding.etUrl.isFocused) {
                    binding.etUrl.setText(url)
                }
                detectFormAndShowButton()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = false

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
        // Go বাটনে click করলে — EditText এর current text সরাসরি পড়ে load করো
        binding.btnGo.setOnClickListener {
            navigateToInput()
        }

        // Keyboard এর "Go" / "Done" বাটনেও কাজ করবে
        binding.etUrl.setOnEditorActionListener { v, _, _ ->
            navigateToInput()
            true
        }

        // EditText এ click করলে সব text select হবে — সহজে মুছে নতুন লেখা যাবে
        binding.etUrl.setOnClickListener {
            binding.etUrl.selectAll()
        }
    }

    private fun navigateToInput() {
        val input = binding.etUrl.text?.toString()?.trim() ?: ""
        if (input.isEmpty()) {
            Toast.makeText(this, "কিছু লিখুন", Toast.LENGTH_SHORT).show()
            return
        }

        val url = when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.contains(".") -> "https://$input"
            else -> "https://www.google.com/search?q=${input.replace(" ", "+")}"
        }

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
        val titles = bookmarks.map { it.title }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Bookmarks")
            .setItems(titles) { _, which ->
                binding.webView.loadUrl(bookmarks[which].url)
            }
            .setNegativeButton("বন্ধ", null)
            .setNeutralButton("Edit") { _, _ ->
                val editTitles = bookmarks.map { "❌ ${it.title}" }.toTypedArray()
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

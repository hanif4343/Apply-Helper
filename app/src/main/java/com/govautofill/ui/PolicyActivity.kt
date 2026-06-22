package com.govautofill.ui

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.govautofill.databinding.ActivityPolicyBinding

class PolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val type = intent.getStringExtra("type") ?: "privacy"
        when (type) {
            "privacy" -> {
                binding.tvTitle.text = "গোপনীয়তা নীতি"
                binding.webView.loadDataWithBaseURL(null, privacyPolicyHtml(), "text/html", "UTF-8", null)
            }
            "terms" -> {
                binding.tvTitle.text = "ব্যবহারের শর্তাবলী"
                binding.webView.loadDataWithBaseURL(null, termsHtml(), "text/html", "UTF-8", null)
            }
        }
    }

    private fun privacyPolicyHtml() = """
<!DOCTYPE html><html><head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style>
  body { font-family: sans-serif; padding: 16px; color: #212121; line-height: 1.6; }
  h1 { color: #1565C0; font-size: 20px; }
  h2 { color: #1565C0; font-size: 16px; margin-top: 20px; }
  p { font-size: 14px; }
  ul { font-size: 14px; }
</style>
</head><body>
<h1>গোপনীয়তা নীতি (Privacy Policy)</h1>
<p>সর্বশেষ আপডেট: জুন ২০২৫</p>

<h2>১. তথ্য সংগ্রহ</h2>
<p>সরকারি চাকরি অটোফিল অ্যাপটি ব্যবহারকারীর নিচের তথ্য <strong>শুধুমাত্র ডিভাইসে</strong> সংরক্ষণ করে:</p>
<ul>
  <li>ব্যক্তিগত তথ্য (নাম, জন্মতারিখ, NID নম্বর, ঠিকানা)</li>
  <li>শিক্ষাগত তথ্য (SSC, HSC, স্নাতক)</li>
  <li>ছবি ও সিগনেচার</li>
  <li>চাকরির আবেদনের ইতিহাস</li>
</ul>
<p><strong>এই তথ্যের কোনোটিই কোনো সার্ভারে বা তৃতীয় পক্ষের কাছে পাঠানো হয় না।</strong></p>

<h2>২. ইন্টারনেট অনুমতি</h2>
<p>অ্যাপটি শুধুমাত্র নিচের কাজে ইন্টারনেট ব্যবহার করে:</p>
<ul>
  <li>Built-in browser-এ সরকারি ওয়েবসাইট লোড করা</li>
  <li>Google AdMob-এর মাধ্যমে বিজ্ঞাপন দেখানো</li>
</ul>

<h2>৩. বিজ্ঞাপন (Advertisements)</h2>
<p>এই অ্যাপ Google AdMob ব্যবহার করে বিজ্ঞাপন দেখায়। AdMob-এর নিজস্ব গোপনীয়তা নীতি অনুযায়ী ব্যবহারকারীর ডিভাইস তথ্য ব্যবহার হতে পারে। বিস্তারিত জানতে: <a href="https://policies.google.com/privacy">Google Privacy Policy</a></p>

<h2>৪. ক্যামেরা ও স্টোরেজ অনুমতি</h2>
<p>ছবি ও সিগনেচার তোলার জন্য ক্যামেরা ও স্টোরেজ অনুমতি নেওয়া হয়। এই ছবি শুধু ডিভাইসে সংরক্ষিত থাকে।</p>

<h2>৫. Accessibility Service</h2>
<p>External browser-এ form fill করতে Accessibility Service ব্যবহার হয়। এটি শুধু আবেদন ফর্মের text field পূরণ করে — কোনো ব্যক্তিগত তথ্য রেকর্ড বা পাঠায় না।</p>

<h2>৬. তৃতীয় পক্ষ</h2>
<p>আমরা কোনো তৃতীয় পক্ষের analytics বা tracking SDK ব্যবহার করি না।</p>

<h2>৭. শিশুদের গোপনীয়তা</h2>
<p>এই অ্যাপ ১৩ বছরের কম বয়সী শিশুদের জন্য নয়।</p>

<h2>৮. যোগাযোগ</h2>
<p>যেকোনো প্রশ্নের জন্য: <strong>govautofill@gmail.com</strong></p>

</body></html>
    """.trimIndent()

    private fun termsHtml() = """
<!DOCTYPE html><html><head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style>
  body { font-family: sans-serif; padding: 16px; color: #212121; line-height: 1.6; }
  h1 { color: #1565C0; font-size: 20px; }
  h2 { color: #1565C0; font-size: 16px; margin-top: 20px; }
  p, ul { font-size: 14px; }
</style>
</head><body>
<h1>ব্যবহারের শর্তাবলী (Terms of Use)</h1>
<p>সর্বশেষ আপডেট: জুন ২০২৫</p>

<h2>১. সম্মতি</h2>
<p>এই অ্যাপ ব্যবহার করলে আপনি এই শর্তাবলীতে সম্মত বলে গণ্য হবেন।</p>

<h2>২. ব্যবহারের নিয়ম</h2>
<ul>
  <li>শুধুমাত্র নিজের সরকারি চাকরির আবেদনের জন্য ব্যবহার করুন</li>
  <li>অন্যের তথ্য দিয়ে আবেদন করা সম্পূর্ণ নিষিদ্ধ</li>
  <li>অ্যাপটি দিয়ে কোনো অবৈধ কাজ করা যাবে না</li>
</ul>

<h2>৩. দায়বদ্ধতা</h2>
<p>আবেদন ফর্ম সঠিকভাবে পূরণ হয়েছে কিনা যাচাই করার দায়িত্ব ব্যবহারকারীর। আমরা ভুল তথ্য পূরণের জন্য দায়ী নই।</p>

<h2>৪. পরিষেবা পরিবর্তন</h2>
<p>সরকারি ওয়েবসাইটের কাঠামো পরিবর্তন হলে auto-fill কাজ না-ও করতে পারে।</p>

<h2>৫. বিজ্ঞাপন</h2>
<p>অ্যাপটি বিজ্ঞাপন দেখায়। বিজ্ঞাপনে ক্লিক করা ঐচ্ছিক।</p>

<h2>৬. সমাপ্তি</h2>
<p>আমরা যেকোনো সময় পরিষেবা পরিবর্তন বা বন্ধ করার অধিকার রাখি।</p>

</body></html>
    """.trimIndent()
}

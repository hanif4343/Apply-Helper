# 🏛️ সরকারি চাকরি অটোফিল অ্যাপ

সরকারি চাকরির আবেদন ফর্ম **স্বয়ংক্রিয়ভাবে** পূরণ করার Android অ্যাপ।

---

## 📱 কীভাবে কাজ করে

1. **একবার** নিজের সব তথ্য সেভ করুন (নাম, ঠিকানা, শিক্ষা, NID ইত্যাদি)
2. Accessibility Service চালু রাখুন
3. যেকোনো সরকারি সাইটে ফর্ম খুলুন
4. অ্যাপ থেকে "ফর্ম পূরণ করুন" বাটন চাপুন → সব field স্বয়ংক্রিয়ভাবে পূরণ হবে!

---

## 🚀 GitHub থেকে APK বানানো (Phone থেকে)

### ধাপ ১: GitHub-এ Upload করুন
```
1. github.com এ নতুন Repository তৈরি করুন
2. এই folder-এর সব file upload করুন
3. Commit করুন
```

### ধাপ ২: Build চালান
```
1. Repository > Actions tab এ যান
2. "Build Android APK" workflow দেখবেন
3. "Run workflow" বাটন চাপুন
4. ৩-৫ মিনিট অপেক্ষা করুন
```

### ধাপ ৩: APK Download করুন
```
1. Actions > সফল run এ ক্লিক করুন
2. Artifacts section থেকে "GovJobAutofill-debug-apk" download করুন
3. Phone এ install করুন
```

---

## ⚙️ প্রথমবার Setup

1. APK install করুন
2. অ্যাপ খুলুন
3. **"প্রোফাইল এডিট করুন"** চাপুন — সব তথ্য দিন
4. **Settings > Accessibility > Gov Job Auto Fill** চালু করুন
5. ব্রাউজারে আবেদন ফর্ম খুলুন
6. অ্যাপে ফিরে **"ফর্ম পূরণ করুন"** চাপুন ✅

---

## 📋 যেসব তথ্য সেভ করা যায়

- নাম (বাংলা ও ইংরেজি), পিতা-মাতার নাম
- জন্ম তারিখ, NID, পাসপোর্ট
- মোবাইল, ইমেইল
- বর্তমান ও স্থায়ী ঠিকানা (গ্রাম, পোস্ট, উপজেলা, জেলা, বিভাগ)
- ধর্ম, রক্তের গ্রুপ, লিঙ্গ, কোটা
- SSC, HSC, স্নাতক তথ্য

---

## 🔧 Tech Stack

- **Language:** Kotlin
- **Min SDK:** Android 8.0 (API 26)
- **Core:** Accessibility Service API
- **Storage:** SharedPreferences (local, secure)
- **Build:** Gradle 8.4

---

## ⚠️ গুরুত্বপূর্ণ নোট

- তথ্য শুধু আপনার ফোনেই সংরক্ষিত থাকে, কোথাও পাঠানো হয় না
- Accessibility permission শুধু form fill এর জন্য ব্যবহার হয়
- কিছু সাইটে dropdown/radio button নিজে select করতে হবে (text field গুলো automatic হবে)

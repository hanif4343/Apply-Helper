package com.govautofill.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.govautofill.model.ProfileEntry
import com.govautofill.model.UserProfile

class ProfileRepository(context: Context) {
    private val prefs = context.getSharedPreferences("gov_autofill_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_ENTRIES = "profile_entries"
        private const val KEY_ACTIVE_ID = "active_profile_id"
        private const val KEY_LEGACY_SINGLE = "user_profile" // আগের ভার্সনের single-profile key
    }

    init {
        migrateLegacyProfileIfNeeded()
    }

    // ── পুরোনো single-profile ফরম্যাট থেকে নতুন multi-profile ফরম্যাটে এক-বার migrate ──
    // (আপডেট করার পরেও আগের প্রোফাইল হারিয়ে যাবে না)
    private fun migrateLegacyProfileIfNeeded() {
        if (getAll().isNotEmpty()) return
        val legacyJson = prefs.getString(KEY_LEGACY_SINGLE, null) ?: return
        try {
            val legacyProfile = gson.fromJson(legacyJson, UserProfile::class.java) ?: return
            if (legacyProfile.fullNameEn.isNotEmpty() || legacyProfile.fullNameBn.isNotEmpty() || legacyProfile.nidNo.isNotEmpty()) {
                val label = legacyProfile.fullNameEn.ifEmpty { legacyProfile.fullNameBn.ifEmpty { "আমার প্রোফাইল" } }
                save(ProfileEntry(label = label, profile = legacyProfile))
            }
        } catch (e: Exception) { /* corrupt legacy data থাকলে ignore করি, crash করবে না */ }
    }

    // ── CRUD ────────────────────────────────────────────────────────────────
    fun getAll(): List<ProfileEntry> {
        val json = prefs.getString(KEY_ENTRIES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ProfileEntry>>() {}.type
            gson.fromJson<List<ProfileEntry>>(json, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    fun save(entry: ProfileEntry) {
        val list = getAll().toMutableList()
        val updated = entry.copy(updatedAt = System.currentTimeMillis())
        val idx = list.indexOfFirst { it.id == updated.id }
        if (idx >= 0) list[idx] = updated else list.add(updated)
        prefs.edit().putString(KEY_ENTRIES, gson.toJson(list)).apply()
        // এটাই প্রথম প্রোফাইল হলে স্বয়ংক্রিয়ভাবে active করে দেই
        if (getActiveProfileId() == null) setActiveProfileId(updated.id)
    }

    fun delete(id: String) {
        val list = getAll().toMutableList()
        list.removeAll { it.id == id }
        prefs.edit().putString(KEY_ENTRIES, gson.toJson(list)).apply()
        if (getActiveProfileId() == id) {
            prefs.edit().remove(KEY_ACTIVE_ID).apply()
            list.firstOrNull()?.let { setActiveProfileId(it.id) }
        }
    }

    fun getById(id: String): ProfileEntry? = getAll().find { it.id == id }

    // ── Active profile (যেটা দিয়ে এখন form fill হবে) ──────────────────────────
    fun setActiveProfileId(id: String) {
        prefs.edit().putString(KEY_ACTIVE_ID, id).apply()
    }

    fun getActiveProfileId(): String? = prefs.getString(KEY_ACTIVE_ID, null)

    fun getActiveEntry(): ProfileEntry? {
        val id = getActiveProfileId()
        return (id?.let { getById(it) }) ?: getAll().firstOrNull()
    }

    /** JsFormFiller / BrowserActivity এর জন্য — সবসময় active profile-এর ডেটা রিটার্ন করে */
    fun getActiveProfile(): UserProfile = getActiveEntry()?.profile ?: UserProfile()

    // পুরোনো কোড compatibility-র জন্য alias (আগে এই নামে call হতো)
    fun getProfile(): UserProfile = getActiveProfile()

    fun hasProfile(): Boolean {
        val p = getActiveProfile()
        return p.fullNameEn.isNotEmpty() || p.nidNo.isNotEmpty()
    }

    // ── Local file backup/restore — কোনো ইন্টারনেট/cloud লাগে না ───────────────
    // ব্যাকআপ ফাইলটা ফোনের যেকোনো জায়গায় (যেমন Downloads) সেভ হয়, app uninstall করলেও
    // ওই ফাইল থাকে — পরে সেটা থেকে restore করা যায়।
    data class BackupPayload(
        val profiles: List<ProfileEntry>,
        val activeId: String?,
        val exportedAt: Long,
        val appVersion: Int = 1
    )

    fun exportBackupJson(): String {
        val payload = BackupPayload(getAll(), getActiveProfileId(), System.currentTimeMillis())
        return gson.toJson(payload)
    }

    /**
     * @param replaceExisting true হলে আগের সব প্রোফাইল মুছে শুধু backup থেকে রাখবে,
     *                         false হলে আগের প্রোফাইলগুলোর সাথে নতুনগুলো যোগ হবে
     * @return কতগুলো প্রোফাইল import হলো
     */
    fun importBackupJson(json: String, replaceExisting: Boolean): Int {
        val type = object : TypeToken<BackupPayload>() {}.type
        val payload: BackupPayload = try {
            gson.fromJson(json, type) ?: throw IllegalArgumentException("খালি ফাইল")
        } catch (e: Exception) {
            throw IllegalArgumentException("ব্যাকআপ ফাইলটি পড়া যাচ্ছে না — এটা কি সঠিক backup ফাইল?")
        }

        if (replaceExisting) {
            prefs.edit().remove(KEY_ENTRIES).remove(KEY_ACTIVE_ID).apply()
        }

        var importedCount = 0
        payload.profiles.forEachIndexed { i, incoming ->
            // id collision এড়াতে নতুন id বরাদ্দ করি, একই ব্যাকআপ বারবার import করলেও ডুপ্লিকেট প্রোফাইল
            // বানাবে কিন্তু পুরোনোগুলো ভেঙে যাবে না
            val newEntry = incoming.copy(id = "${System.currentTimeMillis()}_$i")
            save(newEntry)
            importedCount++
        }
        return importedCount
    }
}

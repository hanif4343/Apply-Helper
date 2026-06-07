package com.govautofill.utils

import android.content.Context
import com.govautofill.model.UserProfile

class ProfileRepository(context: Context) {
    private val prefs = context.getSharedPreferences("gov_autofill_prefs", Context.MODE_PRIVATE)

    fun saveProfile(profile: UserProfile) {
        prefs.edit().putString("user_profile", profile.toJson()).apply()
    }

    fun getProfile(): UserProfile {
        val json = prefs.getString("user_profile", null)
        return if (json != null) UserProfile.fromJson(json) else UserProfile()
    }

    fun hasProfile(): Boolean {
        val p = getProfile()
        return p.fullNameEn.isNotEmpty() || p.nidNo.isNotEmpty()
    }
}

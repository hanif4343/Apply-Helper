package com.govautofill.model

/**
 * একটা single profile-কে wrap করে — কারণ এখন একাধিক প্রোফাইল রাখা যাবে
 * (যেমন: নিজের, ভাইয়ের, স্ত্রীর প্রোফাইল আলাদা আলাদা সেভ থাকবে)।
 */
data class ProfileEntry(
    val id: String = System.currentTimeMillis().toString(),
    var label: String = "",            // যেমন: "নিজের প্রোফাইল", "ভাইয়ের প্রোফাইল"
    var profile: UserProfile = UserProfile(),
    var updatedAt: Long = System.currentTimeMillis()
)

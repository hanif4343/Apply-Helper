package com.govautofill.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Bookmark(val title: String, val url: String, val emoji: String = "🏛️")

class BookmarkManager(context: Context) {
    private val prefs = context.getSharedPreferences("gov_bookmarks", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val defaultBookmarks = listOf(
        Bookmark("Social", "https://dss.teletalk.com.bd/", "📋"),
        Bookmark("Teletalk Jobs", "https://career.teletalk.com.bd", "📱"),
        Bookmark("Bangladesh Bank", "https://erecruitment.bb.org.bd", "🏦"),
        Bookmark("PSC (পিএসসি)", "https://psc.teletalk.com.bd", "🏛️"),
        Bookmark("BRTA", "https://brta.teletalk.com.bd", "🚗"),
        Bookmark("Sonali Bank", "https://career.sonalibank.com.bd", "💰"),
        Bookmark("সরকারি চাকরি", "https://ejobs.gov.bd", "🇧🇩"),
        Bookmark("NSI", "https://nsi.teletalk.com.bd", "🔐")
    )

    fun getBookmarks(): List<Bookmark> {
        val json = prefs.getString("bookmarks", null)
        return if (json != null) {
            val type = object : TypeToken<List<Bookmark>>() {}.type
            gson.fromJson(json, type)
        } else {
            saveBookmarks(defaultBookmarks)
            defaultBookmarks
        }
    }

    fun saveBookmarks(bookmarks: List<Bookmark>) {
        prefs.edit().putString("bookmarks", gson.toJson(bookmarks)).apply()
    }

    fun addBookmark(bookmark: Bookmark) {
        val list = getBookmarks().toMutableList()
        if (list.none { it.url == bookmark.url }) {
            list.add(bookmark)
            saveBookmarks(list)
        }
    }

    fun removeBookmark(url: String) {
        val list = getBookmarks().toMutableList()
        list.removeAll { it.url == url }
        saveBookmarks(list)
    }
}

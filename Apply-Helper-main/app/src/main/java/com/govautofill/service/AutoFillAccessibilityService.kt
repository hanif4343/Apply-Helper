package com.govautofill.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.govautofill.utils.FieldMatcher
import com.govautofill.utils.ProfileRepository

class AutoFillAccessibilityService : AccessibilityService() {

    private lateinit var profileRepo: ProfileRepository
    private var isEnabled = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        profileRepo = ProfileRepository(applicationContext)
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
        info.notificationTimeout = 500
        serviceInfo = info
        isEnabled = true
        Log.d("GovAutoFill", "Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isEnabled) return
        val profile = profileRepo.getProfile()
        if (profile.fullNameEn.isEmpty() && profile.nidNo.isEmpty()) return

        val rootNode = rootInActiveWindow ?: return
        fillAllEditTexts(rootNode, profile)
    }

    private fun fillAllEditTexts(node: AccessibilityNodeInfo, profile: com.govautofill.model.UserProfile) {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            fillAllEditTexts(child, profile)
        }

        if (node.className?.contains("EditText") == true && node.isEditable) {
            val hints = buildList {
                node.hintText?.toString()?.let { add(it) }
                node.text?.toString()?.let { add(it) }
                node.contentDescription?.toString()?.let { add(it) }
                node.viewIdResourceName?.let { add(it) }
            }

            for (hint in hints) {
                val value = FieldMatcher.matchField(hint, profile)
                if (!value.isNullOrEmpty() && node.text?.toString() != value) {
                    val args = Bundle()
                    args.putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        value
                    )
                    node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                    Log.d("GovAutoFill", "Filled [$hint] -> $value")
                    break
                }
            }
        }
    }

    fun triggerFill() {
        val rootNode = rootInActiveWindow ?: return
        val profile = profileRepo.getProfile()
        fillAllEditTexts(rootNode, profile)
    }

    override fun onInterrupt() {
        isEnabled = false
        Log.d("GovAutoFill", "Service interrupted")
    }

    companion object {
        var instance: AutoFillAccessibilityService? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}

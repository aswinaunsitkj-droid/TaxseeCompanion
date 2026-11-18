package com.example.taxseecompanion

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class TaxseeAutoService : AccessibilityService() {

    companion object {
        private const val TAG = "TaxseeAutoService"
        private const val MAX_DISTANCE_KM = 1.0
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNode: AccessibilityNodeInfo = rootInActiveWindow ?: return

        if (isTargetDistanceValid(rootNode, MAX_DISTANCE_KM)) {
            Log.d(TAG, "Order dalam radius ditemukan! Mencari tombol 'Minta Order'...")
            val clicked = clickButtonByText(rootNode, "Minta Order")
            if (clicked) {
                Log.d(TAG, "Berhasil menekan tombol Minta Order secara otomatis.")
            }
        }
    }

    private fun isTargetDistanceValid(node: AccessibilityNodeInfo, maxKm: Double): Boolean {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val text = child.text?.toString()?.lowercase() ?: ""

            if (text.contains("km")) {
                val distanceValue = extractNumber(text)
                if (distanceValue != null && distanceValue <= maxKm) {
                    child.recycle()
                    return true
                }
            }
            if (isTargetDistanceValid(child, maxKm)) {
                child.recycle()
                return true
            }
            child.recycle()
        }
        return false
    }

    private fun extractNumber(s: String): Double? {
        val regex = Regex("""\d+(\.\d+)?""")
        val match = regex.find(s)
        return match?.value?.toDoubleOrNull()
    }

    private fun clickButtonByText(node: AccessibilityNodeInfo, buttonText: String): Boolean {
        val listNodes = node.findAccessibilityNodeInfosByText(buttonText)
        for (targetNode in listNodes) {
            var current: AccessibilityNodeInfo? = targetNode
            while (current != null) {
                if (current.isClickable) {
                    val success = current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    targetNode.recycle()
                    current.recycle()
                    return success
                }
                val parent = current.parent
                if (current != targetNode) {
                    current.recycle()
                }
                current = parent
            }
            targetNode.recycle()
        }
        return false
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service terganggu/dihentikan.")
    }
}

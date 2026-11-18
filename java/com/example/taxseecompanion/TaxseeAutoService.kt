package com.example.taxseecompanion

accessibilityservice.AccessibilityService
accessibilityservice.GestureDescription
graphics.Path
view.accessibility.AccessibilityEvent
view.accessibility.AccessibilityNodeInfo
util.Log

class TaxseeAutoService : AccessibilityService() {

    companion object {
        private const val TAG = "TaxseeAutoService"
        // Target jarak maksimal dalam kilometer (misal: 1.0 km)
        private const val MAX_DISTANCE_KM = 1.0
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNode: AccessibilityNodeInfo = rootInActiveWindow ?: return

        // 1. Cek apakah ada informasi jarak di layar (biasanya mengandung teks "km")
        if (isTargetDistanceValid(rootNode, MAX_DISTANCE_KM)) {
            Log.d(TAG, "Order dalam radius ditemukan! Mencari tombol 'Minta Order'...")
            
            // 2. Jika jarak memenuhi syarat, cari dan klik tombol "Minta Order"
            val clicked = clickButtonByText(rootNode, "Minta Order")
            if (clicked) {
                Log.d(TAG, "Berhasil menekan tombol Minta Order secara otomatis.")
            }
        }
    }

    // Fungsi untuk memindai node teks dan mengevaluasi apakah jaraknya <= 1 km
    private fun isTargetDistanceValid(node: AccessibilityNodeInfo, maxKm: Double): Boolean {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val text = child.text?.toString()?.lowercase() ?: ""

            // Contoh teks jarak di aplikasi: "0.5 km", "0.8 km", dll.
            if (text.contains("km")) {
                val distanceValue = extractNumber(text)
                if (distanceValue != null && distanceValue <= maxKm) {
                    child.recycle()
                    return true
                }
            }
            // Rekursif ke child berikutnya
            if (isTargetDistanceValid(child, maxKm)) {
                child.recycle()
                return true
            }
            child.recycle()
        }
        return false
    }

    // Helper untuk mengambil angka dari string (misal: "0.8 km" -> 0.8)
    private fun extractNumber(s: String): Double? {
        val regex = Regex("""\d+(\.\d+)?""")
        val match = regex.find(s)
        return match?.value?.toDoubleOrNull()
    }

    // Fungsi untuk mencari teks tombol dan melakukan klik
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

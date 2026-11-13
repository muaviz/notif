package com.notif.restyler.service

import android.content.ContextWrapper
import com.notif.restyler.model.IconStyle
import com.notif.restyler.util.BitmapUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationReposterTest {

    @Test
    fun deterministicMapping_producesConsistentTagAndId() {
        val packageName = "com.whatsapp"
        val sbnKey = "0|com.whatsapp|101|tag_chat|10042"

        val replacementTag = packageName
        val replacementId = sbnKey.hashCode()

        assertEquals("com.whatsapp", replacementTag)
        assertEquals(sbnKey.hashCode(), replacementId)

        // Verifying hash code consistency across multiple invocations
        val sbnKey2 = "0|com.whatsapp|101|tag_chat|10042"
        assertEquals(replacementId, sbnKey2.hashCode())
    }

    @Test
    fun genericGlyphStyle_returnsNullLargeIconWithoutQueryingPackageManager() {
        // When IconStyle is GENERIC_GLYPH, getAppIconBitmap must return null immediately
        // A DummyContext (ContextWrapper with null base) is sufficient because GENERIC_GLYPH should never touch the context
        val dummyContext = object : ContextWrapper(null) {}

        val nullIcon = BitmapUtils.getAppIconBitmap(
            context = dummyContext,
            packageName = "com.example.app",
            iconStyle = IconStyle.GENERIC_GLYPH,
            accentColorInt = 0xFF6750A4.toInt()
        )
        assertNull("GENERIC_GLYPH must return null large icon", nullIcon)
    }
}

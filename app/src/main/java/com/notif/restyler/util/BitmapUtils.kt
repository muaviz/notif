package com.notif.restyler.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.notif.restyler.model.IconStyle

object BitmapUtils {

    /**
     * Converts any Android [Drawable] into an ARGB_8888 [Bitmap] scaled to standard dimensions.
     */
    fun drawableToBitmap(drawable: Drawable, targetSizePx: Int = 192): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val src = drawable.bitmap
            if (src.width == targetSizePx && src.height == targetSizePx) {
                return src
            }
            return Bitmap.createScaledBitmap(src, targetSizePx, targetSizePx, true)
        }

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else targetSizePx
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else targetSizePx

        val finalWidth = minOf(width, targetSizePx)
        val finalHeight = minOf(height, targetSizePx)

        val bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Converts a full-color [Bitmap] to a grayscale / monochrome bitmap,
     * with an optional accent tint applied.
     */
    fun toMonochrome(source: Bitmap, tintColor: Int? = null): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val colorMatrix = ColorMatrix().apply {
            setSaturation(0f)
        }
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)

        if (tintColor != null) {
            val tintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.MULTIPLY)
            }
            canvas.drawBitmap(result, 0f, 0f, tintPaint)
        }

        return result
    }

    /**
     * Resolves the app icon bitmap for [packageName] based on user's [IconStyle] preference.
     * Returns null if [IconStyle.GENERIC_GLYPH] is selected or if icon resolution fails.
     */
    fun getAppIconBitmap(
        context: Context,
        packageName: String,
        iconStyle: IconStyle,
        accentColorInt: Int
    ): Bitmap? {
        if (iconStyle == IconStyle.GENERIC_GLYPH) return null

        val drawable = try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (_: Exception) {
            return null
        }

        val bitmap = drawableToBitmap(drawable)
        return when (iconStyle) {
            IconStyle.APP_ICON -> bitmap
            IconStyle.MONOCHROME_APP_ICON -> toMonochrome(bitmap, accentColorInt)
            IconStyle.GENERIC_GLYPH -> null
        }
    }
}

package com.bitcoin.merchant.app.util

import android.graphics.Bitmap
import android.graphics.Color
import com.bitcoin.merchant.app.model.Analytics
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class QrCodeUtil {
    companion object {
        @Throws(Exception::class)
        fun getBitmap(text: String, width: Int): Bitmap {
            try {
                val result: BitMatrix = try {
                    MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, width, null)
                } catch (e: Exception) {
                    throw Exception("Unsupported format", e)
                }
                val w = result.width
                val h = result.height
                val pixels = IntArray(w * h)
                for (y in 0 until h) {
                    val offset = y * w
                    for (x in 0 until w) {
                        pixels[offset + x] = if (result.get(x, y)) Color.BLACK else Color.WHITE
                    }
                }
                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                bitmap.setPixels(pixels, 0, width, 0, 0, w, h)
                return bitmap
            } catch (e: Exception) {
                Analytics.error_generate_qr_code.sendError(e)
                throw e
            }
        }
    }
}
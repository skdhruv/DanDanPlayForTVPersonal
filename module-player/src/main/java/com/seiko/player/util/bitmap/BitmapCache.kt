package com.seiko.player.util.bitmap

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.collection.LruCache

@SuppressLint("LogNotTimber")
object BitmapCache {

    private val mMemCache: LruCache<String, Bitmap> by lazy {
        // Use 16% of the available memory for this memory cache.
        val cacheSize = Runtime.getRuntime().maxMemory() / 6

        object : LruCache<String, Bitmap>(cacheSize.toInt()) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return value.rowBytes * value.height
            }
        }
    }

    @Synchronized
    fun getBitmapFromMemCache(key: String?): Bitmap? {
        if (key == null) return null
        val b = mMemCache.get(key)
        if (b == null) {
            mMemCache.remove(key)
            return null
        }
        return b
    }

    @Synchronized
    fun addBitmapToMemCache(key: String?, bitmap: Bitmap?) {
        if (key != null && bitmap != null && getBitmapFromMemCache(
                key
            ) == null) {
            mMemCache.put(key, bitmap)
        }
    }

    private fun getBitmapFromMemCache(resId: Int): Bitmap? {
        return getBitmapFromMemCache("res:$resId")
    }

    private fun addBitmapToMemCache(resId: Int, bitmap: Bitmap?) {
        addBitmapToMemCache("res:$resId", bitmap)
    }

    @Synchronized
    fun clear() {
        mMemCache.evictAll()
    }

    fun getFromResource(res: Resources, resId: Int): Bitmap? {
        var bitmap =
            getBitmapFromMemCache(resId)
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(res, resId)
            addBitmapToMemCache(
                resId,
                bitmap
            )
        }
        return bitmap
    }
}
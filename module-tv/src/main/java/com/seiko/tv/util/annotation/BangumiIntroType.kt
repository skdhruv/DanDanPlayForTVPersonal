package com.seiko.tv.util.annotation

import androidx.annotation.IntDef

/**
 * 相关作品、系列作品
 */
@IntDef(BangumiIntroType.RELATED, BangumiIntroType.SIMILAR)
@Retention(AnnotationRetention.SOURCE)
annotation class BangumiIntroType {
    companion object {
        const val RELATED = 1
        const val SIMILAR = 2
    }
}
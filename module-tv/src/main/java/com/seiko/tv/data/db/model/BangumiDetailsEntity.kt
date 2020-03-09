package com.seiko.tv.data.db.model

import androidx.room.*
import com.squareup.moshi.JsonClass

//BangumiDetails {
//    type (string): 作品类型 = ['tvseries', 'tvspecial', 'ova', 'movie', 'musicvideo', 'web', 'other', 'jpmovie', 'jpdrama', 'unknown'],
//    typeDescription (string, optional): 类型描述 ,
//    episodes (Array[BangumiEpisode], optional): 剧集列表 ,
//    summary (string, optional): 番剧简介 ,
//    bangumiUrl (string, optional): Bangumi.tv页面地址 ,
//    loushaoUrl (string, optional): 漏勺网页面地址 ,
//    userRating (integer): 用户个人评分（0-10） ,
//    favoriteStatus (string): 关注状态 = ['favorited', 'finished', 'abandoned'],
//    comment (string, optional): 用户对此番剧的备注/评论/标签 ,
//    ratingDetails (inline_model, optional): 各个站点的评分详情 ,
//    relateds (Array[BangumiIntro], optional): 与此作品直接关联的其他作品（例如同一作品的不同季、剧场版、OVA等） ,
//    similars (Array[BangumiIntro], optional): 与此作品相似的其他作品 ,
//    tags (Array[BangumiTag], optional): 标签列表 ,
//    animeId (integer): 作品编号 ,
//    animeTitle (string, optional): 作品标题 ,
//    imageUrl (string, optional): 海报图片地址 ,
//    searchKeyword (string, optional): 搜索关键词 ,
//    isOnAir (boolean): 是否正在连载中 ,
//    airDay (integer): 周几上映，0代表周日，1-6代表周一至周六 ,
//    isFavorited (boolean): 当前用户是否已关注（无论是否为已弃番等附加状态） ,
//    isRestricted (boolean): 是否为限制级别的内容（例如属于R18分级） ,
//    rating (number): 番剧综合评分（综合多个来源的评分求出的加权平均值，0-10分）
//}
@Entity(
    tableName = "BangumiDetails"
)
@JsonClass(generateAdapter = true)
data class BangumiDetailsEntity(
    @PrimaryKey(autoGenerate = false)
    var animeId: Long = 0,
    var animeTitle: String = "",
    var imageUrl: String = "",

    var type: String = "",
    var typeDescription: String = "",
    var summary: String = "",
    var bangumiUrl: String = "",
//    var loushaoUrl: String = "",
//    val userRating: Int,
//    val favoriteStatus: String,
//    val comment: String,
//    val ratingDetails: RatingDetails,
    @Ignore
    var episodes: List<BangumiEpisodeEntity> = emptyList(),
    @Ignore
    var relateds: List<BangumiIntroEntity> = emptyList(),
    @Ignore
    var similars: List<BangumiIntroEntity> = emptyList(),
    @Ignore
    var tags: List<BangumiTagEntity> = emptyList(),

    var isOnAir: Boolean = false,
    var airDay: Int = 0,
    var searchKeyword: String = "",
    var isRestricted: Boolean = false,
    var rating: Float = 0f,

    var addedDate: Long = 0,
    var updateDate: Long = 0,
    var createDate: Long = 0
) {

    @Ignore
    var isFavorited: Boolean = false

    companion object {
        fun empty() = BangumiDetailsEntity(
            type = "",
            typeDescription = "",
            episodes = emptyList(),
            summary = "",
            bangumiUrl = "",
//            loushaoUrl = "",
//            userRating = 0,
//            favoriteStatus = "",
//            comment = "",
//            ratingDetails = RatingDetails.empty(),
            relateds = emptyList(),
            similars = emptyList(),
            tags = emptyList(),
            animeId = 0,
            animeTitle = "",
            imageUrl = "",
            searchKeyword = "",
            isOnAir = false,
            airDay = 0,
            isRestricted = false,
            rating = 0f
        )
    }
}
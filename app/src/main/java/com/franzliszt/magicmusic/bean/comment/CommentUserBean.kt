package com.franzliszt.magicmusic.bean.comment

data class CommentUserBean(
    val anonym: Int,
    val authStatus: Int,
    val avatarDetail: Any?,
    val avatarUrl: String,
    val commonIdentity: Any?,
    val expertTags: Any?,
    val experts: Any?,
    val followed: Boolean,
    val liveInfo: Any?,
    val locationInfo: Any?,
    val mutual: Boolean,
    val nickname: String,
    val remarkName: Any?,
    val socialUserId: Any?,
    val target: Any?,
    val userId: Long,
    val userType: Int,
    val vipRights: Any?,
    val vipType: Int
)
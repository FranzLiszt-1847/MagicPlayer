package com.franzliszt.magicmusic.bean.user

import com.franzliszt.magicmusic.bean.pwdlogin.Profile

data class UserDetailBean(
    val adValid: Boolean,
    val bindings: List<Any>,
    val code: Int,
    val createDays: Int,
    val createTime: Long,
    val level: Int,
    val listenSongs: Int,
    val mobileSign: Boolean,
    val newUser: Boolean,
    val pcSign: Boolean,
    val peopleCanSeeMyPlayRecord: Boolean,
    val profile: Profile,
    val profileVillageInfo: Any,
    val recallUser: Boolean,
    val userPoint: Any
)
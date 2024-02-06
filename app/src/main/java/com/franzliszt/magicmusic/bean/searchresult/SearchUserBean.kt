package com.franzliszt.magicmusic.bean.searchresult

import com.franzliszt.magicmusic.bean.user.UserProfileBean

data class SearchUserBean(
    val userprofileCount: Int,
    val userprofiles: List<UserProfileBean>
)
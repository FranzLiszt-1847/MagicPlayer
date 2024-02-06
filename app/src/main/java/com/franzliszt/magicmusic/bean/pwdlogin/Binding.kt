package com.franzliszt.magicmusic.bean.pwdlogin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


data class Binding(
    val bindingTime: Long,
    val expired: Boolean,
    val expiresIn: Int,
    val id: Long,
    val refreshTime: Int,
    val tokenJsonStr: String,
    val type: Int,
    val url: String,
    val userId: Int
)
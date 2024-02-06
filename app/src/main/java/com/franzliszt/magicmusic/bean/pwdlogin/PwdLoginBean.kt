package com.franzliszt.magicmusic.bean.pwdlogin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


data class PwdLoginBean(
    val account: Account,
    val bindings: List<Binding>,
    val code: Int,
    val cookie: String?,
    val loginType: Int,
    val profile: Profile,
    val token: String?
)
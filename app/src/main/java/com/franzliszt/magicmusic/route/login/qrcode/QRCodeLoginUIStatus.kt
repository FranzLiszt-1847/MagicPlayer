package com.franzliszt.magicmusic.route.login.qrcode

import android.graphics.Bitmap

data class QRCodeLoginUIStatus(
    var bitmap: Bitmap?,
    var refresh:Boolean = false
    )
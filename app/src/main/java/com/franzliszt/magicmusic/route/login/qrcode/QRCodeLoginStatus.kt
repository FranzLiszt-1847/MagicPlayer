package com.franzliszt.magicmusic.route.login.qrcode

sealed class QRCodeLoginStatus(val msg:String){
    data class Expire(val message: String):QRCodeLoginStatus(msg = message)
    data class Waiting(val message: String):QRCodeLoginStatus(msg = message)
    data class Confirm(val message: String):QRCodeLoginStatus(msg = message)
    data class Success(val message: String):QRCodeLoginStatus(msg = message)
    data class NoCookie(val message: String):QRCodeLoginStatus(msg = message)
    data class NetworkFailed(val message: String):QRCodeLoginStatus(msg = message)
    object RefreshQRCode:QRCodeLoginStatus("二维码刷新成功!")
}

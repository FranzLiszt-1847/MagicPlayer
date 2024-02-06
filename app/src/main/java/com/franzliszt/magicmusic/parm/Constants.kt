package com.franzliszt.magicmusic.parm

object Constants {
    const val Cookie:String = "Cookie" //用户登录成功返回的Cookie
    const val Token:String = "Token" //用户登录成功返回的Token
    const val UserId = "UserId" //用户登录成功返回的UserId
    const val LoginMode = "LoginMode" //用来区分用户登录模式为密码登录还是扫码登录，1:密码登录；0:扫码登录
    const val PhoneLoginMode = 1
    const val QRCodeLoginMode = 0

    const val MusicID = "MusicID" //歌曲ID

    const val PlaylistID = "PlaylistID"
    const val PlaylistName = "PlaylistName"
    const val PlaylistCover = "PlaylistCover"
    const val IsPlaylist = "IsPlaylist"

    const val ArtistID = "ArtistID"

    const val ConsumerID = "ConsumerID" //用于进入用户详情界面所传递的参数

    const val RadioStationID = "RadioStationID"

    const val MvID = "MvID"
    const val MlogID = "MlogID"

    const val DownloadURL = "DownloadURL"
    const val DownloadPath = "DownloadPath"
    const val DownloadCover = "DownloadCover"
    const val DownloadName = "DownloadName"


    const val Preference:String = "Preference"
    const val Create:String = "Create"
    const val Favorite:String = "Favorite"

    const val ThemeMode = "ThemeMode" //当前主题样式
}
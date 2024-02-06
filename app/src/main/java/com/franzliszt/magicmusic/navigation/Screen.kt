package com.franzliszt.magicmusic.navigation

sealed class Screen(val route:String){
    //密码登陆
    object PasswordLoginPage:Screen("PasswordLogin")

    //二维码登陆
    object QrCodeLoginPage:Screen("QrCodeLogin")

    //用来承载底部导航栏和侧边栏界面
    object ContainerPage:Screen("Container")

    //主界面，承载底部导航栏
    object NavigationPage:Screen("Navigation")

    //搜索
    object SearchPage:Screen("Search")

    //搜索结果页
    object SearchResultPage:Screen("SearchResult")

    //音乐播放页面
    object MusicPlayerPage:Screen("MusicPlayer")

    //歌单页面
    object PlaylistPage:Screen("Playlist")

    //歌手详细信息界面
    object ArtistPage:Screen("Artist")

    //电台详情页面
    object RadioStationDetailPage:Screen("RadioStationDetail")

    //mv播放页面
    object MvPlayerPage:Screen("MvPlayer")

    //mlog播放页面
    object MlogPlayerPage:Screen("MlogPlayer")

    //底部导航栏
    object RadioStationPage:Screen("RadioStation") //电台
    object RankPage:Screen("Rank") //榜单
    object RecommendPage:Screen("Recommend") //推荐
    object MinePage:Screen("Mine") //我的

    //侧边栏
    object UserProfile:Screen("UserProfile")//个人信息
    object Playback:Screen("Playback")//播放列表
    object RecentPlay:Screen("RecentPlay")//最近播放
    object Download:Screen("Download")//下载
    object Favorite:Screen("Favorite")//收藏
    object Setting:Screen("Setting")//设置
    object About:Screen("About")//关于
}

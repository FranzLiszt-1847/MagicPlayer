package com.franzliszt.magicmusic.navigation

import android.provider.MediaStore.Audio.Radio
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.bean.artist.Artist
import com.franzliszt.magicmusic.parm.Constants
import com.franzliszt.magicmusic.route.artist.ArtistDetailPage
import com.franzliszt.magicmusic.route.drawer.about.AboutPage
import com.franzliszt.magicmusic.route.drawer.download.DownloadMusicPage
import com.franzliszt.magicmusic.route.drawer.favorite.FavoritePage
import com.franzliszt.magicmusic.route.drawer.playback.PlaybackPage
import com.franzliszt.magicmusic.route.drawer.recent.RecentPlayPage
import com.franzliszt.magicmusic.route.drawer.setting.SettingPage
import com.franzliszt.magicmusic.route.drawer.user.UserProfilePage
import com.franzliszt.magicmusic.route.login.pwdlogin.PasswordLoginPage
import com.franzliszt.magicmusic.route.login.qrcode.QrCodeLoginPage
import com.franzliszt.magicmusic.route.musicplayer.MusicPlayerPage
import com.franzliszt.magicmusic.route.nav.container.ContainerPager
import com.franzliszt.magicmusic.route.playlist.PlaylistPage
import com.franzliszt.magicmusic.route.radio.RadioDetailPage
import com.franzliszt.magicmusic.route.search.SearchPage
import com.franzliszt.magicmusic.route.searchresult.SearchResultPage
import com.franzliszt.magicmusic.route.video.mlog.MlogPlayerPage
import com.franzliszt.magicmusic.route.video.mv.MvPlayerPage
import com.franzliszt.magicmusic.tool.SharedPreferencesUtil

@Composable
fun NavigationGraph(
    navHostController: NavHostController,
    startDistance: String
) {
    //起始路径为密码登陆页面
    NavHost(navController = navHostController, startDestination = startDistance){
        //初始化所有路径结点
        //密码登陆页面
        composable(Screen.PasswordLoginPage.route){
            PasswordLoginPage(
                onQrcode = {
                    //跳转到二维码登录界面
                    NavigationAction.toQrcodeLogin(navHostController)
                },
                onLoginSuccess = {
                    //登录成功，跳转到主界面(默认"我的"的界面)
                    SharedPreferencesUtil.instance.putValue(APP.context, Constants.LoginMode, Constants.PhoneLoginMode)
                    NavigationAction.toContainer(navHostController)
                }
            )
        }

        //二维码登陆页面
        composable(Screen.QrCodeLoginPage.route){
            QrCodeLoginPage(
                onBack = { NavigationAction.onBack(navHostController)},
                onNavigation = {
                    SharedPreferencesUtil.instance.putValue(APP.context, Constants.LoginMode, Constants.QRCodeLoginMode)
                    NavigationAction.toContainer(navHostController)
                }
            )
        }

        composable(Screen.ContainerPage.route){
            ContainerPager(
                onSearch = {
                    NavigationAction.toSearch(navHostController)
                },
                onDrawerItem = {
                    NavigationAction.toDrawerItem(navHostController,it)
                },
                onSongItem = {
                    NavigationAction.toMusic(navHostController,it)
                },
                onMinePlaylist = { id->
                    NavigationAction.toPlaylist(
                        navHostController = navHostController,
                        id = id,
                        isPlaylist = true
                    )
                },
                onRecommendPlaylist = { id->
                    NavigationAction.toPlaylist(
                        navHostController = navHostController,
                        id = id,
                        isPlaylist = true
                    )
                },
                onAlbum = { id->
                    NavigationAction.toPlaylist(
                        navHostController = navHostController,
                        id = id,
                        isPlaylist = false
                    )
                },
                onArtist = { id->
                    NavigationAction.toArtist(navHostController, id)
                },
                onRadio = { id->
                    NavigationAction.toRadioDetail(navHostController, id)
                },
                onRank = { id->
                    NavigationAction.toPlaylist(
                        navHostController = navHostController,
                        id = id,
                        isPlaylist = true
                    )
                },
                onLogout = {
                    NavigationAction.toPwdLogin(navHostController)
                },
                onUser = {
                    val id = SharedPreferencesUtil.instance.getValue(APP.context, Constants.UserId,0L) as Long
                    NavigationAction.toUserDetail(navHostController,id)
                }
            )
        }

        //发现
        composable(Screen.SearchPage.route) {
            SearchPage(
                onBack = {
                    NavigationAction.onBack(navHostController)
                },
                onSearch = { key->
                    NavigationAction.toSearchResult(navHostController,key)
                }
            )
        }

        //发现结果页
        composable(
            route = Screen.SearchResultPage.route.plus("?key={key}"),
            arguments = listOf(
                navArgument(name = "key"){
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            SearchResultPage(
                onSongItem = {
                    NavigationAction.toMusic(navHostController,it)
                },
                onAlbumItem = {
                    NavigationAction.toPlaylist(
                        navHostController = navHostController,
                        id = it,
                        isPlaylist = false
                    )
                },
                onArtistItem = {
                    NavigationAction.toArtist(navHostController, it)
                },
                onDjItem = {
                    NavigationAction.toRadioDetail(navHostController, it)
                },
                onPlaylistItem = {
                    NavigationAction.toPlaylist(
                        navHostController = navHostController,
                        id = it,
                        isPlaylist = true
                    )
                },
                onItemMv = {
                    NavigationAction.toMvPage(navHostController,it)
                },
                onUser = {
                    NavigationAction.toUserDetail(navHostController,it)
                },
                onBack = {
                    NavigationAction.onBack(navHostController)
                }
            )
        }

        //音乐播放页面
        composable(
            route = Screen.MusicPlayerPage.route.plus("?MusicID={MusicID}"),
            arguments = listOf(
                navArgument(name = Constants.MusicID){
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ){
            MusicPlayerPage {
                NavigationAction.onBack(navHostController)
            }
        }

        /**
         * 歌单界面
         * 传入歌单id、歌单名称、歌单封面URL*/
        composable(
            route = Screen.PlaylistPage.route.plus("?PlaylistID={PlaylistID}&IsPlaylist={IsPlaylist}"),
            arguments = listOf(
                navArgument(name = Constants.PlaylistID){
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument(name = Constants.IsPlaylist){
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ){
            PlaylistPage(
                onBack = { NavigationAction.onBack(navHostController) },
                onSongItem = { NavigationAction.toMusic(navHostController,it) }
            )
        }

        /**
         * 歌手详情界面*/
        composable(
            route = Screen.ArtistPage.route.plus("?ArtistID={ArtistID}"),
            arguments = listOf(
                navArgument(name = Constants.ArtistID){
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ){
            ArtistDetailPage(
                onBack = {
                    NavigationAction.onBack(navHostController)
                },
                onItemMv = {
                    NavigationAction.toMvPage(navHostController,it)
                },
                onSongItem = {
                    NavigationAction.toMusic(navHostController,it)
                },
                onArtist = {
                    navHostController.navigateUp()
                    NavigationAction.toArtist(navHostController, it)
                },
                onAlbumItem = {
                    NavigationAction.toPlaylist(
                        navHostController = navHostController,
                        id = it,
                        isPlaylist = false
                    )
                }
            )
        }

        /**
         * 电台详情界面*/
        composable(
            route = Screen.RadioStationDetailPage.route.plus("?RadioStationID={RadioStationID}"),
            arguments = listOf(
                navArgument(name = Constants.RadioStationID){
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ){
            RadioDetailPage(
                onBack = { NavigationAction.onBack(navHostController) },
                onSongItem = {  NavigationAction.toMusic(navHostController,it) }
            )
        }


        /**
         * Drawer pages
         * */
        //用户信息
        composable(
            route = Screen.UserProfile.route.plus("?ConsumerID={ConsumerID}"),
            arguments = listOf(
                navArgument(name = Constants.ConsumerID){
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ){
            UserProfilePage{
                NavigationAction.onBack(navHostController)
            }
        }

        //当前播放歌单
        composable(Screen.Playback.route){
            PlaybackPage(
                onSongItem = {
                    NavigationAction.toMusic(navHostController,it)
                },
                onBack = {
                    NavigationAction.onBack(navHostController)
                }
            )
        }

        //最近播放
        composable(Screen.RecentPlay.route){
            RecentPlayPage(
                onSongItem = {
                    NavigationAction.toMusic(navHostController,it)
                },
                onAlbumItem = {
                    NavigationAction.toPlaylist(
                        navHostController = navHostController,
                        id = it,
                        isPlaylist = false
                    )
                },
                onDjItem = { NavigationAction.toRadioDetail(navHostController, it) },
                onPlaylistItem = {
                    NavigationAction.toPlaylist(
                        navHostController = navHostController,
                        id = it,
                        isPlaylist = true
                    )
                },
                onMvItem = {
                    NavigationAction.toMvPage(navHostController,it)
                },
                onMlogItem = {
                    NavigationAction.toMlogPage(navHostController,it)
                },
                onBack = { NavigationAction.onBack(navHostController) }
            )
        }

        //收藏
        composable(Screen.Favorite.route){
            FavoritePage(
                onBack = { NavigationAction.onBack(navHostController) },
                onArtist = {
                    NavigationAction.toArtist(navHostController, it)
                },
                onAlbum = {
                    NavigationAction.toPlaylist(
                        navHostController = navHostController,
                        id = it,
                        isPlaylist = false
                    )
                },
                onVideo = {
                    NavigationAction.toMvPage(navHostController,it.toLong())
                }
            )
        }

        //关于
        composable(Screen.About.route){
            AboutPage {
                NavigationAction.onBack(navHostController)
            }
        }

        //设置
        composable(Screen.Setting.route){
            SettingPage {
                NavigationAction.onBack(navHostController)
            }
        }

        //下载
        composable(Screen.Download.route){
            DownloadMusicPage(
                onBack = {  NavigationAction.onBack(navHostController) },
                onSongItem = {  NavigationAction.toMusic(navHostController,it) }
            )
        }

        //mv
        composable(
            route = Screen.MvPlayerPage.route.plus("?MvID={MvID}"),
            arguments = listOf(
                navArgument(name = Constants.MvID){
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ){
            MvPlayerPage(
                onBack = { NavigationAction.onBack(navHostController) },
                onItemMv = {
                    navHostController.navigateUp()
                    NavigationAction.toMvPage(navHostController,it)
                }
            )
        }

        //mlog
        composable(
            route = Screen.MlogPlayerPage.route.plus("?MlogID={MlogID}"),
            arguments = listOf(
                navArgument(name = Constants.MlogID){
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ){
            MlogPlayerPage{
                NavigationAction.onBack(navHostController)
            }
        }
    }
}

class NavigationAction{
    companion object{
        fun onBack(navHostController: NavHostController){
            navHostController.navigateUp()
        }

        /**
         * 从ContainerPage跳转到PasswordLoginPage
         * 将栈内ContainerPage之前的结点全部出栈
         * inclusive = true：示意包括将ContainerPage也出栈
         * */
        fun toPwdLogin(navHostController: NavHostController){
            navHostController.navigate(Screen.PasswordLoginPage.route){
                popUpTo(Screen.ContainerPage.route){inclusive = true}
            }
        }

        fun toQrcodeLogin(navHostController: NavHostController){
            navHostController.navigate(Screen.QrCodeLoginPage.route)
        }

        fun toNavigation(navHostController: NavHostController){
            navHostController.navigate(Screen.NavigationPage.route)
        }

        fun toContainer(navHostController: NavHostController){
            navHostController.navigate(Screen.ContainerPage.route)
        }

        fun toSearch(navHostController: NavHostController){
            navHostController.navigate(Screen.SearchPage.route)
        }

        fun toSearchResult(navHostController: NavHostController,key:String){
            navHostController.navigate(Screen.SearchResultPage.route.plus("?key=$key"))
        }

        fun toDrawerItem(navHostController: NavHostController,route:String){
            navHostController.navigate(route)
        }

        fun toUserDetail(navHostController: NavHostController,id: Long){
            navHostController.navigate(Screen.UserProfile.route.plus("?ConsumerID=$id"))
        }

        fun toMusic(navHostController: NavHostController,musicID:Long){
            navHostController.navigate(Screen.MusicPlayerPage.route.plus("?MusicID=$musicID"))
        }

        fun toPlaylist(navHostController: NavHostController,id:Long,isPlaylist:Boolean){
            navHostController.navigate(Screen.PlaylistPage.route.plus("?PlaylistID=$id&IsPlaylist=$isPlaylist"))
        }

        fun toArtist(navHostController: NavHostController,id:Long){
            navHostController.navigate(Screen.ArtistPage.route.plus("?ArtistID=$id"))
        }

        fun toRadioDetail(navHostController: NavHostController,id:Long){
            navHostController.navigate(Screen.RadioStationDetailPage.route.plus("?RadioStationID=$id"))
        }

        fun toMvPage(navHostController: NavHostController,id:Long){
            navHostController.navigate(Screen.MvPlayerPage.route.plus("?${Constants.MvID}=$id"))
        }

        fun toMlogPage(navHostController: NavHostController,id:String){
            navHostController.navigate(Screen.MlogPlayerPage.route.plus("?${Constants.MlogID}=$id"))
        }
    }
}
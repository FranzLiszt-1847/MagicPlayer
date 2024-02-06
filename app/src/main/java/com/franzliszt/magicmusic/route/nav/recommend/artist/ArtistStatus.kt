package com.franzliszt.magicmusic.route.nav.recommend.artist

sealed class ArtistStatus(val status:String){
    object Retry:ArtistStatus("Loading failed,please retry!")
    object Finish:ArtistStatus("It is the end!")
}

package com.franzliszt.magicmusic.route.nav.recommend.artist

sealed class ArtistEvent{
    object Retry:ArtistEvent()
    object Finish:ArtistEvent()
}

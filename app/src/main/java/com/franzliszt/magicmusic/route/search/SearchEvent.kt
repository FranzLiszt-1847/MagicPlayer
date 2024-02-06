package com.franzliszt.magicmusic.route.search

import com.franzliszt.magicmusic.bean.recommend.songs.DailySong

sealed class SearchEvent{
    data class ChangeKey(val key:String):SearchEvent()
    data class Search(val key:String):SearchEvent()
    data class InsertMusicItem(val bean:DailySong):SearchEvent()
    object Clear:SearchEvent()
    object ConfirmClear:SearchEvent()
    object CancelClear:SearchEvent()
    object Withdraw:SearchEvent()
}

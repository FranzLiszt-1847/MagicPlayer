package com.franzliszt.magicmusic.route.drawer.download

import com.franzliszt.magicmusic.bean.download.DownloadMusicBean

data class DownloadDialogState(
    val isVisibility:Boolean = false,
    val title:String  = "System prompt",
    val content:String = "Do you want to clear all download records ?",
    val confirmBtn:String = "Confirm",
    val cancelBtn:String = "Cancel"
)

sealed class DownloadEvent{
    data class PlayLocalMusic(val bean: DownloadMusicBean):DownloadEvent()
    data class PlayOrPause(val bean: DownloadMusicBean):DownloadEvent()
    object ShowDialog:DownloadEvent()
    object DialogConfirm:DownloadEvent()
    object DialogCancel:DownloadEvent()
}
package com.franzliszt.magicmusic.usecase.download

import com.franzliszt.magicmusic.room.download.DownloadRepository
import com.franzliszt.magicmusic.room.search.SearchHistoryRepository
import com.franzliszt.magicmusic.room.song.MusicRepository

class UpdateDownloadStateCase(private val repository: DownloadRepository) {
    suspend operator fun invoke(musicID:Long,download:Boolean) = repository.updateDownloadState(musicID, download)
}
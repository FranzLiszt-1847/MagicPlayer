package com.franzliszt.magicmusic.usecase.download

import com.franzliszt.magicmusic.room.download.DownloadRepository
import com.franzliszt.magicmusic.room.search.SearchHistoryRepository
import com.franzliszt.magicmusic.room.song.MusicRepository

class UpdateDownloadTaskIDCase(private val repository: DownloadRepository) {
    suspend operator fun invoke(musicID:Long,taskID:Long) = repository.updateTaskID(musicID, taskID)
}
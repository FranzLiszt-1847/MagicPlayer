package com.franzliszt.magicmusic.usecase.song

import com.franzliszt.magicmusic.room.search.SearchHistoryRepository
import com.franzliszt.magicmusic.room.song.MusicRepository

class UpdateLoadingMusicCase(private val repository: MusicRepository) {
    suspend operator fun invoke(songID:Long,isLoading:Boolean) = repository.updateLoadingStatus(songID, isLoading)
}
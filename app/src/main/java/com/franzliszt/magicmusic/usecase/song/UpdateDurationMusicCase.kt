package com.franzliszt.magicmusic.usecase.song

import com.franzliszt.magicmusic.room.search.SearchHistoryRepository
import com.franzliszt.magicmusic.room.song.MusicRepository

class UpdateDurationMusicCase(private val repository: MusicRepository) {
    suspend operator fun invoke(songID:Long,duration:Long) = repository.updateDuration(songID, duration)
}
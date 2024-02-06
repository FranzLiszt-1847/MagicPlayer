package com.franzliszt.magicmusic.usecase.song

import com.franzliszt.magicmusic.room.search.SearchHistoryRepository
import com.franzliszt.magicmusic.room.song.MusicRepository

class UpdateSizeMusicCase(private val repository: MusicRepository) {
    suspend operator fun invoke(songID:Long,size:String) = repository.updateSize(songID, size)
}
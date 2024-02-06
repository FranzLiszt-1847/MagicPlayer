package com.franzliszt.magicmusic.usecase.song

import com.franzliszt.magicmusic.room.search.SearchHistoryRepository
import com.franzliszt.magicmusic.room.song.MusicRepository

class DeleteAllMusicCase(private val repository: MusicRepository) {
    suspend operator fun invoke() = repository.deleteAll()
}
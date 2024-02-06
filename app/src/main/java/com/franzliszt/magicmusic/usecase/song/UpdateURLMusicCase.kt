package com.franzliszt.magicmusic.usecase.song

import com.franzliszt.magicmusic.room.search.SearchHistoryRepository
import com.franzliszt.magicmusic.room.song.MusicRepository

class UpdateURLMusicCase(private val repository: MusicRepository) {
     suspend operator fun invoke(songID:Long,url:String) = repository.updateURL(songID, url)
}
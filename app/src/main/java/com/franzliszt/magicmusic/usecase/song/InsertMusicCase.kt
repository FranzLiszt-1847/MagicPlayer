package com.franzliszt.magicmusic.usecase.song

import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.room.search.SearchHistoryRepository
import com.franzliszt.magicmusic.room.song.MusicRepository

class InsertMusicCase(private val repository: MusicRepository) {
    suspend operator fun invoke(bean: SongMediaBean) = repository.insert(bean)

}
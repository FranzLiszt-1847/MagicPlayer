package com.franzliszt.magicmusic.usecase.song

import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.room.search.SearchHistoryRepository
import com.franzliszt.magicmusic.room.song.MusicRepository
import com.franzliszt.magicmusic.room.song.MusicRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QueryAllSongsCase(private val repository: MusicRepository) {
    suspend operator fun invoke():List<SongMediaBean>{
        return repository.queryAllSongs().sortedBy { it.createTime }
    }
}
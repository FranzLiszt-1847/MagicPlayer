package com.franzliszt.magicmusic.usecase.song

import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.room.search.SearchHistoryRepository
import com.franzliszt.magicmusic.room.song.MusicRepository
import com.franzliszt.magicmusic.room.song.MusicRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QueryAllMusicCase(private val repository: MusicRepository) {
    operator fun invoke():Flow<List<SongMediaBean>>{
        return repository.queryAll().map { bean->
            //升序，按照创建时间从小到大排序，则先创建的在前面
            bean.sortedBy { it.createTime }
        }
    }
}
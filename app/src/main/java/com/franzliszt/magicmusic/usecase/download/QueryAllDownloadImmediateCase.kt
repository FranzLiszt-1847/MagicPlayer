package com.franzliszt.magicmusic.usecase.download

import com.franzliszt.magicmusic.bean.download.DownloadMusicBean
import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.room.download.DownloadRepository
import com.franzliszt.magicmusic.room.search.SearchHistoryRepository
import com.franzliszt.magicmusic.room.song.MusicRepository
import com.franzliszt.magicmusic.room.song.MusicRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QueryAllDownloadImmediateCase(private val repository: DownloadRepository) {
    operator fun invoke():Flow<List<DownloadMusicBean>> = repository.queryAllImmediate()
}
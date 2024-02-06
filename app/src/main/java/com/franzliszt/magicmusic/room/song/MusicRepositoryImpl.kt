package com.franzliszt.magicmusic.room.song

import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import kotlinx.coroutines.flow.Flow

class MusicRepositoryImpl(private val dao: MusicDao): MusicRepository {
    override fun queryAll(): Flow<List<SongMediaBean>> = dao.queryAll()

    override suspend fun queryAllSongs(): List<SongMediaBean>  = dao.queryAllSongs()

    override suspend fun insert(bean: SongMediaBean) = dao.insert(bean)

    override suspend fun insertAll(bean: List<SongMediaBean>) = dao.insertAll(bean)

    override suspend fun deleteAll() = dao.deleteAll()
    override suspend fun deleteSong(bean: SongMediaBean)  = dao.deleteSong(bean)

    override suspend fun updateURL(songID: Long, url: String) = dao.updateURL(songID, url)

    override suspend fun updateLoadingStatus(songID: Long, isLoading: Boolean) = dao.updateLoadingStatus(songID, isLoading)

    override suspend fun updateDuration(songID: Long, duration: Long) = dao.updateDuration(songID, duration)
    override suspend fun updateSize(songID: Long, size: String) = dao.updateSize(songID, size)
}
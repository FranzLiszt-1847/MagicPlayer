package com.franzliszt.magicmusic.room.download

import com.franzliszt.magicmusic.bean.download.DownloadMusicBean
import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import kotlinx.coroutines.flow.Flow

class DownloadRepositoryImpl(private val dao: DownloadDao): DownloadRepository {
    override fun queryAllImmediate(): Flow<List<DownloadMusicBean>> = dao.queryAllImmediate()

    override suspend fun queryAll(): List<DownloadMusicBean> = dao.queryAll()

    override suspend fun insert(bean: DownloadMusicBean) = dao.insert(bean)

    override suspend fun insertAll(bean: List<DownloadMusicBean>) = dao.insertAll(bean)

    override suspend fun deleteAll() = dao.deleteAll()

    override suspend fun deleteSinger(bean: DownloadMusicBean) = dao.deleteSinger(bean)

    override suspend fun updateTaskID(musicID: Long, taskID: Long) = dao.updateTaskID(musicID, taskID)

    override suspend fun updateDownloadState(musicID: Long, download: Boolean) = dao.updateDownloadState(musicID, download)

}
package com.franzliszt.magicmusic.room.download

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.franzliszt.magicmusic.bean.download.DownloadMusicBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {

    fun queryAllImmediate(): Flow<List<DownloadMusicBean>>

    suspend fun queryAll(): List<DownloadMusicBean>

    suspend fun insert(bean: DownloadMusicBean)

    suspend fun insertAll(bean: List<DownloadMusicBean>)

    suspend fun deleteAll()

    suspend fun deleteSinger(bean: DownloadMusicBean)

    suspend fun updateTaskID(musicID:Long,taskID:Long)

    suspend fun updateDownloadState(musicID:Long,download:Boolean)
}
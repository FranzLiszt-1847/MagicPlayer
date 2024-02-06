package com.franzliszt.magicmusic.room.download

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.franzliszt.magicmusic.bean.download.DownloadMusicBean
import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT *FROM DownloadTable")
    fun queryAllImmediate(): Flow<List<DownloadMusicBean>>


    @Query("SELECT *FROM DownloadTable")
    suspend fun queryAll(): List<DownloadMusicBean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bean: DownloadMusicBean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bean: List<DownloadMusicBean>)

    @Query("DELETE FROM DownloadTable")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteSinger(bean: DownloadMusicBean)

    @Query("UPDATE DownloadTable SET taskID = :taskID WHERE musicID =:musicID")
    suspend fun updateTaskID(musicID:Long,taskID:Long)

    @Query("UPDATE DownloadTable SET download = :download WHERE musicID =:musicID")
    suspend fun updateDownloadState(musicID:Long,download:Boolean)
}
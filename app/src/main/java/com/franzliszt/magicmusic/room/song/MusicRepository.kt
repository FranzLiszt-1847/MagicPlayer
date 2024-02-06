package com.franzliszt.magicmusic.room.song

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import kotlinx.coroutines.flow.Flow

interface MusicRepository {

    fun queryAll(): Flow<List<SongMediaBean>>

    suspend fun queryAllSongs(): List<SongMediaBean>

    suspend fun insert(bean: SongMediaBean)

    suspend fun insertAll(bean: List<SongMediaBean>)

    suspend fun deleteAll()

    suspend fun deleteSong(bean: SongMediaBean)

    suspend fun updateURL(songID:Long,url:String)

    suspend fun updateLoadingStatus(songID:Long,isLoading:Boolean)

    suspend fun updateDuration(songID:Long,duration:Long)

    suspend fun updateSize(songID:Long,size:String)
}
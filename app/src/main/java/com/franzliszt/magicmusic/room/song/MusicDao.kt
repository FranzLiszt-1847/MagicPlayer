package com.franzliszt.magicmusic.room.song

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    /**
     * 获取播放列表所有歌曲*/
    @Query("SELECT *FROM SongTable")
    fun queryAll(): Flow<List<SongMediaBean>>

    @Query("SELECT *FROM SongTable")
    suspend fun queryAllSongs(): List<SongMediaBean>

    /**
     * 插入歌曲*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bean: SongMediaBean)

    /**
     * 一次性插入*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bean: List<SongMediaBean>)

    /**
     * 清空当前播放列表*/
    @Query("DELETE FROM SongTable")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteSong(bean: SongMediaBean)
    /**
     * 更新url*/
    @Query("UPDATE SongTable SET url = :url WHERE songID =:songID")
    suspend fun updateURL(songID:Long,url:String)

    /**
     * 更新loading状态*/
    @Query("UPDATE SongTable SET isLoading = :isLoading WHERE songID =:songID")
    suspend fun updateLoadingStatus(songID:Long,isLoading:Boolean)

    /**
     * 更新duration状态*/
    @Query("UPDATE SongTable SET duration = :duration WHERE songID =:songID")
    suspend fun updateDuration(songID:Long,duration:Long)

    /**
     * 更新size状态*/
    @Query("UPDATE SongTable SET size = :size WHERE songID =:songID")
    suspend fun updateSize(songID:Long,size:String)
}
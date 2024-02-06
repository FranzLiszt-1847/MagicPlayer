package com.franzliszt.magicmusic.room.search

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    /**
     * 查询所有搜索历史记录*/
    @Query("SELECT *FROM SearchRecordTable")
    fun queryAllHistory(): Flow<List<SearchRecordBean>>

    /**
     * 插入搜索记录*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(bean: SearchRecordBean)

    /**
     * 插入搜索记录*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHistory(bean: List<SearchRecordBean>)

    /**
     * 清空搜索记录*/
    @Query("DELETE FROM SearchRecordTable")
    suspend fun deleteAllHistory()


}
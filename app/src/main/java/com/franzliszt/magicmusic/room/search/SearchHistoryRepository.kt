package com.franzliszt.magicmusic.room.search

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {

    fun queryAllHistory(): Flow<List<SearchRecordBean>>

    suspend fun insertHistory(bean: SearchRecordBean)

    suspend fun insertAllHistory(bean: List<SearchRecordBean>)

    suspend fun deleteAllHistory()
}
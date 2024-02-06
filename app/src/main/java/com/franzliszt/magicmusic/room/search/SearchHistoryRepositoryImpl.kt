package com.franzliszt.magicmusic.room.search

import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import kotlinx.coroutines.flow.Flow

class SearchHistoryRepositoryImpl(private val dao: SearchHistoryDao): SearchHistoryRepository {
    override fun queryAllHistory(): Flow<List<SearchRecordBean>> = dao.queryAllHistory()

    override suspend fun insertHistory(bean: SearchRecordBean) = dao.insertHistory(bean)

    override suspend fun insertAllHistory(bean: List<SearchRecordBean>) = dao.insertAllHistory(bean)

    override suspend fun deleteAllHistory() = dao.deleteAllHistory()
}
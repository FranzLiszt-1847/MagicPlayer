package com.franzliszt.magicmusic.usecase.search

import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.room.search.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QueryAllCase(private val repository: SearchHistoryRepository) {
    operator fun invoke():Flow<List<SearchRecordBean>>{
        return repository.queryAllHistory().map { bean->
            //降序
            bean.sortedByDescending { it.createTime }
        }
    }
}
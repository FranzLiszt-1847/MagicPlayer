package com.franzliszt.magicmusic.usecase.search

import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.room.search.SearchHistoryRepository

class InsertAllCase(private val repository: SearchHistoryRepository) {
    suspend operator fun invoke(bean: List<SearchRecordBean>) = repository.insertAllHistory(bean)
}
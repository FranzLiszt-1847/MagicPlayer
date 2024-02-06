package com.franzliszt.magicmusic.usecase.search

import com.franzliszt.magicmusic.room.search.SearchHistoryRepository

class DeleteAllCase(private val repository: SearchHistoryRepository) {
    suspend operator fun invoke() = repository.deleteAllHistory()
}
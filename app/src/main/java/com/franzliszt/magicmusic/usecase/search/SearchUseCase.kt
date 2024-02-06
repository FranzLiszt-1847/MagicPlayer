package com.franzliszt.magicmusic.usecase.search

data class SearchUseCase(
    val queryAll:QueryAllCase,
    val insert:InsertCase,
    val deleteAll:DeleteAllCase,
    val insertAll: InsertAllCase
)

package com.franzliszt.magicmusic.usecase.download

data class DownloadUseCase(
    val queryAllImmediate:QueryAllDownloadImmediateCase,
    val queryAll:QueryAllDownloadCase,
    val insert:InsertDownloadCase,
    val insertAll: InsertAllDownloadCase,
    val delete:DeleteDownloadCase,
    val deleteAll:DeleteAllDownloadCase,
    val updateTaskID:UpdateDownloadTaskIDCase,
    val updateDownloadState:UpdateDownloadStateCase,
)

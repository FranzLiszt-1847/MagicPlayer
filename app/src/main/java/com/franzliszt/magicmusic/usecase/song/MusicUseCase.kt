package com.franzliszt.magicmusic.usecase.song

data class MusicUseCase(
    val queryAll:QueryAllMusicCase,
    val insert:InsertMusicCase,
    val deleteAll:DeleteAllMusicCase,
    val insertAll: InsertAllMusicCase,
    val updateUrl:UpdateURLMusicCase,
    val updateSize:UpdateSizeMusicCase,
    val updateLoading:UpdateLoadingMusicCase,
    val updateDuration: UpdateDurationMusicCase,
    val queryAllSongsCase: QueryAllSongsCase,
    val deleteSong:DeleteMusicCase
)

package com.franzliszt.magicmusic.usecase.download

import com.franzliszt.magicmusic.bean.download.DownloadMusicBean
import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.room.download.DownloadRepository
import com.franzliszt.magicmusic.room.search.SearchHistoryRepository
import com.franzliszt.magicmusic.room.song.MusicRepository

class DeleteDownloadCase(private val repository: DownloadRepository) {
    suspend operator fun invoke(bean: DownloadMusicBean) = repository.deleteSinger(bean)
}
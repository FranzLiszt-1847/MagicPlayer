package com.franzliszt.magicmusic.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arialyy.annotations.Download
import com.franzliszt.magicmusic.bean.download.DownloadMusicBean
import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.room.download.DownloadDao
import com.franzliszt.magicmusic.room.search.SearchHistoryDao
import com.franzliszt.magicmusic.room.song.MusicDao

@Database(entities = [SearchRecordBean::class,SongMediaBean::class,DownloadMusicBean::class], version = 11, exportSchema = false)
abstract class MagicMusicDataBase:RoomDatabase() {
    abstract val searchHistoryDao: SearchHistoryDao
    abstract val musicDao:MusicDao
    abstract val downloadDao:DownloadDao
    companion object{
        const val DATABASE_NAME = "MagicMusic"
    }
}
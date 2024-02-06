package com.franzliszt.magicmusic.bean.song

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SongTable")
data class SongMediaBean(
    val createTime:Long,
    @PrimaryKey val songID:Long, //歌曲id
    val songName:String, //歌曲名字
    val cover:String, //封面url
    val artist:String, //歌手
    var url:String, //音频链接
    var isLoading:Boolean,
    var duration:Long,
    var size:String
)

package com.franzliszt.magicmusic.route.musicplayer

import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.franzliszt.magicmusic.bean.lrc.LyricBean
import com.franzliszt.magicmusic.bean.recommend.songs.DailySong
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.ui.theme.music_bg100

data class MusicPlayerUIStatus(
    val progress:Float = 0f, //进度条
    val isPlaying:Boolean = false, //当前播放状态
    val musicID:Long = 0L, //歌曲ID
    val artist:String = "Unknown", //歌手
    val name:String = "Unknown", //歌曲名称
    val cover:String = "", //歌曲封面
    val totalDuration:String = "00:00", //歌曲总时长
    val currentDuration:String = "00:00", //当前播放时间
    val url:String = "", //歌曲播放URL
    val currentLine:Int = 0, //与播放进度相匹配的歌词行
    val isShowBottomSheet:Boolean = false,
    val playlist:List<SongMediaBean> = emptyList(),
    val lyrics:List<LyricBean> = emptyList() //歌词
)
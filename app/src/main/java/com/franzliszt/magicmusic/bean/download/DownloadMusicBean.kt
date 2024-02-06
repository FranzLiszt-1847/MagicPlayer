package com.franzliszt.magicmusic.bean.download

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.franzliszt.magicmusic.ui.theme.grey
import com.franzliszt.magicmusic.ui.theme.grey200

@Entity(tableName = "DownloadTable")
data class DownloadMusicBean(
    @PrimaryKey var musicID:Long, //音乐ID
    var taskID:Long,
    var musicName:String, //音乐名字
    var artist:String, //歌手
    var cover:String, //封面url
    var url:String, //音乐url-下载链接
    var path:String,//下载地址
    var size:String,//资源大小
    var download:Boolean, //是否已经下载完成
    @Ignore var progress:Float, //下载进度
    @Ignore var speed:String,//下载速度
    @Ignore var progressColor:Color //进度条颜色
){
    constructor():this(0L,0L,"","","","","","",false,0f,"waiting", grey)
}

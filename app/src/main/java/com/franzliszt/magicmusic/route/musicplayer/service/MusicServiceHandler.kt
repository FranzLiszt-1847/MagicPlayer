package com.franzliszt.magicmusic.route.musicplayer.service

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.network.RemoteResult
import com.franzliszt.magicmusic.network.baseApiCall
import com.franzliszt.magicmusic.usecase.song.MusicUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import android.icu.math.BigDecimal
import android.util.Log
import com.arialyy.aria.core.Aria
import com.arialyy.aria.util.CommonUtil
import javax.inject.Inject
import javax.inject.Named

/**
 * 处理播放的相关事宜*/
class MusicServiceHandler @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val musicUseCase: MusicUseCase,
    private val service: MusicApiService
):Player.Listener {

    private var playlist:MutableList<SongMediaBean> = mutableListOf()
    private var currentPlayIndex = -1

    var playState = PlayState.Failed
    //当前歌单id，只有对从歌单处点击播放生效
    private var currentPlaylistId = 0L
    private val _eventFlow = MutableSharedFlow<AudioPlayState>()
    val eventFlow = _eventFlow.asSharedFlow()

    //实时读取播放进度
    private var job: Job? = null

    init {
        exoPlayer.addListener(this)
    }

    /**
     * 当前播放列表
     * 使本地数据库与此数组保持一致，
     * 只有当初始化时，将数据库的数据赋值此数组
     * 其余对此数组的操作在内存中发生，同时变更到数据库，但不对数据库的内容变化做响应监听*/
    @OptIn(DelicateCoroutinesApi::class)
    fun initPlaylist(){
        GlobalScope.launch(Dispatchers.Main){
            val songs = musicUseCase.queryAllSongsCase()
            if (songs.isNotEmpty()){
                playlist.addAll(songs)
            }
        }
    }

    fun getCurrentSongId():Long = currentPlaylistId

    fun getCurrentPlayItem():SongMediaBean?{
        return if (currentPlayIndex != -1){
            playlist[currentPlayIndex]
        }else{
            null
        }
    }

    fun getIsPlaying():Boolean = exoPlayer.isPlaying

    /**
     * 如果播放列表已经存在，则不重复插入
     * 因为播放链接是后面更新的
     * */
    suspend fun isExistPlaylist(bean: SongMediaBean){
        if (playlist.isEmpty() || (playlist.find { it.songID == bean.songID } == null)) {
            onEvent(AudioPlayerEvent.Singer(bean,false))
        }else{
            onEvent(AudioPlayerEvent.Singer(bean,true))
        }
    }

    suspend fun playLocalMusic(bean: SongMediaBean){
        val index = playlist.indexOf(playlist.find { it.songID == bean.songID })
        if (index != -1){
            //存在当前播放列表中
            playlist[index].url = bean.url
            musicUseCase.updateUrl(bean.songID,bean.url)
            onEvent(AudioPlayerEvent.Singer(bean,true))
        }else{
            //不存在当前播放列表中
            onEvent(AudioPlayerEvent.Singer(bean,false))
        }
    }



    /**
     * 从歌单中点击某首歌曲进行播放
     * 清空当前播放列表，并将当前歌单所有歌曲作为当前播放列表
     * 并播放点击的歌曲*/
     fun setMediaItems(songs:List<SongMediaBean>){
        if (playlist.isNotEmpty()){
            playlist.clear()
        }
        playlist.addAll(songs)
    }

    /**
     * 1.1:点击歌单某一歌曲，则被点击的首先播放
     * 1.2:如果播放某歌单内歌曲，则将歌单内所有歌曲替换当前播放列表
     * 2.1:如果播放某一单曲,则将当前歌曲加入播放列表
     * 2.2:因为是按照创建时间从小到大排序,则直接取最后一个元素，则为当前点击的歌曲*/
    private suspend fun replaceMediaItem(index: Int){
        if (playlist.isEmpty())return
        currentPlayIndex = index
        if (!playlist[currentPlayIndex].isLoading) {
            //未加载
            getMusicUrl(playlist[currentPlayIndex].songID){ url,duration,size->
                playlist[currentPlayIndex].url = url
                playlist[currentPlayIndex].duration = duration
                playlist[currentPlayIndex].isLoading = true
                playlist[currentPlayIndex].size = CommonUtil.formatFileSize(size.toDouble())
                setMediaItem(playlist[currentPlayIndex])
            }
        }else{
            setMediaItem(playlist[currentPlayIndex])
        }
    }

    private suspend fun setMediaItem(bean: SongMediaBean){
        exoPlayer.setMediaItem(
            MediaItem.Builder()
                .setUri(bean.url) //播放链接
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setArtist(bean.artist) //歌手
                        .setTitle(bean.songName) //歌曲名称
                        .setSubtitle(bean.artist) // 歌手
                        .setArtworkUri(bean.cover.toUri()) //封面
                        .setDescription("${bean.songID}")
                        .build()
                ).build()
        )
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        startProgress()
        _eventFlow.emit(AudioPlayState.CurrentPlayItem(playlist[currentPlayIndex]))
        _eventFlow.emit(AudioPlayState.Playing(true))
    }


    /**
     * 播放状态改变*/
    @OptIn(DelicateCoroutinesApi::class)
    override fun onPlaybackStateChanged(playbackState: Int) {
        GlobalScope.launch(Dispatchers.Main){
            when (playbackState){
                ExoPlayer.STATE_BUFFERING->{
                    //缓冲状态
                    _eventFlow.emit(AudioPlayState.Buffering(exoPlayer.currentPosition,exoPlayer.duration))
                }
                ExoPlayer.STATE_READY->{
                    //如果已经准备完毕，则立即播放媒体
                    _eventFlow.emit(AudioPlayState.Ready(exoPlayer.duration))
                    if (playlist.isNotEmpty() && currentPlayIndex >=0){
                        _eventFlow.emit(AudioPlayState.CurrentPlayItem(playlist[currentPlayIndex]))
                    }
                }
                ExoPlayer.STATE_ENDED->{
                    //播放完成,自动切换下一首
                    next()
                }
                else->{}
            }
        }
    }


    /**
     * 暂停或播放的状态变化监听*/
    @OptIn(DelicateCoroutinesApi::class)
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        GlobalScope.launch(Dispatchers.Main) {
            _eventFlow.emit(AudioPlayState.Playing(isPlaying))
            if (playlist.isNotEmpty() && currentPlayIndex >=0){
                _eventFlow.emit(AudioPlayState.CurrentPlayItem(playlist[currentPlayIndex]))
            }
            if (isPlaying) {
                startProgress()
            } else {
                stopProgress()
            }
        }
    }

    /**
     * 为歌曲播放时，每隔0.5s查询一次当前播放progress，并通知UI进行更新*/
    private suspend fun startProgress() = job.run {
        while(true){
            delay(500L)
            _eventFlow.emit(AudioPlayState.Progress(exoPlayer.currentPosition,exoPlayer.duration))
        }
    }

    /**
     * 当歌曲暂停时，停止更新progress*/
    private suspend fun stopProgress(){
        job?.cancel()
        _eventFlow.emit(AudioPlayState.Playing(false))
    }

    private suspend fun playOrPause(){
        if (exoPlayer.isPlaying){
            //如果歌曲正在播放，则变为暂停
            exoPlayer.pause()
            stopProgress()
        }else{
            //如果歌曲为暂停状态，则变为播放状态
            exoPlayer.play()
            startProgress()
            _eventFlow.emit(AudioPlayState.Playing(true))
        }
    }


    /**
     * 播放事件处理*/
    suspend fun onEvent(event: AudioPlayerEvent) {
            when (event) {
                is AudioPlayerEvent.PlayOrPause -> {
                    playOrPause()
                }
                is AudioPlayerEvent.SeekTo -> {
                    exoPlayer.seekTo(((exoPlayer.duration * event.positionMs) / 100f).toLong())
                } //滑动Slider
                is AudioPlayerEvent.SeekToDuration -> {
                    exoPlayer.seekTo(event.duration)
                } //点击歌词
                is AudioPlayerEvent.Stop -> {
                    stopProgress()
                }

                is AudioPlayerEvent.ChangeAudioItem -> {
                    //切换播放列表中的其他媒体资源
                    //如果是当前项，则根据当前播放状态变化
                    if (event.index == currentPlayIndex) {
                        playOrPause()
                    } else {
                        replaceMediaItem(event.index)
                    }
                }

                is AudioPlayerEvent.Next -> {
                    //下一首
                    next()
                }

                is AudioPlayerEvent.Prior -> {
                    //上一首
                    prior()
                }
                //如果是从歌单或者专辑处点击音乐播放
                is AudioPlayerEvent.Group -> {
                    val index = playlist.indexOf(playlist.find { it.songID == event.id })
                    if (currentPlaylistId == event.id){
                        _eventFlow.emit(AudioPlayState.Reenter(playlist[index]))
                    }else{
                        currentPlaylistId = event.id
                        replaceMediaItem(index)
                    }
                }
                //如果是从单曲处点击音乐播放
                is AudioPlayerEvent.Singer -> {
                    if (!event.isExist) {
                        //之前不存在
                        unExistence(event.bean)
                    } else {
                        //之前存在
                        existence(event.bean.songID)
                    }
                }

                is AudioPlayerEvent.DeletePlayItem -> {
                    val index = playlist.indexOf(event.bean)
                    if (index != -1){
                        playlist.removeAt(index)
                        if (currentPlayIndex == index) {
                            next()
                        } else if (currentPlayIndex > index) {
                            currentPlayIndex--
                        }
                    }
                }
            }
    }


    /**
     * 之前不在播放列表之中*/
    private suspend fun unExistence(bean: SongMediaBean){
        playlist.add(bean)
        musicUseCase.insert(bean)
        replaceMediaItem(playlist.size-1)
    }

    /**
     * 之前存在播放列表中*/
    private suspend fun existence(songID: Long){
        if (currentPlayIndex < 0 || (playlist[currentPlayIndex].songID != songID)){
            //第一次播放 or 与当前播放项不相同，则替换当前播放项
            val index  = playlist.indexOf(playlist.find { it.songID == songID })
            replaceMediaItem(index)
        }else{
            //退出播放页面后重新进入，没有更改播放项
            _eventFlow.emit(AudioPlayState.CurrentPlayItem(playlist[currentPlayIndex]))
            _eventFlow.emit(AudioPlayState.Playing(true))
        }
    }

     fun getNextIndex():Int = (currentPlayIndex + 1) % playlist.size

     fun getPriorIndex(): Int =
        if (currentPlayIndex <= 0)
            playlist.size - 1
        else
            (currentPlayIndex - 1) % playlist.size

    /**
     * 切换播放列表下一首*/
    private suspend fun next(){
        if (playlist.isNotEmpty()){
            val next =  getNextIndex()
            replaceMediaItem(next)
        }else{
            currentPlayIndex = -1
        }
    }

    /**
     * 切换播放列表上一首*/
    private suspend fun prior(){
        if (playlist.isNotEmpty()){
            val prior = getPriorIndex()
            replaceMediaItem(prior)
        }else{
            currentPlayIndex = -1
        }
    }

    /**
     * 获取歌曲播放URL*/
    private suspend fun getMusicUrl(id:Long,onMusic:suspend (String,Long,Long)->Unit){
        when(val response = baseApiCall { service.getMusicUrl(id) }){
            is RemoteResult.Success->{
                //获取歌曲Url成功
                val url = response.data.data[0].url
                if (url != null && url.isNotEmpty()){
                    playState = PlayState.Successful
                    onMusic(
                        response.data.data[0].url,
                        response.data.data[0].time,
                        response.data.data[0].size
                    )
                }else{
                    //从专辑处点击播放歌曲，可能不能获取音乐源，部分专辑需要购买才能获取
                    playState = PlayState.Failed
                    exoPlayer.pause()
                    stopProgress()
                    _eventFlow.emit(AudioPlayState.NetworkFailed("Playback error, possibly due to some songs not having copyright!"))
                }
            }
            is  RemoteResult.Error->{
                //网络请求失败
                _eventFlow.emit(AudioPlayState.NetworkFailed(response.exception.message.toString()))
            }
        }
    }
}


sealed class AudioPlayerEvent {
    object Next:AudioPlayerEvent()
    object Prior:AudioPlayerEvent()
    object PlayOrPause : AudioPlayerEvent()
    object Stop : AudioPlayerEvent()
    data class ChangeAudioItem(val index:Int) : AudioPlayerEvent()
    data class SeekTo(val positionMs:Float) : AudioPlayerEvent()
    data class SeekToDuration(val duration:Long):AudioPlayerEvent()
    data class DeletePlayItem(val bean: SongMediaBean):AudioPlayerEvent()
    data class Group(val id:Long):AudioPlayerEvent()
    data class Singer(val bean: SongMediaBean,val isExist:Boolean):AudioPlayerEvent()
}

sealed class AudioPlayState {
    data class Ready(val duration: Long) : AudioPlayState()
    data class Progress(val progress: Long,val duration: Long) : AudioPlayState()
    data class Buffering(val progress: Long,val duration: Long) : AudioPlayState()
    data class Playing(val isPlaying: Boolean) : AudioPlayState()
    data class CurrentPlayItem(val bean: SongMediaBean?):AudioPlayState()
    data class NetworkFailed(val msg:String):AudioPlayState()
    data class Reenter(val bean: SongMediaBean?):AudioPlayState()
}

 enum class PlayState{
    Successful,
    Failed
}
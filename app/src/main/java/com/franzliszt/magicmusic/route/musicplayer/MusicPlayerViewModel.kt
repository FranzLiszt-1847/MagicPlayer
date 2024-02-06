package com.franzliszt.magicmusic.route.musicplayer

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.franzliszt.magicmusic.bean.comment.CommentBean
import com.franzliszt.magicmusic.bean.lrc.LyricBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.network.RemoteResult
import com.franzliszt.magicmusic.network.baseApiCall
import com.franzliszt.magicmusic.parm.Constants
import com.franzliszt.magicmusic.route.musicplayer.service.AudioPlayState
import com.franzliszt.magicmusic.route.musicplayer.service.AudioPlayerEvent
import com.franzliszt.magicmusic.route.musicplayer.service.MusicServiceHandler
import com.franzliszt.magicmusic.route.musicplayer.service.PlayState
import com.franzliszt.magicmusic.route.playlist.BottomSheetScreen
import com.franzliszt.magicmusic.route.playlist.CommentUIStatus
import com.franzliszt.magicmusic.route.playlist.NetworkStatus
import com.franzliszt.magicmusic.route.playlist.PlaylistEvent
import com.franzliszt.magicmusic.route.playlist.PlaylistStatus
import com.franzliszt.magicmusic.route.searchresult.transformTime
import com.franzliszt.magicmusic.tool.BitmapUtil
import com.franzliszt.magicmusic.tool.BitmapUtil.Companion.saveToPhoto
import com.franzliszt.magicmusic.tool.LyricUtil
import com.franzliszt.magicmusic.ui.theme.music_bg100
import com.franzliszt.magicmusic.ui.theme.music_bg200
import com.franzliszt.magicmusic.ui.theme.music_bg300
import com.franzliszt.magicmusic.ui.theme.music_bg400
import com.franzliszt.magicmusic.ui.theme.music_bg500
import com.franzliszt.magicmusic.ui.theme.music_bg600
import com.franzliszt.magicmusic.ui.theme.music_bg700
import com.franzliszt.magicmusic.usecase.song.MusicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    private val service: MusicApiService,
    private val savedStateHandle: SavedStateHandle,
    private val musicServiceHandler: MusicServiceHandler,
    private val musicUseCase: MusicUseCase
):ViewModel() {
    private val _uiStatus = mutableStateOf(MusicPlayerUIStatus())
    val uiStatus:State<MusicPlayerUIStatus> = _uiStatus

    private val _commentStatus = mutableStateOf(CommentUIStatus())
    val commentStatus:State<CommentUIStatus> = _commentStatus

    private val _eventFlow = MutableSharedFlow<MusicPlayerStatus>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var job:Job? = null

    //BottomSheet视图状态
    var bottomSheetScreen = mutableStateOf<BottomSheetScreen>(BottomSheetScreen.PlaylistComments)

    private var currentFloorCommentId = 0L //当前楼层评论ID

    private var offset = -1 //评论页数
    private var floorOffset = -1 //评论页数
    private val limit = 30 //评论每次加载数量

    init {
        viewModelScope.launch{
            savedStateHandle.get<Long>(Constants.MusicID)?.let {
                _uiStatus.value = uiStatus.value.copy(
                    musicID = it
                )
            }
            if (_uiStatus.value.lyrics.isEmpty()){
                getMusicLyric(_uiStatus.value.musicID)
            }
            updateInfo()
            getPlaylist()
        }
        playerStatus()
    }

    private fun updateInfo(){
        val bean = musicServiceHandler.getCurrentPlayItem()
        bean?.let {
            _uiStatus.value = uiStatus.value.copy(
                artist = it.artist,
                name = it.songName,
                cover = it.cover,
                musicID = it.songID,
                totalDuration = transformTime(it.duration),
                isPlaying = musicServiceHandler.getIsPlaying()
            )
        }
    }
    /**
     * 监听播放状态*/
    private fun playerStatus(){
        viewModelScope.launch(Dispatchers.IO) {
            musicServiceHandler.eventFlow.collect {
                when(it){
                    is AudioPlayState.Ready->{
                        _uiStatus.value = uiStatus.value.copy(
                            totalDuration = transformTime(it.duration)
                        )
                    }
                    is AudioPlayState.Buffering->{
                        calculateProgress(it.progress,it.duration)
                    }
                    is AudioPlayState.Playing->{
                        _uiStatus.value = uiStatus.value.copy(
                            isPlaying = it.isPlaying
                        )
                    }
                    is AudioPlayState.Progress->{
                        calculateProgress(it.progress,it.duration)
                        val line = matchLyric(it.progress)
                        _uiStatus.value = _uiStatus.value.copy(
                            currentLine = line
                        )
                    }
                    is AudioPlayState.CurrentPlayItem->{
                        if (it.bean != null){
                            _uiStatus.value = uiStatus.value.copy(
                                artist = it.bean.artist,
                                name = it.bean.songName,
                                cover = it.bean.cover,
                                musicID = it.bean.songID,
                                totalDuration = transformTime(it.bean.duration)
                            )
                            //同步更新数据库
                            musicUseCase.updateUrl(it.bean.songID,it.bean.url)
                            musicUseCase.updateLoading(it.bean.songID, true)
                            musicUseCase.updateDuration(it.bean.songID, it.bean.duration)
                            musicUseCase.updateSize(it.bean.songID, it.bean.size)
                        }
                    }

                    is AudioPlayState.Reenter->{
                        if (it.bean != null){
                            _uiStatus.value = uiStatus.value.copy(
                                artist = it.bean.artist,
                                name = it.bean.songName,
                                cover = it.bean.cover,
                                musicID = it.bean.songID,
                                totalDuration = transformTime(it.bean.duration)
                            )
                        }
                    }

                    is AudioPlayState.NetworkFailed->{
                        _eventFlow.emit(MusicPlayerStatus.NetworkFailed(it.msg))
                    }
                }
            }
        }
    }

    fun onEvent(event: MusicPlayerEvent){
        viewModelScope.launch {
            when(event){
                is  MusicPlayerEvent.ProgressChange->{
                    musicServiceHandler.onEvent(AudioPlayerEvent.SeekTo(event.progress))
                }
                is MusicPlayerEvent.ChangePlayStatus->{
                    //改变播放状态，即播放和暂停切换
                    musicServiceHandler.onEvent(AudioPlayerEvent.PlayOrPause)
                }
                is MusicPlayerEvent.Prior->{
                    //在当前播放列表中切换上一首
                    getMusicLyric(_uiStatus.value.playlist[musicServiceHandler.getPriorIndex()].songID)
                    musicServiceHandler.onEvent(AudioPlayerEvent.Prior)
                }
                is MusicPlayerEvent.Next->{
                    //在当前播放列表中切换下一首
                    getMusicLyric(_uiStatus.value.playlist[musicServiceHandler.getNextIndex()].songID)
                    musicServiceHandler.onEvent(AudioPlayerEvent.Next)
                }
                //切换播放项
                is MusicPlayerEvent.ChangePlayMedia->{ musicServiceHandler.onEvent(AudioPlayerEvent.ChangeAudioItem(event.index)) }
                //点击歌词，变更播放进度
                is MusicPlayerEvent.DurationChange-> { musicServiceHandler.onEvent(AudioPlayerEvent.SeekToDuration(event.duration)) }

                is MusicPlayerEvent.BottomSheet->{
                    bottomSheetScreen.value = BottomSheetScreen.Playlist
                    _eventFlow.emit(MusicPlayerStatus.BottomSheet)
                }

                is MusicPlayerEvent.DeleteSong->{
                    //删除播放列表中的歌曲
                    musicUseCase.deleteSong(event.bean)
                    musicServiceHandler.onEvent(AudioPlayerEvent.DeletePlayItem(event.bean))
                }

            }
        }
    }

    fun onCommentEvent(event:PlaylistEvent){
        viewModelScope.launch(Dispatchers.IO) {
            when(event){
                /**
                 * 打开单曲评论*/
                is PlaylistEvent.OpenPlaylistComment->{
                    bottomSheetScreen.value = BottomSheetScreen.PlaylistComments
                    if (_commentStatus.value.commentStatus is NetworkStatus.Waiting){
                        //如果没有加载过评论，则网络请求进行加载，否则不进行重复加载
                        onCommentEvent(PlaylistEvent.NextCommentPage)
                    }
                    _eventFlow.emit(MusicPlayerStatus.BottomSheet)
                }

                is PlaylistEvent.NextCommentPage->{
                    //评论划到了底部，增加下一页数据
                    if ((_commentStatus.value.commentStatus is NetworkStatus.Waiting) || (_commentStatus.value.comments.size < _commentStatus.value.commentCount)){
                        offset++
                        getSongComments(_uiStatus.value.musicID)
                    }else{
                        //评论已经全部加载了
                        _eventFlow.emit(MusicPlayerStatus.Message("It's already the end, there's nothing more！"))
                    }
                }

                /**
                 * 点赞或取消点赞*/
                is PlaylistEvent.AgreeComment->{
                    getAgreeComment(event.id,event.index,event.isFloor)
                }

                /**
                 * 楼层评论*/
                is PlaylistEvent.OpenFloorComment->{
                    currentFloorCommentId = event.id
                    floorOffset = -1
                    _commentStatus.value.floorComments.clear()
                    _commentStatus.value = _commentStatus.value.copy(
                        floorCommentStatus = NetworkStatus.Waiting
                    )
                    bottomSheetScreen.value = BottomSheetScreen.FloorComments
                    onCommentEvent(PlaylistEvent.NextFloorCommentPage)
                    _eventFlow.emit(MusicPlayerStatus.BottomSheet)
                }

                is PlaylistEvent.NextFloorCommentPage->{
                    //评论划到了底部，增加下一页数据
                    if ((_commentStatus.value.floorCommentStatus is NetworkStatus.Waiting) || (_commentStatus.value.floorComments.size < _commentStatus.value.floorCommentCount)){
                        ++floorOffset
                        getFloorComments(currentFloorCommentId)
                    }else{
                        //评论已经全部加载了
                        _eventFlow.emit(MusicPlayerStatus.Message("It's already the end, there's nothing more！"))
                    }
                }

                is PlaylistEvent.ChangeComment->{
                    _commentStatus.value = commentStatus.value.copy(
                        commentText = event.msg
                    )
                }
                is PlaylistEvent.ChangeFloorComment->{
                    _commentStatus.value = commentStatus.value.copy(
                        floorCommentText = event.msg
                    )
                }
                is PlaylistEvent.SendComment->{
                    if (_commentStatus.value.commentText.isEmpty()){
                        _eventFlow.emit(MusicPlayerStatus.Message("The input content cannot be empty!"))
                    }else{
                        //资源评论
                        sendComment(
                            t = 1,
                            content = _commentStatus.value.commentText
                        )
                        _commentStatus.value = commentStatus.value.copy(
                            commentText = ""
                        )
                    }
                }

                is PlaylistEvent.SendFloorComment->{
                    if (_commentStatus.value.floorCommentText.isEmpty()){
                        _eventFlow.emit(MusicPlayerStatus.Message("The input content cannot be empty!"))
                    }else{
                        //回复评论
                        sendComment(
                            commentId = event.commentID,
                            t = 2,
                            content = _commentStatus.value.floorCommentText
                        )
                        _commentStatus.value = commentStatus.value.copy(
                            floorCommentText = ""
                        )
                    }
                }
                else->{}
            }
        }
    }

    /**
     * 获取歌曲评论*/
    private suspend fun getSongComments(id:Long){
        val response = baseApiCall { service.getSongComments(
            id = id,
            time = if (_commentStatus.value.comments.isNotEmpty()) _commentStatus.value.comments[_commentStatus.value.comments.size-1].time else 0L,
            offset = offset*limit,
            limit = limit
        ) }
        when(response){
            is RemoteResult.Success->{
                //下列字段可能会返回null或者返回json中没有该字段，故对其判空处理
                val comments:MutableList<CommentBean> = mutableListOf()
                comments.addAll(response.data.topComments ?: emptyList())
                comments.addAll(response.data.hotComments ?: emptyList())
                comments.addAll(response.data.comments ?: emptyList())
                _commentStatus.value.comments.addAll(comments)
                _commentStatus.value = commentStatus.value.copy(
                    commentStatus = NetworkStatus.Successful
                )
                if (response.data.total != 0L){
                    _commentStatus.value = commentStatus.value.copy(
                        commentCount = response.data.total
                    )
                }
            }
            is RemoteResult.Error->{
                _commentStatus.value = commentStatus.value.copy(
                    commentStatus = NetworkStatus.Failed(response.exception.message.toString())
                )
                _eventFlow.emit(MusicPlayerStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }

    /**
     * 楼层评论*/
    private suspend fun getFloorComments(commentId: Long){
        val response = baseApiCall { service.getFloorComments(
            parentCommentId = commentId,
            id = _uiStatus.value.musicID.toString(),
            type = 0,
            time = if (_commentStatus.value.floorComments.isNotEmpty()) _commentStatus.value.floorComments[_commentStatus.value.floorComments.size-1].time else 0L,
            offset = floorOffset*limit,
            limit = limit
        ) }
        when(response){
            is RemoteResult.Success->{
                val comments:MutableList<CommentBean> = mutableListOf()
                comments.addAll(response.data.data.bestComments ?: emptyList())
                comments.addAll(response.data.data.comments ?: emptyList())
                _commentStatus.value.floorComments.addAll(comments)
                _commentStatus.value = _commentStatus.value.copy(
                    ownFloorComment = response.data.data.ownerComment,
                    floorCommentStatus = NetworkStatus.Successful
                )
                if (response.data.data.totalCount != 0L){
                    _commentStatus.value = _commentStatus.value.copy(
                        floorCommentCount =response.data.data.totalCount
                    )
                }
            }
            is RemoteResult.Error->{
                _commentStatus.value = _commentStatus.value.copy(
                    floorCommentStatus = NetworkStatus.Failed(response.exception.message.toString())
                )
                _eventFlow.emit(MusicPlayerStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }

    /**
     * 给评论点赞*/
    private suspend fun getAgreeComment(
        commentId: Long,
        index:Int,
        isFloor:Boolean
    ){
        val t = if (!isFloor){
            if (_commentStatus.value.comments[index].liked) 0 else 1
        }else{
            if (_commentStatus.value.floorComments[index].liked) 0 else 1
        }
        when(val response = baseApiCall { service.getAgreeComment(cid = commentId, id = _uiStatus.value.musicID.toString(),t = t, type = 0) }){
            is RemoteResult.Success->{
                try {
                    if (!isFloor){
                        _commentStatus.value.comments[index] = _commentStatus.value.comments[index].copy(
                            liked = !_commentStatus.value.comments[index].liked
                        )
                    }else{
                        _commentStatus.value.floorComments[index] = _commentStatus.value.floorComments[index].copy(
                            liked = !_commentStatus.value.floorComments[index].liked
                        )
                    }
                }catch (e:NullPointerException){
                    _eventFlow.emit(MusicPlayerStatus.NetworkFailed(e.message.toString()))
                }
            }
            is RemoteResult.Error->{
                _eventFlow.emit(MusicPlayerStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }


    /**
     * 发送评论*/
    private suspend fun sendComment(
        commentId: Long = 0L,
        t:Int,
        content:String
    ){
        val response = baseApiCall { service.getSendComment(
            id = _uiStatus.value.musicID.toString(),
            commentId = commentId,
            t = t,
            type = 0,
            content = content
        ) }

        when(response){
            is RemoteResult.Success->{
                if (response.data.code == 200){
                    if (t == 1){
                        //资源评论-添加到第一个评论
                        _commentStatus.value.comments.add(0,response.data.comment)
                        _commentStatus.value = _commentStatus.value.copy(
                            commentCount = _commentStatus.value.commentCount+1
                        )
                    }else{
                        //楼层回复
                        _commentStatus.value.floorComments.add(0,response.data.comment)
                        _commentStatus.value = _commentStatus.value.copy(
                            floorCommentCount = _commentStatus.value.floorCommentCount+1
                        )
                    }
                    _eventFlow.emit(MusicPlayerStatus.Message("Comment Successful!"))
                }else{
                    _eventFlow.emit(MusicPlayerStatus.Message("Comment Failed!,The error code is ${response.data.code}"))
                }
            }

            is RemoteResult.Error->{
                _eventFlow.emit(MusicPlayerStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }
    /**
     * 解析歌词*/
    private fun loadLyric(lyric: String) {
        val lyrics = LyricUtil.parseLyric(lyric)
        if (lyric == null || lyric.isEmpty()) {
            val list: List<LyricBean> = listOf(LyricBean(0L, "Error parsing lyrics!"))
            _uiStatus.value = uiStatus.value.copy(lyrics = list)
        } else {
            _uiStatus.value = uiStatus.value.copy(lyrics = lyrics!!)
        }
    }

    /**
     * 匹配歌词
     * 匹配与当前duration想对应的歌词行*/
    private fun matchLyric(duration:Long):Int{
        val lyric = _uiStatus.value.lyrics
        if (lyric.isNotEmpty()){
            var low = 0
            var high = lyric.size
            while (low <= high){
                val mid = (low + high) / 2
                if (duration < lyric[mid].time){
                    high = mid - 1
                }else{
                    if (mid + 1 >= lyric.size || duration < lyric[mid+1].time)
                        return mid
                    low = mid + 1
                }
            }
        }
        return 0
    }

    /**
     * 获取歌曲的歌词*/
    private suspend fun getMusicLyric(id:Long){
        when(val response = baseApiCall { service.getMusicLyric(id) }){
            is RemoteResult.Success->{
                //获取歌词成功
                if (response.data.code == 200 && response.data.lrc != null){
                    loadLyric(response.data.lrc.lyric)
                }else{
                    /**
                     * 从电台处进入，此为纯音乐，没有歌词*/
                    val list: List<LyricBean> = listOf(LyricBean(0L, "纯音乐，没有歌词!"))
                    _uiStatus.value = uiStatus.value.copy(lyrics = list)
                }
            }
            is  RemoteResult.Error->{
                //网络请求失败
                _eventFlow.emit(MusicPlayerStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }

    private suspend fun getSongDetail(id: Long){
        when(val response = baseApiCall { service.getMusicDetail(id) }){
            is RemoteResult.Success->{
                _uiStatus.value = uiStatus.value.copy(
                    artist = response.data.songs[0].ar[0].name,
                    name = response.data.songs[0].name,
                    cover = response.data.songs[0].al.picUrl,
                    musicID = response.data.songs[0].id,
                    totalDuration = transformTime(response.data.songs[0].dt),
                    isPlaying = musicServiceHandler.playState == PlayState.Successful
                )
            }
            is RemoteResult.Error->{
                _eventFlow.emit(MusicPlayerStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }
    /**
     * 获取播放列表*/
    private fun getPlaylist(){
        job?.cancel()
        job = musicUseCase.queryAll().onEach {
            _uiStatus.value = uiStatus.value.copy(playlist = it)
        }.launchIn(viewModelScope)
    }


    private fun calculateProgress(curProgress:Long,duration:Long){
        val progress = if (curProgress > 0) (curProgress.toFloat() / duration.toFloat())*100f else 0f
        val progressString = transformTime(curProgress)
        _uiStatus.value = uiStatus.value.copy(
            progress = progress,
            currentDuration = progressString
        )
    }
}


private fun transformTime(durationTime: Long):String{
    val date = Date(durationTime)
    val format = SimpleDateFormat("mm:ss", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("UTC+8");
    return format.format(date)
}
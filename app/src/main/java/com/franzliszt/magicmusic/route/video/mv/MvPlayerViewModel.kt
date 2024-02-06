package com.franzliszt.magicmusic.route.video.mv

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.MainActivity
import com.franzliszt.magicmusic.MainActivity.Companion.setScreenOrientation
import com.franzliszt.magicmusic.bean.comment.CommentBean
import com.franzliszt.magicmusic.bean.mv.MvBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.network.RemoteResult
import com.franzliszt.magicmusic.network.baseApiCall
import com.franzliszt.magicmusic.parm.Constants
import com.franzliszt.magicmusic.route.musicplayer.service.AudioPlayerEvent
import com.franzliszt.magicmusic.route.playlist.BottomSheetScreen
import com.franzliszt.magicmusic.route.playlist.CommentUIStatus
import com.franzliszt.magicmusic.route.playlist.NetworkStatus
import com.franzliszt.magicmusic.route.playlist.PlaylistEvent
import com.franzliszt.magicmusic.route.playlist.PlaylistStatus
import com.franzliszt.magicmusic.route.video.service.VideoPlayerService
import com.franzliszt.magicmusic.tool.BitmapUtil
import com.franzliszt.magicmusic.tool.BitmapUtil.Companion.saveToPhoto
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MvPlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val service: MusicApiService
):ViewModel() {
    private val _uiState = mutableStateOf(MvPlayerUIState())
    val uiState:State<MvPlayerUIState> = _uiState

    private val _eventFlow = MutableSharedFlow<MvPlayerState>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val mediaItem:MediaItem.Builder = MediaItem.Builder()

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    val mediaController = mutableStateOf<MediaController?>(null)

    /**
     * 评论相关*/
    private val _commentStatus = mutableStateOf(CommentUIStatus())
    val commentStatus:State<CommentUIStatus> = _commentStatus

    //BottomSheet视图状态
    var bottomSheetScreen = mutableStateOf<BottomSheetScreen>(BottomSheetScreen.PlaylistComments)

    val floorComments = mutableStateListOf<CommentBean>()
    val comments = mutableStateListOf<CommentBean>()

    private var currentFloorCommentId = 0L //当前楼层评论ID

    private var offset = -1 //评论页数
    private var floorOffset = -1 //评论页数
    private val limit = 30 //评论每次加载数量

    private val job: Job? = null

    init {
        initController()
        val id = savedStateHandle.get<Long>(Constants.MvID)
        if (id != null){
            viewModelScope.launch(Dispatchers.Main){
                _uiState.value = uiState.value.copy(id = id)
                getMvURL(id)
                getMvDetailInfo(id)
                getSimilarMv(id)
            }
        }
    }

    private val listener = object :Player.Listener{
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            viewModelScope.launch(Dispatchers.Main){
                if (isPlaying){
                    startPlayer()
                }else{
                    stopPlayer()
                }
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when(playbackState){
                ExoPlayer.STATE_READY->{
                    _uiState.value = uiState.value.copy(
                        isPlaying = true
                    )
                }

                ExoPlayer.STATE_ENDED->{
                    _uiState.value = uiState.value.copy(
                        isPlaying = false
                    )
                }

                else->Unit
            }
        }
    }

    /**
     * 开启服务
     * 与Service中的Exoplayer进行绑定*/
    private fun initController() {
        controllerFuture = MediaController.Builder(APP.context, SessionToken(APP.context, ComponentName(APP.context, VideoPlayerService::class.java))).buildAsync()
        controllerFuture.addListener({
            mediaController.value = controllerFuture.get()
            mediaController.value?.addListener(listener)
        }, ContextCompat.getMainExecutor(APP.context))
    }


    private fun setMediaItemUrl(url:String){
        mediaController.value?.let {
            it.setMediaItem(mediaItem.setUri(url).build())
            if (!it.isPlaying){
                it.prepare()
                it.playWhenReady = true
            }
        }
    }

    private fun setMediaInfo(bean:MvBean){
        mediaController.value?.let {
            it.setMediaItem(
                mediaItem.setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(bean.name)
                        .setSubtitle(bean.artistName)
                        .setArtworkUri(bean.cover.toUri())
                        .build()
                ).build()
            )
            if (!it.isPlaying){
                it.prepare()
                it.playWhenReady = true
            }
        }
    }

    fun onEvent(event: PlaylistEvent){
        viewModelScope.launch(Dispatchers.IO) {
            when(event){
                /**
                 * 打开歌单或者专辑评论*/
                is PlaylistEvent.OpenPlaylistComment->{
                    bottomSheetScreen.value = BottomSheetScreen.PlaylistComments
                    if (_commentStatus.value.commentStatus is NetworkStatus.Waiting){
                        //如果没有加载过评论，则网络请求进行加载，否则不进行重复加载
                        onEvent(PlaylistEvent.NextCommentPage)
                    }
                    _eventFlow.emit(MvPlayerState.OpenComment)
                }

                is PlaylistEvent.NextCommentPage->{
                    //评论划到了底部，增加下一页数据
                    if ((_commentStatus.value.commentStatus is NetworkStatus.Waiting) || (comments.size < _commentStatus.value.commentCount)){
                        offset++
                        getMvComments(_uiState.value.id)
                    }else{
                        //评论已经全部加载了
                        _eventFlow.emit(MvPlayerState.Message("It's already the end, there's nothing more！"))
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
                    floorComments.clear()
                    _commentStatus.value = _commentStatus.value.copy(floorCommentStatus = NetworkStatus.Waiting)
                    bottomSheetScreen.value = BottomSheetScreen.FloorComments
                    onEvent(PlaylistEvent.NextFloorCommentPage)
                    _eventFlow.emit(MvPlayerState.OpenComment)
                }

                is PlaylistEvent.NextFloorCommentPage->{
                    //评论划到了底部，增加下一页数据
                    if ((_commentStatus.value.floorCommentStatus is NetworkStatus.Waiting) || (floorComments.size < _commentStatus.value.floorCommentCount)){
                        ++floorOffset
                        getFloorComments(currentFloorCommentId)
                    }else{
                        //评论已经全部加载了
                        _eventFlow.emit(MvPlayerState.Message("It's already the end, there's nothing more！"))
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
                        _eventFlow.emit(MvPlayerState.Message("The input content cannot be empty!"))
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
                        _eventFlow.emit(MvPlayerState.Message("The input content cannot be empty!"))
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
                else-> Unit
            }
        }
    }

    /**
     * 当处于播放状态时
     * 每隔500ms计算一次当前播放进度，并及时刷新到UI*/
    private suspend fun startPlayer() = job.run {
        while(true){
            delay(500L)
            mediaController.value?.let {
                val _progress = if (it.currentPosition > 0 ) (it.currentPosition.toFloat() / it.duration.toFloat()) * 100f else 0f
                _uiState.value = uiState.value.copy(
                    progress = _progress,
                    currentPosition = it.currentPosition
                )
            }
        }
    }

    private fun stopPlayer() =  job?.cancel()

    fun onPlayEvent(event: MvPlayerEvent){
        viewModelScope.launch {
            when(event){
                is MvPlayerEvent.Favorite-> getAgreeResource()

                is MvPlayerEvent.ShowControlPanel->{
                    _uiState.value = uiState.value.copy(
                        isVisibility = !_uiState.value.isVisibility
                    )
                    Log.i("ShowControlPanel","${_uiState.value.isVisibility}")
                }

                is MvPlayerEvent.PlayOrPause->{
                    /**
                     * 播放和暂停事件切换*/
                    mediaController.value?.let {
                        _uiState.value = uiState.value.copy(
                            isPlaying = !it.isPlaying
                        )
                        if (it.isPlaying){
                            it.pause()
                            stopPlayer()
                        }else{
                            it.play()
                            startPlayer()
                        }
                    }
                }

                is MvPlayerEvent.FullScreen->{
                    with(MainActivity.parentThis){
                        if (_uiState.value.isFullScreen){
                            //纵向
                            this.setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        }else{
                            //横向
                            setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                        }
                    }
                    _uiState.value = uiState.value.copy(
                        isFullScreen = !_uiState.value.isFullScreen
                    )
                }

                is MvPlayerEvent.ChangeProgress->{
                    /**
                     * 滑动进度条，改变播放进度*/
                    mediaController.value?.let {
                        it.seekTo(((it.duration * event.progress) / 100f).toLong())
                        if (!it.isPlaying){
                            it.play()
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取Mv评论*/
    private suspend fun getMvComments(id: Long){
        val response = baseApiCall { service.getMvComments(
            id = id,
            time = if (comments.isNotEmpty()) comments[comments.size-1].time else 0L,
            offset = offset*limit,
            limit = limit
        ) }
        when (response) {
            is RemoteResult.Success -> {
                val _comments: MutableList<CommentBean> = _commentStatus.value.comments
                _comments.addAll(response.data.topComments ?: emptyList())
                _comments.addAll(response.data.hotComments ?: emptyList())
                _comments.addAll(response.data.comments ?: emptyList())
                comments.addAll(_comments)
                _commentStatus.value = _commentStatus.value.copy(
                    commentStatus = NetworkStatus.Successful
                )
                if (response.data.total != 0L) {
                    _commentStatus.value = _commentStatus.value.copy(
                        commentCount = response.data.total
                    )
                }
            }

            is RemoteResult.Error -> {
                _commentStatus.value = _commentStatus.value.copy(
                    commentStatus = NetworkStatus.Failed(response.exception.message.toString())
                )
                _eventFlow.emit(MvPlayerState.Message(response.exception.message.toString()))
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
            if (comments[index].liked) 0 else 1
        }else{
            if (floorComments[index].liked) 0 else 1
        }
        when (val response = baseApiCall {
            service.getAgreeComment(
                cid = commentId,
                id = _uiState.value.id.toString(),
                t = t,
                type = 1
            )
        }) {
            is RemoteResult.Success -> {
                try {
                    if (!isFloor) {
                        comments[index] = comments[index].copy(
                            liked = !comments[index].liked
                        )
                    } else {
                        floorComments[index] = floorComments[index].copy(
                            liked = !floorComments[index].liked
                        )
                    }
                } catch (e: NullPointerException) {
                    _eventFlow.emit(MvPlayerState.Message(e.message.toString()))
                }
            }

            is RemoteResult.Error -> {
                _eventFlow.emit(MvPlayerState.Message(response.exception.message.toString()))
            }
        }
    }

    /**
     * 楼层评论*/
    private suspend fun getFloorComments(commentId: Long){
        val response = baseApiCall { service.getFloorComments(
            parentCommentId = commentId,
            id = _uiState.value.id.toString(),
            type = 1,
            time = if (floorComments.isNotEmpty()) floorComments[floorComments.size-1].time else 0L,
            offset = floorOffset*limit,
            limit = limit
        ) }
        when(response){
            is RemoteResult.Success->{
                val _comments:MutableList<CommentBean> = mutableListOf()
                _comments.addAll(response.data.data.bestComments ?: emptyList())
                _comments.addAll(response.data.data.comments ?: emptyList())
                floorComments.addAll(_comments)
                _commentStatus.value = _commentStatus.value.copy(
                    ownFloorComment = response.data.data.ownerComment,
                    floorCommentStatus = NetworkStatus.Successful
                )
                if (response.data.data.totalCount != 0L){
                    _commentStatus.value = _commentStatus.value.copy(
                        floorCommentCount = response.data.data.totalCount
                    )
                }
            }
            is RemoteResult.Error->{
                _commentStatus.value = _commentStatus.value.copy(
                    floorCommentStatus = NetworkStatus.Failed(response.exception.message.toString())
                )
                _eventFlow.emit(MvPlayerState.Message(response.exception.message.toString()))
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
            id = _uiState.value.id.toString(),
            commentId = commentId,
            t = t,
            type = 1,
            content = content
        ) }

        when(response){
            is RemoteResult.Success->{
                if (response.data.code == 200){
                    if (t == 1){
                        //资源评论-添加到第一个评论
                        comments.add(0,response.data.comment)
                        _commentStatus.value = commentStatus.value.copy(
                            commentCount = _commentStatus.value.commentCount+1
                        )
                    }else{
                        //楼层回复
                        floorComments.add(0,response.data.comment)
                        _commentStatus.value = _commentStatus.value.copy(
                            floorCommentCount = _commentStatus.value.floorCommentCount+1
                        )
                    }
                    _eventFlow.emit(MvPlayerState.Message("Comment Successful!"))
                }else{
                    _eventFlow.emit(MvPlayerState.Message("Comment Failed!,The error code is ${response.data.code}"))
                }
            }

            is RemoteResult.Error->{
                _eventFlow.emit(MvPlayerState.Message(response.exception.message.toString()))
            }
        }
    }

    /**
     * 获取Mv地址*/
    private suspend fun getMvURL(id:Long){
        val response = baseApiCall { service.getMvURL(id = id)  }
        when(response){
            is RemoteResult.Success->{
                if (response.data.code == 200 && response.data.data.url != null){
                    _uiState.value = uiState.value.copy(
                        id = id,
                        url = response.data.data.url
                    )
                    setMediaItemUrl(response.data.data.url)
                }else{
                    _eventFlow.emit(MvPlayerState.Message("Happen exception,The error code is ${response.data.code}!"))
                }
            }

            is RemoteResult.Error->{
                _eventFlow.emit(MvPlayerState.Message(response.exception.message.toString()))
            }
        }
    }

    /**
     * 获取Mv详细信息*/
    private suspend fun getMvDetailInfo(id:Long){
        val response = baseApiCall { service.getMvDetailInfo(mvid = id)  }
        when(response){
            is RemoteResult.Success->{
                if (response.data.code == 200){
                    _uiState.value = uiState.value.copy(
                        mvInfo = response.data.data,
                        isFavorite = response.data.subed
                    )
                    setMediaInfo(response.data.data)
                }else{
                    _eventFlow.emit(MvPlayerState.Message("Happen exception,The error code is ${response.data.code}!"))
                }
            }

            is RemoteResult.Error->{
                _eventFlow.emit(MvPlayerState.Message(response.exception.message.toString()))
            }
        }
    }

    /**
     * 获取相似Mv*/
    private suspend fun getSimilarMv(id:Long){
        val response = baseApiCall { service.getSimilarMvs(mvid = id)  }
        when(response){
            is RemoteResult.Success->{
                if (response.data.code == 200){
                    _uiState.value = uiState.value.copy(
                        similarMvs = response.data.mvs,
                        similarState = NetworkStatus.Successful
                    )
                }else{
                    _uiState.value = uiState.value.copy(
                        similarState = NetworkStatus.Failed("Happen exception,The error code is ${response.data.code}!")
                    )
                    _eventFlow.emit(MvPlayerState.Message("Happen exception,The error code is ${response.data.code}!"))
                }
            }

            is RemoteResult.Error->{
                _uiState.value = uiState.value.copy(
                    similarState = NetworkStatus.Failed(response.exception.message.toString())
                )
                _eventFlow.emit(MvPlayerState.Message(response.exception.message.toString()))
            }
        }
    }

    /**
     * 给资源点赞*/
    private suspend fun getAgreeResource(){
        if (_uiState.value.mvInfo == null)return
        val t = if (_uiState.value.isFavorite) 0 else 1
        val count = if (_uiState.value.isFavorite) _uiState.value.mvInfo!!.subCount - 1 else _uiState.value.mvInfo!!.subCount + 1
        when (val response = baseApiCall {
            service.getFavoriteResource(
                id = _uiState.value.id.toString(),
                t = t,
                type = 1
            )
        }) {
            is RemoteResult.Success -> {
                try {
                    if (response.data.code == 200){
                        _uiState.value = uiState.value.copy(
                            isFavorite = !_uiState.value.isFavorite
                        )
                        _uiState.value.mvInfo!!.subCount = count
                    }
                } catch (e: NullPointerException) {
                    _eventFlow.emit(MvPlayerState.Message(e.message.toString()))
                }
            }

            is RemoteResult.Error -> {
                _eventFlow.emit(MvPlayerState.Message(response.exception.message.toString()))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        MediaController.releaseFuture(controllerFuture)
        mediaController.value?.release()
    }
}
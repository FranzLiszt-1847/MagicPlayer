package com.franzliszt.magicmusic.route.playlist

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.franzliszt.magicmusic.bean.playlist.Playlist
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.network.RemoteResult
import com.franzliszt.magicmusic.network.baseApiCall
import com.franzliszt.magicmusic.parm.Constants
import com.franzliszt.magicmusic.tool.GsonFormat
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.franzliszt.magicmusic.bean.comment.CommentBean
import com.franzliszt.magicmusic.bean.recommend.songs.DailySong
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.datapaging.creator
import com.franzliszt.magicmusic.route.musicplayer.service.AudioPlayerEvent
import com.franzliszt.magicmusic.route.musicplayer.service.MusicServiceHandler
import com.franzliszt.magicmusic.tool.BitmapUtil
import com.franzliszt.magicmusic.tool.BitmapUtil.Companion.saveToPhoto
import com.franzliszt.magicmusic.usecase.song.MusicUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.internal.notifyAll

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val service: MusicApiService,
    private val savedStateHandle: SavedStateHandle,
    private val musicServiceHandler: MusicServiceHandler,
    private val musicUseCase: MusicUseCase
):ViewModel() {

    val maxTopBarHeight: Dp
        @Composable
        get() = (LocalConfiguration.current.screenHeightDp * 0.4).dp

    val minTopBarHeight = 70.dp

    private val _uiStatus = mutableStateOf(PlaylistUIStatus())
    val uiStatus:State<PlaylistUIStatus> = _uiStatus

    private val _commentStatus = mutableStateOf(CommentUIStatus())
    val commentStatus:State<CommentUIStatus> = _commentStatus

    //BottomSheet视图状态
    var bottomSheetScreen = mutableStateOf<BottomSheetScreen>(BottomSheetScreen.PlaylistComments)

    private val _eventFlow = MutableSharedFlow<PlaylistStatus>()
    val eventFlow = _eventFlow.asSharedFlow()

    val floorComments = mutableStateListOf<CommentBean>()
    val comments = mutableStateListOf<CommentBean>()

    private var currentFloorCommentId = 0L //当前楼层评论ID

    private var offset = -1 //评论页数
    private var floorOffset = -1 //评论页数
    private val limit = 30 //评论每次加载数量


    init {
        viewModelScope.launch {
            savedStateHandle.get<Long>(Constants.PlaylistID)?.let {
                _uiStatus.value = uiStatus.value.copy(id = it)
            }
            savedStateHandle.get<Boolean>(Constants.IsPlaylist)?.let {
                _uiStatus.value = uiStatus.value.copy(isPlaylist = it)
            }
            if (_uiStatus.value.isPlaylist){
                //歌单
                getPlaylistDetail(
                    _uiStatus.value.id,
                    onTrack = {
                        getPlaylist(_uiStatus.value.id, it)
                    }
                )
            }else{
                //专辑
                getAlbum(_uiStatus.value.id)
            }
        }
    }


    fun onEvent(event:PlaylistEvent){
        viewModelScope.launch {
            when(event){
                is PlaylistEvent.IsShowDialog->{ _uiStatus.value = uiStatus.value.copy(isShowDialog = !_uiStatus.value.isShowDialog) }
                /**
                 * 保存图片到相册*/
                is PlaylistEvent.SavePhoto->{
                    val bitmap = BitmapUtil.toBitmap(url = _uiStatus.value.cover)
                    if (bitmap != null){
                        val uri = bitmap.saveToPhoto("${_uiStatus.value.name}${System.currentTimeMillis()}.jpg",null)
                        if (uri != null){
                            _eventFlow.emit(PlaylistStatus.TransformResult("Save Successful!"))
                        }else{
                            _eventFlow.emit(PlaylistStatus.TransformResult("Save Failed!"))
                        }
                    }else{
                        _eventFlow.emit(PlaylistStatus.TransformResult("Save Failed!"))
                    }
                }
                /**
                 * 打开歌单或者专辑评论*/
                is PlaylistEvent.OpenPlaylistComment->{
                    bottomSheetScreen.value = BottomSheetScreen.PlaylistComments
                    if (_commentStatus.value.commentStatus is NetworkStatus.Waiting){
                        //如果没有加载过评论，则网络请求进行加载，否则不进行重复加载
                        onEvent(PlaylistEvent.NextCommentPage)
                    }
                    _eventFlow.emit(PlaylistStatus.OpenComment)
                }

                is PlaylistEvent.NextCommentPage->{
                    //评论划到了底部，增加下一页数据
                    if ((_commentStatus.value.commentStatus is NetworkStatus.Waiting) || (comments.size < _commentStatus.value.commentCount)){
                        offset++
                        if (_uiStatus.value.isPlaylist){
                            getPlaylistComments(_uiStatus.value.id)
                        }else{
                            getAlbumComments(_uiStatus.value.id)
                        }
                    }else{
                        //评论已经全部加载了
                        _eventFlow.emit(PlaylistStatus.Without("It's already the end, there's nothing more！"))
                    }
                }

                /**
                 * 点赞或取消点赞*/
                is PlaylistEvent.AgreeComment->{
                    val type = if(_uiStatus.value.isPlaylist) 2 else 3
                    getAgreeComment(event.id,event.index,type,event.isFloor)
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
                    _eventFlow.emit(PlaylistStatus.OpenComment)
                }

                is PlaylistEvent.NextFloorCommentPage->{
                    //评论划到了底部，增加下一页数据
                    if ((_commentStatus.value.floorCommentStatus is NetworkStatus.Waiting) || (floorComments.size < _commentStatus.value.floorCommentCount)){
                        ++floorOffset
                        getFloorComments(currentFloorCommentId)
                    }else{
                        //评论已经全部加载了
                        _eventFlow.emit(PlaylistStatus.Without("It's already the end, there's nothing more！"))
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
                        _eventFlow.emit(PlaylistStatus.Without("The input content cannot be empty!"))
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
                        _eventFlow.emit(PlaylistStatus.Without("The input content cannot be empty!"))
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

                /**
                 * 如果与当前播放歌单不一致，则进行全部替换
                 * 如果是从专辑类别处点入，单曲没有图片资源，则用专辑封面代替*/
                is PlaylistEvent.PlayMusicItem->{
                    if (_uiStatus.value.songs[event.index].id != musicServiceHandler.getCurrentSongId()){
                        musicUseCase.deleteAll()
                        _uiStatus.value.songs.map {
                            SongMediaBean(
                                createTime = System.currentTimeMillis(),
                                songID = it.id,
                                songName = it.name,
                                cover = if (_uiStatus.value.isPlaylist) it.al.picUrl else _uiStatus.value.cover,
                                artist = it.ar[0].name,
                                url = "",
                                isLoading = false,
                                duration = 0L,
                                size = ""
                            )
                        }.also {
                            musicServiceHandler.setMediaItems(it)
                            musicUseCase.insertAll(it)
                        }
                    }
                    musicServiceHandler.onEvent(AudioPlayerEvent.Group(_uiStatus.value.songs[event.index].id))
                }
            }
        }
    }


    /**
     * 获取专辑相关数据*/
    private suspend fun getAlbum(id:Long) {
        when(val response = baseApiCall { service.getAlbumSongs(id) }){
            is RemoteResult.Success->{
                _uiStatus.value = uiStatus.value.copy(
                    songs = response.data.songs,
                    cover = response.data.album.blurPicUrl,
                    name = response.data.album.name,
                    description = response.data.album.description,
                    shareCount = response.data.album.info.shareCount,
                    favoriteCount = response.data.album.info.likedCount,
                    commentCount = response.data.album.info.commentCount,
                    artist = response.data.album.artist.name,
                    company = response.data.album.company,
                    type = response.data.album.subType,
                    isFollow = response.data.album.info.liked
                )
            }
            is RemoteResult.Error->{
                _eventFlow.emit(PlaylistStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }

    /**
     * 获取歌单的所有歌曲*/
    private fun getPlaylist(id: Long,trackCount:Int){
        viewModelScope.launch {
            when(val response = baseApiCall { service.getPlaylistSongs(id = id, offset = 0, limit = trackCount) }){
                is RemoteResult.Success->{
                    _uiStatus.value = uiStatus.value.copy(
                        songs = response.data.songs
                    )
                }
                is RemoteResult.Error->{
                    _eventFlow.emit(PlaylistStatus.NetworkFailed(response.exception.message.toString()))
                }
            }
        }
    }

    /**
     * 获取歌单详情*/
    private fun getPlaylistDetail(
        id:Long,
        onTrack: (Int)->Unit
    ){
        service.getPlaylistDetail(id).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val body = response.body().toString()
                if (body.isNotEmpty()){
                    val jsonObject = JSONObject(body)
                    val playlist = jsonObject.getJSONObject("playlist")
                    val bean = GsonFormat.fromJson(playlist.toString(),Playlist::class.java)
                    if (bean != null){
                        _uiStatus.value = uiStatus.value.copy(
                            cover = bean.coverImgUrl,
                            name = bean.name,
                            description = if (bean.description == null) "" else bean.description,
                            shareCount = bean.shareCount,
                            favoriteCount = bean.subscribedCount,
                            commentCount = bean.commentCount,
                            artist = bean.creator.nickname,
                            tags = bean.tags,
                            isFollow = bean.ordered
                        )
                        onTrack(bean.trackCount)
                    }
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
               viewModelScope.launch {
                   _eventFlow.emit(PlaylistStatus.NetworkFailed(t.message.toString()))
               }
            }
        })
    }

    /**
     * 获取歌单评论*/
    private suspend fun getPlaylistComments(id: Long){
        val response = baseApiCall { service.getPlaylistComments(
            id = id,
            time = if (comments.isNotEmpty()) comments[comments.size-1].time else 0L,
            offset = offset*limit,
            limit = limit
        ) }
        when(response){
            is RemoteResult.Success->{
                //下列字段可能会返回null或者返回json中没有该字段，故对其判空处理
                val _comments:MutableList<CommentBean> = mutableListOf()
                _comments.addAll(response.data.topComments ?: emptyList())
                _comments.addAll(response.data.hotComments ?: emptyList())
                _comments.addAll(response.data.comments ?: emptyList())
                comments.addAll(_comments)
                _commentStatus.value = _commentStatus.value.copy(
                    commentStatus = NetworkStatus.Successful
                )
                if (response.data.total != 0L){
                    _commentStatus.value = commentStatus.value.copy(
                        commentCount = response.data.total
                    )
                }
            }
            is RemoteResult.Error->{
                _commentStatus.value = _commentStatus.value.copy(
                    commentStatus = NetworkStatus.Failed(response.exception.message.toString())
                )
                _eventFlow.emit(PlaylistStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }

    /**
     * 获取专辑评论*/
    private suspend fun getAlbumComments(id: Long){
        val response = baseApiCall { service.getAlbumComments(
            id = id,
            time = if (comments.isNotEmpty()) comments[comments.size-1].time else 0L,
            offset = offset*limit,
            limit = limit
        ) }
        when(response){
            is RemoteResult.Success->{
                val comments:MutableList<CommentBean> = _commentStatus.value.comments
                comments.addAll(response.data.topComments ?: emptyList())
                comments.addAll(response.data.hotComments ?: emptyList())
                comments.addAll(response.data.comments ?: emptyList())
                _commentStatus.value = _commentStatus.value.copy(
                    commentStatus = NetworkStatus.Successful,
                    comments = comments
                )
            }
            is RemoteResult.Error->{
                _commentStatus.value = _commentStatus.value.copy(
                    commentStatus = NetworkStatus.Failed(response.exception.message.toString())
                )
                _eventFlow.emit(PlaylistStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }

    /**
     * 给评论点赞*/
    private suspend fun getAgreeComment(
        commentId: Long,
        index:Int,
        type:Int,
        isFloor:Boolean
    ){
        val t = if (!isFloor){
            if (comments[index].liked) 0 else 1
        }else{
            if (floorComments[index].liked) 0 else 1
        }
        when(val response = baseApiCall { service.getAgreeComment(cid = commentId, id = _uiStatus.value.id.toString(),t = t, type = type) }){
            is RemoteResult.Success->{
                try {
                    if (!isFloor){
                        comments[index] = comments[index].copy(
                            liked = !comments[index].liked
                        )
                    }else{
                        floorComments[index] = floorComments[index].copy(
                            liked = !floorComments[index].liked
                        )
                    }
                }catch (e:NullPointerException){
                    _eventFlow.emit(PlaylistStatus.NetworkFailed(e.message.toString()))
                }
            }
            is RemoteResult.Error->{
                _eventFlow.emit(PlaylistStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }

    /**
     * 楼层评论*/
    private suspend fun getFloorComments(commentId: Long){
        val type = if(_uiStatus.value.isPlaylist) 2 else 3
        val response = baseApiCall { service.getFloorComments(
            parentCommentId = commentId,
            id = _uiStatus.value.id.toString(),
            type = type,
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
                _eventFlow.emit(PlaylistStatus.NetworkFailed(response.exception.message.toString()))
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
        val type = if(_uiStatus.value.isPlaylist) 2 else 3
        val response = baseApiCall { service.getSendComment(
            id = _uiStatus.value.id.toString(),
            commentId = commentId,
            t = t,
            type = type,
            content = content
        ) }

        when(response){
            is RemoteResult.Success->{
                if (response.data.code == 200){
                    if (t == 1){
                        //资源评论-添加到第一个评论
                        comments.add(0,response.data.comment)
                        _uiStatus.value = uiStatus.value.copy(
                            commentCount = _uiStatus.value.commentCount+1
                        )
                    }else{
                        //楼层回复
                        floorComments.add(0,response.data.comment)
                        _commentStatus.value = _commentStatus.value.copy(
                            floorCommentCount = _commentStatus.value.floorCommentCount+1
                        )
                    }
                    _eventFlow.emit(PlaylistStatus.CommentResult("Comment Successful!"))
                }else{
                    _eventFlow.emit(PlaylistStatus.CommentResult("Comment Failed!,The error code is ${response.data.code}"))
                }
            }

            is RemoteResult.Error->{
                _eventFlow.emit(PlaylistStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }
}
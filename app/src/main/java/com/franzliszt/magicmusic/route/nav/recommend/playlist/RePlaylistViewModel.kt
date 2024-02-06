package com.franzliszt.magicmusic.route.nav.recommend.playlist

import android.annotation.SuppressLint
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.franzliszt.magicmusic.bean.recommend.dayplaylist.RecommendDayPlaylistBean
import com.franzliszt.magicmusic.bean.recommend.playlist.Result
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.network.RemoteResult
import com.franzliszt.magicmusic.network.baseApiCall
import com.franzliszt.magicmusic.tool.GsonFormat
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class RePlaylistViewModel @Inject constructor(private val service: MusicApiService):ViewModel() {
    private val _uiStatus = mutableStateOf(RePlaylistUIStatus())
    val uiStatus: State<RePlaylistUIStatus> = _uiStatus

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
           getRecommendPlaylist()
            getRecommendEveryDayPlaylist(onPlaylist = {
                _uiStatus.value = uiStatus.value.copy(
                    dayPlaylist = it
                )
            })
        }
    }
    /**
     * 获取推荐的歌单*/
    private suspend fun getRecommendPlaylist(){
        val response = baseApiCall { service.getRecommendPlaylist() }
        when(response){
            is RemoteResult.Success->{
                if (response.data.result != null && response.data.code == 200){
                    _uiStatus.value = uiStatus.value.copy(
                        newPlaylist = response.data.result
                    )
                }else{
                    _eventFlow.emit("The error code is ${response.data.code}")
                }
            }
            is RemoteResult.Error->{
                _eventFlow.emit(response.exception.message.toString())
            }
        }
    }

    /**
     * 获取每日推荐的歌单*/
    private fun getRecommendEveryDayPlaylist(onPlaylist:(List<RecommendDayPlaylistBean>)->Unit) = service.getRecommendEveryDayPlaylist().enqueue(
        object : Callback<JsonObject> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val body = response.body().toString()
                if (body.isNotBlank()){
                    val json = JSONObject(body)
                    val code = json.getInt("code")
                    if (code == 200){
                        val recommend = json.getJSONArray("recommend")
                        val list = GsonFormat.fromListJson(recommend.toString(),
                            RecommendDayPlaylistBean::class.java)
                        onPlaylist(list)
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                onPlaylist(emptyList())
            }
        })
}
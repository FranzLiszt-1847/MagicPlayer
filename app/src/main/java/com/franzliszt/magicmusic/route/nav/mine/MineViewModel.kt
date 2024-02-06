package com.franzliszt.magicmusic.route.nav.mine

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.bean.banner.BannersBean
import com.franzliszt.magicmusic.bean.playlist.PlayListBean
import com.franzliszt.magicmusic.bean.playlist.Playlist
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.network.RemoteResult
import com.franzliszt.magicmusic.network.baseApiCall
import com.franzliszt.magicmusic.parm.Constants
import com.franzliszt.magicmusic.route.playlist.NetworkStatus
import com.franzliszt.magicmusic.tool.SharedPreferencesUtil
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MineViewModel @Inject constructor(private val service: MusicApiService):ViewModel() {

    private val _uiStatus = mutableStateOf(MineUIStatus())
    val uiStatus:State<MineUIStatus> = _uiStatus

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getBanner()
            val loginMode = SharedPreferencesUtil.instance.getValue(APP.context, Constants.LoginMode,-1) as Int
            if (loginMode == Constants.QRCodeLoginMode){
                getAccountInfo {
                    APP.userId = it
                }
            }
            getPlaylist(APP.userId)
        }
    }

    //获取banner相关信息
    private suspend fun getBanner(){
        val response = baseApiCall { service.getBanner() }
        when(response){
            is RemoteResult.Success->{
                if (response.data.code == 200 && response.data.banners.isNotEmpty()){
                    _uiStatus.value = uiStatus.value.copy(
                        banners = response.data.banners
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

    //获取用户歌单，包括用户喜欢的歌曲、用户创建的歌单、用户收藏的歌单
    private suspend fun getPlaylist(uid:Long){
        val response = baseApiCall { service.getPlayList(uid,APP.cookie) }
        when(response){
            is RemoteResult.Success->{
                if (response.data.code == 200 && response.data.playlist.isNotEmpty()){
                    response.data.playlist.forEachIndexed { index, playlist ->
                        if (index == 0){
                            //用户喜欢的歌曲
                            _uiStatus.value = uiStatus.value.copy(preferBean = playlist)
                            playlist?.let { _uiStatus.value.mapPlaylist+= Constants.Preference to true }
                        }else if (playlist.userId == APP.userId){
                            //用户创建的歌单，将登录用户ID和创建歌单的用户ID做对比，判断是否为用户所创建的歌单
                            _uiStatus.value.creates.add(playlist)
                            playlist?.let { _uiStatus.value.mapPlaylist+= Constants.Create to true }
                        }else{
                            //用户收藏的歌单
                            _uiStatus.value.favorites.add(playlist)
                            playlist?.let { _uiStatus.value.mapPlaylist+= Constants.Favorite to true }
                        }
                    }
                    _uiStatus.value = uiStatus.value.copy(
                        playlistState = NetworkStatus.Successful
                    )
                }else{
                    _uiStatus.value = uiStatus.value.copy(
                        playlistState = NetworkStatus.Failed("The error code is ${response.data.code}")
                    )
                    _eventFlow.emit("The error code is ${response.data.code}")
                }
            }

            is RemoteResult.Error->{
                _uiStatus.value = uiStatus.value.copy(
                    playlistState = NetworkStatus.Failed(response.exception.message.toString())
                )
                _eventFlow.emit(response.exception.message.toString())
            }
        }
    }

    private fun getAccountInfo(getUserId:(userId:Long)->Unit){
        service.getAccountInfo().enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val body = response.body().toString()
                if (body.isNotBlank()){
                    val json = JSONObject(body)
                    val code = json.getInt("code")
                    if (code == 200){
                        val accountBean = json.getJSONObject("account")
                        accountBean?.let {
                            val userId = accountBean.getLong("id")
                            getUserId(userId)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {

            }
        })
    }
}
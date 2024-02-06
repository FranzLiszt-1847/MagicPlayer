package com.franzliszt.magicmusic.route.nav.container

import android.app.UiModeManager
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.parm.Constants
import com.franzliszt.magicmusic.route.musicplayer.service.AudioPlayState
import com.franzliszt.magicmusic.route.musicplayer.service.AudioPlayerEvent
import com.franzliszt.magicmusic.route.musicplayer.service.MusicServiceHandler
import com.franzliszt.magicmusic.tool.SharedPreferencesUtil
import com.franzliszt.magicmusic.ui.theme.ThemeModeStatus
import com.franzliszt.magicmusic.ui.theme.setCurrentThemeMode
import com.franzliszt.magicmusic.ui.theme.themeState
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContainerViewModel @Inject constructor(
    private val service: MusicApiService,
    private val musicServiceHandler: MusicServiceHandler
):ViewModel() {

    private val _userStatus = mutableStateOf(ContainerUIStatus())
    val userStatus: State<ContainerUIStatus> = _userStatus

    init {
        viewModelScope.launch {
            getAccount {
                _userStatus.value = userStatus.value.copy(
                    imgUrl = it.imgUrl,
                    nickname = it.nickname
                )
            }
            isDarkMode()

            musicServiceHandler.eventFlow.collectLatest {
                when(it){
                    is AudioPlayState.Playing->{
                        _userStatus.value = userStatus.value.copy( isPlaying = it.isPlaying )
                    }

                    else->{}
                }
            }
        }
    }

    /**
     * 判断系统所使用的模式是否为深色模式*/
    private fun isDarkMode(){
        val systemUIMode = APP.context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if (systemUIMode.nightMode == UiModeManager.MODE_NIGHT_YES){
            _userStatus.value = userStatus.value.copy(
                isDark = true
            )
        }else{
            _userStatus.value = userStatus.value.copy(
                isDark = false
            )
        }
    }

    fun onEvent(event: ContainerEvent){
        viewModelScope.launch {
            when(event){
                is ContainerEvent.ChangePlayStatus->{
                    musicServiceHandler.onEvent(AudioPlayerEvent.PlayOrPause)
                }
                is ContainerEvent.Next->{
                    musicServiceHandler.onEvent(AudioPlayerEvent.Next)
                }
                is ContainerEvent.Prior->{
                    musicServiceHandler.onEvent(AudioPlayerEvent.Prior)
                }

                is ContainerEvent.Logout->{
                    SharedPreferencesUtil.instance.putValue(APP.context,Constants.Cookie,"")
                    SharedPreferencesUtil.instance.putValue(APP.context,Constants.UserId,0L)
                }

                is ContainerEvent.UIMode->{
                    if (event.isDark){
                        //开启深色模式
                        themeState.value = ThemeModeStatus.Dark.mode
                        setCurrentThemeMode(ThemeModeStatus.Dark.mode)
                    }else{
                        //关闭深色模式
                        themeState.value = ThemeModeStatus.Light.mode
                        setCurrentThemeMode(ThemeModeStatus.Light.mode)
                    }
                    _userStatus.value = userStatus.value.copy(
                        isDark = event.isDark
                    )
                }
            }
        }
    }

    private fun getAccount(onAccountInfo: (ContainerUIStatus) -> Unit) = service.getAccountInfo().enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val body = response.body().toString()
                if (body.isNotBlank()) {
                    val json = JSONObject(body)
                    val code = json.getInt("code")
                    if (code == 200) {
                        val profile = json.getJSONObject("profile")
                        profile?.let {
                            val url = profile.getString("avatarUrl")
                            val nickname = profile.getString("nickname")
                            onAccountInfo(ContainerUIStatus(url, nickname))
                        }
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {

            }
        })
}
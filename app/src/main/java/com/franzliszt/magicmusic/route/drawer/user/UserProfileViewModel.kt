package com.franzliszt.magicmusic.route.drawer.user

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.network.RemoteResult
import com.franzliszt.magicmusic.network.baseApiCall
import com.franzliszt.magicmusic.parm.Constants
import com.franzliszt.magicmusic.tool.SharedPreferencesUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val service: MusicApiService,
    savedStateHandle: SavedStateHandle
):ViewModel() {
    private val _uiStatus = mutableStateOf(UserProfileUIStatus())
    val uiStatus: State<UserProfileUIStatus> = _uiStatus

    private val _eventFlow = MutableSharedFlow<UserProfileStatus>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            savedStateHandle.get<Long>(Constants.ConsumerID)?.let {
                if (it != 0L){
                    getUserDetailInfo(it)
                }
            }
        }
    }

    /**
     * 对异常情况进行处理了的网络请求
     * 获取用户详细信息*/
    private suspend fun getUserDetailInfo(userId:Long){
        when(val response = baseApiCall { service.getUserDetailInfo(userId) }){
            is RemoteResult.Success->{
                _uiStatus.value = uiStatus.value.copy(
                    profile = response.data
                )
            }
            is RemoteResult.Error->{
                _eventFlow.emit(UserProfileStatus.NetworkFailed(response.exception.message.toString() ?: "Failed to get a response!"))
            }
        }
    }
}
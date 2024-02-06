package com.franzliszt.magicmusic.route.login.pwdlogin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.bean.pwdlogin.PwdLoginBean
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
class PwdLoginViewModel @Inject constructor(private val service: MusicApiService):ViewModel() {
    private val _userStatus = mutableStateOf(PwdLoginUIStatus(
        text = "",
        label = "UserName",
        hint = "Please input phone or email",
        isShowPwd = false
    ))

    private val _passwordStatus = mutableStateOf(PwdLoginUIStatus(
        text = "",
        label = "PassWord",
        hint = "Please input password",
        isShowPwd = false
    ))

    //若使用网易邮箱登录，必须包含其后缀
    private val emailEnd:String = "@163.com"
    //供外部调用
    val userStatus: State<PwdLoginUIStatus> = _userStatus
    val passwordStatus: State<PwdLoginUIStatus> = _passwordStatus

    private val _eventFlow = MutableSharedFlow<PwdLoginStatus>()
    val eventFlow = _eventFlow.asSharedFlow()

     fun onEvent(event:PwdLoginEvent){
         viewModelScope.launch {
             when(event){
                 is PwdLoginEvent.PwdLogin-> {
                         if (_userStatus.value.text.isEmpty() || _passwordStatus.value.text.isEmpty()){
                             _eventFlow.emit(PwdLoginStatus.LoginEmpty)
                         }else if (_userStatus.value.text.contains(emailEnd)){
                             //邮箱登录
                             getEmailLoginInfo(_userStatus.value.text, _passwordStatus.value.text)
                         }else{
                             //手机密码登录
                            getPwdLoginInfo(_userStatus.value.text, _passwordStatus.value.text)
                         }
                 }
                 is PwdLoginEvent.ForgetPassword-> {
                     _eventFlow.emit(PwdLoginStatus.ForgetPassword)
                 }
                 is PwdLoginEvent.ChangeUserName-> {
                     _userStatus.value = userStatus.value.copy(
                         text = event.username
                     )
                 }
                 is PwdLoginEvent.ChangePassword-> {
                     _passwordStatus.value = passwordStatus.value.copy(
                         text = event.password
                     )
                 }
                 is PwdLoginEvent.ChangePwdStatus-> {
                     _passwordStatus.value = passwordStatus.value.copy(
                         isShowPwd = !event.isShowPwd
                     )
                 }
                 is PwdLoginEvent.GoogleLogin-> {
                     _eventFlow.emit(PwdLoginStatus.GoogleLogin)
                 }
                 is PwdLoginEvent.TwitterLogin-> {
                     _eventFlow.emit(PwdLoginStatus.TwitterLogin)
                 }
             }
         }
    }

    /**
    * 登录方式：账号&密码
    * 获取登录信息*/
    private suspend fun getPwdLoginInfo(username:String, password:String){
        val response = baseApiCall { service.getPhoneLogin(username,password) }
        when(response){
            is RemoteResult.Success->{
                if (response.data.code != 200){
                    _eventFlow.emit(PwdLoginStatus.LoginFailed)
                }else{
                    SharedPreferencesUtil.instance.putValue(APP.context, Constants.Cookie,response.data.cookie!!)
                    SharedPreferencesUtil.instance.putValue(APP.context, Constants.UserId, response.data.account.id)
                    _eventFlow.emit(PwdLoginStatus.LoginSuccess)
                }
            }

            is RemoteResult.Error->{
                _eventFlow.emit(PwdLoginStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }

    /**
    * 登录方式：邮箱&密码
    * 获取登录信息*/
    private suspend fun getEmailLoginInfo(username: String,password: String){
        val response = baseApiCall { service.getEmailLogin(username,password) }
        when(response){
            is RemoteResult.Success->{
                if (response.data.code != 200){
                    _eventFlow.emit(PwdLoginStatus.LoginFailed)
                }else{
                    SharedPreferencesUtil.instance.putValue(APP.context, Constants.Cookie,response.data.cookie!!)
                    SharedPreferencesUtil.instance.putValue(APP.context, Constants.UserId, response.data.account.id)
                    _eventFlow.emit(PwdLoginStatus.LoginSuccess)
                }
            }

            is RemoteResult.Error->{
                _eventFlow.emit(PwdLoginStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }

}
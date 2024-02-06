package com.franzliszt.magicmusic.route.login.qrcode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.bean.BaseResponse
import com.franzliszt.magicmusic.bean.qrcode.QRCodeCookieBean
import com.franzliszt.magicmusic.bean.qrcode.QRCodeImgBean
import com.franzliszt.magicmusic.bean.qrcode.QRCodeKeyBean
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.network.RemoteResult
import com.franzliszt.magicmusic.network.baseApiCall
import com.franzliszt.magicmusic.parm.Constants
import com.franzliszt.magicmusic.tool.SharedPreferencesUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@HiltViewModel
class QrCodeViewModel @Inject constructor(private val service: MusicApiService):ViewModel() {

    /**
     * 轮询此接口可获取二维码扫码状态
     * 800 为二维码过期
     * 801 为等待扫码
     * 802 为待确认
     * 803 为授权登录成功(803 状态码下会返回 cookies)
     * 如扫码后返回502,则需加上noCookie参数,如&noCookie=true*/
    enum class QRCodeStatus(val status: Int){Expire(800),Waiting(801),Confirm(802),Success(803),NoCookie(502)}

    private val _bitmapStatus = mutableStateOf(QRCodeLoginUIStatus(bitmap = null))
    val bitmapStatus:State<QRCodeLoginUIStatus> = _bitmapStatus

    private var  unikey:String = ""
    private var count = 10

    private val _eventFlow = MutableSharedFlow<QRCodeLoginStatus>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val qrCodeTimer: Timer = Timer()
    private val timerTask: TimerTask = object : TimerTask(){
        override  fun run() {
            if (count > 0){
                viewModelScope.launch {
                    if (unikey.isNotEmpty()) {
                         getQRCodeLoginStatus(unikey)
                    }
                }
            }else{
                count = 10
                qrCodeTimer.cancel()
            }
            count--
        }
    }
    init {
        viewModelScope.launch {
            exeQRCode()
            //在等待500ms后首次执行task，之后每隔3000ms执行一次task
            qrCodeTimer.schedule(timerTask,500,3000)
        }
    }

    private suspend fun exeQRCode(){
        getQRCodeLoginKey()
    }

    private suspend fun onEvent(bean:QRCodeCookieBean){
        when(bean.code){
            //二维码过期
            QRCodeStatus.Expire.status-> {
                _eventFlow.emit(QRCodeLoginStatus.Expire(bean.message))
                qrCodeTimer.cancel()
            }
            //等待扫描
            QRCodeStatus.Waiting.status-> {
                _eventFlow.emit(QRCodeLoginStatus.Waiting(bean.message))
            }
            //以扫码，待授权
            QRCodeStatus.Confirm.status-> _eventFlow.emit(QRCodeLoginStatus.Confirm(bean.message))

            QRCodeStatus.Success.status->{
                //授权成功,将cookie保存到本地，以备下次可以免去登录，避免重复登录，被网易云盾监测
                SharedPreferencesUtil.instance.putValue(APP.context, Constants.Cookie,bean.cookie)
                _eventFlow.emit(QRCodeLoginStatus.Success(bean.message))
                qrCodeTimer.cancel()
            }
            //加上noCookie参数,如&noCookie=true
            QRCodeStatus.NoCookie.status->{
                _eventFlow.emit(QRCodeLoginStatus.NoCookie(bean.message))
                qrCodeTimer.cancel()
            }
        }
    }

    fun refreshQRCode(){
        viewModelScope.launch {
            count = 10
            exeQRCode()
            _bitmapStatus.value = bitmapStatus.value.copy(refresh = !_bitmapStatus.value.refresh)
            _eventFlow.emit(QRCodeLoginStatus.RefreshQRCode)
        }
    }

    /**
     * 获取QRCODE KEY*/
    private suspend fun getQRCodeLoginKey(){
        val response = baseApiCall { service.getQRCodeLoginKey() }
        when(response){
            is RemoteResult.Success->{
                if (response.data.code == 200){
                    unikey = response.data.data.unikey
                    getQRCodeLoginImg(unikey)
                }else{
                    count = 10
                    qrCodeTimer.cancel()
                    _eventFlow.emit(QRCodeLoginStatus.NetworkFailed("The error code is ${response.data.code}"))
                }
            }

            is RemoteResult.Error->{
                count = 10
                qrCodeTimer.cancel()
                _eventFlow.emit(QRCodeLoginStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }

    /**
     * 根据QRCODE KEY获取二维码*/
    private suspend fun getQRCodeLoginImg(key: String) {
        val response = baseApiCall { service.getQRCodeLoginImg(key) }
        when (response) {
            is RemoteResult.Success -> {
                if (response.data.code == 200){
                    val bitmap = base64ToBitmap(response.data.data.qrimg)
                    _bitmapStatus.value = bitmapStatus.value.copy(bitmap = bitmap)
                }else{
                    count = 10
                    qrCodeTimer.cancel()
                    _eventFlow.emit(QRCodeLoginStatus.NetworkFailed("The error code is ${response.data.code}"))
                }
            }

            is RemoteResult.Error -> {
                count = 10
                qrCodeTimer.cancel()
                _eventFlow.emit(QRCodeLoginStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }

    /**
     * 获取二维码状态*/
    private suspend fun getQRCodeLoginStatus(key: String){
        val response = baseApiCall { service.getQRCodeLoginStatus(key) }
        when (response) {
            is RemoteResult.Success -> {
                onEvent(response.data)
            }

            is RemoteResult.Error -> {
                count = 10
                qrCodeTimer.cancel()
                _eventFlow.emit(QRCodeLoginStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }

    private fun base64ToBitmap(qrCodeUrl: String): Bitmap {
        val decode: ByteArray = Base64.decode(qrCodeUrl.split(",")[1], Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decode, 0, decode.size)
    }
}
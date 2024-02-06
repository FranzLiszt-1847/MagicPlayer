package com.franzliszt.magicmusic.route.drawer.about

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.franzliszt.magicmusic.APP
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AboutViewModel:ViewModel() {

    val tags = AboutTag.values()
    private val CSDN = "https://blog.csdn.net/News53231323"
    private val Github = "https://github.com/FranzLiszt-1847/MagicPlayer"
    private val Gitee = "https://gitee.com/FranzLiszt1847/MagicPlayer"

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow = _eventFlow.asSharedFlow()

    @SuppressLint("QueryPermissionsNeeded", "WrongConstant")
    fun onEvent(key:String){
        val url = when (key) {
            AboutTag.Github.key -> Github
            AboutTag.Gitee.key -> Gitee
            AboutTag.CSDN.key -> CSDN
            else->{ "" }
        }.trim()

        viewModelScope.launch {
            try {
                if (url.startsWith("http") || url.startsWith("https")) {
                    Intent(Intent.ACTION_VIEW,Uri.parse(url)).apply{
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        APP.context.startActivity(this)
                    }
                }else{
                    _eventFlow.emit("The URL format is incorrect！")
                }
            }catch (e:Exception){
                _eventFlow.emit(e.message.toString())
            }
        }
    }

}

private fun getAppVersion():String{
    val manager = APP.context.packageManager
    var version = ""
    try {
        val info = manager.getPackageInfo(APP.context.packageName,0)
        version = info.versionName
    }catch (e: PackageManager.NameNotFoundException){
        version = e.message.toString()
    }
    return version
}

enum class AboutTag(val key:String,var value:String){
    Version("Current Version", getAppVersion()),
    CSDN("CSDN","FranzLiszt1847"),
    Github("Github","FranzLiszt1847"),
    Gitee("Gitee","FranzLiszt")
}

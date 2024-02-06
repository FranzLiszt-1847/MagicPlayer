package com.franzliszt.magicmusic.route.drawer.setting

import androidx.lifecycle.ViewModel
import com.franzliszt.magicmusic.APP

class SettingViewModel:ViewModel() {
    val maxDownloadNum = 3
    val downloadPath = APP.context.getExternalFilesDir("MagicMusicDownload")?.absolutePath ?: ""
}
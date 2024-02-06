package com.franzliszt.magicmusic.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.parm.Constants
import com.franzliszt.magicmusic.tool.SharedPreferencesUtil

@Stable
class MagicMusicScheme(
    backgroundTop: Color,//password page 上方背景颜色
    backgroundCenter: Color,//password page 中间背景颜色
    backgroundBottom: Color,//password page 底部背景颜色
    backgroundEnd: Color,
    defaultIcon: Color,// 默认图标填充颜色
    selectIcon: Color,// 图标被选中填充颜色
    unselectIcon: Color,//图标未选中填充颜色
    textTitle: Color,// 标题内容文字颜色
    textContent: Color,// 正文内容文字颜色
    loginStart: Color,//登录按钮前半部分背景颜色
    loginEnd: Color,//登录按钮后半部分背景颜色
    loginText:Color,//登录按钮文字颜色
    bottomBar: Color,
    searchBar: Color,
    tabSelect: Color,
    tabUnselect: Color,
    highlightColor: Color,
    background:Color,
    grayBackground:Color,
    stableRank:Color,
    upRank:Color,
    downRank:Color,
    white:Color,
    black:Color,
    progressTrackColor:Color
){
    var backgroundTop: Color by mutableStateOf(backgroundTop)
        private set

    var backgroundCenter: Color by mutableStateOf(backgroundCenter)
        private set

    var backgroundBottom: Color by mutableStateOf(backgroundBottom)
        private set

    var backgroundEnd: Color by mutableStateOf(backgroundEnd)
        private set

    var defaultIcon: Color by mutableStateOf(defaultIcon)
        private set

    var selectIcon: Color by mutableStateOf(selectIcon)
        private set

    var unselectIcon: Color by mutableStateOf(unselectIcon)
        private set

    var textTitle: Color by mutableStateOf(textTitle)
        private set

    var textContent: Color by mutableStateOf(textContent)
        private set

    var loginStart: Color by mutableStateOf(loginStart)
        private set

    var loginEnd: Color by mutableStateOf(loginEnd)
        private set

    var loginText: Color by mutableStateOf(loginText)
        private set

    var bottomBar: Color by mutableStateOf(bottomBar)
        private set

    var searchBar: Color by mutableStateOf(searchBar)
        private set

    var tabSelect: Color by mutableStateOf(tabSelect)
        private set

    var tabUnselect: Color by mutableStateOf(tabUnselect)
        private set

    var highlightColor: Color by mutableStateOf(highlightColor)
        private set
    var background: Color by mutableStateOf(background)
        private set

    var grayBackground: Color by mutableStateOf(grayBackground)
        private set

    var stableRank: Color by mutableStateOf(stableRank)
        private set

    var upRank: Color by mutableStateOf(upRank)
        private set

    var downRank: Color by mutableStateOf(downRank)
        private set

    var white: Color by mutableStateOf(white)
        private set

    var black: Color by mutableStateOf(black)
        private set

    var progressTrackColor: Color by mutableStateOf(progressTrackColor)
        private set
}

val DarkColorScheme = MagicMusicScheme(
    backgroundTop = black300,
    backgroundCenter = black100,
    backgroundBottom = black,
    backgroundEnd = music_bg600,
    defaultIcon = grey,
    selectIcon = white,
    unselectIcon = grey,
    textTitle = white,
    textContent = grey,
    loginStart = black,
    loginEnd = black300,
    loginText = red100,
    bottomBar = black200,
    searchBar = black200,
    tabSelect = white,
    tabUnselect = grey,
    highlightColor = pink100,
    background = black100,
    grayBackground = black300,
    stableRank = grey,
    upRank = green500,
    downRank = red100,
    white = white,
    black = black,
    progressTrackColor = black300,
)

val LightColorScheme = MagicMusicScheme(
    backgroundTop = white,
    backgroundCenter = white,
    backgroundBottom = pink100,
    backgroundEnd = pink200,
    defaultIcon = grey,
    selectIcon = pink100,
    unselectIcon = grey,
    textTitle = black,
    textContent = grey,
    loginStart = grey,
    loginEnd = pink100,
    loginText = white,
    bottomBar = white100,
    searchBar = grey100,
    tabSelect = white,
    tabUnselect = black300,
    highlightColor = pink100,
    background  = white,
    grayBackground = grey200,
    stableRank = grey,
    upRank = green500,
    downRank = red100,
    white = white,
    black = black,
    progressTrackColor = grey200
)


/**
 * 主题状态
 */
val themeState by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    mutableStateOf(getCurrentThemeMode())
}

/**
 * 获取当前主题状态*/
private fun getCurrentThemeMode():String = SharedPreferencesUtil.instance.getValue(APP.context, Constants.ThemeMode,ThemeModeStatus.Light.mode) as String

/**
 * 更改当前主题状态*/
fun setCurrentThemeMode(mode:String) = SharedPreferencesUtil.instance.putValue(APP.context, Constants.ThemeMode,mode)

private fun getCurrentTheme(mode: String):MagicMusicScheme{
    return if (mode == ThemeModeStatus.Light.mode){
        LightColorScheme
    }else{
        DarkColorScheme
    }
}

val LocalMagicMusicScheme = compositionLocalOf {
    LightColorScheme
}


object MagicMusicTheme {
    val colors: MagicMusicScheme
        @Composable
        get() = LocalMagicMusicScheme.current
}

@Composable
fun MagicMusicTheme(
    theme:String,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    /**
     * 暗黑模式下不允许切换主题*/
    val targetColors = if (darkTheme) {
        DarkColorScheme
    } else {
        getCurrentTheme(theme)
    }

    val backgroundTop = animateColorAsState(targetColors.backgroundTop, TweenSpec(500), label = "")
    val backgroundCenter = animateColorAsState(targetColors.backgroundCenter, TweenSpec(500), label = "")
    val backgroundBottom = animateColorAsState(targetColors.backgroundBottom, TweenSpec(500), label = "")
    val backgroundEnd = animateColorAsState(targetColors.backgroundEnd, TweenSpec(500), label = "")
    val defaultIcon = animateColorAsState(targetColors.defaultIcon, TweenSpec(500), label = "")
    val selectIcon = animateColorAsState(targetColors.selectIcon, TweenSpec(500), label = "")
    val unselectIcon = animateColorAsState(targetColors.unselectIcon, TweenSpec(500), label = "")
    val textTitle = animateColorAsState(targetColors.textTitle, TweenSpec(500), label = "")
    val textContent = animateColorAsState(targetColors.textContent, TweenSpec(500), label = "")
    val loginStart = animateColorAsState(targetColors.loginStart, TweenSpec(500), label = "")
    val loginEnd = animateColorAsState(targetColors.loginEnd, TweenSpec(500), label = "")
    val loginText = animateColorAsState(targetColors.loginText, TweenSpec(500), label = "")
    val bottomBar = animateColorAsState(targetColors.bottomBar, TweenSpec(500), label = "")
    val searchBar = animateColorAsState(targetColors.searchBar, TweenSpec(500), label = "")
    val tabSelect = animateColorAsState(targetColors.tabSelect, TweenSpec(500), label = "")
    val tabUnselect = animateColorAsState(targetColors.tabUnselect, TweenSpec(500), label = "")
    val highlightColor = animateColorAsState(targetColors.highlightColor, TweenSpec(500), label = "")
    val background = animateColorAsState(targetColors.background, TweenSpec(500), label = "")
    val grayBackground = animateColorAsState(targetColors.grayBackground, TweenSpec(500), label = "")
    val stableRank = animateColorAsState(targetColors.stableRank, TweenSpec(500), label = "")
    val upRank = animateColorAsState(targetColors.upRank, TweenSpec(500), label = "")
    val downRank = animateColorAsState(targetColors.downRank, TweenSpec(500), label = "")
    val white = animateColorAsState(targetColors.white, TweenSpec(500), label = "")
    val black = animateColorAsState(targetColors.black, TweenSpec(500), label = "")
    val progressTrackColor = animateColorAsState(targetColors.progressTrackColor, TweenSpec(500), label = "")

    val colors = MagicMusicScheme(
        backgroundTop = backgroundTop.value,
        backgroundCenter = backgroundCenter.value,
        backgroundBottom = backgroundBottom.value,
        backgroundEnd = backgroundEnd.value,
        defaultIcon = defaultIcon.value,
        selectIcon = selectIcon.value,
        unselectIcon = unselectIcon.value,
        textTitle = textTitle.value,
        textContent = textContent.value,
        loginStart = loginStart.value,
        loginEnd = loginEnd.value,
        loginText = loginText.value,
        bottomBar = bottomBar.value,
        searchBar = searchBar.value,
        tabSelect = tabSelect.value,
        tabUnselect = tabUnselect.value,
        highlightColor = highlightColor.value,
        background = background.value,
        grayBackground = grayBackground.value,
        stableRank = stableRank.value,
        upRank = upRank.value,
        downRank = downRank.value,
        white = white.value,
        black = black.value,
        progressTrackColor = progressTrackColor.value
    )

    CompositionLocalProvider(LocalMagicMusicScheme provides colors)
    {
        MaterialTheme(
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

enum class ThemeModeStatus(val mode:String){
    Light("light"),
    Dark("dark")
}
package com.franzliszt.magicmusic.route.radio

import com.franzliszt.magicmusic.bean.radio.ProgramDetailBean
import com.franzliszt.magicmusic.bean.radio.program.Program

data class RadioUIStatus(
    val detail:ProgramDetailBean? = null,
    val programs:List<Program> = emptyList()
)

sealed class RadioStatus{
    data class NetworkFailed(val msg:String):RadioStatus()
}
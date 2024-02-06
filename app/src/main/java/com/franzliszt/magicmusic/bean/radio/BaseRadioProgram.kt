package com.franzliszt.magicmusic.bean.radio

import com.franzliszt.magicmusic.bean.radio.program.Program

data class BaseRadioProgram(
    val count:Int,
    val code:Int,
    val programs:List<Program>,
    val more:Boolean
)

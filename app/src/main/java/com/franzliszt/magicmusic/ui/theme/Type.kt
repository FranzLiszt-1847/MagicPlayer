package com.franzliszt.magicmusic.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.franzliszt.magicmusic.R

// Set of Material typography styles to start with
val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    h5 = TextStyle(
        fontFamily = FontFamily(Font(R.font.agbalumo_regular)),
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp
    ),
    h6 = TextStyle(
        fontFamily = FontFamily(Font(R.font.agbalumo_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp
    ),
    subtitle1 = TextStyle(
        fontFamily = FontFamily(Font(R.font.zhimangxing_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = FontFamily(Font(R.font.agbalumo_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    overline = TextStyle(
        fontFamily = FontFamily(Font(R.font.agbalumo_regular)),
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp
    )
)
package com.franzliszt.magicmusic.tool

import android.content.Context

class SharedPreferencesUtil private constructor(){
    companion object{
        val instance:SharedPreferencesUtil by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED){SharedPreferencesUtil()}
        private const val MagicMusicCacheName = "MagicMusicCache"
    }
    fun getValue(context: Context,key: String,defaultValue: Any): Any?{
        val sharedPreferences = context.getSharedPreferences(MagicMusicCacheName,Context.MODE_PRIVATE)
        return when(defaultValue){
            is Int-> {sharedPreferences.getInt(key,defaultValue)}
            is Float-> {sharedPreferences.getFloat(key,defaultValue)}
            is String-> {sharedPreferences.getString(key,defaultValue)}
            is Boolean-> {sharedPreferences.getBoolean(key,defaultValue)}
            is Long-> {sharedPreferences.getLong(key,defaultValue)}
            else -> {}
        }
    }

    fun putValue(context: Context,key: String,value: Any){
        val sharedPreferences = context.getSharedPreferences(MagicMusicCacheName,Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        when(value){
            is Int-> {edit.putInt(key,value)}
            is Float-> {edit.putFloat(key,value)}
            is String-> {edit.putString(key,value)}
            is Boolean-> {edit.putBoolean(key,value)}
            is Long-> {edit.putLong(key,value)}
            else -> {}
        }
        edit.apply()
    }
}
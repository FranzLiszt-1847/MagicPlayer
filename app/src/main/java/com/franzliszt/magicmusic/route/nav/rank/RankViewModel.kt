package com.franzliszt.magicmusic.route.nav.rank

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.franzliszt.magicmusic.bean.rank.Rankbean
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.tool.GsonFormat
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class RankViewModel @Inject constructor(private val service: MusicApiService):ViewModel() {

    private val _uiStatus = mutableStateOf(RankUIStatus())
    val uiStatus: State<RankUIStatus> = _uiStatus

    init {
        try {
            viewModelScope.launch (Dispatchers.IO){
                val official = mutableListOf<Rankbean>()
                val global = mutableListOf<Rankbean>()
                val result = service.getTopListDetail().await()
                val body = result.toString()
                if (body.isNotEmpty()){
                    val jsonObject = JSONObject(body)
                    val code = jsonObject.getInt("code")
                    if (jsonObject != null && code == 200){
                        val jsonArray = jsonObject.getJSONArray("list")
                        val charts = GsonFormat.fromListJson(jsonArray.toString(),Rankbean::class.java)
                        charts.forEach { bean->
                            if (bean.tracks.isNotEmpty()){
                                official.add(bean)
                            }else{
                                global.add(bean)
                            }
                        }
                        _uiStatus.value = uiStatus.value.copy(
                            official = official,
                            global = global
                        )
                    }
                }
            }
        }catch (e:Exception){
            Log.e("RankViewModel Exception:",e.message.toString())
        }
    }


    private suspend fun <T> Call<T>.await(): T =
        suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        continuation.resume(response.body()!!)
                    } else {
                        continuation.resumeWithException(Exception("error"))
                    }
                }
                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
}
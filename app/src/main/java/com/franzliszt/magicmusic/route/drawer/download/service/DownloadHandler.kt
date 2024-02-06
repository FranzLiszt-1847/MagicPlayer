package com.franzliszt.magicmusic.route.drawer.download.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Environment
import android.os.IBinder
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.inf.IEntity
import com.arialyy.aria.core.task.DownloadTask
import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.PermissionApplyState
import com.franzliszt.magicmusic.bean.download.DownloadMusicBean
import com.franzliszt.magicmusic.checkPermission
import com.franzliszt.magicmusic.parm.Constants
import com.franzliszt.magicmusic.usecase.download.DownloadUseCase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class DownloadHandler @Inject constructor(
    private val downloadUseCase: DownloadUseCase
) {

    private val folderName = "MagicMusicDownload"

    private val _eventFlow = MutableSharedFlow<DownloadStateFlow>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val downloadList: MutableList<DownloadMusicBean> = mutableListOf()

    private val downloadConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val downloadBinder = p1 as DownloadService.DownloadBinder
            downloadListener(downloadBinder.service)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {

        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun register() {
        GlobalScope.launch(Dispatchers.Main){
            binderDownloadService()
            downloadList.addAll(downloadUseCase.queryAll())
        }
    }

    fun unRegister() {
        APP.context.unbindService(downloadConnection)
    }


    /**
     * 绑定service服务*/
    private fun binderDownloadService() {
        val intent = Intent(APP.context, DownloadService::class.java)
        APP.context.bindService(intent, downloadConnection, Context.BIND_AUTO_CREATE)
    }

    suspend fun download(bean: DownloadMusicBean) {
        //权限校验
        val result = checkPermission()
        if (!result){
            _eventFlow.emit(DownloadStateFlow.PermissionDenied)
            return
        }
        //文件是否已经存在，如果存在则不重复下载
        val isExist = checkFileState(bean.url)
        if (!isExist){
            downloadList.add(bean)
            downloadUseCase.insert(bean)
            val intent = Intent(APP.context, DownloadService::class.java)
            intent.putExtra(Constants.DownloadURL, bean.url)
            intent.putExtra(Constants.DownloadPath, bean.path)
            intent.putExtra(Constants.DownloadCover, bean.cover)
            intent.putExtra(Constants.DownloadName, bean.musicName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                APP.context.startForegroundService(intent)
            } else {
                APP.context.startService(intent)
            }
        }
    }

    /**
     * 文件读写权限校验*/
    private fun checkPermission():Boolean{
        return when(checkPermission(APP.permissions)){
            PermissionApplyState.PartAuthority->{
                //onCheckResult(false,"Partial permissions authorized!")
                false
            }
            PermissionApplyState.ALlDenied->{
                //onCheckResult(false,"No authorized permissions!")
                false
            }
            PermissionApplyState.AllAuthority->{
                //onCheckResult(true,"")
                true
            }
        }
    }

    fun getCurrentDownloads(): List<DownloadMusicBean> = downloadList

    /**
     * 如果下载文件在机身存储中不存在，则删除数据库中的下载记录*/
    private fun checkFileState(url: String):Boolean{
        var isExist = Aria.download(this).taskExists(url)
        if (isExist){
            val entity = Aria.download(this).getFirstDownloadEntity(url)
            isExist = File(entity.filePath).exists()
            if (!isExist){
                Aria.download(this).load(entity.id).removeRecord()
            }
        }
        return isExist
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun downloadListener(downloadService: DownloadService) {
        downloadService.setDownloadListener(object : DownloadListener {
            override fun onDownloadState(task: DownloadTask,msg:String) {
                val index = searchIndex(task.key)
                if (index == -1) return
                GlobalScope.launch(Dispatchers.Main) {
                    when (task.state) {
                        IEntity.STATE_PRE -> {
                            downloadList[index].taskID = task.entity.id
                            downloadUseCase.updateTaskID(
                                musicID = downloadList[index].musicID,
                                taskID = task.entity.id
                            )
                            _eventFlow.emit(DownloadStateFlow.Prepare(task,index))
                        }

                        IEntity.STATE_WAIT -> {
                            _eventFlow.emit(DownloadStateFlow.Prepare(task,index))
                        }

                        IEntity.STATE_RUNNING -> {
                            _eventFlow.emit(DownloadStateFlow.Running(task,index))
                        }

                        IEntity.STATE_STOP -> {
                            _eventFlow.emit(DownloadStateFlow.Stop(task,index))
                        }

                        IEntity.STATE_CANCEL -> {
                            downloadList.removeAt(index)
                            _eventFlow.emit(DownloadStateFlow.Cancel(task,index))
                        }

                        IEntity.STATE_COMPLETE -> {
                            downloadList[index].download = true
                            downloadUseCase.updateDownloadState(
                                musicID = downloadList[index].musicID,
                                download = true
                            )
                            Aria.download(this).load(task.entity.id).removeRecord()
                            _eventFlow.emit(DownloadStateFlow.Complete(task,index))
                        }

                        IEntity.STATE_FAIL -> {
                            _eventFlow.emit(DownloadStateFlow.Fail(task,index,msg))
                        }
                    }
                }
            }
        })
    }

    private fun searchIndex(url: String) = downloadList.indexOfFirst { it.url == url }

    /**
     * 清除所有下载文件
     * 包括数据库内容以及本地下载的文件*/
    suspend fun clearAllRecords(){
        downloadList.clear()
        downloadUseCase.deleteAll() //清空数据库
        Aria.download(this).removeAllTask(true) //Aria数据库
        val file = File(createDownloadFolder()) //本地文件清空
        deleteDirectory(file)
    }

    private fun deleteDirectory(dir:File) {
        try {
            if (!dir.exists()) return
            if (dir.isDirectory) {
                val files = dir.listFiles()
                if (files == null || files.isEmpty()) {
                    return
                }
                for (file in files) {
                    if (file.isFile) {
                        file.delete()
                    } else if (file.isDirectory) {
                        deleteDirectory(file)
                    }
                }
            }else{
                dir.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

     fun createDownloadFolder(): String {
        val dir = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
            APP.context.getExternalFilesDir(folderName)?.absolutePath ?: ""
        else
            APP.context.filesDir.absolutePath + File.separator + folderName
        val file = File(dir)
        if (!file.exists())
            file.mkdirs()
        return dir
    }
}

sealed class DownloadStateFlow {
    object PermissionDenied: DownloadStateFlow()
    data class Prepare(val task: DownloadTask,val index:Int) : DownloadStateFlow()
    data class Running(val task: DownloadTask,val index:Int) : DownloadStateFlow()
    data class Stop(val task: DownloadTask,val index:Int) : DownloadStateFlow()
    data class Fail(val task: DownloadTask,val index:Int,val error: String) : DownloadStateFlow()
    data class Complete(val task: DownloadTask,val index:Int) : DownloadStateFlow()
    data class Cancel(val task: DownloadTask,val index:Int) : DownloadStateFlow()
    data class Other(val task: DownloadTask,val index:Int) : DownloadStateFlow()
}
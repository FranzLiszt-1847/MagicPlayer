package com.franzliszt.magicmusic.tool

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.franzliszt.magicmusic.APP
import java.io.FileNotFoundException
import java.io.OutputStream

class BitmapUtil {
    companion object{
        //封面保存位置
        private val ALBUM_DIR = Environment.DIRECTORY_PICTURES
        /**
         * 将网络图片转为Bitmap*/
        suspend fun toBitmap(
            url: String
        ): Bitmap?{
            val request = ImageRequest.Builder(context = APP.context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = APP.context.imageLoader.execute(request)
            if (result is SuccessResult){
                return (result.drawable as BitmapDrawable).bitmap
            }
            return null
        }

        /**
         * 保存图片到相册*/
        fun Bitmap.saveToPhoto(
            fileName: String,
            relativePath: String?,
            quality:Int = 90
        ): Uri?{
            val resolver = APP.context.contentResolver
            val uri = resolver.insertMediaImage(fileName,relativePath) ?: return null
            uri.outputStream(resolver).use {
                if (it != null){
                    val format = fileName.getBitmapFormat()
                    this.compress(format,quality,it)
                    uri.finishPending(resolver)
                }
            }
            return uri
        }


        private fun ContentResolver.insertMediaImage(
            fileName:String,
            relativePath:String?
        ): Uri?{
            val imgValue = ContentValues().apply {
                val mimeType = fileName.getMimeType()
                if (mimeType != null){
                    put(MediaStore.Images.Media.MIME_TYPE,mimeType)
                }
                val date = System.currentTimeMillis()
                put(MediaStore.Images.Media.DATE_ADDED,date/1000)
                put(MediaStore.Images.Media.DATE_MODIFIED,date/1000)
            }
            //Android 10以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                val path = if (relativePath != null) "${ALBUM_DIR}/${relativePath}" else ALBUM_DIR
                imgValue.apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME,fileName)
                    put(MediaStore.Images.Media.RELATIVE_PATH,path)
                    put(MediaStore.Images.Media.IS_PENDING,1)
                }
                val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                return this.insert(collection,imgValue) //exception
            }
            return null
        }

        private fun Uri.finishPending(
            resolver: ContentResolver
        ){
            ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING,0)
                resolver.update(this@finishPending,this,null,null)
            }
        }

        private fun Uri.outputStream(resolver: ContentResolver): OutputStream? {
            return try {
                resolver.openOutputStream(this)
            } catch (e: FileNotFoundException) {
                null
            }
        }

        private fun String.getMimeType(): String? {
            val fileName = this.lowercase()
            return when {
                fileName.endsWith(".png") -> "image/png"
                fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
                fileName.endsWith(".webp") -> "image/webp"
                fileName.endsWith(".gif") -> "image/gif"
                else -> null
            }
        }

        private fun String.getBitmapFormat(): Bitmap.CompressFormat {
            val fileName = this.lowercase()
            return when {
                fileName.endsWith(".png") -> Bitmap.CompressFormat.PNG
                fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> Bitmap.CompressFormat.JPEG
                fileName.endsWith(".webp") -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.PNG
            }
        }
    }
}
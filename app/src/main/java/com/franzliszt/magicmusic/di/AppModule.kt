package com.franzliszt.magicmusic.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import androidx.room.Room
import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.room.MagicMusicDataBase
import com.franzliszt.magicmusic.room.download.DownloadRepository
import com.franzliszt.magicmusic.room.download.DownloadRepositoryImpl
import com.franzliszt.magicmusic.room.search.SearchHistoryRepository
import com.franzliszt.magicmusic.room.search.SearchHistoryRepositoryImpl
import com.franzliszt.magicmusic.room.song.MusicRepository
import com.franzliszt.magicmusic.room.song.MusicRepositoryImpl
import com.franzliszt.magicmusic.route.drawer.download.service.DownloadHandler
import com.franzliszt.magicmusic.route.musicplayer.notification.MusicNotificationManager
import com.franzliszt.magicmusic.route.musicplayer.service.MusicService
import com.franzliszt.magicmusic.route.musicplayer.service.MusicServiceHandler
import com.franzliszt.magicmusic.usecase.download.DeleteAllDownloadCase
import com.franzliszt.magicmusic.usecase.download.DeleteDownloadCase
import com.franzliszt.magicmusic.usecase.download.DownloadUseCase
import com.franzliszt.magicmusic.usecase.download.InsertAllDownloadCase
import com.franzliszt.magicmusic.usecase.download.InsertDownloadCase
import com.franzliszt.magicmusic.usecase.download.QueryAllDownloadCase
import com.franzliszt.magicmusic.usecase.download.QueryAllDownloadImmediateCase
import com.franzliszt.magicmusic.usecase.download.UpdateDownloadStateCase
import com.franzliszt.magicmusic.usecase.download.UpdateDownloadTaskIDCase
import com.franzliszt.magicmusic.usecase.search.DeleteAllCase
import com.franzliszt.magicmusic.usecase.search.InsertAllCase
import com.franzliszt.magicmusic.usecase.search.InsertCase
import com.franzliszt.magicmusic.usecase.search.QueryAllCase
import com.franzliszt.magicmusic.usecase.search.SearchUseCase
import com.franzliszt.magicmusic.usecase.song.DeleteAllMusicCase
import com.franzliszt.magicmusic.usecase.song.DeleteMusicCase
import com.franzliszt.magicmusic.usecase.song.InsertAllMusicCase
import com.franzliszt.magicmusic.usecase.song.InsertMusicCase
import com.franzliszt.magicmusic.usecase.song.MusicUseCase
import com.franzliszt.magicmusic.usecase.song.QueryAllMusicCase
import com.franzliszt.magicmusic.usecase.song.QueryAllSongsCase
import com.franzliszt.magicmusic.usecase.song.UpdateDurationMusicCase
import com.franzliszt.magicmusic.usecase.song.UpdateLoadingMusicCase
import com.franzliszt.magicmusic.usecase.song.UpdateSizeMusicCase
import com.franzliszt.magicmusic.usecase.song.UpdateURLMusicCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule{
    private const val BASEURL = "http://8.130.47.16:3000"

    @Singleton
    @Provides
    fun provideClient(@ApplicationContext context: Context):OkHttpClient{
        return OkHttpClient.Builder()
            .connectTimeout(3000L, TimeUnit.MILLISECONDS)
            .writeTimeout(3000L, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient):Retrofit{
        return Retrofit.Builder()
            .baseUrl(BASEURL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideMusicService(retrofit: Retrofit): MusicApiService = retrofit.create(MusicApiService::class.java)

    @Singleton
    @Provides
    fun provideDataBase():MagicMusicDataBase{
        return Room.databaseBuilder(
            context = APP.context,
            klass = MagicMusicDataBase::class.java,
            name = MagicMusicDataBase.DATABASE_NAME
        ).build()
    }

    @Singleton
    @Provides
    fun provideSearchHistoryRepository(dataBase: MagicMusicDataBase): SearchHistoryRepository = SearchHistoryRepositoryImpl(dataBase.searchHistoryDao)

    @Singleton
    @Provides
    fun provideSearchHistoryUseCase(repository: SearchHistoryRepository):SearchUseCase =
        SearchUseCase(
            queryAll = QueryAllCase(repository),
            insert = InsertCase(repository),
            deleteAll = DeleteAllCase(repository),
            insertAll = InsertAllCase(repository)
        )

    @Singleton
    @Provides
    fun provideMusicRepository(dataBase: MagicMusicDataBase): MusicRepository = MusicRepositoryImpl(dataBase.musicDao)

    @Singleton
    @Provides
    fun provideMusicUseCase(repository: MusicRepository):MusicUseCase =
        MusicUseCase(
            queryAll = QueryAllMusicCase(repository),
            insert = InsertMusicCase(repository),
            deleteAll = DeleteAllMusicCase(repository),
            insertAll = InsertAllMusicCase(repository),
            updateUrl = UpdateURLMusicCase(repository),
            updateLoading = UpdateLoadingMusicCase(repository),
            queryAllSongsCase = QueryAllSongsCase(repository),
            updateDuration = UpdateDurationMusicCase(repository),
            deleteSong = DeleteMusicCase(repository),
            updateSize = UpdateSizeMusicCase(repository)
        )

    @Singleton
    @Provides
    fun provideDownloadRepository(dataBase: MagicMusicDataBase): DownloadRepository = DownloadRepositoryImpl(dataBase.downloadDao)

    @Singleton
    @Provides
    fun provideDownloadUseCase(repository: DownloadRepository):DownloadUseCase =
        DownloadUseCase(
            queryAllImmediate = QueryAllDownloadImmediateCase(repository),
            queryAll = QueryAllDownloadCase(repository),
            insertAll = InsertAllDownloadCase(repository),
            insert = InsertDownloadCase(repository),
            delete = DeleteDownloadCase(repository),
            deleteAll = DeleteAllDownloadCase(repository),
            updateTaskID = UpdateDownloadTaskIDCase(repository),
            updateDownloadState = UpdateDownloadStateCase(repository)
        )

    @Singleton
    @Provides
    fun provideAudioAttributes():AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @OptIn(UnstableApi::class)
    @Singleton
    @Provides
    fun provideMusicExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ):ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .setTrackSelector(DefaultTrackSelector(context))
        .build()

    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: ExoPlayer,
    ): MediaSession = MediaSession.Builder(context, player).build()

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        player: ExoPlayer,
    ): MusicNotificationManager = MusicNotificationManager(
        context = context,
        exoPlayer = player
    )

    @Provides
    @Singleton
    fun provideServiceHandler(
        exoPlayer: ExoPlayer,
        musicUseCase: MusicUseCase,
        service: MusicApiService
    ): MusicServiceHandler
    = MusicServiceHandler(
        exoPlayer = exoPlayer,
        musicUseCase = musicUseCase,
        service = service
    )

    @Provides
    @Singleton
    fun provideDownloadHandler(useCase: DownloadUseCase):DownloadHandler = DownloadHandler(useCase)
}
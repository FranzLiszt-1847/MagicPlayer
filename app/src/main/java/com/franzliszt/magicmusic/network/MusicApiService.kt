package com.franzliszt.magicmusic.network

import com.franzliszt.magicmusic.bean.banner.BannersBean
import com.franzliszt.magicmusic.bean.BaseResponse
import com.franzliszt.magicmusic.bean.albums.AlbumDetailBean
import com.franzliszt.magicmusic.bean.albums.AlbumsBean
import com.franzliszt.magicmusic.bean.artist.ArtistAlbumBean
import com.franzliszt.magicmusic.bean.albums.BaseAlbums
import com.franzliszt.magicmusic.bean.albums.BaseProduct
import com.franzliszt.magicmusic.bean.albums.DigitAlbumsBean
import com.franzliszt.magicmusic.bean.albums.FavoriteAlbumBean
import com.franzliszt.magicmusic.bean.artist.Artist
import com.franzliszt.magicmusic.bean.artist.ArtistBriefBean
import com.franzliszt.magicmusic.bean.artist.ArtistSongBean
import com.franzliszt.magicmusic.bean.artist.BaseArtist
import com.franzliszt.magicmusic.bean.banner.BannerBean
import com.franzliszt.magicmusic.bean.lrc.LyricInfoBean
import com.franzliszt.magicmusic.bean.artist.ArtistMvBean
import com.franzliszt.magicmusic.bean.comment.BaseCommentBean
import com.franzliszt.magicmusic.bean.comment.FloorCommentBean
import com.franzliszt.magicmusic.bean.comment.MlogCommentBean
import com.franzliszt.magicmusic.bean.comment.SendCommentBean
import com.franzliszt.magicmusic.bean.dj.DjRadioBean
import com.franzliszt.magicmusic.bean.mv.MvBean
import com.franzliszt.magicmusic.bean.mv.MvDetailBean
import com.franzliszt.magicmusic.bean.mv.MvUrlBean
import com.franzliszt.magicmusic.bean.playlist.PlayListBean
import com.franzliszt.magicmusic.bean.playlist.Playlist
import com.franzliszt.magicmusic.bean.pwdlogin.PwdLoginBean
import com.franzliszt.magicmusic.bean.qrcode.QRCodeCookieBean
import com.franzliszt.magicmusic.bean.qrcode.QRCodeImgBean
import com.franzliszt.magicmusic.bean.qrcode.QRCodeKeyBean
import com.franzliszt.magicmusic.bean.radio.BaseRadioProgram
import com.franzliszt.magicmusic.bean.radio.BaseRadioStation
import com.franzliszt.magicmusic.bean.radio.ProgramDetailBean
import com.franzliszt.magicmusic.bean.radio.RecommendRadioBean
import com.franzliszt.magicmusic.bean.radio.program.BaseProgram
import com.franzliszt.magicmusic.bean.radio.program.NewHotRadioBean
import com.franzliszt.magicmusic.bean.radio.program.ProgramRankBean
import com.franzliszt.magicmusic.bean.recent.RecentBean
import com.franzliszt.magicmusic.bean.recent.RecentPlayBean
import com.franzliszt.magicmusic.bean.recommend.newsongs.RecommendNewSongsBean
import com.franzliszt.magicmusic.bean.recommend.playlist.RecommendPlaylistBean
import com.franzliszt.magicmusic.bean.recommend.songs.DailySong
import com.franzliszt.magicmusic.bean.recommend.songs.RecommendSongsBean
import com.franzliszt.magicmusic.bean.search.BaseSearch
import com.franzliszt.magicmusic.bean.search.DefaultSearchBean
import com.franzliszt.magicmusic.bean.search.HotSearchBean
import com.franzliszt.magicmusic.bean.search.SearchSuggestionBean
import com.franzliszt.magicmusic.bean.searchresult.SearchAlbumBean
import com.franzliszt.magicmusic.bean.searchresult.SearchArtistBean
import com.franzliszt.magicmusic.bean.searchresult.SearchDjBean
import com.franzliszt.magicmusic.bean.searchresult.SearchMvBean
import com.franzliszt.magicmusic.bean.searchresult.SearchPlaylistBean
import com.franzliszt.magicmusic.bean.searchresult.SearchSongBean
import com.franzliszt.magicmusic.bean.searchresult.SearchUserBean
import com.franzliszt.magicmusic.bean.searchresult.SearchVideoBean
import com.franzliszt.magicmusic.bean.song.BaseSong
import com.franzliszt.magicmusic.bean.song.SongUrlBean
import com.franzliszt.magicmusic.bean.user.UserDetailBean
import com.franzliszt.magicmusic.bean.video.MlogInfoBean
import com.franzliszt.magicmusic.bean.video.VideoBean
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {
    //手机密码登录接口
    @GET("/login/cellphone")
    suspend fun getPhoneLogin(@Query("phone")phone:String,@Query("password")password:String): PwdLoginBean

    //邮箱登录接口
    @GET("/login")
    suspend fun getEmailLogin(@Query("email")email:String,@Query("password")password: String): PwdLoginBean

    /**
     * 二维码登录涉及到 3 个接口
     * 调用务必带上时间戳,防止缓存
     * */
    //调用此接口可生成一个 key
    @GET("/login/qr/key")
    suspend fun getQRCodeLoginKey(@Query("timerstamp")timerstamp:String=System.currentTimeMillis().toString()):BaseResponse<QRCodeKeyBean>

    //调用此接口传入上一个接口生成的key,可生成二维码图片的 base64 和二维码信息,可使用 base64 展示图片
    @GET("/login/qr/create")
    suspend fun getQRCodeLoginImg(
        @Query("key")key:String,
        @Query("qrimg")qrimg:Boolean=true,
        @Query("timerstamp")timerstamp:String=System.currentTimeMillis().toString()
    ):BaseResponse<QRCodeImgBean>

    /**
     * 轮询此接口可获取二维码扫码状态
     * 800 为二维码过期
     * 801 为等待扫码
     * 802 为待确认
     * 803 为授权登录成功(803 状态码下会返回 cookies)
     * 如扫码后返回502,则需加上noCookie参数,如&noCookie=true*/
    @GET("/login/qr/check")
    suspend fun getQRCodeLoginStatus(
        @Query("key")key:String,
        @Query("timerstamp")timerstamp:String=System.currentTimeMillis().toString()
    ):QRCodeCookieBean

    /**
     * 获取banner相关信息
     * type:资源类型,对应以下类型,默认为 0 即 PC
     * 0: pc
     * 1: android
     * 2: iphone
     * 3: ipad*/
    @GET("/banner")
    suspend fun getBanner(@Query("type")type:Int = 1): BannersBean

    /**
     * 获取用户歌单
     * 包含用户喜欢的歌曲、用户创建的歌单、用户收藏的歌单*/
    @GET("/user/playlist")
    suspend fun getPlayList(
        @Query("uid")uid:Long,
        @Query("cookie")cookie: String
    ):PlayListBean

    /**
     * 当为QRCode登录模式时
     * 通过此接口获取userId
     * Call作为返回值时，无需用suspend修饰*/
    @GET("/user/account")
    fun getAccountInfo():Call<JsonObject>

    /**
     * 获取推荐的歌单*/
    @GET("/personalized")
    suspend fun getRecommendPlaylist(@Query("limit")limit:Int=10): RecommendPlaylistBean

    /**
     * 获取每日推荐的歌单,默认30个*/
    @GET("/recommend/resource")
    fun getRecommendEveryDayPlaylist():Call<JsonObject>

    /**
     * 可获取推荐新音乐
     * limit默认为10*/
    @GET("/personalized/newsong")
    suspend fun getRecommendSongs(@Query("limit")limit:Int=10):RecommendNewSongsBean


    /**
     * 获取每日推荐的歌曲,默认30首*/
    @GET("/recommend/songs")
    suspend fun getRecommendEveryDaySongs():BaseResponse<RecommendSongsBean>

    /**
     * 获取最新专辑*/
    @GET("/album/newest")
    suspend fun getNewestAlbums(): BaseAlbums<List<AlbumsBean>>

    /**
     *  获取最新数字专辑*/
    @GET("/album/list")
    suspend fun getDigitNewestAlbums(@Query("limit")limit: Int = 10):BaseProduct<List<DigitAlbumsBean>>

    /**
     * 数字专辑&数字单曲-榜单
     * 说明 : 调用此接口 ,可获取数字专辑&数字单曲-榜单
     * 可选参数 :
     * limit : 返回数量 , 默认为 30
     * offset : 偏移数量，用于分页 , 如 :( 页数 -1)*30, 其中 30 为 limit 的值 , 默认为 0
     * albumType : 0为数字专辑;1 为数字单曲
     * type : daily:日榜,week:周榜,year:年榜,total:总榜*/
    @GET("/album/songsaleboard")
    suspend fun getDigitAlbumsRank(@Query("type")type:String="daily",@Query("albumType")albumType:Int= 0):BaseProduct<List<DigitAlbumsBean>>

    /**
     * 调用此接口,可获取歌手分类列表
     * 可选参数
     * limit : 返回数量 , 默认为 30
     * offset : 偏移数量，用于分页 , 如 : 如 :( 页数 -1)*30, 其中 30 为 limit 的值 ,
     * initial: 按首字母索引查找参数,如 /artist/list?type=1&area=96&initial=b 返回内容将以 name 字段开头为 b 或者拼音开头为 b 为顺序排列
     * type 取值:
     * -1:全部
     * 1:男歌手
     * 2:女歌手
     * 3:乐队
     * area 取值:
     * -1:全部
     * 7华语
     * 96欧美
     * 8:日本
     * 16韩国
     * 0:其他*/
    @GET("/artist/list")
    suspend fun getAllArtists(
        @Query("type")type:Int=-1,
        @Query("area")area:Int=-1,
        @Query("limit")limit:Int = 20,
        @Query("offset")offset:Int = 0
    ):BaseArtist

    /**
     * 可获取榜单详情*/
    @GET("/toplist/detail")
    fun getTopListDetail():Call<JsonObject>

    /**
     * 可获取电台 banner*/
    @GET("/dj/banner")
    suspend fun getRadioStationBanner(
        @Query("cookie")cookie: String
    ):BaseResponse<List<BannerBean>>

    /**
     * 可获取电台个性推荐列表 可选参数 :
     * limit : 返回数量,默认为 6,总条数最多 6 条*/
    @GET("/dj/personalize/recommend")
    suspend fun getRecommendRadioStation(@Query("cookie")cookie: String):BaseResponse<List<RecommendRadioBean>>

    /**
     * 说明 : 调用此接口,可获取热门电台
     * 可选参数 :
     * limit : 返回数量 , 默认为 30
     * offset : 偏移数量，用于分页 , 如 :( 页数 -1)*30, 其中 30 为 limit 的值*/
    @GET("/dj/hot")
    suspend fun getHotRadioStation(
        @Query("cookie")cookie: String,
        @Query("offset")offset: Int=0,
        @Query("limit")limit: Int=10
    ): BaseRadioStation<List<RecommendRadioBean>>

    /**
     * 说明 : 登录后调用此接口 , 可获得电台节目榜
     * 可选参数 :
     * limit : 返回数量 , 默认为 100
     * offset : 偏移数量，用于分页 , 如 :( 页数 -1)*100, 其中 100 为 limit 的值 , 默认为 0*/
    @GET("/dj/program/toplist")
    suspend fun getProgramRanking(@Query("offset")offset: Int=0,@Query("limit")limit: Int=20):BaseProgram<List<ProgramRankBean>>

    /**
     * 新晋电台榜/热门电台榜
     * 可选参数 :
     * limit : 返回数量 , 默认为 100
     * offset : 偏移数量，用于分页 , 如 :( 页数 -1)*100, 其中 100 为 limit 的值 , 默认为 0
     * type: 榜单类型: new 为新晋电台榜,hot为热门电台榜
     * */
    @GET("/dj/toplist")
    suspend fun getNewHotProgramRanking(
        @Query("offset")offset: Int=0,
        @Query("limit")limit: Int=20,
        @Query("type")type:String):BaseProgram<List<NewHotRadioBean>>

    /**
     * 默认搜索词*/
    @GET("/search/default")
    suspend fun getDefaultSearch():BaseResponse<DefaultSearchBean>

   /**
    * 热搜列表*/
    @GET("/search/hot")
    suspend fun getHotSearch():BaseSearch<HotSearchBean>

    /**
     * 搜索建议*/
    @GET("/search/suggest")
    suspend fun getSearchSuggestion(@Query("keywords")keywords:String):BaseSearch<SearchSuggestionBean>

    /**
     * 搜索结果
     * 说明 : 调用此接口 , 传入搜索关键词可以搜索该音乐 / 专辑 / 歌手 / 歌单 / 用户
     * 必选参数 : keywords : 关键词
     * 可选参数 : limit : 返回数量 , 默认为 30
     * offset : 偏移数量，用于分页,默认为 0
     * type: 搜索类型；默认为 1 即单曲 ,
     * 取值意义 :
     * 1: 单曲,
     * 10: 专辑,
     * 100: 歌手,
     * 1000: 歌单,
     * 1002: 用户,
     * 1004: MV,
     * 1006: 歌词,//abandon
     * 1009: 电台,
     * 1014: 视频,
     * 1018:综合,//abandon
     * 2000:声音//abandon
     * */
    @GET("/cloudsearch")
     suspend fun getSearchSongResult(
        @Query("keywords")keywords:String,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit:Int = 30,
        @Query("type")type:Int = 1,
        ):BaseSearch<SearchSongBean>

    @GET("/cloudsearch")
    suspend fun getSearchAlbumResult(
        @Query("keywords")keywords:String,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit:Int = 30,
        @Query("type")type:Int = 10,
    ):BaseSearch<SearchAlbumBean>

    @GET("/cloudsearch")
    suspend fun getSearchArtistResult(
        @Query("keywords")keywords:String,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit:Int = 30,
        @Query("type")type:Int = 100,
    ):BaseSearch<SearchArtistBean>

    @GET("/cloudsearch")
    suspend fun getSearchPlaylistResult(
        @Query("keywords")keywords:String,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit:Int = 30,
        @Query("type")type:Int = 1000,
    ):BaseSearch<SearchPlaylistBean>

    @GET("/cloudsearch")
    suspend fun getSearchUserResult(
        @Query("keywords")keywords:String,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit:Int = 30,
        @Query("type")type:Int = 1002,
    ):BaseSearch<SearchUserBean>

    @GET("/cloudsearch")
    suspend fun getSearchMvResult(
        @Query("keywords")keywords:String,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit:Int = 30,
        @Query("type")type:Int = 1004,
    ):BaseSearch<SearchMvBean>

    @GET("/cloudsearch")
    suspend fun getSearchDjResult(
        @Query("keywords")keywords:String,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit:Int = 30,
        @Query("type")type:Int = 1009,
    ):BaseSearch<SearchDjBean>

    @GET("/cloudsearch")
    suspend fun getSearchVideoResult(
        @Query("keywords")keywords:String,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit:Int = 30,
        @Query("type")type:Int = 1014,
    ):BaseSearch<SearchVideoBean>

    /**
     * 获取用户详细信息*/
    @GET("/user/detail")
    suspend fun getUserDetailInfo(@Query("uid")uid: Long):UserDetailBean

    /**
     * 必选参数 : id :
     * 音乐 id
     * level: 播放音质等级
     * standard => 标准,higher => 较高,exhigh=>极高, lossless=>无损, hires=>Hi-Res, jyeffect => 高清环绕声, sky => 沉浸环绕声, jymaster => 超清母带
     */
    @GET("/song/url/v1")
    suspend fun getMusicUrl(@Query("id")id:Long,@Query("level")level:String="jymaster"):BaseResponse<List<SongUrlBean>>

    /**
     * 获取歌曲详细信息*/
    @GET("/song/detail")
    suspend fun getMusicDetail(@Query("ids")ids:Long):BaseSong<List<DailySong>>

    /**
     * 逐字歌词是通过 lrc/klyric/tlyric/yrc 区分的吗？
     * lrc -- 标准 lrc 歌词
     * klyric -- 音译歌词
     * tlyric -- 中文翻译歌词
     * romalrc 为音译歌词 (例如罗马字<romaji>)
     * yrc -- 逐字歌词
     * */
    @GET("/lyric")
    suspend fun getMusicLyric(@Query("id")id:Long):LyricInfoBean

    /**
     * 获取歌单所有歌曲*/
    @GET("/playlist/track/all")
    suspend fun getPlaylistSongs(
        @Query("id")id:Long,
        @Query("offset")offset:Int = 0,
        @Query("offset")limit:Int = 20,
    ):BaseSong<List<DailySong>>

    @GET("/album")
    suspend fun getAlbumSongs(
        @Query("id")id:Long
    ):AlbumDetailBean

    @GET("/playlist/detail")
    fun getPlaylistDetail(@Query("id")id: Long):Call<JsonObject>


    /**
     * 获取歌手部分信息*/
    @GET("/artist/detail")
    suspend fun getArtistDetailInfo(@Query("id")id:Long):BaseResponse<ArtistBriefBean>
    /**
     * 获取歌手热门50首歌曲
     */
    @GET("/artists")
    suspend fun getArtistSongs(@Query("id")id:Long): ArtistSongBean

    /**
     * 获取歌手专辑*/
    @GET("/artist/album")
    suspend fun getArtistAlbums(
        @Query("id")id:Long,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit: Int = 20
    ): ArtistAlbumBean

    /**
     * 获取歌手MV*/
    @GET("/artist/mv")
    suspend fun getArtistMvs( @Query("id")id:Long): ArtistMvBean

    /**
     * 获取与歌手相似的其他歌手*/
    @GET("/simi/artist")
    suspend fun getSimilarArtist(@Query("id")id:Long):BaseArtist

    /**
     * 获取电台详情-类似歌单*/
    @GET("/dj/detail")
    suspend fun getRadioStationDetail(@Query("rid")rid:Long):BaseResponse<ProgramDetailBean>

    /**
     * 获取电台音频-类似歌单中的歌曲*/
    @GET("/dj/program")
    suspend fun getRadioPrograms(
        @Query("rid")rid:Long,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit: Int = 20
    ):BaseRadioProgram

    /**
     * 获取歌单评论*/
    @GET("/comment/playlist")
    suspend fun getPlaylistComments(
        @Query("id")id:Long,
        @Query("time")time:Long,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit: Int = 20
    ):BaseCommentBean

    /**
     * 获取专辑评论*/
    @GET("/comment/album")
    suspend fun getAlbumComments(
        @Query("id")id:Long,
        @Query("time")time:Long,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit: Int = 20
    ):BaseCommentBean


    /**
     * parentCommentId: 楼层评论 id
     * id : 资源 id
     * time:上一页最后一项的time
     * type: 数字,资源类型
     * 0: 歌曲
     * 1: mv
     * 2: 歌单
     * 3: 专辑
     * 4: 电台节目
     * 5: 视频
     * 6: 动态
     * 7: 电台*/
    @GET("/comment/floor")
    suspend fun getFloorComments(
        @Query("parentCommentId")parentCommentId:Long,
        @Query("id")id:String,
        @Query("time")time:Long,
        @Query("type")type:Int,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit: Int = 20
    ):BaseResponse<FloorCommentBean>

    /**
     * 获取歌曲评论*/
    @GET("/comment/music")
    suspend fun getSongComments(
        @Query("id")id:Long,
        @Query("time")time:Long,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit: Int = 20
    ):BaseCommentBean

    /**
     * cid :评论 id
     * id :资源id
     * t : 是否点赞 , 1 为点赞 ,0 为取消点赞
     * type: 数字 , 资源类型,与上述楼层评论一致
     * 返回结果-successful:{code:200}
     * */
    @GET("/comment/like")
    suspend fun getAgreeComment(
        @Query("cid")cid:Long,
        @Query("id")id:String,
        @Query("t")t:Int,
        @Query("type")type:Int,
    ):BaseResponse<Any>

    /**
     * t:{1:发送, 2:回复}
     * type: 数字,资源类型
     * id:对应资源 id
     * content :要发送的内容
     * commentId :回复的评论 id (回复评论时必填)*/
    @GET("/comment")
    suspend fun getSendComment(
        @Query("id")id:String,
        @Query("commentId")commentId:Long,
        @Query("t")t:Int,
        @Query("type")type:Int,
        @Query("content")content:String
    ):SendCommentBean

    /**
     * 收藏专辑或者取消收藏专辑
     * id : 专辑 id
     * t : 1 为收藏,其他为取消收藏*/
    @GET("/album/sub")
    suspend fun getFavoriteAlbum(
        @Query("id")id:Long,
        @Query("t")t:Int
    )

    /**
     * 收藏歌单或者取消收藏歌单
     * id : 歌单 id
     * t :1:收藏,2:取消收藏*/
    @GET("/playlist/subscribe")
    suspend fun getFavoritePlaylist(
        @Query("id")id:Long,
        @Query("t")t:Int
    )

    /**
     * 最近播放-歌曲
     * */
    @GET("/record/recent/song")
    suspend fun getRecentSongs(
        @Query("cookie")cookie:String,
        @Query("limit")limit: Int = 30
    ):BaseResponse<RecentBean<DailySong>>

    /**
     * 最近播放-视频
     * */
    @GET("/record/recent/video")
    suspend fun getRecentVideos(
        @Query("cookie")cookie:String,
        @Query("limit")limit: Int = 30
    ):BaseResponse<RecentBean<Any>>

    /**
     * 最近播放-歌单
     * */
    @GET("/record/recent/playlist")
    suspend fun getRecentPlaylists(
        @Query("cookie")cookie:String,
        @Query("limit")limit: Int = 30
    ):BaseResponse<RecentBean<Playlist>>

    /**
     * 最近播放-专辑
     * */
    @GET("/record/recent/album")
    suspend fun getRecentAlbums(
        @Query("cookie")cookie:String,
        @Query("limit")limit: Int = 30
    ):BaseResponse<RecentBean<AlbumsBean>>

    /**
     * 最近播放-播客
     * */
    @GET("/record/recent/dj")
    suspend fun getRecentDjs(
        @Query("cookie")cookie:String,
        @Query("limit")limit: Int = 30
    ):BaseResponse<RecentBean<DjRadioBean>>

    /**
     * 获取收藏的歌手列表*/
    @GET("/artist/sublist")
    suspend fun getFavoriteArtist(
        @Query("cookie")cookie:String,
        @Query("offset")offset: Int,
        @Query("limit")limit :Int
    ):BaseResponse<List<Artist>>


    /**
     * 获取收藏的专辑列表*/
    @GET("/album/sublist")
    suspend fun getFavoriteAlbum(
        @Query("cookie")cookie:String,
        @Query("offset")offset: Int,
        @Query("limit")limit :Int
    ):BaseResponse<List<FavoriteAlbumBean>>

    /**
     * 获取收藏的Mv列表*/
    @GET("/mv/sublist")
    suspend fun getFavoriteMvs(
        @Query("cookie")cookie:String,
        @Query("offset")offset: Int,
        @Query("limit")limit :Int
    ):BaseResponse<List<VideoBean>>

    /**
     * 获取下载的歌曲URL*/
    @GET("/song/download/url")
    suspend fun getDownloadURL(
        @Query("cookie")cookie: String,
        @Query("id")id:Long,
        @Query("br")br:Int = 999000
    ):BaseResponse<SongUrlBean>

    /**
     * 获取MV视频播放地址*/
    @GET("/mv/url")
    suspend fun getMvURL(
        @Query("id")id:Long,
        @Query("r")r:Int = 1080
    ):BaseResponse<MvUrlBean>

    /**
     * 获取Mlog视频播放地址*/
    @GET("/mlog/url")
    suspend fun getMlogURL(
        @Query("id")id:String,
        @Query("res")res:Int = 1080
    ):BaseResponse<MlogInfoBean>

    /**
     * 获取相似的MV*/
    @GET("/simi/mv")
    suspend fun getSimilarMvs(
        @Query("mvid")mvid:Long
    ):SearchMvBean

    /**
     * 获取mv的详细信息*/
    @GET("/mv/detail")
    suspend fun getMvDetailInfo(
        @Query("mvid")mvid:Long
    ):MvDetailBean

    /**
     * 获取mv的评论*/
    @GET("/comment/mv")
    suspend fun getMvComments(
        @Query("id")id:Long,
        @Query("time")time:Long,
        @Query("offset")offset:Int = 0,
        @Query("limit")limit: Int = 20
    ):BaseCommentBean

    /**
     * 获取mlog的评论*/
    @GET("/comment/new")
    suspend fun getMlogComments(
        @Query("id")id:String,
        @Query("type")type:Int = 5,
        @Query("sortType")sortType:Int = 3,
        @Query("cursor")cursor:Long,
        @Query("pageNo")pageNo:Int = 0,
        @Query("pageSize")pageSize: Int = 20
    ):BaseResponse<MlogCommentBean>

    /**
     * mlog id转视频id*/
    @GET("/mlog/to/video")
    suspend fun getVideoId(
        @Query("id")id:String
    ):BaseResponse<String>

    /**
     * type:
     * 0: 歌曲
     * 1: mv
     * 2: 歌单
     * 3: 专辑
     * 4: 电台节目
     * 5: 视频
     * 6: 动态
     * 7: 电台
     *
     * t:1为点赞，0为取消点赞
     * */
    @GET("/resource/like")
    suspend fun getFavoriteResource(
        @Query("id")id:String,
        @Query("type")type:Int,
        @Query("t")t:Int
    ):BaseResponse<Any>
}      
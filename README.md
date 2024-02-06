# Magic Music APP

## 概述

此项目基于网易云API，使用Compose UI编写而成，项目整体采用MVVM架构，主要实现音视频资源播放(Media3-Exoplayer)、前台服务、歌曲下载、资源评论、歌曲解析、歌词逐行匹配等功能

## 效果预览-图片资源

> https://photos.app.goo.gl/41n5WRSf471zfLWJ9

## 效果预览-视频资源

> https://live.csdn.net/v/363120

## 功能预览

- [x] 主题
  - [x] 亮色主题
  - [x] 深色主题
  - [x] 手动切换主题样式
  - [x] 系统切换主题样式

- [x] 登录
  - [x] 手机号码登录
  - [x] 邮箱登录
  - [x] 扫码登录
- [x] 歌曲(Media3-Exoplayer)
  - [x] 歌曲播放（本地资源、网络资源）
  - [x] 歌词解析
  - [x] 歌词匹配（逐行）
  - [x] 评论歌曲
  - [x] 收藏歌曲
  - [x] 前台服务（通知栏媒体样式）
- [x] 视频(Media3-Exoplayer)
  - [x] 自定义exoplayer样式
  - [x] 视频横屏与竖屏切换
  - [x] 收藏视频
  - [x] 评论视频
  - [x] 分享视频
  - [x] 前台服务（通知栏媒体样式）
- [x] 下载(Aria)
  - [x] 歌曲下载
  - [x] 前台服务（通知栏媒体样式）
  - [x] 清空下载内容
- [x] 前台服务
  - [x] 音视频媒体通知栏样式
  - [x] 下载进度条通知栏样式
- [x] 歌单
  - [x] 歌单
  - [x] 专辑
  - [x] 电台
- [x] 搜索
  - [x] 搜索记录
  - [x] 清空搜索记录
  - [x] 搜索建议
  - [x] 热门搜索
  - [x] 搜索结果（歌曲、歌单、专辑、歌手...）
- [x] 评论
  - [x] 资源评论（歌曲、歌单、专辑...）
  - [x] 楼层评论(回复某人的评论)
  - [x] 发送评论
  - [x] 点赞评论

- [x] 收藏
- [x] 最近播放
- [x] 播放列表
- [x] 用户信息
- [x] 推荐
  - [x] 歌单推荐
  - [x] 专辑推荐
  - [x] 歌曲推荐
  - [x] 歌手推荐
- [x] 榜单

## Library

|   Library Name   | Description      |
| :--------------: | ---------------- |
| retrofit、okhttp | 用户网络请求     |
|       hilt       | 用于依赖注入     |
| media-exoplayer  | 用于音视频播放   |
|       aria       | 用于资源下载     |
|       coil       | 用于网络图片加载 |
|      pager       | 用户多页面切换   |
|     paging3      | 用户分页加载     |
|       room       | 本地资源存储     |
|       ...        | ...              |
|                  |                  |

## 部分功能实现效果图

### 登录

<Img src="MagicMusicPictures/Screenshot_20240205-192216.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-192203.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-190539.png" width="200">













<Img src="MagicMusicPictures/Screenshot_20240205-192209.png" width="200">

</br>

### Navigation

<Img src="MagicMusicPictures/Screenshot_20240205-194028.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-194047.png" width="200">



















</br>

### 歌曲播放

<Img src="MagicMusicPictures/Screenshot_20240205-193258.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-193302.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-193340.png" width="200">



















</br>

### 视频播放

<Img src="MagicMusicPictures/Screenshot_20240205-193520.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-193522.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-193537.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-193602.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-193552.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-220807.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-193529.png" width="400">



































<Img src="MagicMusicPictures/Screenshot_20240205-193543.png" width="400">

</br>

### 搜索

<Img src="MagicMusicPictures/Screenshot_20240205-192634.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-214446.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-192744.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-192747.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-214440.png" width="200">



































</br>

### 搜索结果

<Img src="MagicMusicPictures/Screenshot_20240205-192640.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-192643.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-192646.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-192727.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-192729.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-192724.png" width="200">



































### 推荐

<Img src="MagicMusicPictures/Screenshot_20240205-193930.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-193934.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-193947.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-194003.png" width="200">



















### 榜单

<Img src="MagicMusicPictures/Screenshot_20240205-194032.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-192328.png" width="200">



















### 歌手详情

<Img src="MagicMusicPictures/Screenshot_20240205-192539.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-192614.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-192617.png" width="200">



















<Img src="MagicMusicPictures/Screenshot_20240205-194011.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-194017.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-194022.png" width="200">



















### 歌曲下载

<Img src="MagicMusicPictures/Screenshot_20240205-193630.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-193642.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-193647.png" width="200">



















### 评论

<Img src="MagicMusicPictures/Screenshot_20240205-192914.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-192917.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-192922.png" width="200">



















### 前台服务

<Img src="MagicMusicPictures/Screenshot_20240205-193347.png" width="200"><Img src="MagicMusicPictures/Screenshot_20240205-193633.png" width="200">



















### Other

每一个页面都适配了亮色主题和深色主题，由于篇幅冗余，还有些许页面没有贴出效果图，有意者，可以点击上方图片资源或者视频资源链接进行观看！

## End

欢迎诸位issue！

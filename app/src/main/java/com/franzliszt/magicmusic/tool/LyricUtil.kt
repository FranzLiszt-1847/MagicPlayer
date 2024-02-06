package com.franzliszt.magicmusic.tool

import android.text.format.DateUtils
import com.franzliszt.magicmusic.bean.lrc.LyricAuthorBean
import com.franzliszt.magicmusic.bean.lrc.LyricBean
import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern


class LyricUtil{
    companion object{
        //其中“(.+)”是匹配任意长度字符，“//”d是匹配0-9任一数字，“//d{2,3}”是匹配2位或者3位数字
        private val PATTERN_LINE = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d{2,3}\\])+)(.+)")
        private val PATTERN_TIME = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})\\]")
        /**
         * 解析文本型歌词
         * @param lrcText
         */
         fun parseLyric(lrcText: String): List<LyricBean>? {
            if (lrcText.isEmpty()) {
                return null
            }
            val entityList: MutableList<LyricBean> = ArrayList<LyricBean>()
            // 以换行符为分割点
            val array = lrcText.split("\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (line in array) {
                // 循环遍历按行解析
                val list: List<LyricBean>? = parseLine(line)
                list?.let {
                    entityList.addAll(it)
                }
            }

            // 以时间为基准，从小到大排列
            entityList.sortBy {
                it.time
            }
            return entityList
        }

        /**
         * 解析每一句歌词
         * 其中头部和尾部存在歌手、编曲等JSON信息
         * 中间为标准LRC歌词格式
         * @param line
         */
        private fun parseLine(line: String): List<LyricBean>? {
            var newLine = line
            val entryList: MutableList<LyricBean> = ArrayList<LyricBean>()
            if (newLine.isEmpty()) {
                return null
            }
            // 去除空格
            newLine = line.trim { it <= ' ' }
            /**
             * 作者等信息：
             * [{"t":0,"c":[{"tx":"作词: "},{"tx":"黄家驹","li":"http://p1.music.126.net/2rERC5bz1BD0GZrU06saTw==/109951166629360845.jpg","or":"orpheus://nm/artist/home?id=189688&type=artist"}]},
             *  {"t":1000,"c":[{"tx":"作曲: "},{"tx":"黄家驹","li":"http://p1.music.126.net/2rERC5bz1BD0GZrU06saTw==/109951166629360845.jpg","or":"orpheus://nm/artist/home?id=189688&type=artist"}]},
             *  {"t":2000,"c":[{"tx":"编曲: "},{"tx":"Beyond"},{"tx":"/"},{"tx":"梁邦彦"}]},
             *  {"t":3000,"c":[{"tx":"制作人: "},{"tx":"Beyond"},{"tx":"/"},{"tx":"梁邦彦"}]},
             *  {"t":271852,"c":[{"tx":"录音: "},{"tx":"Shunichi Yokoi"}]}]
             * */
            /***
             * 歌词和时间：[00:18.466]今天我 寒夜里看雪飘过
             * */
            val lineMatcher: Matcher = PATTERN_LINE.matcher(newLine)
            // 正则表达式，判断line中是否包含“[00:00.00]xxx”格式的内容"
            // 如果没有，则为JSON字符串
            try {
                if (!lineMatcher.matches()) {
                    if (!PATTERN_TIME.matcher(newLine).matches()){
                        //解析作者等信息
                        val infoBean = GsonFormat.fromJson(newLine,LyricAuthorBean::class.java)
                        var content = ""
                        infoBean.c.forEach {
                            //将所有信息组成一行
                            content += it.tx
                        }
                        entryList.add(LyricBean(infoBean.t,content))
                    }else{
                        //某一行歌词只包含“[00:00.00]”内容，不包含文字，则不进行处理
                        return null
                    }
                }
            }catch (e:Exception){
               println(e.message)
                return null
            }

            // 获取文本内容
            val text: String? = lineMatcher.group(3)
            // 获取时间标签
            val times: String? = lineMatcher.group(1)
            val timeMatcher: Matcher? = times?.let { PATTERN_TIME.matcher(it) }
            if (timeMatcher != null) {
                //将时间转为毫秒级
                while (timeMatcher.find()) {
                    val min: Long = timeMatcher.group(1)?.toLong() ?:0L // 分
                    val sec: Long = timeMatcher.group(2)?.toLong() ?:0L // 秒
                    val mil: Long = timeMatcher.group(3)?.toLong() ?:0L // 毫秒
                    val time: Long = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil * 10
                    entryList.add(LyricBean(text = text ?: "", time = time))
                }
            }
            return entryList
        }
    }
}

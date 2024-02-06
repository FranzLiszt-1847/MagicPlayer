package com.franzliszt.magicmusic.tool


import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.region.CityBean
import com.franzliszt.magicmusic.bean.region.CountyBean
import com.franzliszt.magicmusic.bean.region.ProvinceBean

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder

object XMLParserUtil {
    private val provinceTag = "province"
    private val cityTag = "city"
    private val countyTag = "district"
    private val NAME = "name"
    private val CODE = "zipcode"
    private lateinit var pullParser : XmlPullParser
    private lateinit var provinceList:MutableList<ProvinceBean>

    init {
        provinceList = ArrayList<ProvinceBean>()
        create()
        resolve()
    }


    private fun create(){
        val factory = XmlPullParserFactory.newInstance();
        pullParser = factory.newPullParser();
        val inputStream = APP.context.resources.openRawResource(R.raw.region);
        pullParser.setInput(InputStreamReader(inputStream));
    }

    private fun resolve(){
        var province: ProvinceBean = ProvinceBean()
        var currencyProvince = false
        var currentCityFlag = false
        var currentCity: Int = -1
        var currentCounty: Int = -1
        try {
        var type = pullParser.eventType
        while (type != XmlPullParser.END_DOCUMENT){
            when(type){
                XmlPullParser.START_DOCUMENT-> provinceList = ArrayList<ProvinceBean>()

                XmlPullParser.START_TAG->{
                    when (pullParser.name) {
                        provinceTag -> {
                            /**
                             * 省份*/
                            province = ProvinceBean()
                            currencyProvince = true

                            province.provinceName = pullParser.getAttributeValue(null, NAME)
                            province.provinceCode = pullParser.getAttributeValue(null, CODE).toInt()
                        }
                        cityTag -> {
                            /**
                             * 城市*/
                            if (currencyProvince){
                                currentCity = -1
                                currencyProvince = false
                                province.cityList = ArrayList<CityBean>()
                            }
                            currentCityFlag = true
                            currentCity++
                            val bean = CityBean()
                            bean.cityName =  pullParser.getAttributeValue(null, NAME)
                            bean.cityCode = pullParser.getAttributeValue(null, CODE).toInt()
                            province.cityList.add(bean)
                        }
                        countyTag -> {
                            /**
                             * 区县*/
                            if (currentCityFlag){
                                currentCounty = -1
                                currentCityFlag = false
                                province.cityList[currentCity].countyList = ArrayList<CountyBean>()
                            }
                            currentCounty++
                            val bean = CountyBean()
                            bean.countyName = pullParser.getAttributeValue(null, NAME)
                            bean.countyCode = pullParser.getAttributeValue(null, CODE).toInt()
                            province.cityList[currentCity].countyList.add(bean)
                        }
                    }
                }

                XmlPullParser.END_TAG->{
                    val provinceName = pullParser.name
                    if (pullParser.name == provinceTag) provinceList.add(province)
                }
                XmlPullParser.END_DOCUMENT->{}
            }
            type = pullParser.next()
        }
        }catch (e: XmlPullParserException){
            e.printStackTrace()
        }catch (e:IOException){
            e.printStackTrace()
        }catch (e:NullPointerException){
            e.printStackTrace()
        }
    }

    /**
     * 查询省级*/
    fun getProvinceNameByCode_B(code: Int):ProvinceBean?{
        val province = searchProvince(code)
        province?.let { return province }
        return null
    }

    fun getProvinceNameByCode_S(code: Int):String?{
        val province = searchProvince(code)
        province?.let { province.provinceName }
        return null
    }

    /**
     * 根据行政代码获取省/城市名
     * 模糊搜索:传入省或者城市编码，具体类型不明
     * 查找某个城市的名称，首先通过找到该省然后在进一步通过城市行政代码查询该市名称*/

    /**
     * 返回组合类型
     * 例如：湖南-长沙*/
    fun getCityNameByCode(provinceCode: Int,cityCode: Int,split: String): String{
        val builder = StringBuilder()
        val province = searchProvince(provinceCode)
        if (province != null){
            builder.append(province.provinceName).append(split)
            val city = searchCity(cityCode, province)
            city?.let {
                builder.append(city.cityName)
                return builder.toString()
            }
        }
        return ""
    }

    /**
     * 单独返回城市名称*/
    fun getCityNameByCode(province: ProvinceBean, cityCode: Int): String{
        val city = searchCity(cityCode, province)
        city?.let { return city.cityName }
        return ""
    }

    private fun searchProvince(code: Int): ProvinceBean?{
        if (provinceList.size == 0)return null
        for (i in 0 until provinceList.size){
            if (code == provinceList[i].provinceCode) return provinceList[i]
        }
        return null
    }

    private fun searchCity(code: Int,bean: ProvinceBean): CityBean?{
        if (bean.cityList.size == 0) return null
        for (i in 0 until bean.cityList.size){
            if (code == bean.cityList[i].cityCode) return bean.cityList[i]
        }
        return null
    }
}
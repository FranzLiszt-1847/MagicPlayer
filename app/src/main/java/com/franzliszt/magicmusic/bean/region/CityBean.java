package com.franzliszt.magicmusic.bean.region;

import java.util.List;

public class CityBean {
    private String cityName;//市
    private int cityCode;//市代码
    private List<CountyBean> countyList;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public List<CountyBean> getCountyList() {
        return countyList;
    }

    public void setCountyList(List<CountyBean> countyList) {
        this.countyList = countyList;
    }
}

package com.franzliszt.magicmusic.tool

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser

class GsonFormat {
    companion object{
        /**
         * resolve jsonObject*/
        fun <T> fromJson(json: String,c: Class<T>): T{
            val gson =  Gson();
            return  gson.fromJson(json,c);
        }
        /**
         * resolve jsonArray*/
        fun <T> fromListJson(json: String,c: Class<T>): List<T>{
            val list  =  ArrayList<T>();
            val gson =  Gson();
            val array: JsonArray =  JsonParser().parse(json).asJsonArray;
            for (element: JsonElement in array) {
                list.add(gson.fromJson(element,c));
            }
            return list;
        }
    }
}
package com.franzliszt.magicmusic.route.search

sealed class SearchStatus(val msg:String){
    data class SearchSuccess(val key:String):SearchStatus(msg = key)
    data class Message(val message:String):SearchStatus(msg = message)
    object SearchFailed:SearchStatus("Not Found!")
    object SearchEmpty:SearchStatus("The key cannot be empty!")
    object Clear:SearchStatus("Clear Successful!")
    object Withdraw:SearchStatus("Withdraw Successful!")
}

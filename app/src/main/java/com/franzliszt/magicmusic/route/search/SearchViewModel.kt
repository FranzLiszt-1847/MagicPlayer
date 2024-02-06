package com.franzliszt.magicmusic.route.search

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.franzliszt.magicmusic.bean.search.DefaultSearchBean
import com.franzliszt.magicmusic.bean.search.HotSearch
import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.bean.search.SearchSuggestionBean
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.network.RemoteResult
import com.franzliszt.magicmusic.network.baseApiCall
import com.franzliszt.magicmusic.usecase.search.SearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val service: MusicApiService,
    private val useCase: SearchUseCase
):ViewModel() {

    /**
     * 清除历史记录Dialog相关信息提示字符*/
    val title = "Clear History"
    val content = "You are clearing the historical search history, please confirm if you want to delete it？"
    val confirm = "CONFIRM"
    val cancel = "CANCEL"
    private val _uiStatus = mutableStateOf(SearchUIStatus())
    val uiStatus:State<SearchUIStatus> = _uiStatus

    private val _eventFlow = MutableSharedFlow<SearchStatus>()
    val eventFlow = _eventFlow.asSharedFlow()

    //暂存被删除的历史记录，通过撤回按钮可以恢复
    private var histories:List<SearchRecordBean> = emptyList()

    private var job: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getDefaultSearch()
            getHotSearch()
            getHistories()
        }
    }

    fun onEvent(event: SearchEvent){
        when(event){
            is SearchEvent.ChangeKey->{
                viewModelScope.launch(Dispatchers.IO) {
                    _uiStatus.value = uiStatus.value.copy(
                        keywords = event.key
                    )
                    if (event.key.isNotEmpty()){
                      getSearchSuggestion(key = event.key)
                    }else{
                        _uiStatus.value = uiStatus.value.copy(
                            suggestions = null,
                            isEmptySuggestions = true
                        )
                    }
                }
            }
            is SearchEvent.Search->{
                viewModelScope.launch {
                    if (event.key.isNotEmpty()){
                        //如果输入的关键不为空，则跳转到搜索结果页
                        useCase.insert(SearchRecordBean(
                            createTime = System.currentTimeMillis(),
                            keyword = event.key
                        ))
                        _eventFlow.emit(SearchStatus.SearchSuccess(event.key))
                    }else{
                        _eventFlow.emit(SearchStatus.SearchEmpty)
                    }
                }
            }

            is SearchEvent.Clear->{
                //发起请求清除历史搜索记录，显示Dialog，二次确认
                _uiStatus.value = uiStatus.value.copy(
                    isShowDialog = true
                )
            }

            is SearchEvent.CancelClear->{
                //取消删除历史搜索记录
                _uiStatus.value = uiStatus.value.copy(
                    isShowDialog = false
                )
            }

            is SearchEvent.ConfirmClear->{
                //清除历史搜索记录
                viewModelScope.launch {
                    histories = _uiStatus.value.histories
                    useCase.deleteAll()
                    _uiStatus.value = uiStatus.value.copy(
                        isShowClear = false,
                        isShowDialog = false,
                        histories = emptyList()
                    )
                    _eventFlow.emit(SearchStatus.Clear)
                }
            }

            is SearchEvent.Withdraw->{
                //撤回清除历史记录操作
                viewModelScope.launch {
                    if (histories.isNotEmpty()){
                        useCase.insertAll(histories)
                    }
                    histories = emptyList()
                    _eventFlow.emit(SearchStatus.Withdraw)
                }
            }

            else ->{}
        }
    }

    /**
     * 默认搜索词*/
    private suspend fun getDefaultSearch(){
        val response = baseApiCall { service.getDefaultSearch() }
        when(response){
            is RemoteResult.Success->{
                if (response.data.data != null && response.data.code == 200){
                    _uiStatus.value = uiStatus.value.copy(
                        default = response.data.data.showKeyword
                    )
                }else{
                    _eventFlow.emit(SearchStatus.Message("The error code is ${response.data.code}"))
                }
            }
            is RemoteResult.Error->{
                _eventFlow.emit(SearchStatus.Message(response.exception.message.toString()))
            }
        }
    }

    /**
     * 热搜列表*/
    private suspend fun getHotSearch() {
        val response = baseApiCall { service.getHotSearch() }
        when(response){
            is RemoteResult.Success->{
                if (response.data.result != null && response.data.code == 200){
                    _uiStatus.value = uiStatus.value.copy(
                        hots = response.data.result.hots
                    )
                }else{
                    _eventFlow.emit(SearchStatus.Message("The error code is ${response.data.code}"))
                }
            }
            is RemoteResult.Error->{
                _eventFlow.emit(SearchStatus.Message(response.exception.message.toString()))
            }
        }
    }

    /**
     * 搜索建议*/
    private suspend fun getSearchSuggestion(key:String){
        val response = baseApiCall { service.getSearchSuggestion(keywords = key) }
        when(response){
            is RemoteResult.Success->{
                if (response.data.result != null && response.data.code == 200){
                    _uiStatus.value = uiStatus.value.copy(
                        suggestions = response.data.result,
                        isEmptySuggestions = false
                    )
                }else{
                    _uiStatus.value = uiStatus.value.copy(
                        suggestions = null,
                        isEmptySuggestions = true
                    )
                    _eventFlow.emit(SearchStatus.SearchFailed)
                }
            }
            is RemoteResult.Error->{
                _eventFlow.emit(SearchStatus.Message(response.exception.message.toString()))
            }
        }
    }

    /**
     * 获取历史搜索记录*/
    private fun getHistories(){
        job?.cancel()
        job = useCase.queryAll().onEach { histories->
            if (histories.isNotEmpty()){
                _uiStatus.value = uiStatus.value.copy(
                    histories = histories,
                    isShowClear = true
                )
            }else{
                _uiStatus.value = uiStatus.value.copy(
                    histories = emptyList(),
                    isShowClear = false
                )
            }
        }.launchIn(viewModelScope)
    }
}
package com.franzliszt.magicmusic.datapaging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState


class BaseDataPagingSource<T:Any>(
    private val block:suspend (Int,Int)->List<T>
):PagingSource<Int, T>() {
    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val offset = params.key ?: 0
        val limit = params.loadSize
        return try {
            val response = block(offset,limit)
            val previousPage = if (offset > 0) offset - 1 else null //前一页
            val nextPage = offset + 1 //下一页
            LoadResult.Page(
                data = response,
                prevKey = previousPage,
                nextKey = nextPage
            )
        } catch (e:Exception){
            LoadResult.Error(e)
        }
    }
}

//默认加载项
const val PAGE_SIZE:Int = 20

fun <T : Any> creator(
    pageSize: Int = PAGE_SIZE,
    enablePlaceholders: Boolean = false,
    block: suspend (Int, Int) -> List<T>
): Pager<Int, T> = Pager(
    config = PagingConfig(
        pageSize = pageSize,
        enablePlaceholders = enablePlaceholders,
        initialLoadSize = pageSize
    ),
    pagingSourceFactory = { BaseDataPagingSource(block = block) }
)



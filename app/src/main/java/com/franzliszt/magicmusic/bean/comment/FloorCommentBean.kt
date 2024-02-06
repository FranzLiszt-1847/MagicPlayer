package com.franzliszt.magicmusic.bean.comment

data class FloorCommentBean(
    val bestComments: List<CommentBean>,
    val comments: List<CommentBean>,
    val currentComment: Any,
    val hasMore: Boolean,
    val ownerComment: CommentBean,
    val time: Long,
    val totalCount: Long
)
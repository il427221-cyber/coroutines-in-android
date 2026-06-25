package ru.netology.nmedia.dto

data class Post(
    var id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val saved: Boolean,
    val serverId:Long?,
    val newStatus: Boolean = false
)


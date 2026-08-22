package ru.netology.nmedia.dto

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override var id: Long,
    val author: String,
    val authorAvatar: String,
    val authorId: Long,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val saved: Boolean,
    val serverId:Long?,
    val newStatus: Boolean = false,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false
): FeedItem

data class Ad (
    override val id: Long,
    val image: String
): FeedItem

data class Attachment(
    val url: String,
    val type: AttachmentType
)
enum class AttachmentType {
    IMAGE
}




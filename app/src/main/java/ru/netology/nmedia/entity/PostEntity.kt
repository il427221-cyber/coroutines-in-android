package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val saved: Boolean,
    val serverId: Long?,
    val newStatus: Boolean = false,
    @Embedded
    val attachment: Attachment? = null
) {
    fun toDto() = Post(
        id = localId,
        author = author,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        likedByMe = likedByMe,
        likes = likes,
        saved = saved,
        serverId = serverId,
        newStatus = newStatus,
        attachment = attachment
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                localId = dto.id,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                content = dto.content,
                published = dto.published,
                likedByMe = dto.likedByMe,
                likes = dto.likes,
                saved = dto.saved,
                serverId = dto.serverId,
                newStatus = dto.newStatus,
                attachment = dto.attachment
            )

    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)

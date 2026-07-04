package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<List<Post>>
    fun getNewer(id:Long):Flow<Int>

    fun getNewPostsCount(): Flow<Int>

    suspend fun markAllNewPostsAsVisible()
    suspend fun getAll()
    suspend fun save(post: Post,file: File?)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long, likedByMe: Boolean)
}

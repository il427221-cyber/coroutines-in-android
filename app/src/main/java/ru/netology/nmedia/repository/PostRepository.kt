package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.authorization.AuthState
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    fun getNewPostsCount(): Flow<Int>
    suspend fun markAllNewPostsAsVisible()
    suspend fun getLatest()
    suspend fun save(post: Post,file: File?)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long, likedByMe: Boolean)
    suspend fun updateUser(login: String, password: String): AuthState
    suspend fun registerUser(login: String, password: String,name: String): AuthState
}

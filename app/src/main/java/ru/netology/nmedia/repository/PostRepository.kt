package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.authorization.AuthState
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
    suspend fun updateUser(): AuthState
    suspend fun registerUser(): AuthState
}

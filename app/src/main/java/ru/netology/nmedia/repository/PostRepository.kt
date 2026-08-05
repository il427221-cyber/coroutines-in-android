package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
    suspend fun updateUser(login: String, password: String): AuthState
    suspend fun registerUser(login: String, password: String,name: String): AuthState

//    suspend fun registerWithPhoto(
//        login: RequestBody,
//        pass: RequestBody,
//        name: RequestBody,
//        file: File): AuthState
}

package ru.netology.nmedia.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okio.IOException
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import kotlin.time.Duration.Companion.milliseconds

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getAllLocalPosts().map(List<PostEntity>::toDto)

    override suspend fun getAll() {
        try {
            val response = PostsApi.service.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.map { it.copy(serverId = it.id, saved = true) }.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val entityToinsert = PostEntity.fromDto(post.copy(saved = false, serverId = null))
            val generatedLocalId = dao.insert(entityToinsert)
            val postToSend: Post = if (post.serverId == null) {
                post.copy(id = 0L)
            } else {
                post.copy(id = post.serverId)
            }
                val response = PostsApi.service.save(postToSend)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                } else {
                    val body = response.body() ?: throw ApiError(response.code(), response.message())
                    dao.getPostById(generatedLocalId) ?: return
                    dao.removeById(generatedLocalId)
                    val updatedEntity = PostEntity.fromDto(
                        body.copy(
                            saved = true,
                            serverId = body.id
                        )
                    )
                    dao.insert(updatedEntity)
                }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun removeById(id: Long) {
        try{
            dao.removeById(id)
            PostsApi.service.removeById(id)
        } catch(e:Exception) {
            throw NetworkError
        } catch(e:Exception) {
            throw UnknownError
        }

    }

    override suspend fun likeById(id: Long, likedByMe: Boolean) {
        try{
            dao.likeById(id)
            if(likedByMe)
                PostsApi.service.dislikeById(id)
             else
                PostsApi.service.likeById(id)
        } catch (e:Exception){
            dao.likeById(id)
            throw NetworkError
        } catch (e:Exception) {
            dao.likeById(id)
            throw UnknownError
        }
    }

    override fun getNewer(id: Long): Flow<Int> = flow{
        while(true) {
            delay(10_000.milliseconds)
            val response = PostsApi.service.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.map { it.copy(serverId = it.id, saved = true, newStatus = true) }.toEntity())
            emit(body.size)
        }
    }.catch{e -> throw AppError.from(e) }


    override fun getNewPostsCount(): Flow<Int> {
        return dao.getNewPostsCount()
    }

    override suspend fun markAllNewPostsAsVisible() {
        dao.markAllNewPostsVisible()
    }
}

package ru.netology.nmedia.repository

import androidx.lifecycle.*
import okio.IOException
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll().map(List<PostEntity>::toDto)

    override suspend fun getAll() {
        try {
            val response = PostsApi.service.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val entityToinsert = PostEntity.fromDto(post.copy(saved = false,serverId = null))
            val generatedLocalId = dao.insert(entityToinsert)
            val response = PostsApi.service.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body()?: throw ApiError(response.code(), response.message())
            val existingEntity = dao.getPostById(generatedLocalId)?: return
            val updatedEntity = existingEntity.copy(
                saved = true,
                serverId = body.serverId
            )
            dao.update(updatedEntity)

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
            if(likedByMe) {
                PostsApi.service.likeById(id)
            }
            PostsApi.service.dislikeById(id)
        } catch (e:Exception){
            throw NetworkError
        } catch (e:Exception) {
            throw UnknownError
        }
    }
}

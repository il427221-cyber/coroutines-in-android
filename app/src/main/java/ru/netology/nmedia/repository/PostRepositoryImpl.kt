package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.nmedia.api.*
import ru.netology.nmedia.authorization.AppAuth
import ru.netology.nmedia.authorization.AuthState
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.File
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostsApiService,
    private val appAuth: AppAuth,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb

) : PostRepository {
    private val pagingSource = dao.getPagingSource()

    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    override val data: Flow<PagingData<Post>>
            = appAuth.authStateFlow.flatMapLatest { authState ->
            Pager(config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                pagingSourceFactory = { dao.getPagingSource() },
                remoteMediator = PostRemoteMediator(
                    apiService = apiService,
                    postDao = dao,
                    appAuth = appAuth,
                    postRemoteKeyDao = postRemoteKeyDao,
                    appDb = appDb)
            ).flow.map{it.map(PostEntity::toDto)}
        }
        .flowOn(Dispatchers.Default)

    override suspend fun getLatest() {
        try {
            val response = apiService.getLatest(5)
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

    override suspend fun save(post: Post, file: File?) {
        try {
            val postWithAttachment = file?.let{
                Attachment(upload(it).id, AttachmentType.IMAGE)
            }
                ?.let{post.copy(attachment = it)
                }?:post

            val entityToinsert = PostEntity.fromDto(postWithAttachment.copy(saved = false, serverId = null))
            val generatedLocalId = dao.insert(entityToinsert)
            val postToSend: Post = if (postWithAttachment.serverId == null) {
                postWithAttachment.copy(id = 0L)
            } else {
                postWithAttachment.copy(id = postWithAttachment.serverId, attachment = postWithAttachment.attachment)
            }
                val response = apiService.save(postToSend)
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
    private suspend fun upload(file: File): Media =
        apiService.saveMedia(
            MultipartBody.Part.createFormData("file","file",file.asRequestBody())
        )

    override suspend fun removeById(id: Long) {
        try{
            dao.removeById(id)
            apiService.removeById(id)
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
                apiService.dislikeById(id)
            } else {
                apiService.likeById(id)
            }
            pagingSource.invalidate()

        } catch (e:Exception){
            dao.likeById(id)
            throw NetworkError
        }
    }

    override fun getNewPostsCount(): Flow<Int> {
        return dao.getNewPostsCount()
    }

    override suspend fun markAllNewPostsAsVisible() {
        dao.markAllNewPostsVisible()
    }

     override suspend fun updateUser(login:String, password: String): AuthState {
        val response = apiService.updateUser(login,password)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }
        return response.body() ?: throw ApiError(response.code(), response.message())
    }

    override suspend fun registerUser(login: String, password: String,name: String): AuthState {
        val response = apiService.registerUser(login, password, name)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }
        return response.body() ?: throw ApiError(response.code(), response.message())
    }
}

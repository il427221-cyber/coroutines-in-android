package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = 0,
    saved = false,
    serverId = 0
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(context = application).postDao())

    val data: LiveData<FeedModel> = repository.data.map { FeedModel(it, it.isEmpty()) }
        .catch { it.printStackTrace() }
        .asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo:LiveData<PhotoModel?>
        get() = _photo

    val newerCount = data.switchMap {
        repository.getNewer(it.posts.firstOrNull()?.id ?: 0)
            .catch { _dataState.postValue(FeedModelState(error = true)) }
            .asLiveData(Dispatchers.Default)
    }

    val newPostsCount: LiveData<Int> = repository.getNewPostsCount().asLiveData(Dispatchers.Default)

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String>
        get() = _errorEvent

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun markAllNewPostsAsVisible() {
        viewModelScope.launch {
            repository.markAllNewPostsAsVisible()
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun setPhoto(photoModel: PhotoModel?) {
        _photo.value = photoModel
    }

    fun save() {
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.save(it,_photo.value?.file)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        _photo.value = null
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        val currentPosts = data.value?.posts.orEmpty()
        val post = currentPosts.find { it.id == id } ?: return
        viewModelScope.launch {
            try {
                repository.likeById(id, likedByMe = post.likedByMe)
            } catch (_: Exception) {
                _dataState.value = FeedModelState(error = true)
            }

        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (_: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }
}

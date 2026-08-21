package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.authorization.AppAuth
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorId = 0,
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = 0,
    saved = false,
    serverId = 0
)

@HiltViewModel
    class PostViewModel @Inject constructor(
        private val repository: PostRepository,
        appAuth: AppAuth
    ) : ViewModel() {
        private val cached = repository.data.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<FeedItem>> = appAuth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map{post ->
                    if(post is Post) {
                        post.copy(ownedByMe = post.authorId == myId)
                    } else {
                        post
                    }
                }
                }
        }

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo:LiveData<PhotoModel?>
        get() = _photo

    val newPostsCount: LiveData<Int> = repository.getNewPostsCount().asLiveData(Dispatchers.Default)

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String>
        get() = _errorEvent

    private val _refreshPostsEvent = MutableSharedFlow<Unit>()
    val refreshPostsEvent = _refreshPostsEvent.asSharedFlow()

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getLatest()
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
            repository.getLatest()
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

    fun likeById(id: Long, likedByMe: Boolean) {
        viewModelScope.launch {
            _dataState.value = FeedModelState(loading = true)
            try {
                repository.likeById(id, likedByMe = likedByMe)
                _dataState.value = FeedModelState(loading = false)
                _refreshPostsEvent.emit(Unit)
            } catch (e: Exception) {
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

package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.authorization.AppAuth
import ru.netology.nmedia.authorization.AuthState
import ru.netology.nmedia.repository.PostRepository
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth
) : ViewModel() {

//    @Inject
//    lateinit var appAuth: AppAuth

    val data: LiveData<AuthState> = appAuth.authStateFlow
        .asLiveData(Dispatchers.Default)
    val authenticated: Boolean
        get() = appAuth.authStateFlow.value.id != 0L

    fun updateUser(login:String, password:String) {
        viewModelScope.launch {
            val authResponse = repository.updateUser(login,password)
            appAuth.setAuth(authResponse.id, authResponse.token.toString())
        }
    }
}



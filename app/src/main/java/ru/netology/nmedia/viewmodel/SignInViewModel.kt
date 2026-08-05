package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.authorization.AppAuth
import ru.netology.nmedia.authorization.AuthState
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.repository.PostRepository

class SignInViewModel(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {

    private val dependencyContainer = DependencyContainer.getInstance()

    val data: LiveData<AuthState> = appAuth
        .authStateFlow
        .asLiveData(Dispatchers.Default)
    val authenticated: Boolean
        get() = dependencyContainer.appAuth.authStateFlow.value.id != 0L

    fun updateUser(login:String, password:String) {
        viewModelScope.launch {
            val authResponse = repository.updateUser(login,password)
            dependencyContainer.appAuth.setAuth(authResponse.id, authResponse.token.toString())
        }
    }
}



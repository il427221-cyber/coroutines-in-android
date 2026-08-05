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


    class AuthViewModel(
        private val repository: PostRepository,
        appAuth: AppAuth
    ) : ViewModel() {
        private val dependencyContainer = DependencyContainer.getInstance()

    val data: LiveData<AuthState> = appAuth.authStateFlow
        .asLiveData(Dispatchers.Default)

    val authenticated: Boolean
        get() = dependencyContainer.appAuth.authStateFlow.value.id != 0L

    fun registerUser(login: String, password: String,name: String) {
        viewModelScope.launch {
            val registerResponse = repository.registerUser(login, password, name)
            dependencyContainer.appAuth.setAuth(registerResponse.id, registerResponse.token.toString())

        }
    }
}
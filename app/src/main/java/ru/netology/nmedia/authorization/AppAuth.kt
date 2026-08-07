package ru.netology.nmedia.authorization

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.service.FcmModule
import javax.inject.Inject
import javax.inject.Singleton

data class AuthState(
    val id: Long = 0,
    val token: String? = null
)

@Singleton
class AppAuth @Inject constructor (
    @ApplicationContext
    private val context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val idKey = "id"
    private val tokenKey = "token"

    private val _authStateFlow: MutableStateFlow<AuthState>

    init {
        val id = prefs.getLong(idKey, 0)
        val token = prefs.getString(tokenKey, null)

        if (id == 0L || token == null) {
            _authStateFlow = MutableStateFlow(AuthState())
            with(prefs.edit()) {
                clear()
                apply()
            }
        } else {
            _authStateFlow = MutableStateFlow(AuthState(id, token))
        }
        sendPushToken()
    }

    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _authStateFlow.value = AuthState(id, token)
        with(prefs.edit()) {
            putLong(idKey, id)
            putString(tokenKey, token)
            apply()
        }
        sendPushToken()
    }

    @Synchronized
    fun removeAuth() {
        _authStateFlow.value = AuthState()
        with(prefs.edit()) {
            clear()
            commit()
        }
        sendPushToken()
    }

    @InstallIn(/* ...value = */ SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun getApiService(): PostsApiService
    }

    fun sendPushToken(token:String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                val entryPoint = EntryPointAccessors.fromApplication(context, AppAuthEntryPoint:: class.java)
                val fcmPoint = EntryPointAccessors.fromApplication(context, FcmModule.FireBaseEntryPoint::class.java)

                entryPoint.getApiService().sendPushToken(
                    PushToken(
                        token?: fcmPoint.getFCMService().token.await()

                    )
                )
            }
        }
    }

}
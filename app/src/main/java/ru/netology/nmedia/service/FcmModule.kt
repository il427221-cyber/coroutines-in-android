package ru.netology.nmedia.service

import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class FcmModule {
    @InstallIn(/* ...value = */ SingletonComponent::class)
    @EntryPoint
    interface FireBaseEntryPoint {
        fun getFCMService(): FirebaseMessaging
    }
    @Singleton
    @Provides
    fun provideFcmApi(): FirebaseMessaging = FirebaseMessaging.getInstance()
}
package ru.netology.nmedia.activity

import com.google.android.gms.common.GoogleApiAvailability
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class GoogleAPIModule {
    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface GoogleAPIEntryPoint {
        fun getGoogleApiService(): GoogleApiAvailability
    }
    @Singleton
    @Provides
    fun provideGoogleAPI(): GoogleApiAvailability = GoogleApiAvailability.getInstance()
}
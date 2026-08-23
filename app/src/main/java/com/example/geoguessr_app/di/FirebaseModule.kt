package com.example.geoguessr_app.di

import com.example.geoguessr_app.data.firebase.FirebaseAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuthRepository():
            FirebaseAuthRepository {

        return FirebaseAuthRepository()
    }
}
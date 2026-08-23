package com.example.geoguessr_app.di

import com.example.geoguessr_app.data.firebase.FirebaseAuthRepository
import com.example.geoguessr_app.data.firebase.MultiplayerRepository
import com.example.geoguessr_app.data.firebase.SessionRepository
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

    @Provides
    @Singleton
    fun provideMultiplayerRepository():
            MultiplayerRepository {

        return MultiplayerRepository()
    }

    @Provides
    @Singleton
    fun provideSessionRepository():
            SessionRepository {

        return SessionRepository()
    }
}
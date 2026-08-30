package com.example.geoguessr_app.di

import com.example.geoguessr_app.data.firebase.FirebaseAuthRepository
import com.example.geoguessr_app.data.firebase.MultiplayerRepository
import com.example.geoguessr_app.data.firebase.SessionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Stellt sämtliche Firebase-bezogenen Abhängigkeiten zentral für die
 * gesamte Anwendung bereit.
 *
 * ARCHITEKTUR-VERBESSERUNG: Löst zwei zuvor bestehende Redundanzen auf:
 * Die Datenbank-URL war bislang identisch in bis zu fünf verschiedenen
 * Repository-Klassen hinterlegt, und FirebaseAuth.getInstance() wurde
 * unabhängig voneinander an mehreren Stellen aufgerufen, obwohl es
 * sich stets um dieselbe, geräteweite Sitzung handelt. Durch die
 * zentrale Bereitstellung als Hilt-Singleton existiert nun garantiert
 * genau eine Instanz pro Firebase-Dienst in der gesamten App.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    private const val DB_URL =
        "https://bsi-geoguessr-app-63b7f-default-rtdb.europe-west1.firebasedatabase.app/"

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance(DB_URL)

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuthRepository(auth: FirebaseAuth): FirebaseAuthRepository {
        return FirebaseAuthRepository(auth)
    }

    @Provides
    @Singleton
    fun provideMultiplayerRepository(database: FirebaseDatabase): MultiplayerRepository {
        return MultiplayerRepository(database)
    }

    @Provides
    @Singleton
    fun provideSessionRepository(database: FirebaseDatabase): SessionRepository {
        return SessionRepository(database)
    }
}
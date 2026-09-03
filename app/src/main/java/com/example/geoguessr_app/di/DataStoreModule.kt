package com.example.geoguessr_app.di

import android.content.Context
import com.example.geoguessr_app.data.datastore.DailyQuestDataStoreRepository
import com.example.geoguessr_app.data.datastore.OnboardingDataStoreRepository
import com.example.geoguessr_app.data.datastore.StatisticsDataStoreRepository
import com.example.geoguessr_app.data.datastore.ThemeDataStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Stellt die lokalen DataStore-Repositories bereit, die den
 * Gast-Fortschritt (Statistiken und Daily Quests) persistieren.
 *
 * Beide Klassen besitzen bewusst keinen @Inject-Konstruktor, da sie
 * einen Context-Parameter benötigen, der über @ApplicationContext von
 * Hilt aufgelöst wird – daher die explizite Bereitstellung hier statt
 * einer automatischen Konstruktor-Injektion.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideStatisticsDataStoreRepository(
        @ApplicationContext context: Context
    ): StatisticsDataStoreRepository {
        return StatisticsDataStoreRepository(context)
    }

    @Provides
    @Singleton
    fun provideDailyQuestDataStoreRepository(
        @ApplicationContext context: Context
    ): DailyQuestDataStoreRepository {
        return DailyQuestDataStoreRepository(context)
    }

    @Provides
    @Singleton
    fun provideThemeDataStoreRepository(
        @ApplicationContext context: Context
    ): ThemeDataStoreRepository {
        return ThemeDataStoreRepository(context)
    }

    
    @Provides
    @Singleton
    fun provideOnboardingDataStoreRepository(
        @ApplicationContext context: Context
    ): OnboardingDataStoreRepository {
        return OnboardingDataStoreRepository(context)
    }
}
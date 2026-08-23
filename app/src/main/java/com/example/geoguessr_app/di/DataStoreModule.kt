package com.example.geoguessr_app.di

import android.content.Context
import com.example.geoguessr_app.data.datastore.StatisticsDataStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideStatisticsRepository(
        @ApplicationContext context: Context
    ): StatisticsDataStoreRepository {

        return StatisticsDataStoreRepository(
            context = context
        )
    }
}
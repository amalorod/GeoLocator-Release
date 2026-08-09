package com.example.geoguessr_app.di

import com.example.geoguessr_app.data.location.LocalLocationRepository
import com.example.geoguessr_app.domain.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Verknüpft den Repository-Vertrag mit seiner Implementierung.
 *
 * Aufrufer hängen nur von LocationRepository ab. Hilt entscheidet,
 * dass dafür LocalLocationRepository verwendet wird.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        implementation: LocalLocationRepository
    ): LocationRepository
}
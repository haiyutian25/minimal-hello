package com.example.core.data.di

import com.example.core.data.repository.GreetingRepository
import com.example.core.data.repository.GreetingRepositoryImpl
import com.example.core.data.repository.UserPreferencesRepository
import com.example.core.data.repository.UserPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindGreetingRepository(
        impl: GreetingRepositoryImpl,
    ): GreetingRepository
}

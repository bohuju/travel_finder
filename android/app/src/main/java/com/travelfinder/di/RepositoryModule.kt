package com.travelfinder.di

import com.travelfinder.data.repository.PostRepositoryImpl
import com.travelfinder.data.repository.TripRepositoryImpl
import com.travelfinder.domain.repository.PostRepository
import com.travelfinder.domain.repository.TripRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 仓储模块
 * 绑定仓储接口到实现
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindTripRepository(
        tripRepositoryImpl: TripRepositoryImpl
    ): TripRepository
}

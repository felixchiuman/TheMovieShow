package com.felix.themovieshow.di

import com.felix.themovieshow.data.repository.HomeRepository
import com.felix.themovieshow.data.repository.HomeRepositoryImpl
import com.felix.themovieshow.data.repository.MovieDetailRepository
import com.felix.themovieshow.data.repository.MovieDetailRepositoryImpl
import com.felix.themovieshow.data.repository.ReviewRepository
import com.felix.themovieshow.data.repository.ReviewRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Satu module untuk semua binding repository yang mendaftarkan tiap repository (HomeRepository, LoginRepository, dst).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
// dari abstact ke object kalau mau pakai provides
    @Binds
    @Singleton
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository

    @Binds
    @Singleton
    abstract fun bindMovieDetailRepository(impl: MovieDetailRepositoryImpl): MovieDetailRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository

    /*@Provides
    @Singleton
    fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository = impl

    @Provides
    @Singleton
    fun bindMovieDetailRepository(impl: MovieDetailRepositoryImpl): MovieDetailRepository = impl

    @Provides
    @Singleton
    fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository = impl*/
}

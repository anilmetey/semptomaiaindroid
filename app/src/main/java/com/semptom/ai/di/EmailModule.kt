package com.semptom.ai.di

import com.semptom.ai.data.email.EmailService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EmailModule {

    @Provides
    @Singleton
    fun provideEmailService(): EmailService {
        return EmailService()
    }
}

package com.semptom.ai.di

import com.semptom.ai.data.local.dao.InMemorySymptomEntryDao
import com.semptom.ai.data.local.dao.SymptomEntryDao
import com.semptom.ai.data.repository.InMemoryAuthRepository
import com.semptom.ai.data.repository.InMemoryJournalRepository
import com.semptom.ai.data.repository.InMemoryProfileRepository
import com.semptom.ai.data.repository.InMemoryMedicationRepository
import com.semptom.ai.data.repository.InMemoryModelRepository
import com.semptom.ai.domain.repository.AuthRepository
import com.semptom.ai.domain.repository.ProfileRepository
import com.semptom.ai.domain.repository.JournalRepository
import com.semptom.ai.domain.repository.MedicationRepository
import com.semptom.ai.domain.repository.ModelRepository
import com.semptom.ai.domain.repository.SymptomRepository
import com.semptom.ai.data.repository.SymptomRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSymptomRepository(): SymptomRepository {
        return SymptomRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext appContext: Context
    ): AuthRepository {
        return InMemoryAuthRepository(appContext)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(): ProfileRepository {
        return InMemoryProfileRepository()
    }

    @Provides
    @Singleton
    fun provideModelRepository(): ModelRepository {
        return InMemoryModelRepository()
    }

    @Provides
    @Singleton
    fun provideJournalRepository(): JournalRepository {
        return InMemoryJournalRepository()
    }

    @Provides
    @Singleton
    fun provideSymptomEntryDao(): SymptomEntryDao {
        return InMemorySymptomEntryDao()
    }

    @Provides
    @Singleton
    fun provideMedicationRepository(): MedicationRepository {
        return InMemoryMedicationRepository()
    }
}

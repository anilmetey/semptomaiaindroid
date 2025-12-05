package com.semptom.ai

import android.app.Application
import com.semptom.ai.data.repository.ModelRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class SemptomAIApplication : Application() {
    @Inject
    lateinit var modelRepository: ModelRepository

    override fun onCreate() {
        super.onCreate()
        GlobalScope.launch {
            modelRepository.initialize()
        }
    }
}

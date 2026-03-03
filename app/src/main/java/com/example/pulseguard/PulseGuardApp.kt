// com.example.pulseguard.PulseGuardApp
package com.example.pulseguard

import android.app.Application
import com.example.pulseguard.di.databaseModule
import com.example.pulseguard.di.repositoryModule
import com.example.pulseguard.di.useCaseModule
import com.example.pulseguard.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class for Pulse Guard.
 *
 * Initialises the Koin dependency injection framework with all application-level
 * modules. The Koin logger is limited to [Level.ERROR] to suppress verbose
 * output in non-debug builds.
 *
 * Module load order:
 * 1. [databaseModule]  – Room database + DAO (no dependencies)
 * 2. [repositoryModule] – Repository bound to DAO
 * 3. [useCaseModule]   – Use cases bound to Repository
 * 4. [viewModelModule] – ViewModels bound to Use cases
 */
class PulseGuardApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@PulseGuardApp)
            modules(
                databaseModule,
                repositoryModule,
                useCaseModule,
                viewModelModule,
            )
        }
    }
}

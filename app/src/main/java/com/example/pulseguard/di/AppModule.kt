// com.example.pulseguard.di.AppModule
package com.example.pulseguard.di

import androidx.room.Room
import com.example.pulseguard.data.local.PulseGuardDatabase
import com.example.pulseguard.data.repository.BloodPressureRepository
import com.example.pulseguard.data.repository.BloodPressureRepositoryImpl
import com.example.pulseguard.domain.usecase.AddMeasurementUseCase
import com.example.pulseguard.domain.usecase.GetDashboardDataUseCase
import com.example.pulseguard.ui.screens.dashboard.DashboardViewModel
import com.example.pulseguard.ui.screens.entry.EntryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

private const val DATABASE_NAME = "pulse_guard.db"

/**
 * Koin module that provides the Room database singleton and its DAO.
 *
 * The database is created lazily on first access and lives for the entire
 * application lifecycle (`single`).
 */
val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            PulseGuardDatabase::class.java,
            DATABASE_NAME,
        ).build()
    }

    single { get<PulseGuardDatabase>().bloodPressureDao() }
}

/**
 * Koin module that binds [BloodPressureRepository] to its implementation.
 */
val repositoryModule = module {
    single<BloodPressureRepository> { BloodPressureRepositoryImpl(get()) }
}

/**
 * Koin module providing domain use-case instances.
 *
 * Use cases are declared as `factory` so each injection site (ViewModel)
 * receives a fresh, non-shared instance. This avoids accidental state leakage
 * between ViewModels in future phases.
 */
val useCaseModule = module {
    factory { AddMeasurementUseCase(get()) }
    factory { GetDashboardDataUseCase(get()) }
}

/**
 * Koin module providing ViewModel instances.
 *
 * The `viewModel` DSL scopes each instance to its Compose host (NavBackStackEntry)
 * and ensures automatic cleanup on destination removal.
 */
val viewModelModule = module {
    viewModel { EntryViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
}

// com.example.pulseguard.di.AppModule
package com.example.pulseguard.di

import androidx.room.Room
import com.example.pulseguard.data.local.PulseGuardDatabase
import com.example.pulseguard.data.repository.BloodPressureRepositoryImpl
import com.example.pulseguard.domain.repository.BloodPressureRepository
import com.example.pulseguard.domain.usecase.AddMeasurementUseCase
import com.example.pulseguard.domain.usecase.DeleteMeasurementUseCase
import com.example.pulseguard.domain.usecase.ExportToPdfUseCase
import com.example.pulseguard.domain.usecase.GetDashboardDataUseCase
import com.example.pulseguard.ui.screens.dashboard.DashboardViewModel
import com.example.pulseguard.ui.screens.entry.EntryViewModel
import com.example.pulseguard.ui.screens.export.ExportViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

private const val DATABASE_NAME = "pulse_guard.db"

/**
 * Koin module that provides the Room database singleton and its DAO.
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
    single<BloodPressureRepository> { BloodPressureRepositoryImpl(get(), androidContext()) }
}

/**
 * Koin module providing domain use-case instances.
 */
val useCaseModule = module {
    factory { AddMeasurementUseCase(get()) }
    factory { DeleteMeasurementUseCase(get()) }
    factory { GetDashboardDataUseCase(get()) }
    factory { ExportToPdfUseCase(get()) }
}

/**
 * Koin module providing ViewModel instances.
 */
val viewModelModule = module {
    viewModel { EntryViewModel(get()) }
    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { ExportViewModel(get(), get()) }
}

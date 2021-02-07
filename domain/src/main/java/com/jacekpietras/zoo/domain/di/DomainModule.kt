package com.jacekpietras.zoo.domain.di

import com.jacekpietras.zoo.domain.interactor.*
import org.koin.dsl.module

val domainModule = module {

    factory {
        GetBuildingsUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetRoadsUseCase(
            mapRepository = get()
        )
    }
    factory {
        GetUserPositionUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        InsertUserPositionUseCase(
            gpsRepository = get(),
        )
    }
    factory {
        UploadHistoryUseCase(
            mailGateway = get(),
        )
    }
}
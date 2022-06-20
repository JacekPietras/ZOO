package com.jacekpietras.zoo.domain.feature.tsp.di

import com.jacekpietras.zoo.domain.feature.tsp.MySalesmanProblemSolver
import org.koin.dsl.module

val tspModule = module {
    single {
        MySalesmanProblemSolver(
            mapRepository = get(),
            graphAnalyzer = get(),
        )
    }
}
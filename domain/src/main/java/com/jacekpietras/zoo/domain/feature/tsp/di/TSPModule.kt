package com.jacekpietras.zoo.domain.feature.tsp.di

import com.jacekpietras.zoo.domain.feature.tsp.algorithms.SimulatedAnnealing
import com.jacekpietras.zoo.domain.feature.tsp.StageTSPSolver
import com.jacekpietras.zoo.domain.feature.tsp.StageTSPSolverImpl
import org.koin.dsl.module

val tspModule = module {
    single<StageTSPSolver> {
        StageTSPSolverImpl(
            mapRepository = get(),
            graphAnalyzer = get(),
            tspAlgorithm = SimulatedAnnealing(),
        )
    }
}
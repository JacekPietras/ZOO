package com.jacekpietras.zoo.domain.feature.tsp.di

import com.jacekpietras.zoo.domain.feature.tsp.SimulatedAnnealing
import com.jacekpietras.zoo.domain.feature.tsp.StageTravellingSalesmanProblemSolver
import org.koin.dsl.module

val tspModule = module {
    single {
        StageTravellingSalesmanProblemSolver(
            mapRepository = get(),
            graphAnalyzer = get(),
            tspAlgorithm = SimulatedAnnealing(),
        )
    }
}
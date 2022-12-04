package com.jacekpietras.zoo.domain.feature.tsp.di

import com.jacekpietras.zoo.domain.feature.tsp.algorithms.SimulatedAnnealing
import com.jacekpietras.zoo.domain.feature.tsp.StageTravellingSalesmanProblemSolver
import com.jacekpietras.zoo.domain.feature.tsp.StageTravellingSalesmanProblemSolverImpl
import org.koin.dsl.module

val tspModule = module {
    single<StageTravellingSalesmanProblemSolver> {
        StageTravellingSalesmanProblemSolverImpl(
            mapRepository = get(),
            graphAnalyzer = get(),
            tspAlgorithm = SimulatedAnnealing(),
        )
    }
}
package com.jacekpietras.zoo.domain.feature.tsp.di

import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.tsp.algorithms.SimulatedAnnealing
import com.jacekpietras.zoo.domain.feature.tsp.StageTSPSolver
import com.jacekpietras.zoo.domain.feature.tsp.StageTSPSolverImpl
import com.jacekpietras.zoo.domain.feature.tsp.algorithms.MyTwoOptHeuristicTSP
import com.jacekpietras.zoo.domain.feature.tsp.plus
import org.koin.dsl.module

val tspModule = module {
    single<StageTSPSolver> {
        StageTSPSolverImpl(
            mapRepository = get(),
            graphAnalyzer = get(),
            tspAlgorithm = MyTwoOptHeuristicTSP<Stage>() + SimulatedAnnealing(),
        )
    }
}
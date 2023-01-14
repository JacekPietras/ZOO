package com.jacekpietras.zoo.domain.feature.tsp.di

import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.tsp.StageTSPSolver
import com.jacekpietras.zoo.domain.feature.tsp.StageTSPSolverImpl
import com.jacekpietras.zoo.domain.feature.tsp.algorithms.MyNewTwoOptHeuristicTSP
import com.jacekpietras.zoo.domain.feature.tsp.algorithms.NearestNeighbor
import com.jacekpietras.zoo.domain.feature.tsp.plus
import com.jacekpietras.zoo.domain.feature.tsp.times
import org.koin.dsl.module

val tspModule = module {
    single<StageTSPSolver> {
        StageTSPSolverImpl(
            mapRepository = get(),
            graphAnalyzer = get(),
            tspAlgorithm = MyNewTwoOptHeuristicTSP<Stage>() * (NearestNeighbor<Stage>() + MyNewTwoOptHeuristicTSP()),
        )
    }
}
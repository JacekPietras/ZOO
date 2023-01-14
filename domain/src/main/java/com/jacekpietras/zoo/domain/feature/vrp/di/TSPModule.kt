package com.jacekpietras.zoo.domain.feature.vrp.di

import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.feature.vrp.StageVRPSolver
import com.jacekpietras.zoo.domain.feature.vrp.StageVRPSolverImpl
import com.jacekpietras.zoo.domain.feature.vrp.algorithms.MyNewTwoOptHeuristicVRP
import com.jacekpietras.zoo.domain.feature.vrp.algorithms.NearestNeighborVRP
import com.jacekpietras.zoo.domain.feature.vrp.plus
import com.jacekpietras.zoo.domain.feature.vrp.times
import org.koin.dsl.module

val vrpModule = module {
    single<StageVRPSolver> {
        StageVRPSolverImpl(
            mapRepository = get(),
            graphAnalyzer = get(),
            vrpAlgorithm = MyNewTwoOptHeuristicVRP<Stage>() * (NearestNeighborVRP<Stage>() + MyNewTwoOptHeuristicVRP()),
        )
    }
}
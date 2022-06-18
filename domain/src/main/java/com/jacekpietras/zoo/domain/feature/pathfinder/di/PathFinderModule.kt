package com.jacekpietras.zoo.domain.feature.pathfinder.di

import com.jacekpietras.zoo.domain.feature.pathfinder.MySalesmanProblemSolver
import org.koin.dsl.module

val pathFinderModule = module {
    single {
        MySalesmanProblemSolver(
            mapRepository = get(),
            graphAnalyzer = get(),
        )
    }
}
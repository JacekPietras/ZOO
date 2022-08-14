package com.jacekpietras.zoo.planner.mapper

import com.jacekpietras.zoo.core.text.Dictionary.findReadableName
import com.jacekpietras.zoo.core.text.RichText
import com.jacekpietras.zoo.domain.feature.animal.model.AnimalEntity
import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.planner.R
import com.jacekpietras.zoo.planner.model.PlannerItem
import com.jacekpietras.zoo.planner.model.PlannerItem.Footer
import com.jacekpietras.zoo.planner.model.PlannerItem.Header
import com.jacekpietras.zoo.planner.model.PlannerState
import com.jacekpietras.zoo.planner.model.PlannerViewState
import com.jacekpietras.zoo.planner.model.SuggestedItem

internal class PlannerStateMapper {

    fun from(
        state: PlannerState,
        plan: List<Pair<Stage, List<AnimalEntity>>>?
    ): PlannerViewState {

        val suggestedItems = mutableListOf<SuggestedItem>()
        val isExitInPlan = plan?.any { (stage, _) -> stage is Stage.InRegion && stage.region is Region.ExitRegion } ?: false
        if (!isExitInPlan) suggestedItems.add(SuggestedItem.Exit)

        val list = plan
            ?.takeIf { it.haveRegions() }
            ?.removeDuplicateStages()
            ?.map { (stage, animals) ->
                when (stage) {
                    is Stage.InRegion -> {
                        if (stage.region is Region.ExitRegion) {
                            PlannerItem.RegionItem(
                                title = RichText(R.string.exit),
                                regionId = stage.region.id.id,
                                isMutable = true,
                                isFixed = true,
                                isSeen = stage.seen,
                            )
                        } else {
                            PlannerItem.RegionItem(
                                title = stage.region.id.id.findReadableName(),
                                info = RichText(animals.map(AnimalEntity::name).joinToString()),
                                regionId = stage.region.id.id,
                                isMultiple = stage is Stage.Multiple,
                                isMutable = stage.mutable || stage.seen,
                                isFixed = stage.seen,
                                isSeen = stage.seen,
                            )
                        }
                    }
                    is Stage.InUserPosition -> {
                        PlannerItem.UserPositionItem
                    }
                }
            }
            ?.addHeader()
            ?.addFooter(suggestedItems.isNotEmpty())

        return PlannerViewState(
            list = list ?: emptyList(),
            suggestedItems = suggestedItems,
            isEmptyViewVisible = plan?.haveRegions() == false,
            isShowingUnseeDialog = state.regionUnderUnseeing != null,
        )
    }

    private fun List<PlannerItem>.addHeader(): List<PlannerItem> =
        listOf(Header) + this

    private fun List<PlannerItem>.addFooter(enabled: Boolean): List<PlannerItem> =
        if (enabled) {
            this + Footer
        } else {
            this
        }


    private fun List<Pair<Stage, List<AnimalEntity>>>.haveRegions() =
        this.any { (stage, _) -> stage is Stage.InRegion }

    private fun List<Pair<Stage, List<AnimalEntity>>>.removeDuplicateStages() =
        distinctBy { (stage, _) ->
            when (stage) {
                is Stage.InRegion -> {
                    stage.region
                }
                else -> {
                    stage
                }
            }
        }
}

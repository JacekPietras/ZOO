package com.jacekpietras.zoo.domain.feature.pathfinder

class Travel<City>(
    var travel: ArrayList<City> = ArrayList(),
    var previousTravel: ArrayList<City> = ArrayList(),
) {
    
    fun swapRandomly() {
        val a = generateRandomIndex()
        val b = generateRandomIndex()
        previousTravel = ArrayList(travel)
        val x = travel[a]
        val y = travel[b]
        travel[a] = y
        travel[b] = x
    }

    fun revertSwap() {
        travel = previousTravel
    }

    private fun generateRandomIndex(): Int =
        (Math.random() * travel.size).toInt()
}
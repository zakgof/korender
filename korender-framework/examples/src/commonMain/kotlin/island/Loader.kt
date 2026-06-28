package com.zakgof.korender.examples.island

import com.zakgof.korender.scope.KorenderScope
import island.loadRunway

class Loader(kc: KorenderScope) {


    val heightMapLoading = kc.loadImage("island/terrain/height.png")
    val heightFunc = Height(heightMapLoading)
    val runwaySeedLoading = kc.load("island/terrain/runway.bin") { loadRunway(it) }
    val deferredBuildings = kc.load("island/building/buildings.bin") { kc.loadBuildings(it) }
    val deferredBranches = kc.load("island/tree/branches.bin") { loadBranches(it) }
    val deferredCards = kc.load("island/tree/cards.bin") { loadCards(it) }
    val deferredTreeSeeds = kc.load("island/tree/trees.bin") { loadTreeSeeds(heightFunc, it) }
    
    val deferreds = listOf(heightMapLoading, runwaySeedLoading, deferredBuildings, deferredBranches, deferredCards, deferredTreeSeeds)


    fun loaded() = deferreds.all { it.isCompleted }
    fun percent() = deferreds.count { it.isCompleted } * 100 / deferreds.size
}
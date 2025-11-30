package com.zakgof.korender.examples.island

import com.zakgof.korender.context.KorenderContext
import loadRunway

class Loader(kc: KorenderContext) {


    val heightMapLoading = kc.loadImage("island/terrain/height.png")
    val heightFunc = Height(heightMapLoading)
    val runwaySeedLoading = kc.load("files/island/terrain/runway.bin") { loadRunway(it) }
    val deferredBuildings = kc.load("files/island/building/buildings.bin") { loadBuildings(it) }
    val deferredBranches = kc.load("files/island/tree/branches.bin") { loadBranches(it) }
    val deferredCards = kc.load("files/island/tree/cards.bin") { loadCards(it) }
    val deferredTreeSeeds = kc.load("files/island/tree/trees.bin") { loadTreeSeeds(heightFunc, it) }
    
    val deferreds = listOf(heightMapLoading, runwaySeedLoading, deferredBuildings, deferredBranches, deferredCards, deferredTreeSeeds)


    fun loaded() = deferreds.all { it.isCompleted }
    fun percent() = deferreds.count { it.isCompleted } * 100 / deferreds.size
}
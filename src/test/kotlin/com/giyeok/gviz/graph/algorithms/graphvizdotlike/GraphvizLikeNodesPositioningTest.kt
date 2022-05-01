package com.giyeok.gviz.graph.algorithms.graphvizdotlike

import kotlin.test.Test

class GraphvizLikeNodesPositioningTest {
  @Test
  fun testCalculateAdjacentGraph() {
    val positioning = GraphLayout(Graphs.graph2)
    val ranks = positioning.calculateRanks()
    println(ranks)
    val adjGraph = positioning.adjacentGraph(ranks)
    println(adjGraph)
  }
}

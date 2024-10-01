package com.giyeok.gviz.layout.graphvizdotlike

import com.giyeok.gviz.layout.graphvizdotlike.utils.GraphExUtils.adjacentGraph
import com.google.common.truth.Truth.assertThat
import kotlin.test.Test

class GraphvizLikeNodesPositioningTest {
  @Test
  fun testCalculateAdjacentGraph() {
    val positioning = GraphLayout(Graphs.graph5, 30.0, 20.0, edgeWeights = mapOf("be" to 10.0))
    val ranks = positioning.calculateRanks()
    assertThat(ranks).containsExactly(
      "a", 0,
      "c", 1,
      "b", 2,
      "d", 2,
      "e", 3,
    )
    println(ranks)
    val (adjGraph, adjRanks) = adjacentGraph(positioning.initialGraphEx, ranks)
    println(adjGraph)

    val rankOrdersAlgorithm = RankOrdersAlgorithm(adjGraph.graph, adjRanks)
    val orders = rankOrdersAlgorithm.solve()
    println(orders)
  }

  @Test
  fun test() {
    val graph = Graphs.graph4
    val positioning = GraphLayout(graph, 30.0, 20.0)
    val ranks = positioning.calculateRanks()
    // 인접하지 않은(2이상 떨어진) 랭크 사이의 엣지는 가상 노드를 추가해서 모든 엣지가 인접한 랭크 사이에만 존재하도록 만들기
    val (adjacentGraph, aranks) = adjacentGraph(positioning.initialGraphEx, ranks)
    val rankOrders = positioning.calculateRankOrders(adjacentGraph, aranks)
    println(rankOrders)
    val nodeCoordAlgorithm =
      NodeCoordAlgorithm(adjacentGraph, graph.nodeSizes, rankOrders, 30.0, 20.0)
    val mainAxisCoords = nodeCoordAlgorithm.calculateMainAxisCoords()
    println(mainAxisCoords)
    val subAxisCoords = nodeCoordAlgorithm.calculateSubAxisCoords()
    println(subAxisCoords)
  }
}

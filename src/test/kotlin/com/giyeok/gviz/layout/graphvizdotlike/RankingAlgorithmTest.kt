package com.giyeok.gviz.layout.graphvizdotlike


import com.giyeok.gviz.layout.graphvizdotlike.utils.IntNetworkSimplexSolver
import com.giyeok.gviz.layout.graphvizdotlike.RankingAlgorithm
import com.giyeok.gviz.layout.graphvizdotlike.utils.GraphExUtils.adjacentGraph
import com.google.common.truth.Truth.assertThat
import kotlin.test.Test

class RankingAlgorithmTest {

  @Test
  fun testInitRank() {
    val g0 = GraphLayout(Graphs.graph2, 30.0, 20.0).initialGraphEx
    val rankCalc = RankingAlgorithm(g0)
    val g1 = rankCalc.prepareRankingGraph(g0)

    val solver = IntNetworkSimplexSolver(g1.graph, g1.edgeMinLengths, g1.edgeWeights)
    assertThat(solver.initRank()).containsExactly(
      "a", 0,
      "e", 1,
      "f", 1,
      "b", 1,
      "g", 2,
      "c", 2,
      "d", 3,
      "h", 4,
    )
  }

  @Test
  fun testInitialFeasibleTree() {
    val rankCalc = RankingAlgorithm(GraphLayout(Graphs.graph4, 30.0, 20.0).initialGraphEx)
    val g0 = rankCalc.graphEx

    val solver = IntNetworkSimplexSolver(g0.graph, g0.edgeMinLengths, g0.edgeWeights)
    val g1 = solver.initialFeasibleTree()
    println(g1)
  }

  @Test
  fun testRemoveCycles1() {
    val g0 = GraphLayout(Graphs.genCharGraph("ab", "ba"), 30.0, 20.0).initialGraphEx
    val rankCalc = RankingAlgorithm(g0)
    val g1 = rankCalc.removeCycles(g0)
    println(g1)
  }

  @Test
  fun testRemoveCycles2() {
    val g0 =
      GraphLayout(Graphs.genCharGraph("ab", "bc", "cd", "db", "cx"), 30.0, 20.0).initialGraphEx
    val rankCalc = RankingAlgorithm(g0)
    val g1 = rankCalc.removeCycles(g0)
    println(g1)
    println(rankCalc.calculateRanks())
    val ranks = rankCalc.calculateRanks()

    val (adjacentGraph, adjacentRanks) = adjacentGraph(g0, ranks)
    val rankOrdersCalc = RankOrdersAlgorithm(adjacentGraph.graph, adjacentRanks)
    val orders = rankOrdersCalc.solve()
    println(orders)
  }

  @Test
  fun testCalculateRanks() {
//    val rankCalc = RankCalculator(
//      Graphs.graph3, sameRanks = listOf(
//        setOf("Mashey", "Bourne"),
//        setOf("Formshell", "csh"),
//        setOf("esh", "vsh"),
//        setOf("ksh", "System-V"),
//        setOf("v9sh", "tcsh"),
//        setOf("ksh-i"),
//        setOf("KornShell", "Perl", "rc"),
//        setOf("tcl", "Bash"),
//        setOf("POSIX", "ksh-POSIX"),
//      )
//    )
    val rankCalc = RankingAlgorithm(GraphLayout(Graphs.graph4, 30.0, 20.0).initialGraphEx)
    val ranks = rankCalc.calculateRanks()
    ranks.entries.groupBy { it.value }.entries.sortedBy { it.key }
      .forEach { entry -> println("${entry.key}: ${entry.value.map { it.key }.sorted()}") }
    println(ranks)
  }
}

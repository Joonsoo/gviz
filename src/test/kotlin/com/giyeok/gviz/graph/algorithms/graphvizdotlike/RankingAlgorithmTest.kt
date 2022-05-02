package com.giyeok.gviz.graph.algorithms.graphvizdotlike


import com.google.common.truth.Truth.assertThat
import kotlin.test.Test

class RankingAlgorithmTest {

  @Test
  fun testInitRank() {
    val rankCalc = RankingAlgorithm(Graphs.graph2)
    val g0 = rankCalc.initialGraphEx()

    val g1 = rankCalc.prepareRankingGraph(g0)
    assertThat(rankCalc.initRank(g1)).containsExactly(
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
    val rankCalc = RankingAlgorithm(Graphs.graph4)
    val g0 = rankCalc.initialGraphEx()

    val g1 = rankCalc.initialFeasibleTree(g0)
    println(g1)
  }

  @Test
  fun testRemoveCycles1() {
    val rankCalc = RankingAlgorithm(Graphs.genCharGraph("ab", "ba"))
    val g0 = rankCalc.initialGraphEx()
    val g1 = rankCalc.removeCycles(g0)
    println(g1)
  }

  @Test
  fun testRemoveCycles2() {
    val rankCalc = RankingAlgorithm(Graphs.genCharGraph("ab", "bc", "cd", "db", "cx"))
    val g0 = rankCalc.initialGraphEx()
    val g1 = rankCalc.removeCycles(g0)
    println(g1)
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
    val rankCalc = RankingAlgorithm(Graphs.graph4)
    val ranks = rankCalc.calculateRanks()
    ranks.entries.groupBy { it.value }.entries.sortedBy { it.key }
      .forEach { entry -> println("${entry.key}: ${entry.value.map { it.key }.sorted()}") }
    println(ranks)
  }
}

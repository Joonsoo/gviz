package com.giyeok.gviz.layout.graphvizdotlike.utils

import com.giyeok.gviz.graph.BaseGraph

class DoubleNetworkSimplexSolver(
  val graph: BaseGraph,
  val edgeMinLengths: Map<String, Double>,
  val edgeWeights: Map<String, Double>,
) {
  fun solve(): Map<String, Double> {
    TODO()
  }
}

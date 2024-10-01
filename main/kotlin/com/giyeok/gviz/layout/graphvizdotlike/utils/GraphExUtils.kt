package com.giyeok.gviz.layout.graphvizdotlike.utils

import com.giyeok.gviz.graph.Edge
import com.giyeok.gviz.layout.graphvizdotlike.GraphEx
import kotlin.math.abs

// 랭크가 2이상 벌어진 노드 사이의 엣지를 사이의 랭크에 virtual node를 추가해준 그래프를 반환
object GraphExUtils {
  // 랭크가 2이상 벌어진 노드 사이의 엣지를 사이의 랭크에 virtual node를 추가해준 그래프와 새로운 그래프의 rank를 반환
  // self edge는 제거, multi edge는 병합
  fun adjacentGraph(graphEx: GraphEx, ranks: Map<String, Int>): Pair<GraphEx, Map<String, Int>> {
    var graphEx = graphEx
    val mranks = ranks.toMutableMap()
    // self edge는 제거
    graphEx.graph.directedEdges.filter { it.value.start == it.value.end }.forEach {
      graphEx = graphEx.removeEdge(it.key)
    }
    graphEx.graph.directedEdges.forEach { (edgeName, edge) ->
      val startRank = ranks.getValue(edge.start)
      val endRank = ranks.getValue(edge.end)
      if (abs(startRank - endRank) >= 2) {
        var lastNode = edge.start
        val splittingEdges = mutableListOf<String>()
        val interRankRange =
          if (startRank < endRank) startRank + 1 until endRank
          else startRank - 1 downTo endRank + 1
        for (rank in interRankRange) {
          val interNode = graphEx.newId()
          graphEx = graphEx.addVirtualNode(interNode)
          mranks[interNode] = rank
          val interEdge = graphEx.newId()
          splittingEdges.add(interEdge)
          graphEx = graphEx.addVirtualEdge(interEdge, Edge(lastNode, interNode))
          lastNode = interNode
        }
        val interEdge = graphEx.newId()
        splittingEdges.add(interEdge)
        graphEx = graphEx.addVirtualEdge(interEdge, Edge(lastNode, edge.end))
        graphEx = graphEx.splitEdge(edgeName, splittingEdges)
      }
    }
    return Pair(graphEx, mranks)
  }

  fun mergeDuplicateEdges(graphEx: GraphEx): GraphEx {
    var graphEx = graphEx

    val edgesByPair = graphEx.graph.directedEdges.entries.groupBy { it.value }
      .mapValues { e -> e.value.map { it.key }.toSet() }
    edgesByPair.filter { it.value.size >= 2 }.forEach { pair ->
      graphEx = graphEx.mergeDirectedEdges(pair.value, graphEx.newId())
    }
    return graphEx
  }
}

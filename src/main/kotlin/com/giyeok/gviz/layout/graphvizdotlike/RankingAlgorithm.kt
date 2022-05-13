package com.giyeok.gviz.layout.graphvizdotlike

import com.giyeok.gviz.graph.Edge
import com.giyeok.gviz.graph.SizedGraph
import com.giyeok.gviz.layout.graphvizdotlike.utils.GraphExUtils
import com.giyeok.gviz.layout.graphvizdotlike.utils.GraphExUtils.mergeDuplicateEdges
import com.giyeok.gviz.layout.graphvizdotlike.utils.IntNetworkSimplexSolver

/**
 * 1단계. 각 노드의 rank를 결정한다.
 *
 * rank는 노드의 main axis(기본은 top to bottom의 Y축) 좌표를 결정한다. 즉 rank가 같은 노드들은 모두 같은 y축
 * 좌표를 갖는다.
 *
 * 이 때 rank를 결정하는 목표는 모든 edge (e=v->w)에 대해 weight(e) * (rank(w)-rank(v))의 sum을 최소화하는 것
 * 이 때 rank(w)-rank(v) >= edgeMinLengths(e) 의 조건은 만족되어야 한다.
 *
 * 위의 linear programming 문제만 해결할 수 있으면 어떤 방식을 사용해도 좋지만, 논문에서는 network simplex라는걸
 * 소개하고 있고, 이 방식을 3단계에서도 사용하고 있어서 비슷하게 구현해 보긴 하는데.. 아직 논문에서 설명하는 network
 * simplex를 제대로 이해를 못해서 아무렇게나 만들어 놓았음.. 검증하면서 수정해야 함
 */
// TODO graphEx를 파라메터로 받고 나머지는 클래스에서 갖고 있는게 좀 이상함.. 수정할 것
class RankingAlgorithm(
  val graphEx: GraphEx,
  // edge의 weight. 지정하지 않으면 기본값은 1. 음수이면 안됨.
  val edgeWeights: Map<String, Double> = mapOf(),
  // edge의 최소 길이. 지정하지 않으면 기본값은 1. 음수이면 안됨.
  val edgeMinLengths: Map<String, Int> = mapOf(),
  val minRanks: Set<String> = setOf(),
  val maxRanks: Set<String> = setOf(),
  val sameRanks: List<Set<String>> = listOf(),
) {
  // graphEx에서 minRanks, maxRanks, sameRanks에서 같은 랭크를 가져야된다고 지정된 노드들을 하나로 합치고
  // maxRanks 노드에서 나가는 엣지와 minRanks 노드로 들어오는 엣지들은 방향을 뒤집은 그래프를 반환
  fun prepareRankingGraph(graphEx: GraphEx): GraphEx {
    var graphEx = graphEx
    if (minRanks.isNotEmpty()) {
      val minRankNode = graphEx.newId()
      graphEx = graphEx.mergeNodes(minRanks, minRankNode)
      // 들어오는 엣지가 없는 노드 v에 대해서는, (minRanks 노드 -> v)인 엣지를 추가하고(minLength=0으로 해서),
      val noincomingNodes =
        graphEx.graph.nodes.filter { (graphEx.graph.incomingEdges[it] ?: listOf()).isEmpty() }
      noincomingNodes.forEach { node ->
        graphEx = graphEx.addVirtualEdge(Edge(minRankNode, node))
      }
      // minRank로 들어오는 엣지들은 방향을 뒤집고 시작
      (graphEx.graph.incomingEdges[minRankNode] ?: listOf()).forEach { incomingEdge ->
        graphEx = graphEx.reverseEdge(incomingEdge.edgeName, graphEx.newId())
      }
    }
    if (maxRanks.isNotEmpty()) {
      val maxRankNode = graphEx.newId()
      graphEx = graphEx.mergeNodes(maxRanks, maxRankNode)
      // 나가는 엣지가 없는 노드 v에 대해서는, (v -> maxRanks 노드)인 엣지를 추가한다.
      val nooutgoingNodes =
        graphEx.graph.nodes.filter { (graphEx.graph.outgoingEdges[it] ?: listOf()).isEmpty() }
      nooutgoingNodes.forEach { node ->
        graphEx = graphEx.addVirtualEdge(Edge(maxRankNode, node))
      }
      // maxRank에서 나가는 엣지들은 방향을 뒤집고 시작
      (graphEx.graph.outgoingEdges[maxRankNode] ?: listOf()).forEach { outgoingEdge ->
        graphEx = graphEx.reverseEdge(outgoingEdge.edgeName, graphEx.newId())
      }
    }
    sameRanks.forEach { sameRankNodes ->
      graphEx = graphEx.mergeNodes(sameRankNodes, graphEx.newId())
    }

    // 같은 두 노드 사이의 여러 엣지는 weight를 모두 더한 하나의 엣지로 치환
    graphEx = mergeDuplicateEdges(graphEx)
    return graphEx
  }

  fun removeCycles(graphEx: GraphEx): GraphEx {
    // TODO 구현해야함 -> 그런데 이 알고리즘은 논문에 자세히 안나온것같은데..? 그냥 사이클 여러개에 속한 엣지를 뒤집는다는 식으로 하면 되는걸까?
    // root를 구할 수 있으면, 거기로부터 DFS를 해서 노드들의 partial order를 구할 수 있다
    // 그러면 엣지는 트리에 속한 엣지이거나, cross edge, forward edge, back edge 중에 하나가 된다.
    // cross edge는 관계 없는 두 노드 사이의 엣지, forward edge는 한 노드에서 그 하위 노드로 가는 엣지, back edge는 한 노드에서 그 상위 노드로 가는 엣지.
    // 여기서 back edge의 방향을 바꾸면 그래프에서 사이클을 없앨 수 있다.
    // 방향을 바꿀 back edge의 수가 최소가 되도록 root를 설정할 수 있으면 좋은데, NP-complete인데다 결과에 별 영향이 없음
    // -> 가장 많은 사이클에 속한 엣지를 뒤집는 방식의 휴리스틱 사용

    check(graphEx.graph.undirectedEdges.isEmpty())
    var graphEx = graphEx

    var foundCycle = true
    while (foundCycle) {
      foundCycle = false
      val traversedNodes = mutableSetOf<String>()
      // edge id -> cycle 갯수
      val cycleCounter = mutableMapOf<String, Int>()

      // path는 (node id, edge id)의 pair
      fun traverse(node: String, path: List<Pair<String, String>>, pathNodes: Set<String>) {
        if (pathNodes.contains(node)) {
          foundCycle = true
          val cycleIdx = path.indexOfLast { it.first == node }
          val cyclePath = path.drop(cycleIdx)
          cyclePath.forEach { (_, pathEdge) ->
            cycleCounter[pathEdge] = (cycleCounter[pathEdge] ?: 0) + 1
          }
          // 싸이클 발견
        } else {
          traversedNodes.add(node)
          graphEx.graph.outgoingEdgesFrom(node).forEach { namedEdge ->
            traverse(namedEdge.edge.end, path + Pair(node, namedEdge.edgeName), pathNodes + node)
          }
        }
      }
      graphEx.graph.nodes.forEach { node ->
        if (!traversedNodes.contains(node)) {
          traverse(node, listOf(), setOf())
        }
      }

      if (foundCycle) {
        val maxCycleEdge = cycleCounter.entries.sortedByDescending { it.value }.first()
        graphEx = graphEx.reverseEdge(maxCycleEdge.key, graphEx.newId())
      }
    }
    return graphEx
  }

  fun calculateRanks(): Map<String, Int> {
    var graphEx = graphEx
    graphEx = prepareRankingGraph(graphEx)
    // TODO undirected edge들을 임의의 directed edge로 바꿔서 처리해도 될까?
    graphEx = removeCycles(graphEx)
    // TODO 싸이클을 제거한 뒤에 머지해야될 엣지가 생기면?

    val edgeMinLengths = graphEx.edgeMinLengths +
      (graphEx.graph.edgeLabels - graphEx.edgeMinLengths.keys).associateWith { 1 }
    val edgeWeights = graphEx.edgeWeights +
      (graphEx.graph.edgeLabels - graphEx.edgeWeights.keys).associateWith { 1.0 }
    val ranks = IntNetworkSimplexSolver(graphEx.graph, edgeMinLengths, edgeWeights)
      .solve()

    // TODO balance: 이건 뭔지 잘 모르겠음

    // replacedNodes 반영해서 실제 랭크 반환
    val finalRanks = mutableMapOf<String, Int>()
    ranks.forEach { (node, rank) ->
      val originalNodes = graphEx.replacedNodes[node] ?: setOf(node)
      originalNodes.forEach { finalRanks[it] = rank }
    }
    return finalRanks
  }
}

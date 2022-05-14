package com.giyeok.gviz.layout.graphvizdotlike

import com.giyeok.gviz.figure.Size
import com.giyeok.gviz.graph.BaseGraph
import com.giyeok.gviz.graph.Edge
import com.giyeok.gviz.layout.graphvizdotlike.utils.IntNetworkSimplexSolver

class NodeCoordAlgorithm(
  val graphEx: GraphEx,
  // graph에는 가상 노드가 포함되어 있기 때문에 graph의 모든 노드의 size가 정의되지 않을 수 있음
  val nodeSizes: Map<String, Size>,
  val rankOrders: List<List<String>>,
  val mainAxisMinSeparation: Double,
  val subAxisMinSeparation: Double,
  // 아래 세 변수는 dot 논문에서 설명하는 Ω값.
  // 엣지가 모두 실제 노드(사용자가 정의한)인 경우,
  // 한쪽은 실제 노드이고 한쪽은 adjacent graph로 만들면서 추가된 노드이면 2,
  // 양쪽 모두 추가된 노드이면 8.
  // 숫자가 커질수록 엣지가 반듯하게 그려질 가능성이 높아지는것 같음
  val realNodesEdgeWeight: Int = 1,
  val mixedNodesEdgeWeight: Int = 2,
  val virtualNodesEdgeWeight: Int = 8,
) {
  init {
    check(graphEx.graph.undirectedEdges.isEmpty())
  }

  // TODO top-bottom인지 left-right인지에 따라 달라지도록
  fun mainAxisSize(node: String): Double? = nodeSizes[node]?.height
  fun subAxisSize(node: String): Double? = nodeSizes[node]?.width

  // 각 랭크의 Y축 좌표 계산.
  // 각 rank의 높이를 구하고 inter rank 간격을 더해서 밖에서 Y축 계산.
  // + 엣지 스플라인이 너무 급하게 꺾이지 않도록 늘어날 수도 있다는데 코드를 봐야할듯
  // 각 랭크의 Y축 좌표
  fun calculateMainAxisCoords(): List<Double> {
    val mainAxisSizes = rankOrders.map { nodes ->
      val nodeSizes = nodes.mapNotNull { mainAxisSize(it) }
      check(nodeSizes.isNotEmpty())
      nodeSizes.maxOrNull()!!
    }
    return mainAxisSizes.windowed(2).scan(0.0) { acc, w ->
      acc + (w[0] + w[1]) / 2 + mainAxisMinSeparation
    }
  }

  // 노드 X축 좌표 계산.
  // 노드 ID -> X축 좌표.
  fun calculateSubAxisCoords(): Map<String, Double> {
    // 논문에서 설명하는대로 그래프를 바꿔서 NetworkSimplexSolver로 계산하면 된다는데..
    // 논문에는 여러가지 휴리스틱을 설명하고 있는데 랭킹 계산할 때 사용한 network simplex를 하면 된다고 함. 위의 휴리스틱이 필요한건가?

    var auxGraph = graphEx.copy(
      graph = BaseGraph(
        nodes = graphEx.graph.nodes,
        directedEdges = mapOf(),
        undirectedEdges = mapOf()
      )
    )

    graphEx.graph.directedEdges.forEach { (edgeName, edge) ->
      val edgeNode = auxGraph.newId()
      auxGraph = auxGraph.addVirtualNode(edgeNode)
      val edgeWeight = auxGraph.edgeWeight(edgeName)
      val edgeTypeWeight: Int = when {
        auxGraph.virtualNodes.contains(edge.start) && auxGraph.virtualNodes.contains(edge.end) ->
          virtualNodesEdgeWeight
        !auxGraph.virtualNodes.contains(edge.start) && !auxGraph.virtualNodes.contains(edge.end) ->
          realNodesEdgeWeight
        else -> mixedNodesEdgeWeight
      }
      val edgeToStartEdge = auxGraph.newId()
      val edgeToEndEdge = auxGraph.newId()
      auxGraph = auxGraph.addVirtualEdge(edgeToStartEdge, Edge(edgeNode, edge.start))
      auxGraph = auxGraph.addVirtualEdge(edgeToEndEdge, Edge(edgeNode, edge.end))
      auxGraph = auxGraph.setEdgeMinLength(edgeToStartEdge, 0)
      auxGraph = auxGraph.setEdgeMinLength(edgeToEndEdge, 0)
      auxGraph = auxGraph.setEdgeWeight(edgeToStartEdge, edgeWeight * edgeTypeWeight)
      auxGraph = auxGraph.setEdgeWeight(edgeToEndEdge, edgeWeight * edgeTypeWeight)
    }

    rankOrders.forEach { rankNodes ->
      rankNodes.windowed(2).forEach { adjNodes ->
        val leftNode = adjNodes[0]
        val rightNode = adjNodes[1]
        val leftNodeSize = subAxisSize(leftNode) ?: 0.0
        val rightNodeSize = subAxisSize(rightNode) ?: 0.0
        val adjNodesEdge = auxGraph.newId()
        auxGraph = auxGraph.addVirtualEdge(adjNodesEdge, Edge(leftNode, rightNode))
        auxGraph = auxGraph.setEdgeMinLength(
          adjNodesEdge,
          ((leftNodeSize + rightNodeSize) / 2 + subAxisMinSeparation).toInt()
        )
      }
    }

    return IntNetworkSimplexSolver(
      auxGraph.graph,
      auxGraph.fullEdgeMinLengths(),
      auxGraph.fullEdgeWeights()
    ).solve().mapValues { it.value.toDouble() }
  }
}

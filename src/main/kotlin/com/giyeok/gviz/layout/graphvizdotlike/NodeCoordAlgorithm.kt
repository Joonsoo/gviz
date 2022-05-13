package com.giyeok.gviz.layout.graphvizdotlike

import com.giyeok.gviz.draw.Size

class NodeCoordAlgorithm(
  val graph: GraphEx,
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

    TODO()
  }
}

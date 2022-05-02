package com.giyeok.gviz.graph.algorithms.graphvizdotlike

class CoordAlgorithm {
  // 노드 X축 좌표 계산.
  // 노드 ID -> X축 좌표.
  fun calculateXCoords(graph: GraphEx, rankOrders: List<List<String>>): Map<String, Double> {
    TODO()
  }

  // 각 랭크의 Y축 좌표 계산.
  // 각 rank의 높이를 구하고 inter rank 간격을 더해서 밖에서 Y축 계산.
  // + 엣지 스플라인이 너무 급하게 꺾이지 않도록 늘어날 수도 있다는데 코드를 봐야할듯
  // 랭크 -> Y축 좌표
  fun calculateYCoords(graph: GraphEx, rankOrders: List<List<String>>): Map<Int, Double> {
    TODO()
  }
}

package com.giyeok.gviz.graph

interface GraphLayoutAlgorithm {
  val graph: SizedGraph

  // `graph`의 노드들의 위치를 계산해서 반환.
  // 반환되는 포지션은 각 노드의 중앙 지점이라고 생각하고 구현. 실제 사용할 때는 적당히 보정해서 사용할 것.
  // -> layout 알고리즘에서 각 노드의 상단 중앙 지점을 반환할 수 있게 할까?
  // `graph`에 속한 노드들의 ID의 집합과 반환되는 Map의 키의 집합이 일치해야 함.
  // 반환되는 그래프의 Position의 범위는 알 수 없음. 사용하는 측에서 min-max를 계산해서 적절히 사용해야 함
  fun layoutGraph(): GraphLayoutResult
}

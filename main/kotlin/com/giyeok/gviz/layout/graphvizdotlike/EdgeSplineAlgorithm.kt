package com.giyeok.gviz.layout.graphvizdotlike

import com.giyeok.gviz.figure.Position
import com.giyeok.gviz.graph.BaseGraph
import com.giyeok.gviz.graph.EdgeSegment
import com.giyeok.gviz.graph.EdgeSpline

class EdgeSplineAlgorithm {
  fun calculateEdgeSplines(
    graph: BaseGraph,
    graphEx: GraphEx,
    nodeCoords: Map<String, Position>
  ): Map<String, EdgeSpline> {
    // TODO 실제로 구현
    // 1. 실제 엣지의 시작점-끝점을 직선으로 이어보고,
    // 2. 엣지가 차지할 수 있는 bounding box를 벗어나는 지점이 있으면
    // 3. 가장 많이 벗어나는 지점에 컨트롤 포인트를 추가한다.
    // 4. 모든 선이 bounding box 안에 포함될 때까지 2-3 과정을 반복한다.
    // 5. 1~4의 과정에서 얻어진 직선 segments를 부드럽게 베지어 커브로 잇는다.
    // 6. 만약 베지어 커브가 bounding box를 벗어나면 커브를 좀더 급하게 만들어본다.
    // 7. 베지어 커브가 bounding box 안에 포함될 때까지 커브를 급하게 만드는 과정을 반복한다.
    // -> 최악의 경우에는 곡선이 아니라 5의 결과가 그대로 반환될 수도 있음
    return graph.directedEdges.mapValues { (edgeName, _) ->
      val splits = graphEx.splitEdges[edgeName] ?: listOf(edgeName)
      EdgeSpline(
        splits.map { splitEdgeName ->
          val splitEdge = graphEx.graph.directedEdges.getValue(splitEdgeName)
          val startPosition = nodeCoords.getValue(splitEdge.start)
          val endPosition = nodeCoords.getValue(splitEdge.end)
          EdgeSegment.LineSegment(startPosition, endPosition)
        }
      )
    }
  }
}

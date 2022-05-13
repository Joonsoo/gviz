package com.giyeok.gviz.graph

import com.giyeok.gviz.figure.Position

data class GraphLayoutResult(
  val positions: Map<String, Position>,
  // edge id -> edge가 사용할 수 있는 영역에 대한 정보
  val edges: Map<String, EdgeSpline>,
)

// TODO 시작하는 지점의 각도, 끝나는 지점의 각도 추가(화살표 그리기 위해서)
// TODO edge label은 어디에 그릴지?
data class EdgeSpline(val segments: List<EdgeSegment>)

sealed class EdgeSegment {
  //    val path = Path2D.Double()
  //    path.moveTo(100.0, 100.0)
  //    path.quadTo(200.0, 100.0, 200.0, 200.0)
  //    path.moveTo(200.0, 200.0)
  //    path.curveTo(300.0, 200.0, 300.0, 300.0, 200.0, 300.0)
  //    g.draw(path)

  data class LineSegment(val start: Position, val end: Position) : EdgeSegment()

  data class QuadBezierSegment(
    val start: Position,
    val control: Position,
    val end: Position
  ) : EdgeSegment()

  data class CubicBezierSegment(
    val start: Position,
    val control1: Position,
    val control2: Position,
    val end: Position
  ) : EdgeSegment()
}

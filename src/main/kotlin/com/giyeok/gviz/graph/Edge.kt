package com.giyeok.gviz.graph

data class Edge(val start: String, val end: String) {
  fun reverse(): Edge = Edge(end, start)
}

data class NamedEdge(val edgeName: String, val edge: Edge)

// end는 이쪽의 반대편 노드 이름
data class NamedConnection(val name: String, val end: String, val isStart: Boolean)

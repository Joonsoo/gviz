package com.giyeok.gviz.graph

data class Edge(val start: String, val end: String)

data class NamedEdge(val name: String, val edge: Edge)
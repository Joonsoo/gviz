package com.giyeok.gviz.graph

import com.giyeok.gviz.render.Size

class SizedGraph(
  val nodeSizes: Map<String, Size>,
  val edgeLabelSizes: Map<String, Size>,
  directedEdges: Map<String, Edge>,
  undirectedEdges: Map<String, Edge>,
) : BaseGraph(nodeSizes.keys, directedEdges, undirectedEdges) {
}

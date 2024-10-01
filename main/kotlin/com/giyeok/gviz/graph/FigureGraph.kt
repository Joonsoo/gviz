package com.giyeok.gviz.graph

import com.giyeok.gviz.figure.Figure
import com.giyeok.gviz.figure.FigureSizeMeasurer

class FigureGraph(
  val nodeFigures: Map<String, Figure>,
  val edgeLabelFigures: Map<String, Figure>,
  directedEdges: Map<String, Edge>,
  undirectedEdges: Map<String, Edge>,
) : BaseGraph(nodeFigures.keys, directedEdges, undirectedEdges) {
  fun toSizedNodesGraph(sizeMeasurer: FigureSizeMeasurer): SizedGraph = SizedGraph(
    nodeFigures.mapValues { sizeMeasurer.measureSize(it.value) },
    edgeLabelFigures.mapValues { sizeMeasurer.measureSize(it.value) },
    directedEdges,
    undirectedEdges
  )
}

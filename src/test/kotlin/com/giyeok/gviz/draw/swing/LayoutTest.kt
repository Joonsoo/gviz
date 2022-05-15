package com.giyeok.gviz.draw.swing

import com.giyeok.gviz.figure.TextFigure
import com.giyeok.gviz.graph.BaseGraph
import com.giyeok.gviz.graph.FigureGraph
import com.giyeok.gviz.layout.graphvizdotlike.GraphLayout
import com.giyeok.gviz.layout.graphvizdotlike.Graphs
import com.giyeok.gviz.render.swing.FigureGraphView
import com.giyeok.gviz.render.swing.SwingFigureSizeMeasurer
import com.giyeok.gviz.render.swing.SwingFigureStyles
import java.awt.Graphics2D
import javax.swing.JFrame
import javax.swing.WindowConstants

class LayoutTest {
}

fun main() {
  val graph = Graphs.graph4 as BaseGraph

  val frame = JFrame()

  frame.setSize(800, 600)
  frame.isVisible = true
  frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

  val g2 = (frame.graphics as Graphics2D)

  val figureGraph = FigureGraph(
    graph.nodes.associateWith { node -> TextFigure(node, "") },
    mapOf(),
    graph.directedEdges,
    graph.undirectedEdges,
  )
  val styles = SwingFigureStyles()

  val figureSizeMeasurer = SwingFigureSizeMeasurer(styles, g2)
  val sizedGraph = figureGraph.toSizedNodesGraph(figureSizeMeasurer)
  val layout = GraphLayout(sizedGraph, 30.0, 20.0).layoutGraph()

  println("Layout done")

  val graphView = FigureGraphView(figureGraph, sizedGraph, styles, layout)
  frame.add(graphView)

  graphView.repaint()
  graphView.revalidate()
  frame.repaint()
  frame.revalidate()
}

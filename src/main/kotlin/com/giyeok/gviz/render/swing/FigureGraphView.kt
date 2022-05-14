package com.giyeok.gviz.render.swing

import com.giyeok.gviz.graph.EdgeSegment
import com.giyeok.gviz.graph.FigureGraph
import com.giyeok.gviz.graph.GraphLayoutResult
import com.giyeok.gviz.graph.SizedGraph
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JComponent

class FigureGraphView(
  val figureGraph: FigureGraph,
  val sizedGraph: SizedGraph,
  val styles: SwingFigureStyles,
  val layout: GraphLayoutResult
) : JComponent() {
  val figureRenderer = SwingFigureRenderer(styles)

  override fun paintComponent(g: Graphics) {
    val g2 = g as Graphics2D

    g2.setRenderingHint(
      RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON
    )

    val baseTransform = Transform(100.0, 100.0)

    layout.edges.forEach { (_, spline) ->
      spline.segments.forEach { segment ->
        when (segment) {
          is EdgeSegment.CubicBezierSegment -> TODO()
          is EdgeSegment.LineSegment -> {
            val start = baseTransform.move(segment.start.x, segment.start.y)
            val end = baseTransform.move(segment.end.x, segment.end.y)
            g2.drawLine(start.x.toInt(), start.y.toInt(), end.x.toInt(), end.y.toInt())
          }
          is EdgeSegment.QuadBezierSegment -> TODO()
        }
      }
    }

    figureGraph.nodeFigures.forEach { (nodeName, nodeFigure) ->
      val position = layout.positions.getValue(nodeName)
      val size = sizedGraph.nodeSizes.getValue(nodeName)
      figureRenderer.draw(
        g2,
        baseTransform.move(position.x - size.width / 2, position.y - size.height / 2),
        nodeFigure
      )
    }
  }
}

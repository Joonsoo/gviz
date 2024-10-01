package com.giyeok.gviz.render.swing

import com.giyeok.gviz.figure.Figure
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Path2D
import javax.swing.JComponent

class FigureView(val figure: Figure, val styles: SwingFigureStyles) : JComponent() {
  private val renderer = SwingFigureRenderer(styles)

  private var sizeInitialized = false

  override fun paintComponent(g: Graphics) {
    super.paintComponent(g)

    val g2 = g as Graphics2D
    g2.setRenderingHint(
      RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON
    )
    renderer.draw(g2, Transform(0.0, 0.0), figure)

    if (!sizeInitialized) {
      sizeInitialized = true

      val sizeMeasurer = SwingFigureSizeMeasurer(styles, g2)

      val figureSize = sizeMeasurer.measureSize(figure)
      println(figureSize)

      preferredSize = Dimension(figureSize.width.toInt(), figureSize.height.toInt())
    }

//    val path = Path2D.Double()
//    path.moveTo(100.0, 100.0)
//    path.quadTo(200.0, 100.0, 200.0, 200.0)
//    path.moveTo(200.0, 200.0)
//    path.curveTo(300.0, 200.0, 300.0, 300.0, 200.0, 300.0)
//    path.lineTo(400.0, 400.0)
//    g.draw(path)
  }
}

package com.giyeok.gviz.render.swing

import com.giyeok.gviz.figure.Figure
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JComponent

class FigureView(val figure: Figure, styles: SwingFigureStyles) : JComponent() {
  private val renderer = SwingFigureRenderer(styles)

  override fun paintComponent(g: Graphics) {
    super.paintComponent(g)
    renderer.draw(g as Graphics2D, Transform(0.0, 0.0), figure)
  }
}

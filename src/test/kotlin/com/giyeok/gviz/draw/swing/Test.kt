package com.giyeok.gviz.draw.swing

import com.giyeok.gviz.figure.ContainerFigure
import com.giyeok.gviz.figure.HorizFlowFigure
import com.giyeok.gviz.figure.TextFigure
import com.giyeok.gviz.figure.VertFlowFigure
import com.giyeok.gviz.render.swing.FigureView
import com.giyeok.gviz.render.swing.SwingFigureSizeMeasurer
import com.giyeok.gviz.render.swing.SwingFigureStyles
import java.awt.Graphics2D
import javax.swing.JFrame
import javax.swing.WindowConstants

fun main() {
  val frame = JFrame()

  frame.setSize(800, 600)
  frame.isVisible = true
  frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

  val g2 = (frame.graphics as Graphics2D)
  val styles = SwingFigureStyles()

  val measurer = SwingFigureSizeMeasurer(styles, g2)

  val figure = ContainerFigure(
    HorizFlowFigure(
      listOf(
        ContainerFigure(
          VertFlowFigure(
            listOf(
              TextFigure("Hello", ""),
              TextFigure("World!", ""),
            ), ""
          ),
          ""
        ),
        ContainerFigure(TextFigure("Wow!", ""), "")
      ), ""
    ), ""
  )
  val figureView = FigureView(figure, styles)
  println(measurer.measureSize(figure))

  frame.add(figureView)

  frame.isVisible = true
}

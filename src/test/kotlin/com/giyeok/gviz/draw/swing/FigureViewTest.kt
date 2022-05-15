package com.giyeok.gviz.draw.swing

import com.giyeok.gviz.figure.ContainerFigure
import com.giyeok.gviz.figure.HorizFlowFigure
import com.giyeok.gviz.figure.TextFigure
import com.giyeok.gviz.figure.VertFlowFigure
import com.giyeok.gviz.render.swing.FigureView
import com.giyeok.gviz.render.swing.SwingFigureStyles
import javax.swing.JFrame
import javax.swing.WindowConstants

class FigureViewTest {
}

fun main() {
  val frame = JFrame()

  frame.setSize(800, 600)
  frame.isVisible = true
  frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

  val styles = SwingFigureStyles()

  val figure = ContainerFigure(
    VertFlowFigure(
      listOf(
        TextFigure("Hello", ""),
        HorizFlowFigure(
          listOf(
            ContainerFigure(TextFigure("1", ""), ""),
            ContainerFigure(TextFigure("2", ""), ""),
            ContainerFigure(TextFigure("3", ""), ""),
            VertFlowFigure(
              listOf(
                TextFigure("가나다", ""),
                TextFigure("라마바", ""),
                TextFigure("XYZ", ""),
                TextFigure("XYZ", ""),
              ), ""
            )
          ), ""
        ),
        TextFigure("Hello", ""),
        TextFigure("Hello", ""),
      ),
      ""
    ),
    ""
  )

  val figureView = FigureView(figure, styles)

  frame.add(figureView)

  figureView.repaint()
  figureView.revalidate()
  frame.repaint()
  frame.revalidate()
}

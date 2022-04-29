package com.giyeok.gviz.render.swing

import com.giyeok.gviz.figure.*
import java.awt.Color
import java.awt.Graphics2D

class SwingFigureRenderer(val styles: SwingFigureStyles) {
  fun draw(g2: Graphics2D, transform: Transform, figure: Figure) {
    val measurer = SwingFigureSizeMeasurer(styles, g2.fontRenderContext)

    when (figure) {
      is EmptyFigure -> TODO()
      is TextFigure -> {
        val rectangle = measurer.measureTextFigure(figure)
        val leftTop = transform.apply(0.0, 0.0)
        val point = transform.apply(-rectangle.x, -rectangle.y)
        // TODO styles 처리
        g2.font = styles.font
        g2.color = Color.YELLOW
        g2.fillRect(leftTop.x, leftTop.y, rectangle.width.toInt(), rectangle.height.toInt())
        g2.color = Color.BLACK
        g2.drawString(figure.text, point.x, point.y)
      }
      is ImageFigure -> TODO()
      is ContainerFigure -> {
        // TODO styles 처리. 지금은 그냥 실선 border
        val bodySize = measurer.measureSize(figure.body)
        val leftTop = transform.apply(0.0, 0.0)
        draw(g2, transform.move(2.0, 2.0), figure.body)
        g2.color = Color.BLACK
        g2.drawRect(leftTop.x, leftTop.y, bodySize.width.toInt() + 4, bodySize.height.toInt() + 4)
      }
      is VertFlowFigure -> {
        if (figure.children.isNotEmpty()) {
          var relativeTop = 0.0
          figure.children.forEach { child ->
            val childSize = measurer.measureSize(child)
            draw(g2, transform.move(0.0, relativeTop), child)
            relativeTop += childSize.height
          }
        }
      }
      is HorizFlowFigure -> {
        if (figure.children.isNotEmpty()) {
          var relativeLeft = 0.0
          figure.children.forEach { child ->
            val childSize = measurer.measureSize(child)
            draw(g2, transform.move(relativeLeft, 0.0), child)
            relativeLeft += childSize.width
          }
        }
      }
      is GridFigure -> {
        val gridSizes = measurer.measureGridSizes(figure)
        TODO()
      }
    }
  }
}

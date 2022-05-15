package com.giyeok.gviz.render.swing

import com.giyeok.gviz.figure.*
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.GeneralPath

class SwingFigureRenderer(val styles: SwingFigureStyles) {
  fun draw(g2: Graphics2D, transform: Transform, figure: Figure) {
    val measurer = SwingFigureSizeMeasurer(styles, g2)

    when (figure) {
      is EmptyFigure -> TODO()
      is TextFigure -> {
        val leftTop = transform.apply(0.0, 0.0)
        val style = styles.getTextStyle(figure.styleClass)
        // TODO styles 처리
        g2.font = style.font
//        g2.color = Color.YELLOW
//        g2.fillRect(leftTop.x, leftTop.y, rectangle.width.toInt(), rectangle.height.toInt())
        g2.color = style.color
        g2.drawString(figure.text, leftTop.x.toInt(), leftTop.y.toInt() + g2.fontMetrics.ascent)
      }
      is ImageFigure -> TODO()
      is ContainerFigure -> {
        // TODO styles 처리. 지금은 그냥 실선 border
        val style = styles.getContainerStyle(figure.styleClass)
        val childLeftTop = transform.move(
          style.marginLeft + style.borderWidth + style.paddingLeft,
          style.marginTop + style.borderWidth + style.paddingTop,
        )
        draw(g2, childLeftTop, figure.body)
        if (style.border != null) {
          g2.color = Color.BLACK
          val bodySize = measurer.measureSize(figure.body)
          val borderOffset = transform.move(style.marginLeft, style.marginTop)
          val leftTop = borderOffset.apply(0.0, 0.0)
          val rightTop = borderOffset.apply(
            style.borderWidth + style.paddingLeft + bodySize.width + style.paddingRight,
            0.0
          )
          val rightBottom = borderOffset.apply(
            style.borderWidth + style.paddingLeft + bodySize.width + style.paddingRight,
            style.borderWidth + style.paddingTop + bodySize.height + style.paddingBottom,
          )
          val leftBottom = borderOffset.apply(
            0.0,
            style.borderWidth + style.paddingTop + bodySize.height + style.paddingBottom,
          )
          val shape = GeneralPath()
          shape.moveTo(leftTop.x, leftTop.y)
          shape.lineTo(rightTop.x, rightTop.y)
          shape.lineTo(rightBottom.x, rightBottom.y)
          shape.lineTo(leftBottom.x, leftBottom.y)
          shape.lineTo(leftTop.x, leftTop.y)
          g2.stroke = style.border.stroke
          g2.draw(shape)
        }
      }
      is VertFlowFigure -> {
        if (figure.children.isNotEmpty()) {
          val childSizes = figure.children.map { measurer.measureSize(it) }
          val width = childSizes.maxOf { it.width }
          var relativeTop = 0.0
          val style = styles.getVertFlowStyle(figure.styleClass)
          figure.children.forEachIndexed { idx, child ->
            val childSize = childSizes[idx]
            val x = when (style.horizAlignment) {
              Alignment.LEADING -> 0.0
              Alignment.CENTER -> (width - childSize.width) / 2
              Alignment.TRAILING -> width - childSize.width
            }
            draw(g2, transform.move(x, relativeTop), child)
            relativeTop += childSize.height + style.separation
          }
        }
      }
      is HorizFlowFigure -> {
        if (figure.children.isNotEmpty()) {
          val childSizes = figure.children.map { measurer.measureSize(it) }
          val height = childSizes.maxOf { it.height }
          var relativeLeft = 0.0
          val style = styles.getHorizFlowStyle(figure.styleClass)
          figure.children.forEachIndexed { idx, child ->
            val childSize = childSizes[idx]
            val y = when (style.vertAlignment) {
              Alignment.LEADING -> 0.0
              Alignment.CENTER -> (height - childSize.height) / 2
              Alignment.TRAILING -> height - childSize.height
            }
            draw(g2, transform.move(relativeLeft, y), child)
            relativeLeft += childSize.width + style.separation
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

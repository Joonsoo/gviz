package com.giyeok.gviz.render.swing

import com.giyeok.gviz.figure.*
import com.giyeok.gviz.figure.FigureSizeMeasurer
import com.giyeok.gviz.figure.Size
import java.awt.Graphics2D
import java.awt.geom.Rectangle2D
import java.lang.Double.max

class SwingFigureSizeMeasurer(
  val styles: SwingFigureStyles,
  val g2: Graphics2D,
) : FigureSizeMeasurer {
  private fun Rectangle2D.toSize() = Size(width, height)

  data class GridSizes(val columnWidths: List<Double>, val rowHeights: List<Double>)

  fun measureTextFigure(figure: TextFigure): Rectangle2D {
    val style = styles.getTextStyle(figure.styleClass)
    return style.font.getStringBounds(figure.text, g2.fontRenderContext)
  }

  fun measureGridSizes(figure: GridFigure): GridSizes {
    val columns = figure.rows[0].cells.size
    val colWidths = MutableList(columns) { 0.0 }
    val rowHeights = MutableList(figure.rows.size) { 0.0 }
    figure.rows.forEachIndexed { rowIndex, row ->
      val cellSizes = row.cells.map { measureSize(it) }
      cellSizes.forEachIndexed { colIndex, cellSize ->
        colWidths[colIndex] = max(colWidths[colIndex], cellSize.width)
        rowHeights[rowIndex] = max(rowHeights[rowIndex], cellSize.height)
      }
    }
    return GridSizes(colWidths, rowHeights)
  }

  // TODO styleClass와 styles
  override fun measureSize(figure: Figure): Size = when (figure) {
    is EmptyFigure -> Size(0.0, 0.0)
    is TextFigure -> measureTextFigure(figure).toSize()
    is ImageFigure -> TODO()
    is ContainerFigure -> {
      // TODO border
      val bodySize = measureSize(figure.body)
      val style = styles.getContainerStyle(figure.styleClass)
      Size(
        style.marginLeft + style.paddingLeft + style.borderWidth * 2 +
          bodySize.width + style.paddingRight + style.marginRight,
        style.marginTop + style.paddingTop + style.borderWidth * 2 +
          bodySize.height + style.paddingBottom + style.marginBottom,
      )
    }
    is VertFlowFigure -> {
      val children = figure.children.map { measureSize(it) }
      if (children.isEmpty()) {
        Size(0.0, 0.0)
      } else {
        val style = styles.getVertFlowStyle(figure.styleClass)
        Size(
          children.maxOf { it.width },
          children.sumOf { it.height } + (style.separation * (children.size - 1)))
      }
    }
    is HorizFlowFigure -> {
      val children = figure.children.map { measureSize(it) }
      if (children.isEmpty()) {
        Size(0.0, 0.0)
      } else {
        val style = styles.getHorizFlowStyle(figure.styleClass)
        Size(children.sumOf { it.width } + (style.separation * (children.size - 1)),
          children.maxOf { it.height })
      }
    }
    is GridFigure -> {
      val gridSizes = measureGridSizes(figure)
      val style = styles.getGridStyle(figure.styleClass)
      val width = if (gridSizes.columnWidths.isEmpty()) 0.0 else {
        gridSizes.columnWidths.sum() + (style.colsSeparation * (gridSizes.columnWidths.size - 1))
      }
      val height = if (gridSizes.rowHeights.isEmpty()) 0.0 else {
        gridSizes.rowHeights.sum() + (style.rowsSeparation * (gridSizes.rowHeights.size - 1))
      }
      Size(width, height)
    }
  }
}

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
    styles.applyStyle(g2, figure.styleClass)
    return styles.font.getStringBounds(figure.text, g2.fontRenderContext)
  }

  fun measureGridSizes(figure: GridFigure): GridSizes {
    styles.applyStyle(g2, figure.styleClass)
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
      styles.applyStyle(g2, figure.styleClass)
      val bodySize = measureSize(figure.body)
      Size(bodySize.width + 4, bodySize.height + 4)
    }
    is VertFlowFigure -> {
      styles.applyStyle(g2, figure.styleClass)
      val children = figure.children.map { measureSize(it) }
      if (children.isEmpty()) {
        Size(0.0, 0.0)
      } else {
        Size(children.maxOf { it.width }, children.sumOf { it.height })
      }
    }
    is HorizFlowFigure -> {
      styles.applyStyle(g2, figure.styleClass)
      val children = figure.children.map { measureSize(it) }
      if (children.isEmpty()) {
        Size(0.0, 0.0)
      } else {
        Size(children.sumOf { it.width }, children.maxOf { it.height })
      }
    }
    is GridFigure -> {
      styles.applyStyle(g2, figure.styleClass)
      val gridSizes = measureGridSizes(figure)
      Size(gridSizes.columnWidths.sum(), gridSizes.rowHeights.sum())
    }
  }
}

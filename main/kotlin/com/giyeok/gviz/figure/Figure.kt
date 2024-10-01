package com.giyeok.gviz.figure

import java.awt.Image

sealed class Figure

data class EmptyFigure(val styleClass: String) : Figure()

data class TextFigure(val text: String, val styleClass: String) : Figure()

data class ImageFigure(val image: Image, val styleClass: String) : Figure()

data class ContainerFigure(val body: Figure, val styleClass: String) : Figure()

data class VertFlowFigure(val children: List<Figure>, val styleClass: String) : Figure()

data class HorizFlowFigure(val children: List<Figure>, val styleClass: String) : Figure()

data class GridFigure(val rows: List<GridRow>, val styleClass: String) : Figure() {
  init {
    // 각 row의 cell 수가 모두 같은지 확인
    check(rows.map { it.cells.size }.distinct().size == 1)
  }
}

data class GridRow(val cells: List<Figure>, val styleClass: String)

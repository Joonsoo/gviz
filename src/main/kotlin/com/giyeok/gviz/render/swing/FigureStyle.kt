package com.giyeok.gviz.render.swing

import java.awt.Color
import java.awt.Font
import java.awt.Stroke

sealed class FigureStyle

data class TextStyle(
  val font: Font,
  val color: Color,
) : FigureStyle()

data class ContainerStyle(
  val marginTop: Double = 0.0,
  val marginLeft: Double = 0.0,
  val marginBottom: Double = 0.0,
  val marginRight: Double = 0.0,
  val paddingTop: Double = 0.0,
  val paddingLeft: Double = 0.0,
  val paddingBottom: Double = 0.0,
  val paddingRight: Double = 0.0,
  val border: BorderStyle?,
) : FigureStyle() {
  val borderWidth = border?.width ?: 0.0
}

data class BorderStyle(
  val color: Color,
  val width: Double,
  val stroke: Stroke,
)

data class VertFlowStyle(
  val separation: Double,
  val horizAlignment: Alignment,
) : FigureStyle()

data class HorizFlowStyle(
  val separation: Double,
  val vertAlignment: Alignment,
) : FigureStyle()

enum class Alignment {
  LEADING,
  CENTER,
  TRAILING,
}

data class GridStyle(
  val rowsSeparation: Double,
  val colsSeparation: Double,
) : FigureStyle()

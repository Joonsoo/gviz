package com.giyeok.gviz.render.swing

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font

class SwingFigureStyles(
  val textStyles: Map<String, TextStyle> = mapOf(),
  val containerStyles: Map<String, ContainerStyle> = mapOf(),
  val vertFlowStyles: Map<String, VertFlowStyle> = mapOf(),
  val horizFlowStyles: Map<String, HorizFlowStyle> = mapOf(),
  val gridStyles: Map<String, GridStyle> = mapOf(),
  val defaultTextStyle: TextStyle = TextStyle(
    font = Font("Monospaced", Font.PLAIN, 15),
    color = Color.BLACK
  ),
  val defaultContainerStyle: ContainerStyle = ContainerStyle(
    0.0, 0.0, 0.0, 0.0,
    0.0, 0.0, 0.0, 0.0,
    border = BorderStyle(Color.GRAY, 1.0, BasicStroke())
  ),
  val defaultVertFlowStyles: VertFlowStyle = VertFlowStyle(1.0, Alignment.CENTER),
  val defaultHorizFlowStyles: HorizFlowStyle = HorizFlowStyle(1.0, Alignment.CENTER),
  val defaultGridStyle: GridStyle = GridStyle(0.0, 0.0)
) {
  fun getTextStyle(styleClass: String): TextStyle =
    textStyles[styleClass] ?: defaultTextStyle

  fun getContainerStyle(styleClass: String): ContainerStyle =
    containerStyles[styleClass] ?: defaultContainerStyle

  fun getVertFlowStyle(styleClass: String): VertFlowStyle =
    vertFlowStyles[styleClass] ?: defaultVertFlowStyles

  fun getHorizFlowStyle(styleClass: String): HorizFlowStyle =
    horizFlowStyles[styleClass] ?: defaultHorizFlowStyles

  fun getGridStyle(styleClass: String): GridStyle =
    gridStyles[styleClass] ?: defaultGridStyle
}

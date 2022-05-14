package com.giyeok.gviz.render.swing

import java.awt.Font
import java.awt.Graphics2D

class SwingFigureStyles {
  val font = Font("Monospaced", Font.PLAIN, 15)

  fun applyStyle(g2: Graphics2D, styleClass: String) {
    g2.font = font
  }
}

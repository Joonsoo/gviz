package com.giyeok.gviz.render.swing

import java.awt.Point


class Transform(val x: Double, val y: Double) {
  fun move(x: Double, y: Double): Transform =
    Transform(this.x + x, this.y + y)

  fun apply(x: Double, y: Double): Point {
    return Point((this.x + x).toInt(), (this.y + y).toInt())
  }
}

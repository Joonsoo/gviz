package com.giyeok.gviz.render.swing

import com.giyeok.gviz.figure.Position


class Transform(val x: Double, val y: Double) {
  fun move(x: Double, y: Double): Transform =
    Transform(this.x + x, this.y + y)

  fun apply(x: Double, y: Double): Position {
    return Position(this.x + x, this.y + y)
  }
}

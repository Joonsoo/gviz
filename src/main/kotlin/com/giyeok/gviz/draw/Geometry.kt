package com.giyeok.gviz.draw

data class Position(val x: Double, val y: Double)

data class Size(val width: Double, val height: Double)

data class Rectangle(val center: Position, val size: Size)

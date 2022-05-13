package com.giyeok.gviz.draw

import com.giyeok.gviz.figure.Figure

interface FigureSizeMeasurer {
  fun measureSize(figure: Figure): Size
}
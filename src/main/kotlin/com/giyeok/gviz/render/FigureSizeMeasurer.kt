package com.giyeok.gviz.render

import com.giyeok.gviz.figure.Figure

interface FigureSizeMeasurer {
  fun measureSize(figure: Figure): Size
}
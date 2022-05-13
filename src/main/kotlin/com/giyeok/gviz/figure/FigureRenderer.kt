package com.giyeok.gviz.figure

import java.awt.Color
import java.awt.Font
import javax.swing.JFrame
import javax.swing.JTextPane
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext

interface FigureRenderer {
}

fun main() {
  val frame = JFrame()

  val pane = JTextPane()

  val sc = StyleContext()

//  var red = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.red)
//  red = sc.addAttribute(red, StyleConstants.FontFamily, "Lucida Console")
//  red = sc.addAttribute(red, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED)

  var default = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.black)
  default = sc.addAttribute(default, StyleConstants.FontFamily, Font.MONOSPACED)
  default = sc.addAttribute(default, StyleConstants.FontSize, 15)
  default = sc.addAttribute(default, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED)
  val red = sc.addAttribute(default, StyleConstants.Foreground, Color.red)
  var blue = sc.addAttribute(default, StyleConstants.Foreground, Color.blue)
  blue = sc.addAttribute(blue, StyleConstants.Underline, true)
  blue = sc.addAttribute(blue, StyleConstants.Background, Color.yellow)

  val doc = DefaultStyledDocument(sc)
  doc.insertString(0, "Hello ", default)
  doc.insertString(doc.length, "World!", red)

  doc.setCharacterAttributes(3, 6, blue, true)

  pane.document = doc

  frame.add(pane)
  frame.setSize(800, 600)
  frame.isVisible = true
}

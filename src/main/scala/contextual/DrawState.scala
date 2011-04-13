package contextual

import java.awt.Color
import java.awt.geom.AffineTransform
import math.Pi

class DrawState(var color: HSBColor, val coordinateTransform: AffineTransform) {
  def this() = this(new HSBColor(0, 0, 0), new AffineTransform)

  def translate(x: Double, y: Double) = coordinateTransform.translate(x, y)
  def scale(x: Double, y: Double)     = coordinateTransform.scale(x, y)
  def scale(s: Double)                = coordinateTransform.scale(s, s)
  def rotate(a: Double)               = coordinateTransform.rotate(a / 180 * Pi)
  def shear(x: Double, y: Double)     = coordinateTransform.shear(x, y)
  def hue                    = color.hue
  def hue_=(h: Int)          = color = color.withHue(h)
  def saturation             = color.saturation
  def saturation_=(s: Float) = color = color.withSaturation(s)
  def brightness             = color.brightness
  def brightness_=(b: Float) = color = color.withBrightness(b)

  def copy =
    new DrawState(color, new AffineTransform(coordinateTransform))
}

class HSBColor(val hue: Int, val saturation: Float, val brightness: Float) {
  def withHue(h: Int)          = new HSBColor(h, saturation, brightness)
  def withSaturation(s: Float) = new HSBColor(hue, norm(s), brightness)
  def withBrightness(b: Float) = new HSBColor(hue, saturation, norm(b))
  def toColor = Color.getHSBColor(hue / 360f, saturation, brightness)
  override def toString = (hue, saturation, brightness).toString

  private def norm(x: Float) = 0f max (1f min x)
}

object HSBColor {
  implicit def hsbColor2Color(c: HSBColor) = c.toColor
}

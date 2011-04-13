package contextual

import java.awt.{ Color, Graphics2D, Shape }
import java.awt.geom.AffineTransform

case class Primitive(shape: Shape, transform: AffineTransform, color: Color) {
  def paint(g: Graphics2D) {
    val original = g.getTransform
    g.transform(transform)
    g.setColor(color)
    g.fill(shape)
    g.setTransform(original)
  }
}

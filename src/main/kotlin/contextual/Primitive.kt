package contextual

import java.awt.Shape
import java.awt.Graphics2D
import java.awt.geom.AffineTransform

public class Primitive(val shape: Shape, val transform: AffineTransform, val color: HSBColor) {
    fun paint(g: Graphics2D) {
        val original = g.getTransform()!!
        g.transform(transform)
        g.setColor(color.toColor())
        g.fill(shape)
        g.setTransform(original)
    }
}

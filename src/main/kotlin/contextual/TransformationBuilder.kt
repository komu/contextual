package contextual

import java.awt.geom.AffineTransform

class TransformationBuilder {

    var color = HSBColor.BLACK
    val coordinateTransform =  AffineTransform()

    var hue: Int
        get() = color.hue
        set(h) { color = color.withHue(h) }

    var saturation: Float
        get() = color.saturation
        set(s) { color = color.withSaturation(s) }

    var brightness: Float
        get() = color.brightness
        set(s) { color = color.withBrightness(s) }

    fun translate(dx: Double, dy: Double) = coordinateTransform.translate(dx, dy)
    fun scale(s: Double)                  = coordinateTransform.scale(s, s)
    fun scale(sx: Double, sy: Double)     = coordinateTransform.scale(sx, sy)
    fun rotate(angle: Double)             = coordinateTransform.rotate(angle / 180 * Math.PI)
    fun shear(x: Double, y: Double)       = coordinateTransform.shear(x, y)

    fun build(): (DrawState) -> DrawState =
        if (color == HSBColor.BLACK && coordinateTransform.isIdentity())
            { s -> s }
        else if (color == HSBColor.BLACK)
            { s -> DrawState(s.color, s.coordinateTransform * coordinateTransform) }
        else if (coordinateTransform.isIdentity())
            { s -> DrawState(s.color + color, s.coordinateTransform) }
        else
            { s -> DrawState(s.color + color, s.coordinateTransform * coordinateTransform) }
}

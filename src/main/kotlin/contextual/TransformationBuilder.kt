package contextual

import java.awt.geom.AffineTransform

class TransformationBuilder {

    var color = HSBColor.BLACK
    val coordinateTransform =  AffineTransform()

    fun hue(h: Int): TransformationBuilder {
        color = color.withHue(h)
        return this
    }

    fun saturation(s: Float): TransformationBuilder {
        color = color.withSaturation(s)
        return this
    }

    fun brightness(b: Float): TransformationBuilder {
        color = color.withBrightness(b)
        return this
    }

    fun translate(dx: Double, dy: Double): TransformationBuilder {
        coordinateTransform.translate(dx, dy)
        return this
    }

    fun scale(s: Double) = scale(s, s)

    fun scale(sx: Double, sy: Double): TransformationBuilder {
        coordinateTransform.scale(sx, sy)
        return this
    }

    fun rotate(angle: Double): TransformationBuilder {
        coordinateTransform.rotate(angle / 180 * Math.PI)
        return this
    }

    fun shear(x: Double, y: Double): TransformationBuilder {
        coordinateTransform.shear(x, y)
        return this
    }

    fun flip(a: Double) =
        if (a == 90.0)
            scale(-1.0, 1.0)
        else
            throw UnsupportedOperationException("flip is supported only for 90 degrees")

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

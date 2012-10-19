package contextual

import java.awt.geom.AffineTransform
import java.awt.Color
import kotlin.template.toString

class DrawState(val color: HSBColor, val coordinateTransform: AffineTransform) {

    class object {
        fun initial() = DrawState(HSBColor.BLACK, AffineTransform())
    }

    fun transformBy(ops: List<(DrawStateBuilder) -> Unit>): DrawState {
        val builder = DrawStateBuilder(color, AffineTransform(coordinateTransform))
        for (op in ops)
            op(builder)
        return builder.build()
    }
}

class DrawStateBuilder(var color: HSBColor, val coordinateTransform: AffineTransform) {
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

    fun build() = DrawState(color, coordinateTransform)
}

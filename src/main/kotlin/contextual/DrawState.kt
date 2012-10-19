package contextual

import java.awt.geom.AffineTransform
import java.awt.Color
import kotlin.template.toString

class DrawState(val color: HSBColor, val coordinateTransform: AffineTransform) {

    class object {
        fun initial() = DrawState(HSBColor.BLACK, AffineTransform())
    }
}

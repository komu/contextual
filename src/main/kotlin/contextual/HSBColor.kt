package contextual

import java.awt.Color
import java.lang.Math.max
import java.lang.Math.min

class HSBColor(val hue: Int, val saturation: Float, val brightness: Float) {

    class object {
        val BLACK = HSBColor(0, 0.toFloat(), 0.toFloat())
    }

    fun withHue(h: Int)          = HSBColor(h, saturation, brightness)
    fun withSaturation(s: Float) = HSBColor(hue, norm(s), brightness)
    fun withBrightness(b: Float) = HSBColor(hue, saturation, norm(b))
    fun toColor() = Color.getHSBColor(hue / 360.toFloat(), saturation, brightness)
    fun toString() = "($hue, $saturation, $brightness)"

    fun plus(c: HSBColor) = HSBColor(hue + c.hue, saturation + c.saturation, brightness + c.brightness)

    private fun norm(x: Float) = max(0.toFloat(), min(1.toFloat(), x))

    fun equals(o: Any?) =
        o is HSBColor && hue == o.hue && saturation == o.saturation && brightness == o.brightness

    fun hashCode() =
        (hue.hashCode() * 31 + saturation.hashCode()) * 31 + brightness.hashCode()
}

package contextual

import java.awt.Color
import java.lang.Math.max
import java.lang.Math.min

data class HSBColor(val hue: Int, val saturation: Float, val brightness: Float) {

    companion object {
        val BLACK = HSBColor(0, 0.toFloat(), 0.toFloat())
    }

    fun withHue(h: Int)          = HSBColor(h, saturation, brightness)
    fun withSaturation(s: Float) = HSBColor(hue, norm(s), brightness)
    fun withBrightness(b: Float) = HSBColor(hue, saturation, norm(b))
    fun toColor(): Color = Color.getHSBColor(hue / 360.toFloat(), saturation, brightness)
    override fun toString() = "($hue, $saturation, $brightness)"

    operator fun plus(c: HSBColor) = HSBColor(hue + c.hue, saturation + c.saturation, brightness + c.brightness)

    private fun norm(x: Float) = max(0.toFloat(), min(1.toFloat(), x))
}

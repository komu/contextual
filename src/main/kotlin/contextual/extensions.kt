package contextual

import java.awt.geom.AffineTransform

fun AffineTransform.times(t: AffineTransform): AffineTransform {
    val result = AffineTransform(this)
    result.concatenate(t)
    return result
}

fun String.contains(ch: Char) =
    indexOf(ch) != -1

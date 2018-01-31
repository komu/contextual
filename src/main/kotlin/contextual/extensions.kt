package contextual

import java.awt.geom.AffineTransform

operator fun AffineTransform.times(t: AffineTransform): AffineTransform {
    val result = AffineTransform(this)
    result.concatenate(t)
    return result
}

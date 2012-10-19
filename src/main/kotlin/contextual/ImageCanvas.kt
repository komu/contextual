package contextual

import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.JComponent
import java.awt.RenderingHints.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ImageCanvas(val image: BufferedImage) : JComponent() {

    val lock = ReentrantLock()
    val g = image.createGraphics()!!

    {
        setBackground(Color.WHITE)
        g.translate(image.getWidth() / 2, image.getHeight() / 2)
        g.scale(1.0, -1.0)
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
    }

    override fun paintComponent(g: Graphics?) {
        g!!
        g.setColor(getBackground())
        g.fillRect(0, 0, getWidth(), getHeight())

        lock.withLock {
            g.drawImage(image, 0, 0, null)
        }
    }

    fun draw(p: Primitive) {
        lock.withLock {
            p.paint(g)
            repaint()
        }
    }
}

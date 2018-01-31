package contextual

import java.awt.Color
import java.awt.Graphics
import java.awt.RenderingHints.*
import java.awt.image.BufferedImage
import java.util.concurrent.locks.ReentrantLock
import javax.swing.JComponent
import kotlin.concurrent.withLock

class PrimitiveDrawingCanvas(width: Int, height: Int): JComponent() {

    private val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    private val lock = ReentrantLock()
    private val imageContext = image.createGraphics()!!

    init {
        background = Color.WHITE
        imageContext.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
    }

    override fun paintComponent(g: Graphics) {
        g.color = background
        g.fillRect(0, 0, width, height)

        lock.withLock {
            g.drawImage(image, 0, 0, null)
        }
    }

    fun draw(p: Primitive) {
        lock.withLock {
            imageContext.transform = p.transform
            imageContext.color = p.color
            imageContext.fill(p.shape)
        }
        repaint(100)
    }
}

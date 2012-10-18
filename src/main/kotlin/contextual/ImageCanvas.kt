package contextual

import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.JComponent

class ImageCanvas(val image: BufferedImage) : JComponent() {

    {
        setBackground(Color.WHITE)
    }

    override fun paintComponent(g: Graphics?) {
        g!!
        g.setColor(getBackground())
        g.fillRect(0, 0, getWidth(), getHeight())

        // TODO: synchronize
        //image.synchronized {
            g.drawImage(image, 0, 0, null)
        //}
    }
}

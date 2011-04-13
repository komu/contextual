package contextual

import java.awt.{ Color, Graphics }
import java.awt.image.BufferedImage
import javax.swing.JComponent

class ImageCanvas(val image: BufferedImage) extends JComponent {
  setBackground(Color.WHITE)
  
  override def paintComponent(g: Graphics) {
    g.setColor(getBackground)
    g.fillRect(0, 0, getWidth, getHeight)
    
    image.synchronized {
      g.drawImage(image, 0, 0, null)
    }
  }
}

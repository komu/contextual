package contextual

import java.awt.RenderingHints.*
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.UIManager
import java.util.Random
import java.util.concurrent.LinkedBlockingQueue
import javax.swing.Painter
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread
import java.awt.geom.AffineTransform
import java.awt.Shape
import java.awt.Dimension

fun main(args: Array<String>) {

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val root = RuleParser.parse("rules/foo.ctx")

    val size = Dimension(1200, 800)
    val frame = JFrame("contextual")
    val canvas = PrimitiveDrawingCanvas(size.width, size.height)
    frame.add(canvas)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setSize(size)
    frame.setLocationRelativeTo(null)
    frame.setVisible(true)

    val initialTransform = AffineTransform()
    initialTransform.translate(size.width / 2.0, size.height / 2.0)
    initialTransform.scale(1.0, -1.0)

    val processor = Processor()
    processor.addWorkItems(arrayList(root), DrawState(HSBColor.BLACK, initialTransform), 0)

    while (true)
        canvas.draw(processor.takeResult())
}

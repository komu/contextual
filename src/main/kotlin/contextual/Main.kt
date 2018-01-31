package contextual

import java.awt.Dimension
import java.awt.geom.AffineTransform
import javax.swing.JFrame
import javax.swing.UIManager

fun main(args: Array<String>) {

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val root = RuleParser.parse("rules/foo.ctx")

    val imageSize = Dimension(1200, 800)

    val canvas = PrimitiveDrawingCanvas(imageSize.width, imageSize.height)

    JFrame("contextual").apply {
        add(canvas)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        size = imageSize
        setLocationRelativeTo(null)
        isVisible = true
    }

    val initialTransform = AffineTransform()
    initialTransform.translate(imageSize.width / 2.0, imageSize.height / 2.0)
    initialTransform.scale(1.0, -1.0)

    val processor = Processor()
    processor.addWorkItems(listOf(root), DrawState(HSBColor.BLACK, initialTransform), 0)

    while (true)
        canvas.draw(processor.takeResult())
}

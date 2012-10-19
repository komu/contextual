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

fun main(args: Array<String>) {

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    var emptyImage = BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB)

    val root = RuleParser.parse("rules/foo.ctx")

    val primQueue = ArrayBlockingQueue<Primitive>(1000)
    val frame = JFrame("contextual")
    val canvas = ImageCanvas(emptyImage)
    frame.add(canvas)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setSize(1200, 800)
    frame.setLocationRelativeTo(null)
    frame.setVisible(true)

    thread {
        while (true)
            canvas.draw(primQueue.take()!!)
    }

    val workQueue = LinkedBlockingQueue<WorkItem>()

    val ctx = ProcessingContext(workQueue, primQueue)
    ctx.addWorkItem(root, DrawState.initial(), 0)
    3.times {
        thread {
            while (true) {
                val workItem = workQueue.take()!!
                workItem.process(ctx)
            }
        }
    }
}

class WorkItem(val rule: Rule, val state: DrawState, val depth: Int) {
    fun process(ctx: ProcessingContext) {
        rule.process(ctx, state, depth)
    }
}

class ProcessingContext(private val workQueue: BlockingQueue<WorkItem>,
                        private val resultQueue: BlockingQueue<Primitive>,
                        val maxDepth: Int = 500)
{
    private val random = Random()

    fun addShape(shape: Shape, state: DrawState) {
        resultQueue.put(Primitive(shape, state.coordinateTransform, state.color))
    }

    fun addWorkItem(rule: Rule, state: DrawState, depth: Int) {
        workQueue.put(WorkItem(rule, state, depth))
    }

    fun randomInt(n: Int) =
        random.nextInt(n)
}

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

fun main(args: Array<String>) {

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val random = Random()
    val randomSeed = random.nextLong()
    println("random seed: $randomSeed")
    random.setSeed(randomSeed)

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

    Thread(Painter(canvas, primQueue)).start()

    val workQueue = LinkedBlockingQueue<WorkItem>()
    workQueue.put(WorkItem(root, DrawState.initial(), 0))

    Thread(PrimitiveGenerator(workQueue, primQueue, random)).start()
    Thread(PrimitiveGenerator(workQueue, primQueue, random)).start()
    Thread(PrimitiveGenerator(workQueue, primQueue, random)).start()
}

data class WorkItem(val rule: Rule, val state: DrawState, val depth: Int)

class PrimitiveGenerator(val workQueue: BlockingQueue<WorkItem>,
                         val resultQueue: BlockingQueue<Primitive>,
                         val random: Random) : Runnable {
    var maxDepth = 500

    override fun run() {
        while (true) {
            val (rule, st, d) = workQueue.take()!!
            process(rule, st, d)
        }
    }

    private fun process(rule: Rule, st: DrawState, d: Int) {
        when (rule) {
            is TransformRule -> process(rule.rule, rule.transform(st), d)
            is RandomRule    -> process(rule.rule(random), st, d)
            is CompoundRule  -> if (rule.size == 1) {
                                    process(rule.rules[0], st, d)
                                } else if (d < maxDepth) {
                                    for (r in rule.rules)
                                        workQueue.put(WorkItem(r, st, d + 1))
                                }
            is PrimitiveRule -> resultQueue.put(Primitive(rule.shape, st.coordinateTransform, st.color))
            else             -> { }
        }
    }
}

class Painter(val canvas: ImageCanvas, val queue: BlockingQueue<Primitive>) : Runnable {
    val image = canvas.image
    val g = image.createGraphics()!!

    {
        g.translate(image.getWidth() / 2, image.getHeight() / 2)
        g.scale(1.0, -1.0)
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
    }

    override fun run() {
        while (true) {
            val prim = queue.take()!!

            // TODO: sync: image.synchronized {
            prim.paint(g)
            canvas.repaint()
            //}
        }
    }
}


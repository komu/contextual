package contextual

import java.awt.RenderingHints._
import java.awt.image.BufferedImage
import javax.swing.{ JFrame, UIManager }
import java.util.Random
import scala.collection.mutable.{ ArrayBuffer, Queue }

import java.util.concurrent.{ ArrayBlockingQueue, BlockingQueue, LinkedBlockingQueue }

object Main {
  def main(args: Array[String]) {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
    
    val random = new Random
    val randomSeed = random.nextLong
    println("random seed: " + randomSeed)
    random.setSeed(randomSeed)
    
    var emptyImage = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB)
    
    val root = RuleParser.parse("rules/foo.ctx")
    
    val primQueue = new ArrayBlockingQueue[Primitive](1000)
    val frame = new JFrame("contextual")
    val canvas = new ImageCanvas(emptyImage)
    frame.add(canvas)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setSize(1200, 800)
    frame.setLocationRelativeTo(null)
    frame.setVisible(true)
    
    new Thread(new Painter(canvas, primQueue)).start
  
    val workQueue = new LinkedBlockingQueue[WorkItem] 
    workQueue.put(WorkItem(root, new DrawState, 0))

    new Thread(new PrimitiveGenerator(workQueue, primQueue, random)).start
    new Thread(new PrimitiveGenerator(workQueue, primQueue, random)).start
    new Thread(new PrimitiveGenerator(workQueue, primQueue, random)).start
  }
}

case class WorkItem(rule: Rule, state: DrawState, depth: Int)

class PrimitiveGenerator(workQueue: BlockingQueue[WorkItem],
                         resultQueue: BlockingQueue[Primitive],
                         random: Random) extends Runnable {
  var maxDepth = 500

  def run() {
    while (true) {
      val WorkItem(rule, st, d) = workQueue.take
      process(rule, st, d)
    }
  }

  private def process(rule: Rule, st: DrawState, d: Int) {
    rule match {
    case r: TransformRule =>
      process(r.rule, r.transform(st), d)
    case r: RandomRule =>
      process(r.rule(random), st, d)
    case r: CompoundRule =>
      if (r.size == 1) {
        process(r.rules(0), st, d)
      } else if (d < maxDepth) {
        r.foreach { c => workQueue.put(WorkItem(c, st, d + 1)) }
      }
    case r: PrimitiveRule =>
      resultQueue.put(new Primitive(r.shape, st.coordinateTransform, st.color))
    }
  }
}

class Painter(canvas: ImageCanvas, queue: BlockingQueue[Primitive]) extends Runnable {
  val image = canvas.image
  val g = image.createGraphics
  g.translate(image.getWidth / 2, image.getHeight / 2)
  g.scale(1, -1)
  g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
  
  def run {
    while (true) {
      val prim = queue.take()
      
      image.synchronized {
        prim.paint(g)
        canvas.repaint()
      }
    }
  }
}

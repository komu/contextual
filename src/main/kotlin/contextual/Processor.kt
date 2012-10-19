package contextual

import java.awt.Shape
import java.util.Random
import java.util.concurrent.BlockingQueue
import com.apple.jobjc.Function
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.ArrayBlockingQueue

class Processor(val threads: Int = 3, val maxDepth: Int = 500) {

    private val resultQueue = ArrayBlockingQueue<Primitive>(1000)
    private val workerScheduler = Executors.newFixedThreadPool(threads)!!
    private val random = Random()

    fun addResult(shape: Shape, state: DrawState) {
        resultQueue.put(Primitive(shape, state.coordinateTransform, state.color.toColor()))
    }

    fun takeResult(): Primitive =
        resultQueue.take()!!

    fun addWorkItem(rule: Rule, state: DrawState, depth: Int) {
        workerScheduler.execute(runnable { rule.process(this, state, depth) })
    }

    fun randomInt(n: Int) =
        random.nextInt(n)
}

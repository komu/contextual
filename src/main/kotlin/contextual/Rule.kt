package contextual

import java.util.ArrayList
import java.util.Random
import java.awt.Shape
import java.awt.geom.Rectangle2D
import java.awt.geom.Ellipse2D

public abstract class Rule { }

class TransformRule(val rule: Rule) : Rule() {
    val ops: MutableList<(DrawState) -> Unit> = arrayList<(DrawState) -> Unit>()

    fun translate(x: Double, y: Double) = reg { it.translate(x, y) }
    fun scale(r: Double)                = reg { it.scale(r) }
    fun scale(x: Double, y: Double)     = reg { it.scale(x, y) }
    fun rotate(degrees: Double)         = reg { it.rotate(degrees) }
    fun shear(x: Double, y: Double)     = reg { it.shear(x, y) }
    fun saturation(s: Float)            = reg { it.saturation += s }
    fun brightness(b: Float)            = reg { it.brightness += b }
    fun hue(h: Int)                     = reg { it.hue += h }

    private fun reg(op: (DrawState) -> Unit) {
        ops.add(op)
    }

    fun transform(tr: DrawState): DrawState {
        val t = tr.copy()
        for (op in ops)
            op(t)
        return t
    }
}

class RandomRule : Rule() {

    class object {
        fun single(rule: Rule): RandomRule {
            val result = RandomRule()
            result.add(1, rule)
            return result
        }
    }

    val rules: MutableList<Pair<Int,Rule>> = arrayList<Pair<Int,Rule>>()

    fun add(r: Int, rule: Rule) {
        rules.add(Pair(r, rule))
    }

    fun rule(random: Random): Rule {
        val weightSum = rules.fold(0) { (x, r) -> x+r.first }

        val n = random.nextInt(weightSum)
        var sum = 0
        for ((w,r) in rules) {
          sum += w
          if (sum > n)
              return r
        }
        throw AssertionError("no rule found")
    }
}

class CompoundRule(val rules: List<Rule>) : Rule() {
    val size = rules.size
}

class PrimitiveRule(val shape: Shape) : Rule() {

    class object {
        val SQUARE = PrimitiveRule(Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0))
        val CIRCLE = PrimitiveRule(Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0))
    }
}

package contextual

import java.util.ArrayList
import java.util.Random
import java.awt.Shape
import java.awt.geom.Rectangle2D
import java.awt.geom.Ellipse2D
import java.util.Queue
import java.util.concurrent.BlockingQueue

public abstract class Rule {
    abstract fun process(ctx: Processor, state: DrawState, depth: Int)

    class object {
        fun compound(rules: List<TransformRule>): Rule =
            if (rules.size == 1)
                rules[0]
            else
                CompoundRule(rules)
    }
}

class TransformRule(val rule: Rule, val transform: (DrawState) -> DrawState) : Rule() {

    override fun process(ctx: Processor, state: DrawState, depth: Int) =
        rule.process(ctx, transform(state), depth)
}

class RandomRule : Rule() {

    val rules: MutableList<Pair<Int,Rule>> = arrayList<Pair<Int,Rule>>()
    var weightSum = 0

    override fun process(ctx: Processor, state: DrawState, depth: Int) =
        randomRule(ctx).process(ctx, state, depth)

    fun addBranch(weight: Double, rule: Rule) {
        val normalizedWeight = (weight*100).toInt()
        rules.add(Pair(normalizedWeight, rule))
        weightSum += normalizedWeight
    }

    private fun randomRule(ctx: Processor): Rule {
        if (rules.size == 1) return rules[0].second

        val n = ctx.randomInt(weightSum)
        var sum = 0
        for ((w,r) in rules) {
            sum += w
            if (sum > n)
                return r
        }
        throw AssertionError("no rule found")
    }

    class object {
        fun single(rule: Rule): RandomRule {
            val result = RandomRule()
            result.addBranch(1.0, rule)
            return result
        }
    }
}

class CompoundRule(val rules: List<TransformRule>) : Rule() {

    override fun process(ctx: Processor, state: DrawState, depth: Int) {
        if (rules.size == 1)
            rules[0].process(ctx, state, depth)
        else if (depth < ctx.maxDepth)
            for (r in rules)
                ctx.addWorkItem(r, state, depth + 1)
    }
}

class PrimitiveRule(val shape: Shape) : Rule() {

    override fun process(ctx: Processor, state: DrawState, depth: Int) =
        ctx.addResult(shape, state)

    class object {
        val SQUARE = PrimitiveRule(Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0))
        val CIRCLE = PrimitiveRule(Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0))
    }
}

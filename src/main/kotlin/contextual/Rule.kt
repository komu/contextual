package contextual

import java.awt.Shape
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.util.ArrayList

abstract class Rule {
    abstract fun process(ctx: Processor, state: DrawState, depth: Int)

    companion object {
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

    val rules: MutableList<Pair<Int,Rule>> = ArrayList()
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

    companion object {
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
            ctx.addWorkItems(rules, state, depth + 1)
    }
}

class PrimitiveRule(val shape: Shape) : Rule() {

    override fun process(ctx: Processor, state: DrawState, depth: Int) =
        ctx.addResult(shape, state)

    companion object {
        val SQUARE = PrimitiveRule(Rectangle2D.Double(-0.5, -0.5, 1.0, 1.0))
        val CIRCLE = PrimitiveRule(Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0))
    }
}

package contextual

import java.io.File
import java.lang.Character.isLetter
import java.awt.geom.AffineTransform

class RuleParser(private val input: String) {

    private var pos = 0

    class object {

        fun parse(file: String): Rule =
            parse(File(file))

        fun parse(file: File): Rule =
            buildRules(RuleParser(file.readText()).parseRuleFile())
    }

    fun parseRuleFile(): RuleFile {
        val startShape = parseStartShape()
        val rules = parseRuleDefinitions()

        return RuleFile(startShape, rules)
    }

    fun parseStartShape(): String {
        expectSymbol("startshape")
        return readSymbol()
    }

    fun parseRuleDefinitions(): List<RuleBranch> {
        val rules = listBuilder<RuleBranch>()

        while (hasMore())
            rules.add(parseRuleDefinition())

        return rules.build()
    }

    fun parseRuleDefinition(): RuleBranch {
        expectSymbol("rule")
        val name = readSymbol()
        val weight = if (nextCharIs('{')) 1.0 else parseNumber()
        val applications = parseApplications()

        return RuleBranch(name, weight, applications)
    }

    fun parseApplications(): List<RuleApplication> {
        val result = listBuilder<RuleApplication>()

        expectChar('{')
        while (!nextCharIs('}'))
            result.add(parseApplication())
        expectChar('}')

        return result.build()
    }

    fun parseApplication(): RuleApplication {
        val name = readSymbol()
        val transformations = parseTransformations()
        return RuleApplication(name, transformations)
    }

    fun parseTransformations(): (DrawState) -> DrawState {
        val transformationBuilder = TransformationBuilder()

        expectChar('{')
        while (!nextCharIs('}'))
            parseTransformation(transformationBuilder)
        expectChar('}')

        return transformationBuilder.build()
    }

    fun parseTransformation(b: TransformationBuilder) {
        val symbol = readSymbol()
        when (symbol) {
            "saturation" -> b.saturation(parseNumber().toFloat())
            "sat"        -> b.saturation(parseNumber().toFloat())
            "hue"        -> b.hue(parseNumber().toInt())
            "brightness" -> b.brightness(parseNumber().toFloat())
            "b"          -> b.brightness(parseNumber().toFloat())
            "x"          -> b.translate(parseNumber(), 0.0)
            "y"          -> b.translate(0.0, parseNumber())
            "size"       -> b.scale(parseNumber())
            "s"          -> b.scale(parseNumber())
            "rotate"     -> b.rotate(parseNumber())
            "r"          -> b.rotate(parseNumber())
            "flip"       -> b.flip(parseNumber())
            else         -> throw fail("unexpected symbol '$symbol'")
        }
    }

    fun parseNumber(): Double {
        skipWhitespace()

        if (!hasMore())
            throw fail("expected number, but got EOF")

        val token = readTokenFromAlphabet("-.0123456789")
        try {
            return token.toDouble()
        } catch (e: NumberFormatException) {
            throw fail("expected number, but got $token")
        }
    }

    fun nextCharIs(ch: Char): Boolean {
        skipWhitespace()
        return hasMore() && input[pos] == ch
    }

    fun expectChar(expected: Char) {
        skipWhitespace()

        val ch = readChar()
        if (ch != expected)
            throw fail("expected char '$expected', but got '$ch'")
    }

    fun expectSymbol(expected: String) {
        val symbol = readSymbol()
        if (expected != symbol)
            throw fail("expected symbol '$expected', but got: '$symbol'")
    }

    fun readSymbol(): String {
        skipWhitespace()

        val sb = StringBuilder()
        while (pos < input.size && (isLetter(input[pos]) || (sb.length > 0 && input[pos].isDigit())) )
            sb.append(input[pos++])

        if (sb.length != 0)
            return String(sb)
        else
            throw fail("expected symbol")
    }

    fun hasMore(): Boolean {
        skipWhitespace()
        return pos < input.size
    }

    private fun readTokenFromAlphabet(alphabet: String): String {
        val sb = StringBuilder()

        while (pos < input.size && input[pos] in alphabet)
            sb.append(readChar())

        return String(sb)
    }

    private fun readChar(): Char =
        if (pos < input.size)
            input[pos++]
        else
            throw fail("unexpected EOF")

    private fun skipWhitespace() {
        while (pos < input.size) {
            val ch = input[pos]
            if (ch == ';') {
                skipEndOfLine()
            } else if (!ch.isWhitespace())
                break

            pos++
        }
    }

    private fun skipEndOfLine() {
        while (pos < input.size && input[pos] != '\n')
            pos++
    }

    private fun fail(message: String): ParseException =
        ParseException(pos, message)
}

class ParseException(pos: Int, message: String) : RuntimeException("$pos: $message")

class RuleMap {
    private val ruleMap = hashMap<String,RandomRule>()

    fun get(name: String) =
        ruleMap[name] ?: throw IllegalArgumentException("no such rule '$name'")

    fun set(name: String, rule: RandomRule) {
        ruleMap[name] = rule
    }
}

fun buildRules(ruleFile: RuleFile): Rule {
    val rules = RuleMap()
    rules["SQUARE"] = RandomRule.single(PrimitiveRule.SQUARE)
    rules["CIRCLE"] = RandomRule.single(PrimitiveRule.CIRCLE)

    // First install empty placeholders for all the rules
    for (rule in ruleFile.rules)
        rules[rule.name] = RandomRule()

    // ...then link them to definitions
    for (r in ruleFile.rules)
        rules[r.name].addBranch(r.weight, r.buildRule(rules))

    val rootTransform = TransformationBuilder().scale(6.0).translate(0.0, -35.0).build()
    return TransformRule(rules[ruleFile.start], rootTransform)
}

class RuleFile(val start: String, val rules: List<RuleBranch>)

class RuleBranch(val name: String, val weight: Double, val applications: List<RuleApplication>) {
    fun buildRule(rules: RuleMap) =
        Rule.compound(applications.map { it.buildRule(rules) })
}

class RuleApplication(val name: String, val transformation: (DrawState) -> DrawState) {
    fun buildRule(rules: RuleMap) =
        TransformRule(rules[name], transformation)
}

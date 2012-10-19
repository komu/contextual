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
        val applications = parseList { parseApplication() }

        return RuleBranch(name, weight, applications)
    }

    fun parseApplication(): RuleApplication {
        val name = readSymbol()
        val transformations = parseList { parseTransformation() }
        return RuleApplication(name, buildTransform(transformations))
    }

    fun parseTransformation(): (TransformationBuilder) -> Unit {
        val symbol = readSymbol()
        return when (symbol) {
            "saturation" -> saturation(parseNumber())
            "sat"        -> saturation(parseNumber())
            "hue"        -> hue(parseNumber().toInt())
            "x"          -> translateX(parseNumber())
            "y"          -> translateY(parseNumber())
            "size"       -> scale(parseNumber())
            "s"          -> scale(parseNumber())
            "rotate"     -> rotate(parseNumber())
            "r"          -> rotate(parseNumber())
            "flip"       -> flip(parseNumber())
            "brightness" -> brightness(parseNumber())
            "b"          -> brightness(parseNumber())
            else         -> throw fail("unexpected symbol $symbol")
        }
    }

    fun parseList<T>(parser: () -> T): List<T> {
        val result = listBuilder<T>()

        expectChar('{')
        while (!nextCharIs('}'))
            result.add(parser())
        expectChar('}')

        return result.build()
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

    private fun String.contains(ch: Char) =
        indexOf(ch) != -1
}

class ParseException(pos: Int, message: String) : RuntimeException("$pos: $message")

fun buildRules(ruleFile: RuleFile): Rule {
    val ruleMap = hashMap<String,RandomRule>()

    fun getRule(name: String) =
        ruleMap[name] ?: throw Exception("no such rule '$name'")

    fun buildRule(applications: List<RuleApplication>) =
        CompoundRule(applications.map { r ->
            TransformRule(getRule(r.name), r.transformation)
        })

    ruleMap["SQUARE"] = RandomRule.single(PrimitiveRule.SQUARE)
    ruleMap["CIRCLE"] = RandomRule.single(PrimitiveRule.CIRCLE)

    // First install empty placeholders for all the rules
    for (rule in ruleFile.rules)
        ruleMap[rule.name] = RandomRule()

    // ...then link them to definitions
    for (r in ruleFile.rules)
        getRule(r.name).addBranch(r.weight, buildRule(r.applications))

    return TransformRule(getRule(ruleFile.start), buildTransform(arrayList<(TransformationBuilder) -> Unit>({ it.scale(6.0, 6.0) }, { it.translate(0.0, -35.0) })))
}

private fun buildTransform(ops: List<(TransformationBuilder) -> Unit>): (DrawState) -> DrawState {
    val tx = TransformationBuilder()
    for (op in ops)
        op(tx)
    return tx.build()
}

class RuleFile(val start: String, val rules: List<RuleBranch>)

class RuleBranch(val name: String, val weight: Double, val applications: List<RuleApplication>)

class RuleApplication(val name: String, val transformation: (DrawState) -> DrawState)

fun saturation(s: Double)         = { (r: TransformationBuilder) -> r.saturation += s.toFloat() }
fun brightness(b: Double)         = { (r: TransformationBuilder) -> r.brightness += b.toFloat() }
fun hue(h: Int)                   = { (r: TransformationBuilder) -> r.hue += h }
fun translateX(dx: Double)        = { (r: TransformationBuilder) -> r.translate(dx, 0.0) }
fun translateY(dy: Double)        = { (r: TransformationBuilder) -> r.translate(0.0, dy) }
fun rotate(a: Double)             = { (r: TransformationBuilder) -> r.rotate(a) }
fun scale(s: Double)              = { (r: TransformationBuilder) -> r.scale(s) }
fun scale(sx: Double, sy: Double) = { (r: TransformationBuilder) -> r.scale(sx, sy) }
fun flip(a: Double)               = if (a == 90.0) scale(-1.0, 1.0) else throw UnsupportedOperationException("flip is supported only for 90 degrees")

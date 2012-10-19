package contextual

object RuleParser {

    fun parse(file: String): Rule {
        val ruleFile = RuleFile("SEED1", arrayList(seed1(), seed2(), seed3()))
        return buildRules(ruleFile)
    }

    fun seed1(): RuleDef =
        RuleDef("SEED1", 1.0, arrayList(Repl("SQUARE", arrayList(saturation(0.6), hue(120))),
                                        Repl("SEED1", arrayList(translateY(1.2), scale(0.99), rotate(1.5)))))

    fun seed2(): RuleDef =
        RuleDef("SEED1", 0.05, arrayList(Repl("SEED1", arrayList(flip(90)))))

    fun seed3(): RuleDef {
        val a = Repl("SEED1", arrayList(rotate(-5.0), brightness(0.01)))
        val b = Repl("SEED1", arrayList(translateY(1.0), translateX(-0.5), scale(0.7), rotate(30.0), flip(90), brightness(0.1)))
        val c = Repl("SEED1", arrayList(translateY(1.0), translateX(0.5), scale(0.7), rotate(-30.0), flip(90), brightness(0.05)))
        return RuleDef("SEED1", 0.05, arrayList(a, b, c))
    }

    fun builtin(): Rule {
        val rule = RandomRule()

        rule.add(100, createSeed1(rule))
        rule.add(5, createSeed2(rule))
        rule.add(5, createSeed3(rule))

        return rule
    }

    fun createSeed1(self: Rule): Rule {
        val square = TransformRule(PrimitiveRule.SQUARE)
        square.saturation(0.6.toFloat())
        square.hue(120)

        val next = TransformRule(self)
        next.translate(0.0, 1.2)
        next.scale(0.99)
        next.rotate(1.5)

        return CompoundRule(arrayList(square, next))
    }

    fun createSeed2(self: Rule): Rule {
        //SEED1 { flip 90 }

        val rule = TransformRule(self)
        rule.scale(-1.0, 1.0) // flip 90
        return rule
    }

    fun createSeed3(self: Rule): Rule {
        val rule1 = TransformRule(self)
        rule1.rotate(-5.0)
        rule1.brightness(0.01.toFloat())

        val rule2 = TransformRule(self)
        rule2.translate(-0.5, 1.0)
        rule2.scale(0.7)
        rule2.rotate(30.0)
        rule2.scale(-1.0, 1.0) // flip 90
        rule2.brightness(0.1.toFloat())

        val rule3 = TransformRule(self)
        rule3.translate(-0.5, 1.0)
        rule3.scale(0.7)
        rule3.rotate(-30.0)
        rule3.scale(-1.0, 1.0) // flip 90
        rule3.brightness(0.05.toFloat())

        return CompoundRule(arrayList(rule1, rule2, rule3))
    }
}

    /*
  import AST._

  lexical.delimiters ++= List("{", "}", ".", "-")
  lexical.reserved ++= List("startshape", "rule", "saturation", "sat", "hue", "x", "y",
                            "size", "rotate", "flip", "brightness",
                            "r", "b", "s")

  def rulefile: Parser[RuleFile] = startshape ~ rep1(rule) ^^
    { case start ~ rules => RuleFile(start, rules) }

  def startshape: Parser[String] = "startshape" ~> ident

  def rule: Parser[RuleDef] =
    "rule" ~> ident ~ opt(floatingLit) ~ ("{" ~> rep(replacement) <~ "}") ^^
    { case name ~ weight ~ repls => RuleDef(name, weight.getOrElse(1), repls) }

  def replacement: Parser[Repl] =
    ruleref ~ ("{" ~> rep(shapeRepl) <~ "}") ^^
    { case (s ~ rs) => Repl(s,rs) }

  def shapeRepl: Parser[ShapeRepl] =
    saturation | hue | x | y | size | rotate | flip | brightness

  def hue: Parser[Hue]               = "hue"                  ~> integerLit  ^^ Hue
  def saturation: Parser[Saturation] = ("sat" | "saturation") ~> floatingLit ^^ Saturation
  def brightness: Parser[Brightness] = ("b" | "brightness")   ~> floatingLit ^^ Brightness
  def x: Parser[TranslateX]          = "x"                    ~> floatingLit ^^ TranslateX
  def y: Parser[TranslateY]          = "y"                    ~> floatingLit ^^ TranslateY
  def flip: Parser[Flip]             = "flip"                 ~> integerLit  ^^ Flip
  def size: Parser[Scale]            = ("s" | "size")         ~> floatingLit ^^ (s => Scale(s,s))
  def rotate: Parser[Rotate]         = ("r" | "rotate")       ~> floatingLit ^^ Rotate

  def ruleref: Parser[String] = ident

  def integerLit: Parser[Int] = numericLit ^^ (_.toInt)
  def floatingLit: Parser[Double] =
    (opt("-") ~ numericLit ~ opt("." ~> opt(numericLit))) ^^
    { case (sign ~ x ~ rest) =>
        val r = rest.flatMap(x => x).getOrElse("0")
        (sign.getOrElse("+") + x + "." + r).toDouble
    }

  def parse(file: String) = {
    val code = Source.fromFile(file).mkString
    val tokens = new lexical.Scanner(code)
    phrase(rulefile)(tokens) match {
      case Success(r,_) => buildRules(r)
      case NoSuccess(e,_) => throw new Exception(e.toString)
    }
  }
*/

fun buildRules(ruleFile: RuleFile): Rule {
    val ruleMap = hashMap<String,RandomRule>()

    fun getRule(name: String) =
        ruleMap[name] ?: throw Exception("no such rule '$name'")

    fun buildRule(repls: List<Repl>): Rule =
        CompoundRule(repls.map { r ->
            val rule = getRule(r.name)
            val child = TransformRule(rule)
            for (repl in r.shapeRepls)
                repl(child)
            child
        })

    ruleMap["SQUARE"] = RandomRule.single(PrimitiveRule.SQUARE)
    ruleMap["CIRCLE"] = RandomRule.single(PrimitiveRule.CIRCLE)

    // First install empty placeholders for all the rules
    for (rule in ruleFile.rules)
        ruleMap[rule.name] = RandomRule()

    // ...then link them to definitions
    for (r in ruleFile.rules)
        getRule(r.name).add((r.weight * 100).toInt(), buildRule(r.repls))

    val root = TransformRule(getRule(ruleFile.start))
    //root.scale(200.0, 200.0) // easterbox
    root.scale(6.0, 6.0)
    root.translate(0.0, -35.0)
    return root
}

data class RuleFile(val start: String, val rules: List<RuleDef>)

data class RuleDef(val name: String, val weight: Double, val repls: List<Repl>)

data class Repl(val name: String, val shapeRepls: List<(TransformRule) -> Unit>) {}

fun saturation(val s: Double)  = { (r: TransformRule) -> r.saturation(s.toFloat()) }
fun brightness(val b: Double)  = { (r: TransformRule) -> r.brightness(b.toFloat()) }
fun hue(val h: Int)            = { (r: TransformRule) -> r.hue(h) }
fun translateX(val dx: Double) = { (r: TransformRule) -> r.translate(dx, 0.0) }
fun translateY(val dy: Double) = { (r: TransformRule) -> r.translate(0.0, dy) }
fun rotate(val a: Double)      = { (r: TransformRule) -> r.rotate(a) }
fun scale(val s: Double)       = { (r: TransformRule) -> r.scale(s) }
fun scale(val sx: Double, val sy: Double) = { (r: TransformRule) -> r.scale(sx, sy) }
fun flip(val a: Int)           = { (r: TransformRule) -> if (a == 90) r.scale(-1.0, 1.0) else throw UnsupportedOperationException("flip is supported only for 90 degrees") }

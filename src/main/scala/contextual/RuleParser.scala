package contextual

import scala.io.Source
import scala.collection.mutable
import scala.util.parsing.combinator.syntactical._

object RuleParser extends StandardTokenParsers {
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
  
  def buildRules(rulefile: RuleFile): Rule = {
    val ruleMap = mutable.Map[String,RandomRule]()

    def buildRule(repls: List[Repl]): Rule =
      new CompoundRule(repls.map { r =>
        val child = new TransformRule(ruleMap(r.name))
        r.shapeRepls.foreach(applyRepl(child, _))
        child
      })

    ruleMap("SQUARE") = new RandomRule {
      this += (1, PrimitiveRule.Square)
    }
    ruleMap("CIRCLE") = new RandomRule {
      this += (1, PrimitiveRule.Circle)
    }
    
    for (rule <- rulefile.rules)
      ruleMap += (rule.name -> new RandomRule)
    
    for (r <- rulefile.rules) {
      val rule = ruleMap(r.name)
      val prob = (r.weight * 100).toInt
      rule += (prob, buildRule(r.repls))
    }
    
    val tr = new TransformRule(ruleMap(rulefile.start))
    //tr.scale(200, 200) // easterbox
    tr.scale(6, 6)
    tr.translate(0, -35)
    tr
  }
  
  def applyRepl(rule: TransformRule, repl: ShapeRepl) = repl match {
    case Saturation(s) => rule.saturation(s.toFloat)
    case Brightness(b) => rule.brightness(b.toFloat)
    case Hue(h)        => rule.hue(h)
    case TranslateX(x) => rule.translate(x, 0)
    case TranslateY(y) => rule.translate(0, y)
    case Scale(x, y)   => rule.scale(x, y)
    case Rotate(a)     => rule.rotate(a)
    case Flip(90)      => rule.scale(-1, 1)
    case Flip(_)       => throw new UnsupportedOperationException("flip")
  }
}

object AST {
  case class RuleFile(start: String, rules: List[RuleDef])
  
  case class RuleDef(name: String, weight: Double, repls: List[Repl])
  
  case class Repl(name: String, shapeRepls: List[ShapeRepl])
  
  sealed abstract class ShapeRepl
  case class Saturation(s: Double)       extends ShapeRepl
  case class Brightness(b: Double)       extends ShapeRepl
  case class Hue(h: Int)                 extends ShapeRepl
  case class TranslateX(x: Double)       extends ShapeRepl
  case class TranslateY(y: Double)       extends ShapeRepl
  case class Flip(a: Int)                extends ShapeRepl
  case class Scale(x: Double, y: Double) extends ShapeRepl
  case class Rotate(a: Double)           extends ShapeRepl
}

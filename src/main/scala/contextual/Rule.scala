package contextual

import java.awt.Shape
import java.awt.geom.{ Ellipse2D, Rectangle2D }
import java.util.Random
import scala.collection.mutable.ArrayBuffer

sealed abstract class Rule

class TransformRule(val rule: Rule) extends Rule {
  val ops = new ArrayBuffer[DrawState => Unit]

  def translate(x: Double, y: Double) = reg(_.translate(x, y))
  def scale(r: Double)                = reg(_.scale(r))
  def scale(x: Double, y: Double)     = reg(_.scale(x, y))
  def rotate(degrees: Double)         = reg(_.rotate(degrees))
  def shear(x: Double, y: Double)     = reg(_.shear(x, y))
  def saturation(s: Float)            = reg(_.saturation += s)
  def brightness(b: Float)            = reg(_.brightness += b)
  def hue(h: Int)                     = reg(_.hue += h)
  
  private def reg(op: DrawState => Unit) = ops += op

  def transform(tr: DrawState) = {
    val t = tr.copy
    ops.foreach(_(t))
    t
  }
}

class RandomRule extends Rule {
  val rules = new ArrayBuffer[(Int,Rule)]

  def += (r: (Int,Rule)) = rules += r
  
  def rule(random: Random): Rule = {
    val weightSum = rules.foldLeft(0) { (x,y) => x + y._1 }
    val n = random.nextInt(weightSum)
    var sum = 0
    for ((w,r) <- rules) {
      sum += w
      if (sum > n)
        return r
    }
    throw new AssertionError("no rule found")
  }
}

class CompoundRule(val rules: List[Rule]) extends Rule {
  def foreach(thunk: Rule => Unit) = rules.foreach(thunk)
  def size = rules.size
}

class PrimitiveRule(val shape: Shape) extends Rule

object PrimitiveRule {
  object Square extends PrimitiveRule(new Rectangle2D.Double(-0.5, -0.5, 1, 1))
  object Circle extends PrimitiveRule(new Ellipse2D.Double(-0.5, -0.5, 1, 1))
}

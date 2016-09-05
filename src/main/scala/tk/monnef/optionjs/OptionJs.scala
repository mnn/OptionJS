package tk.monnef.optionjs

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object OptionJsInit extends JSApp {
  @JSExport override def main(): Unit = {
    js.Dynamic.global.Option = OptionJs.apply[Any] _
  }
}

object OptionJs {
  def apply[T](value: T): OptionJs[T] = {
    getNormalizedValue[T](value) match {
      case null => NoneJs[T](value)
      case _ => SomeJs[T](value)
    }
  }

  def getNormalizedValue[T](value: T): T =
    if (js.isUndefined(value) || value == null) null.asInstanceOf[T]
    else value
}

abstract class OptionJs[+A] {

  @JSExport val value: A

  @JSExport def get: A = value

  @JSExport def map[B](fn: js.Function1[A, B]): OptionJs[B]

  @JSExport def getOrElse[B >: A](default: B): B = if (isEmpty) default else get

  @JSExport def orElse[B >: A](other: OptionJs[B]): OptionJs[B]

  @JSExport def orNull[B >: A](implicit ev: Null <:< B): B = this getOrElse ev(null)

  @JSExport def isEmpty: Boolean

  @JSExport def isDefined: Boolean = !isEmpty

  @JSExport def nonEmpty: Boolean = isDefined

  @JSExport def count(p: (A) => Boolean): Int = filter(p).size

  @JSExport def size: Int = if (isEmpty) 0 else 1

  @JSExport def filter(p: (A) => Boolean): OptionJs[A]

  @JSExport def filterNot(p: (A) => Boolean): OptionJs[A] = filter(x => !p(x))

  @JSExport def find(p: (A) => Boolean): OptionJs[A] = filter(p)

  @JSExport def exists(p: (A) => Boolean): Boolean = filter(p).isDefined

  @JSExport def forall(p: (A) => Boolean): Boolean = exists(p)

  @JSExport def flatten[B](implicit ev: A <:< OptionJs[B]): OptionJs[B] = ev(get)

  @JSExport def flatMap[B](f: (A) => OptionJs[B]): OptionJs[B] = map(f).flatten

  @JSExport def toArray[B >: A]: js.Array[B]
}

@JSExport("Some") case class SomeJs[A](rawValue: A) extends OptionJs[A] {

  import OptionJs._

  val value = getNormalizedValue(rawValue)

  override def map[B](fn: js.Function1[A, B]): OptionJs[B] = SomeJs[B](fn(get))

  override def orElse[B >: A](other: OptionJs[B]): OptionJs[B] = this

  override def isEmpty: Boolean = false

  override def filter(p: (A) => Boolean): OptionJs[A] = if (p(value)) this else NoneJs(value)

  override def toArray[B >: A]: js.Array[B] = js.Array(value)
}

@JSExport("None") case class NoneJs[A](noneValue: Any) extends OptionJs[A] {
  val value = null.asInstanceOf[A]

  override def map[B](fn: js.Function1[A, B]): OptionJs[B] = NoneJs(noneValue)

  override def orElse[B >: A](other: OptionJs[B]): OptionJs[B] = other

  override def isEmpty: Boolean = true

  override def filter(p: (A) => Boolean): OptionJs[A] = this

  override def toArray[B >: A]: js.Array[B] = js.Array()
}

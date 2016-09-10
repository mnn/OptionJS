package tk.monnef.optionjs

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.{JSApp, JavaScriptException}
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

  def getNormalizedValue[T](value: T): T = {
    val isNaN = value.isInstanceOf[Double] && value.asInstanceOf[Double].isNaN
    if (js.isUndefined(value) || value == null || isNaN) null.asInstanceOf[T]
    else value
  }
}

@JSExport("NoneError") class NoneError extends JavaScriptException(js.Error("get cannot be called on an instance of None."))

@JSExport("FlattenError") class FlattenError(msg: String) extends JavaScriptException(js.Error(msg))

abstract class OptionJs[+A] {

  @JSExport val value: A

  @JSExport def get: A = value

  @JSExport def map[B](fn: js.Function1[A, B]): OptionJs[B]

  @JSExport def foreach(fn: js.Function1[A, Unit]): Unit

  @JSExport def forEach(fn: js.Function1[A, Unit]): Unit = foreach(fn)

  @JSExport def getOrElse[B >: A](default: B): B = if (isEmpty) default else get

  @JSExport def orElse[B >: A](other: OptionJs[B]): OptionJs[B]

  @JSExport def orNull: A

  @JSExport def isEmpty: Boolean

  @JSExport def isDefined: Boolean = !isEmpty

  @JSExport def nonEmpty: Boolean = isDefined

  @JSExport def count(p: js.Function1[A, Boolean]): Int = filter(p).size

  @JSExport def size: Int = if (isEmpty) 0 else 1

  @JSExport def filter(p: js.Function1[A, Boolean]): OptionJs[A]

  @JSExport def filterNot(p: js.Function1[A, Boolean]): OptionJs[A] = filter((x: A) => !p(x))

  @JSExport def find(p: js.Function1[A, Boolean]): OptionJs[A] = filter(p)

  @JSExport def exists(p: js.Function1[A, Boolean]): Boolean = filter(p).isDefined

  @JSExport def forall(p: js.Function1[A, Boolean]): Boolean = exists(p)

  //  @JSExport def flatten[B](implicit ev: A <:< OptionJs[B]): OptionJs[B] = ev(get)
  @JSExport def flatten[B]: OptionJs[B]

  @JSExport def flatMap[B](f: js.Function1[A, OptionJs[B]]): OptionJs[B] = map(f).flatten

  @JSExport def toArray[B >: A](): js.Array[B]

  @JSExport def toObject(fieldName: String = "value"): js.Dynamic

  @JSExport def mapIf[B >: A](cond: js.Function1[A, Boolean], fn: js.Function1[A, B]): OptionJs[B]

  @JSExport def mapIf[B >: A](cond: Boolean, fn: js.Function1[A, B]): OptionJs[B]

  @JSExport def `match`[B >: A, C](some: js.Function2[B, SomeJs[B], C], none: js.Function1[NoneJs[B], C]): C

  @JSExport def contains[B >: A](other: B): Boolean
}

@JSExport("Some") case class SomeJs[+A](rawValue: A) extends OptionJs[A] {

  import OptionJs._

  val value = getNormalizedValue(rawValue)

  override def map[B](fn: js.Function1[A, B]): OptionJs[B] = SomeJs[B](fn(value))

  override def foreach(fn: js.Function1[A, Unit]): Unit = fn(value)

  override def orElse[B >: A](other: OptionJs[B]): OptionJs[B] = this

  override def isEmpty: Boolean = false

  override def filter(p: js.Function1[A, Boolean]): OptionJs[A] = if (p(value)) this else NoneJs(value)

  override def toArray[B >: A](): js.Array[B] = js.Array(value)

  override def toObject(fieldName: String): js.Dynamic = js.Dynamic.literal((fieldName, value.asInstanceOf[js.Any]))

  override def mapIf[B >: A](cond: js.Function1[A, Boolean], fn: js.Function1[A, B]): OptionJs[B] = mapIf(cond(value), fn)

  override def mapIf[B >: A](cond: Boolean, fn: js.Function1[A, B]): OptionJs[B] = if (cond) map(fn) else this

  override def `match`[B >: A, C](some: js.Function2[B, SomeJs[B], C], none: js.Function1[NoneJs[B], C]): C = some(value, this)

  override def contains[B >: A](other: B): Boolean = other == value

  override def orNull: A = value

  @JSExport def flatten[B]: OptionJs[B] =
    if (value.isInstanceOf[OptionJs[_]]) value.asInstanceOf[OptionJs[B]]
    else throw new FlattenError("Cannot flatten Some without inner Option.")
}

@JSExport("None") case class NoneJs[+A](noneValue: Any) extends OptionJs[A] {
  val value = null.asInstanceOf[A]

  override def map[B](fn: js.Function1[A, B]): OptionJs[B] = NoneJs(noneValue)

  override def foreach(fn: js.Function1[A, Unit]): Unit = ()

  override def orElse[B >: A](other: OptionJs[B]): OptionJs[B] = other

  override def isEmpty: Boolean = true

  override def filter(p: js.Function1[A, Boolean]): OptionJs[A] = this

  override def toArray[B >: A](): js.Array[B] = js.Array()

  override def toObject(fieldName: String): js.Dynamic = js.Dynamic.literal()

  override def mapIf[B >: A](cond: js.Function1[A, Boolean], fn: js.Function1[A, B]): OptionJs[B] = this

  override def mapIf[B >: A](cond: Boolean, fn: js.Function1[A, B]): OptionJs[B] = this

  override def `match`[B >: A, C](some: js.Function2[B, SomeJs[B], C], none: js.Function1[NoneJs[B], C]): C = none(this)

  override def contains[B >: A](other: B): Boolean = false

  override def get: A = throw new NoneError()

  override def orNull: A = null.asInstanceOf[A]

  @JSExport def flatten[B]: OptionJs[B] = throw new FlattenError("Cannot flatten None.")
}

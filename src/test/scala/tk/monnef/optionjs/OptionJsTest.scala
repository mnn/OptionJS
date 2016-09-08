package tk.monnef.optionjs

import utest._

import scala.scalajs.js
import scala.scalajs.js._
import scala.scalajs.js.JSConverters._

object OptionJsTest extends TestSuite {
  val tests = this {
    "normalized value" - {
      def fn[T >: Null](x: T) = OptionJs.getNormalizedValue[T](x)
      assert(
        fn(js.undefined) == null,
        fn(null) == null,
        fn(Double.NaN) == null,
        fn("xx") == "xx",
        fn(1) == 1
      )
    }

    "picking proper class" - {
      assert(
        OptionJs("cool") == SomeJs("cool"),
        OptionJs(js.undefined).isInstanceOf[NoneJs[_]],
        OptionJs(js.undefined) == NoneJs(js.undefined),
        OptionJs(null) == NoneJs(null)
      )
    }

    "mapp" - {
      assert(
        OptionJs(4).map((a: Int) => a + "x").value == "4x",
        OptionJs(4).map((a: Int) => a * 2).value == 8,
        SomeJs(4).map((a: Int) => a * 2).get == 8
      )
    }

    "foreach" - {
      var calledWith = -1
      OptionJs(4).foreach((x: Int) => calledWith = x)
      assert(calledWith == 4)

      calledWith = -1
      OptionJs[Int](null.asInstanceOf[Int]).foreach((x: Int) => calledWith = x)
      assert(calledWith == -1) // not called
    }

    "getOrElse" - {
      assert(
        OptionJs(99).getOrElse(1) == 99,
        OptionJs(null.asInstanceOf[Int]).getOrElse(1) == 1,
        SomeJs(5).getOrElse(null.asInstanceOf[Int]) == 5,
        NoneJs(null).getOrElse(4) == 4
      )
    }

    "orElse" - {
      assert(
        OptionJs(5).orElse(OptionJs(4)).get == 5,
        OptionJs(null.asInstanceOf[Int]).orElse(OptionJs(4)).get == 4
      )
    }

    "orNull" - {
      assert(
        OptionJs("").orNull == "",
        OptionJs(1.asInstanceOf[java.lang.Integer]).orNull == 1,
        OptionJs(js.undefined).orNull == null
      )
    }

    "isEmpty" - {
      assert(
        OptionJs(js.undefined).isEmpty,
        OptionJs(null).isEmpty,
        !OptionJs("").isEmpty,
        !OptionJs(1).isEmpty
      )
    }

    "isDefined" - {
      assert(
        OptionJs("").isDefined,
        !OptionJs(null).isDefined
      )
    }

    "nonEmpty" - {
      assert(
        OptionJs("").nonEmpty,
        !OptionJs(null).nonEmpty
      )
    }

    "count" - {
      assert(
        OptionJs("").count((x: String) => x == "") == 1,
        OptionJs(null).count((x: Null) => true) == 0
      )
    }

    "size" - {
      assert(
        OptionJs("").size == 1,
        OptionJs(null).size == 0
      )
    }

    "filter" - {
      assert(
        OptionJs(2).filter((x: Int) => x > 0) == SomeJs(2),
        OptionJs(2).filter((x: Int) => x > 4) == NoneJs(2),
        OptionJs(null).filter((x: Null) => true) == NoneJs(null)
      )
    }

    "filterNot" - {
      assert(
        OptionJs(2).filterNot((x: Int) => x > 0) == NoneJs(2),
        OptionJs(2).filterNot((x: Int) => x > 4) == SomeJs(2),
        OptionJs(null).filterNot((x: Null) => false) == NoneJs(null)
      )
    }

    "find" - {
      assert(
        OptionJs(2).find((x: Int) => x > 0) == SomeJs(2),
        OptionJs(2).find((x: Int) => x > 4) == NoneJs(2),
        OptionJs(null).find((x: Null) => true) == NoneJs(null)
      )
    }

    "exists" - {
      assert(
        OptionJs(2).exists((x: Int) => x > 0),
        !OptionJs(2).exists((x: Int) => x > 4),
        !OptionJs(null).exists((x: Null) => true)
      )
    }

    "forall" - {
      assert(
        OptionJs(2).forall((x: Int) => x > 0),
        !OptionJs(2).forall((x: Int) => x > 4),
        !OptionJs(null).forall((x: Null) => true)
      )
    }

    "flatten" - {
      assert(
        OptionJs(OptionJs("a")).flatten == SomeJs("a"),
        OptionJs(OptionJs(null)).flatten == NoneJs(null)
      )
    }

    "flatMap" - {
      assert(
        OptionJs("a").flatMap(x => SomeJs(x * 2)) == SomeJs("aa")
      )
    }

    "mapIf" - {
      assert(
        OptionJs(5).mapIf((x: Int) => x > 1, (x: Int) => x + 1).get == 6,
        OptionJs(5).mapIf((x: Int) => x > 10, (x: Int) => x + 1).get == 5,
        OptionJs[Null](null).mapIf((_: Null) => true, (_: Null) => 5).orNull == null,
        OptionJs(5).mapIf(true, (x: Int) => x + 1).get == 6,
        OptionJs(5).mapIf(false, (x: Int) => x + 1).get == 5
      )
    }

    "match" - {
      var someCalled, noneCalled = false
      var params = Array[scala.Any]()
      var ret: String = null
      val someHandler: js.Function2[Int, SomeJs[Int], String] = (v: Int, o: SomeJs[Int]) => {
        params = Array(v, o)
        someCalled = true
        v.toString
      }: String
      val noneHandler: js.Function1[NoneJs[Int], String] = (o: NoneJs[Int]) => {
        params = Array(o)
        noneCalled = true
        Option(o.noneValue).map(_.toString).getOrElse("null")
      }: String
      def reset() = {
        someCalled = false
        noneCalled = false
        params = Array()
        var ret = null
      }
      def check(isSome: Boolean, expectedParams: Array[scala.Any], expectedReturnValue: scala.Any) {
        if (isSome) assert(someCalled && !noneCalled)
        else assert(!someCalled && noneCalled)
        assert(params.sameElements(expectedParams))
        assert()
      }

      ret = OptionJs(2).`match`(someHandler, noneHandler)
      check(true, Array(2, SomeJs(2)), "2")
      reset()

      ret = OptionJs(10).`match`(someHandler, noneHandler)
      check(true, Array(10, SomeJs(10)), "10")
      reset()

      ret = OptionJs[Int](null.asInstanceOf[Int]).`match`(someHandler, noneHandler)
      check(false, Array(NoneJs(null)), "null")
      reset()
    }

    "contains" - {
      assert(
        OptionJs("a").contains("a"),
        !OptionJs("a").contains("b"),
        OptionJs(1).contains(1),
        !OptionJs(null).contains(null),
        SomeJs(null).contains(null),
        !NoneJs(null).contains(null)
      )
    }

    "toArray" - {
      assert(
        OptionJs("a").toArray().toArray.sameElements(Array("a")),
        OptionJs[Null](null).toArray().toArray.sameElements(Array[Null]())
      )
    }

    "toObject" - {
      assert(
        OptionJs("a").toObject().selectDynamic("value").asInstanceOf[String] == "a",
        OptionJs("a").toObject("moo").selectDynamic("moo").asInstanceOf[String] == "a",
        js.isUndefined(OptionJs[Null](null).toObject().selectDynamic("value"))
      )
    }
  }
}

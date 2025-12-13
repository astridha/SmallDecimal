@file:Suppress("ClassName")

package io.github.astridha.decimal

import io.github.astridha.decimal.Decimal.Companion.getRoundingMode
import kotlin.test.Test
import kotlin.test.assertEquals

// Tests according to:
// https://developer.android.com/reference/java/math/RoundingMode

class Rounding_UP_Tests {

    @Test
    fun decimalRoundingTests_UP() {
        Decimal.setRoundingMode(Decimal.RoundingMode.UP)
        //Decimal.setPrecision(0)
        assertEquals(
            "6",
            "5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  5.5"
        )
        assertEquals(
            "3",
            "2.5".Dc.setScale(0).toPlainString(),
            "H${getRoundingMode()} round(0)  2.5"
        )
        assertEquals(
            "2",
            "1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.6"
        )
        assertEquals(
            "2",
            "1.1".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.1"
        )
        assertEquals(
            "1",
            "1.0".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.0"
        )
        assertEquals(
            "-1",
            "-1.0".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.0"
        )
        assertEquals(
            "-2",
            "-1.1".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.1"
        )
        assertEquals(
            "-2",
            "-1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.6"
        )
        assertEquals(
            "-3",
            "-2.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -2.5"
        )
        assertEquals(
            "-6",
            "-5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -5.5"
        )
    }
}

class Rounding_DOWN_Tests {

    @Test
    fun decimalRoundingTests_HALF_DOWN() {
        Decimal.setRoundingMode(Decimal.RoundingMode.DOWN)
        //Decimal.setPrecision(0)
        assertEquals(
            "5",
            "5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  5.5"
        )
        assertEquals(
            "2",
            "2.5".Dc.setScale(0).toPlainString(),
            "H${getRoundingMode()} round(0)  2.5"
        )
        assertEquals(
            "1",
            "1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.6"
        )
        assertEquals(
            "1",
            "1.1".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.1"
        )
        assertEquals(
            "1",
            "1.0".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.0"
        )
        assertEquals(
            "-1",
            "-1.0".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.0"
        )
        assertEquals(
            "-1",
            "-1.1".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.1"
        )
        assertEquals(
            "-1",
            "-1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.6"
        )
        assertEquals(
            "-2",
            "-2.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -2.5"
        )
        assertEquals(
            "-5",
            "-5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -5.5"
        )
    }
}

class Rounding_CEILING_Tests {

    @Test
    fun decimalRoundingTests_CEILING() {
        Decimal.setRoundingMode(Decimal.RoundingMode.CEILING)
        //Decimal.setPrecision(0)
        assertEquals(
            "6",
            "5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  5.5"
        )
        assertEquals(
            "3",
            "2.5".Dc.setScale(0).toPlainString(),
            "H${getRoundingMode()} round(0)  2.5"
        )
        assertEquals(
            "2",
            "1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.6"
        )
        assertEquals(
            "2",
            "1.1".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.1"
        )
        assertEquals(
            "1",
            "1.0".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.0"
        )
        assertEquals(
            "-1",
            "-1.0".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.0"
        )
        assertEquals(
            "-1",
            "-1.1".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.1"
        )
        assertEquals(
            "-1",
            "-1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.6"
        )
        assertEquals(
            "-2",
            "-2.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -2.5"
        )
        assertEquals(
            "-5",
            "-5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -5.5"
        )
    }
}

class Rounding_FLOOR_Tests {

    @Test
    fun decimalRoundingTests_FLOOR() {
        Decimal.setRoundingMode(Decimal.RoundingMode.FLOOR)
        //Decimal.setPrecision(0)
        assertEquals(
            "5",
            "5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  5.5"
        )
        assertEquals(
            "2",
            "2.5".Dc.setScale(0).toPlainString(),
            "H${getRoundingMode()} round(0)  2.5"
        )
        assertEquals(
            "1",
            "1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.6"
        )
        assertEquals(
            "1",
            "1.1".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.1"
        )
        assertEquals(
            "1",
            "1.0".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.0"
        )
        assertEquals(
            "-1",
            "-1.0".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.0"
        )
        assertEquals(
            "-2",
            "-1.1".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.1"
        )
        assertEquals(
            "-2",
            "-1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.6"
        )
        assertEquals(
            "-3",
            "-2.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -2.5"
        )
        assertEquals(
            "-6",
            "-5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -5.5"
        )
    }
}

class Rounding_HALF_UP_Tests {

    @Test
    fun decimalRoundingTests_HALF_UP() {
        Decimal.setRoundingMode(Decimal.RoundingMode.HALF_UP)
        //Decimal.setPrecision(0)
        assertEquals(
            "6",
            "5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  5.5"
        )
        assertEquals(
            "3",
            "2.5".Dc.setScale(0).toPlainString(),
            "H${getRoundingMode()} round(0)  2.5"
        )
        assertEquals(
            "2",
            "1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.6"
        )
        assertEquals(
            "1",
            "1.1".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.1"
        )
        assertEquals(
            "1",
            "1.0".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.0"
        )
        assertEquals(
            "-1",
            "-1.0".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.0"
        )
        assertEquals(
            "-1",
            "-1.1".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.1"
        )
        assertEquals(
            "-2",
            "-1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.6"
        )
        assertEquals(
            "-3",
            "-2.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -2.5"
        )
        assertEquals(
            "-6",
            "-5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -5.5"
        )
    }
}

class Rounding_HALF_DOWN_Tests {

    @Test
    fun decimalRoundingTests_HALF_DOWN() {
        Decimal.setRoundingMode(Decimal.RoundingMode.HALF_DOWN)
        //Decimal.setPrecision(0)
        assertEquals(
            "5",
            "5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  5.5"
        )
        assertEquals(
            "2",
            "2.5".Dc.setScale(0).toPlainString(),
            "H${getRoundingMode()} round(0)  2.5"
        )
        assertEquals(
            "2",
            "1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.6"
        )
        assertEquals(
            "1",
            "1.1".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.1"
        )
        assertEquals(
            "1",
            "1.0".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.0"
        )
        assertEquals(
            "-1",
            "-1.0".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.0"
        )
        assertEquals(
            "-1",
            "-1.1".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.1"
        )
        assertEquals(
            "-2",
            "-1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.6"
        )
        assertEquals(
            "-2",
            "-2.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -2.5"
        )
        assertEquals(
            "-5",
            "-5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -5.5"
        )
    }
}



class Rounding_HALF_EVEN_Tests {

    @Test
    fun decimalRoundingTests_HALF_EVEN() {
        Decimal.setRoundingMode(Decimal.RoundingMode.HALF_EVEN)
        //Decimal.setPrecision(0)
        assertEquals(
            "6",
            "5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  5.5"
        )
        assertEquals(
            "2",
            "2.5".Dc.setScale(0).toPlainString(),
            "H${getRoundingMode()} round(0)  2.5"
        )
        assertEquals(
            "2",
            "1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.6"
        )
        assertEquals(
            "1",
            "1.1".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.1"
        )
        assertEquals(
            "1",
            "1.0".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  1.0"
        )
        assertEquals(
            "-1",
            "-1.0".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.0"
        )
        assertEquals(
            "-1",
            "-1.1".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.1"
        )
        assertEquals(
            "-2",
            "-1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -1.6"
        )
        assertEquals(
            "-2",
            "-2.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -2.5"
        )
        assertEquals(
            "-6",
            "-5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} round(0)  -5.5"
        )
    }
}



class OtherRounding_xUP_Tests {

    @Test
    fun decimalRoundingTests_UP() {
        Decimal.setRoundingMode(Decimal.RoundingMode.UP)
        //Decimal.setPrecision(0)
        assertEquals(
            "5600",
            "5555.12".Dc.setScale(-2).toPlainString(),
            "${getRoundingMode()} round(-2)  5555.12 => 5600"
        )
        assertEquals(
            "5100",
            "5000.02".Dc.setScale(-2).toPlainString(),
            "{Decimal.getRoundingMode()} round(-2)  5000.02 => 5100"
        )
        assertEquals(
            "5000",
            "5000.00".Dc.setScale(-2).toPlainString(),
            "{Decimal.getRoundingMode()} round(-2)  5000.00 => 5000"
        )
        assertEquals(
            "6",
            "5.5".Dc.setScale(0).toPlainString(),
            "{Decimal.getRoundingMode()} operator 5.5=> 6"
        )
        assertEquals(
            "-5600",
            "-5555.12".Dc.setScale(-2).toPlainString(),
            "${getRoundingMode()} round(-2)  -5555.12 => -5600"
        )
        assertEquals(
            "-5100",
            "-5000.02".Dc.setScale(-2).toPlainString(),
            "${getRoundingMode()} round(-2)  -5000.02 => -5100"
        )
        assertEquals(
            "-5000",
            "-5000.00".Dc.setScale(-2).toPlainString(),
            "${getRoundingMode()} round(-2)  -5000.00 => -5000"
        )
        assertEquals(
            "-6",
            "-5.5".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} operator -5.5=> -6"
        )
        assertEquals(
            "3",
            "2.5".Dc.setScale(0).toPlainString(),
            "{$Decimal.getRoundingMode()} operator 2.5 => 3"
        )
        assertEquals(
            "2",
            "1.6".Dc.setScale(0).toPlainString(),
            "${getRoundingMode()} operator 1.6 => 2"
        )
        assertEquals(
            "2",
            "1.1".Dc.setScale(0, Decimal.RoundingMode.UP).toPlainString(),
            "${getRoundingMode()} operator 1.1 => 2, ${getRoundingMode()}"
        )
        assertEquals(
            "1",
            "1.0".Dc.setScale(0).toPlainString(),
            "{Decimal.getRoundingMode()} operator 1.0 => 1, ${getRoundingMode()}"
        )
    }
}

class ArithmeticRoundingTest {

    @Test
    fun opRoundingTests() {
        //var d: Decimal("13.7777")
        assertEquals(
            "13.778",
            "13.7777".Dc.setScale(3).toPlainString(),
            "operator 13.7777.Dc.setScale(3)"
        )

        /*


        d=13.7777.Dc.roundHalfEven()
        assertEquals(
            "13.8",
            d.roundHalfEven(1).toPlainString(),
            "operator roundingHalfEven(13.7777.Dc)"
        )

        d=12.7777.Dc
        assertEquals(
            "12.8",
            d.roundHalfEven(1).toPlainString(),
            "operator roundingHalfEven(12.7777.Dc)"
        )

        d=12.7777.Dc
        assertEquals(
            "12",
            d.roundHalfEven(0).toPlainString(),
            "operator roundingHalfEven(12.7777.Dc)"
        )

        d=13.7777.Dc
        assertEquals(
            "14",
            d.roundHalfEven(0).toPlainString(),
            "operator roundingHalfEven(12.7777.Dc)"
        )

         */
    }

    /*******    other arbitrary tests     *****/

    class otherRounding_HALF_EVEN_Tests {

        @Test
        fun otherDecimalRoundingTests_HALF_EVEN() {
            Decimal.setRoundingMode(Decimal.RoundingMode.HALF_EVEN)
            //Decimal.setPrecision(0)
            assertEquals(
                "56",
                "55.50".Dc.setScale(0).toPlainString(),
                "HALF_EVEN round(0)  55.50 => 56"
            )
            assertEquals(
                "54",
                "54.50".Dc.setScale(0).toPlainString(),
                "HALF_EVEN round(0)  54.50 => 54"
            )
                assertEquals(
                "106",
                "106.5".Dc.setScale(0).toPlainString(),
                "HALF_EVEN round(0)  106.5 => 106"
            )
            assertEquals(
                "-106",
                "-106.5".Dc.setScale(0).toPlainString(),
                "HALF_EVEN round(0)  -106.5 => -106"
            )
            assertEquals(
                "13.778",
                13.7777.Dc.setScale(3).toPlainString(),
                "operator roundingHalfEven(13.7777.Dc)"
            )
        }
    }



}
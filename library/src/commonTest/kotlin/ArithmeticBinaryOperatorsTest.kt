package io.github.astridha.smalldecimal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class ArithmeticBinaryOperatorsTest {

    @Test fun opPlusTests() {
        assertEquals(
            "15",
            (10.Dc + 5.Dc).toString(),
            "operator (10.Dc + 5.Dc)"
        )
        assertEquals(
            "15.7",
            (10.4.Dc + 5.3.Dc).toString(),
            "operator (10.4.Dc + 5.3.Dc)"
        )
        assertEquals(
            "15.78",
            (10.4.Dc + 5.38.Dc).toString(),
            "operator (10.4.Dc + 5.38.Dc)"
        )
        assertEquals(
            "0.03",
            (0.01 + 0.02).toString(),
            "Double (0.01 + 0.02)"
        )
        assertEquals(
            "0.03",
            (0.01.Dc + 0.02.Dc).toString(),
            "operator (0.01Dc + 0.02.Dc)"
        )
        assertEquals(  // this is (Int plus Decimal)!
            "1.03",
            (1 + 0.03.Dc).toString(),
            "operator (1 + 0.03.Dc)"
        )
        assertEquals(  // this is (Int plus Decimal)!
            1.03.Dc,
            1 + 0.03.Dc,
            "plain (1 + 0.03.Dc)"
        )
        /*
        assertEquals(  // this is Overflow!
            576460752303423488.Dc,
            Decimal.MAX_VALUE + 1L.Dc,
            "plain (${Decimal.MAX_VALUE} + 1L.Dc)"
        )*/

        assertFailsWith(
            ArithmeticException::class,
            "plain (${Decimal.MAX_VALUE} + 1L.Dc)",
            {Decimal.MAX_VALUE + 1L.Dc}
        )
    }

    @Test fun opMinusTests() {
        assertEquals(
            "6",
            (11.Dc - 5.Dc).toString(),
            "operator (11.Dc - 5.Dc): 6"
        )
        assertEquals(
            "5.1",
            (10.4.Dc - 5.3.Dc).toString(),
            "operator (10.4.Dc - 5.3.Dc)"
        )
        assertEquals(
            "5.02",
            (10.4.Dc - 5.38.Dc).toString(),
            "operator (10.4.Dc - 5.38.Dc)"
        )
        assertEquals(
            "-5.02",
            (5.38.Dc - 10.4.Dc).toString(),
            "operator (5.38.Dc - 10.4.Dc)"
        )
    }

    @Test fun opTimesTests() {
        assertEquals(
            "12",
            (3.Dc * 4.Dc).toString(),
            "operator (3.Dc * 4.Dc)"
        )
        assertEquals(
            "14",
            (3.5.Dc * 4.Dc).toString(),
            "operator (3.5.Dc * 4.Dc)"
        )
        assertEquals(
            "2.26",
            (0.5.Dc * 4.52.Dc).toString(),
            "operator (0,5.Dc * 4.52.Dc)"
        )
        assertEquals(
            "2.6",
            (1.3.Dc * 2).toString(),
            "operator with Int (1.3.Dc * 2)"
        )
        val mydeci = 1.3.Dc * 2
        assertEquals(
            "2.6",
            mydeci.toString(),
            "operator with mydeci = (1.3.Dc * 2)"
        )
        assertEquals(
            "-18199999992.72",
            (1999999999.2.Dc * (-9.1).Dc).toString(),
            "operator with (19999999992..Dc * (-9.1).Dc)"
        )
        /*
        assertEquals(
            "2.6",
            (1999999999.2.Dc * (-99999999999.1).Dc).toRawDecimalString(),
            "operator with (1999999999.2.Dc * (-99999999999.1).Dc)"
        )
        */
        assertFailsWith(
            ArithmeticException::class,
            "operator with (1999999999.2.Dc * (-99999999999.1).Dc)",
            {(1999999999.2.Dc * (-99999999999.1).Dc).toString()}
        )
        /*
        assertEquals(  // this is Overflow!
            576460752303423487.Dc,
            Decimal.MAX_VALUE * 1.1.Dc,
            "plain (${Decimal.MAX_VALUE} * 1.1.Dc)"
        )
        assertEquals(  // this is Overflow!
            576460752303423487.Dc,
            (Decimal.MAX_VALUE+1) * 1.1.Dc,
            "plain (${Decimal.MAX_VALUE} * 1.1.Dc)"
        )
         */

        assertFailsWith(
            ArithmeticException::class,
            "plain (${Decimal.MAX_VALUE} * 1.1.Dc)",
            {Decimal.MAX_VALUE * 1.1.Dc}
        )
    }

    @Test fun opDivTests() {
        assertEquals(
            "4",
            (12.Dc / 3.Dc).toString(),
            "operator (12.Dc / 3.Dc)"
        )
        assertEquals(
            "4",
            (12.Dc / 3.Dc).toString(),
            "operator (12.Dc / 3.Dc)"
        )
        assertEquals(
            "0.2",
            (0.8.Dc / 4.Dc).toString(),
            "operator ((0.8).Dc / 4.Dc)"
        )

        assertEquals(
            "3.428571428571429",
            (12.Dc / 3.5.Dc).toString(),
            "operator (12.Dc / 3.5.Dc)"
        )

        assertEquals(
            "-0.2",
            ((-0.8).Dc / 4.Dc).toString(),
            "operator ((-0.8).Dc / 4.Dc)"
        )
        assertEquals(
            "0.2",
            ((-0.8).Dc / (-4).Dc).toString(),
            "operator ((-0.8).Dc / (-4).Dc)"
        )
        assertEquals(
            "0.177777777777778",
            ((0.8).Dc / (4.5).Dc).toString(),
            "operator ((0.8).Dc / (4.5).Dc)"
        )
        assertEquals(
            "0.177777777777778",
            ((-0.8).Dc / (-4.5).Dc).toString(),
            "operator ((-0.8).Dc / (-4.5).Dc)"
        )
    }

    @Test fun opRemTests() {
        assertEquals(
            "0",
            (12.Dc % 3.Dc).toString(),
            "operator (12.Dc % 3.Dc)"
        )
        assertEquals(
            "1",
            (13.Dc % 3.Dc).toString(),
            "operator (13.Dc % 3.Dc)"
        )
        assertEquals(
            "1.5",
            (12.Dc % 3.5.Dc).toString(),
            "operator (12.Dc % 3.5.Dc)"
        )

        assertEquals(
            "2",
            (5.Dc % 3.Dc).toString(),
            "operator (5.Dc % 3.Dc)"
        )
        assertEquals(
            "2",
            (5.Dc % (-3).Dc).toString(),
            "operator (5.Dc % (-3).Dc)"
        )
        assertEquals(
            "-2",
            ((-5).Dc % 3.Dc).toString(),
            "operator ((-5).Dc % 3.Dc)"
        )
        assertEquals(
            "-2",
            ((-5).Dc % (-3).Dc).toString(),
            "operator ((-5).Dc % (-3).Dc)"
        )
    }

    @Test fun opModTests() {

        assertEquals(
            "3",
            ((-1).Dc mod 4.Dc).toString(),
            "operator ((-1).Dc mod 4.Dc)"
        )

        assertEquals(
            "2",
            (5.Dc mod 3.Dc).toString(),
            "operator (5.Dc mod 3.Dc)"
        )
        assertEquals(
            "-1",
            (5.Dc mod (-3).Dc).toString(),
            "operator (5.Dc mod (-3).Dc)"
        )
        assertEquals(
            "1",
            ((-5).Dc mod 3.Dc).toString(),
            "operator ((-5).Dc mod 3.Dc)"
        )
        assertEquals(
            "-2",
            ((-5).Dc mod (-3).Dc).toString(),
            "operator ((-5).Dc mod (-3).Dc)"
        )
    }


}
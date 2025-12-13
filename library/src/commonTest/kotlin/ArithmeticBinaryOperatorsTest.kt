package io.github.astridha.decimal

import kotlin.test.Test
import kotlin.test.assertEquals


class ArithmeticBinaryOperatorsTest {

    @Test
    fun opPlusTests() {
        assertEquals(
            "15",
            (10.Dc + 5.Dc).toPlainString(),
            "operator (10.Dc + 5.Dc)"
        )
        assertEquals(
            "15.7",
            (10.4.Dc + 5.3.Dc).toPlainString(),
            "operator (10.4.Dc + 5.3.Dc)"
        )
        assertEquals(
            "15.78",
            (10.4.Dc + 5.38.Dc).toPlainString(),
            "operator (10.4.Dc + 5.38.Dc)"
        )
        assertEquals(
            "0.03",
            (0.01 + 0.02).toString(),
            "Double (0.01 + 0.02)"
        )
        assertEquals(
            "0.03",
            (0.01.Dc + 0.02.Dc).toPlainString(),
            "operator (0.01Dc + 0.02.Dc)"
        )
        assertEquals(  // this is (Int plus Decimal)!
            "1.03",
            (1 + 0.03.Dc).toPlainString(),
            "operator (1 + 0.03.Dc)"
        )
        assertEquals(  // this is (Int plus Decimal)!
            1.03.Dc,
            1 + 0.03.Dc,
            "plain (1 + 0.03.Dc)"
        )
    }

    @Test
    fun opMinusTests() {
        assertEquals(
            "6",
            (11.Dc - 5.Dc).toPlainString(),
            "operator (11.Dc - 5.Dc): 6"
        )
        assertEquals(
            "5.1",
            (10.4.Dc - 5.3.Dc).toPlainString(),
            "operator (10.4.Dc - 5.3.Dc)"
        )
        assertEquals(
            "5.02",
            (10.4.Dc - 5.38.Dc).toPlainString(),
            "operator (10.4.Dc - 5.38.Dc)"
        )
    }

    @Test
    fun opTimesTests() {
        assertEquals(
            "12",
            (3.Dc * 4.Dc).toPlainString(),
            "operator (3.Dc * 4.Dc)"
        )
        assertEquals(
            "14",
            (3.5.Dc * 4.Dc).toPlainString(),
            "operator (3.5.Dc * 4.Dc)"
        )
        assertEquals(
            "2.26",
            (0.5.Dc * 4.52.Dc).toPlainString(),
            "operator (0,5.Dc * 4.52.Dc)"
        )
        assertEquals(
            "2.6",
            (1.3.Dc * 2).toPlainString(),
            "operator with Int (1.3.Dc * 2)"
        )
        val mydeci = 1.3.Dc * 2
        assertEquals(
            "2.6",
            mydeci.toPlainString(),
            "operator with mydeci = (1.3.Dc * 2)"
        )
    }

    @Test
    fun opDivTests() {
        assertEquals(
            "4",
            (12.Dc / 3.Dc).toPlainString(),
            "operator (12.Dc / 3.Dc)"
        )
        assertEquals(
            "4",
            (12.Dc / 3.Dc).toPlainString(),
            "operator (12.Dc / 3.Dc)"
        )
        assertEquals(
            "0.2",
            (0.8.Dc / 4.Dc).toPlainString(),
            "operator ((0.8).Dc / 4.Dc)"
        )

        assertEquals(
            "3.428571428571429",
            (12.Dc / 3.5.Dc).toPlainString(),
            "operator (12.Dc / 3.5.Dc)"
        )

        assertEquals(
            "-0.2",
            ((-0.8).Dc / 4.Dc).toPlainString(),
            "operator ((-0.8).Dc / 4.Dc)"
        )
        assertEquals(
            "0.2",
            ((-0.8).Dc / (-4).Dc).toPlainString(),
            "operator ((-0.8).Dc / (-4).Dc)"
        )
        assertEquals(
            "0.177777777777778",
            ((0.8).Dc / (4.5).Dc).toPlainString(),
            "operator ((0.8).Dc / (4.5).Dc)"
        )
        assertEquals(
            "0.177777777777778",
            ((-0.8).Dc / (-4.5).Dc).toPlainString(),
            "operator ((-0.8).Dc / (-4.5).Dc)"
        )
    }

    @Test
    fun opRemTests() {
        assertEquals(
            "0",
            (12.Dc % 3.Dc).toPlainString(),
            "operator (12.Dc % 3.Dc)"
        )
        assertEquals(
            "1",
            (13.Dc % 3.Dc).toPlainString(),
            "operator (13.Dc % 3.Dc)"
        )
        assertEquals(
            "1.5",
            (12.Dc % 3.5.Dc).toPlainString(),
            "operator (12.Dc % 3.5.Dc)"
        )
    }
}
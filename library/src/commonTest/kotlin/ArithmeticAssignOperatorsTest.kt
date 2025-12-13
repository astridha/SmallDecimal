package io.github.astridha.decimal

import io.github.astridha.decimal.*
import kotlin.test.Test
import kotlin.test.assertEquals


class ArithmeticAssignOperatorsTest {

    @Test
    fun opPlusAssignTests() {
        var d: Decimal
        d=12.Dc
        d += 3.Dc
        assertEquals(
            "15",
            d.toPlainString(),
            "operator (12.Dc += 3.Dc)"
        )
        d=12.Dc
        d += 3
        assertEquals(
            "15",
            d.toPlainString(),
            "operator (12.Dc += 3)"
        )
        d=12.468.Dc
        d += 3
        assertEquals(
            "15.468",
            d.toPlainString(),
            "operator (12.468Dc += 3)"
        )
        d=12.468.Dc
        d += 3.1111
        assertEquals(
            "15.5791",
            d.toPlainString(),
            "operator (12.468Dc += 3.1111)"
        )
    }

    @Test
    fun opMinusAssignTests() {
        var d: Decimal
        d=12.Dc
        d -= 3.Dc
        assertEquals(
            "9",
            d.toPlainString(),
            "operator (12.Dc -= 3.Dc)"
        )
        d=12.Dc
        d -= 3
        assertEquals(
            "9",
            d.toPlainString(),
            "operator (12.Dc -= 3)"
        )
    }


    @Test
    fun opTimesAssignTests() {
        var d: Decimal
        d=12.Dc
        d *= 3.Dc
        assertEquals(
            "36",
            d.toPlainString(),
            "operator (12.Dc *= 3.Dc)"
        )
        d=12.Dc
        d *= 3
        assertEquals(
            "36",
            d.toPlainString(),
            "operator (12.Dc *= 3)"
        )
    }

    @Test
    fun opDivAssignTests() {
        var d: Decimal
        d=12.Dc
        d /= 3.Dc
        assertEquals(
            "4",
            d.toPlainString(),
            "operator (12.Dc /= 3.Dc)"
        )
        d=12.Dc
        d /= 3
        assertEquals(
            "4",
            d.toPlainString(),
            "operator (12.Dc /= 3)"
        )
    }


    @Test
    fun opRemAssignTests() {
        var d: Decimal
        d=12.Dc
        d %= 3.Dc
        assertEquals(
            "0",
            d.toPlainString(),
            "operator (12.Dc %= 3)"
        )
        d=12.Dc
        d %= 3
        assertEquals(
            "0",
            d.toPlainString(),
            "operator (12.Dc %= 3)"
        )
    }
}
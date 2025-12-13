package io.github.astridha.decimal

import kotlin.test.Test
import kotlin.test.assertEquals


class UnaryOperatorsTest {

    @Test
    fun opPlusPlusTests() {
        var d = Decimal(11)
        assertEquals(
            Decimal(11),
            d++,
            "op Dc++" // ++ inc happens after evaluation
        )
        assertEquals(
            Decimal(13),
            ++d,
            "op ++Dc" // ++ happens before evaluation
        )
        d = 12.777.Dc
        assertEquals(
            Decimal(13.777),
            ++d,
            " op 12.777.Dc, ++d" // ++ happens before evaluation
        )
    }

    @Test
    fun opMinusMinusTests() {
        var d = Decimal(11)
        assertEquals(
            Decimal(11),
            d--,
            "op Dc--" // -- dec happens before evaluation
        )
        assertEquals(
            Decimal(9),
            --d,
            "op --Dc"
        )
        d = 12.777.Dc
        assertEquals(
            Decimal(11.777),
            --d,
            " op 12.777.Dc, --d" // -- dec happens before evaluation
        )
    }


    @Test
    fun absTests() {
        val d = Decimal(-11)
        assertEquals(
            Decimal(11),
            abs(d),
            "abs(-11.Dc)"
        )
        assertEquals(
            1.23456789.Dc,
            abs((-1.23456789).Dc),
            "abs(-1.23456789.Dc)"
        )
        assertEquals(
            1.23456789.Dc,
            abs((-1.23456789).Dc),
            "abs(-1.23456789.Dc)"
        )
        assertEquals(
            1.23456789.Dc,
            abs("-1.23456789".Dc),
            "abs(''-1.23456789'')"
        )

    }



}
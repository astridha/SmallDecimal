package io.github.astridha.smalldecimal

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
    @Test fun numDecimalPlacesTests() {
        val d = Decimal(-11)
        assertEquals(
            3,
            Decimal(1.234).numDecimalPlaces,
            "abs(-11.Dc)"
        )
        assertEquals(
            5,
            Decimal(1.23445).numDecimalPlaces,
            "abs(-11.Dc)"
        )
        assertEquals(
            0,
            Decimal(1.234).round().numDecimalPlaces,
            "abs(-11.Dc)"
        )
        assertEquals(
            0,
            Decimal(1).round(5).numDecimalPlaces,
            "abs(-11.Dc)"
        )

    }

    @Test fun signTests() {
        val d = Decimal(-11)
        assertEquals(
            1.toDecimal(),
            Decimal(1.234).sign,
            "abs(-11.Dc)"
        )
        assertEquals(
            1.2345.Dc,
            Decimal(1.234).sign,
            "abs(-11.Dc)"
        )
        assertEquals(
            -1.toDecimal(),
            Decimal(-4.5678).sign,
            "abs(-11.Dc)"
        )
        assertEquals(
            0.toDecimal(),
            Decimal(1.0).sign,
            "abs(-11.Dc)"
        )
    }



    }
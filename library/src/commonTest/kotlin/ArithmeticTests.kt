package io.github.astridha.smalldecimal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class ArithmeticTests {

    @Test fun arithmeticPlusTests() {
        assertEquals(
            "15.112",
            10.Dc.plus(5.11111.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.UP)).toString(),
            "operator (10.Dc.plus 5.11111.Dc, 3, Decimal.RoundingMode.UP)"
        )
        assertEquals(
            "15.111",
            10.Dc.plus(5.11111.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.DOWN)).toString(),
            "operator (10.Dc.plus 5.11111.Dc, 3, Decimal.RoundingMode.DOWN)"
        )
        assertEquals(
            "15.111",
            10.Dc.plus(5.11111.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.HALF_EVEN)).toString(),
            "operator (10.Dc.plus 5.11111.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
    }

    @Test fun arithmeticMinusTests() {
        assertEquals(
            "4.001",
            10.Dc.minus(5.99999.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.UP)).toString(),
            "operator (10.Dc.minus 5.99999.Dc, 3, Decimal.RoundingMode.UP)"
        )
        assertEquals(
            "4",
            10.Dc.minus(5.99999.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.DOWN)).toString(),
            "operator (10.Dc.minus 5.99999.Dc, 3, Decimal.RoundingMode.DOWN)"
        )
        assertEquals(
            "4",
            10.Dc.minus(5.99999.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.HALF_EVEN)).toString(),
            "operator (10.Dc.minus 5.99999.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
    }

    @Test fun arithmeticTimesTests() {
        assertEquals(
            "60",
            10.Dc.times(5.99999.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.UP)).toString(),
            "operator (10.Dc.times 5.99999.Dc, 3, Decimal.RoundingMode.UP)"
        )
        assertEquals(
            "59.999",
            10.Dc.times(5.99999.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.DOWN)).toString(),
            "operator (10.Dc.times 5.99999.Dc, 3, Decimal.RoundingMode.DOWN)"
        )
        assertEquals(
            "60",
            10.Dc.times(5.99999.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.HALF_EVEN)).toString(),
            "operator (10.Dc.times 5.99999.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
        assertEquals(
            "10",
            10.Dc.times(0.99999.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.HALF_EVEN)).toString(),
            "operator (10.Dc.times 0.99999.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
        assertEquals(
            "-10",
            10.Dc.times((-0.99999).Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.HALF_EVEN)).toString(),
            "operator (10.Dc.times -0.99999.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
        assertEquals(
            "-9.999",
            10.Dc.times((-0.99999).Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.DOWN)).toString(),
            "operator (10.Dc.times -0.99999.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
    }

    @Test fun arithmeticDivTests() {
        assertEquals(
            "1.667",
            10.Dc.div(5.99999.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.UP)).toString(),
            "operator (10.Dc.div 5.99999.Dc, 3, Decimal.RoundingMode.UP)"
        )
        assertEquals(
            "1.666",
            10.Dc.div(5.99999.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.DOWN)).toString(),
            "operator (10.Dc.div 5.99999.Dc, 3, Decimal.RoundingMode.DOWN)"
        )
        assertEquals(
            "1.667",
            10.Dc.div(5.99999.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.HALF_EVEN)).toString(),
            "operator (10.Dc.div 5.99999.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
        assertEquals(
            "10",
            10.Dc.div(0.99999.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.HALF_EVEN)).toString(),
            "operator (10.Dc.times 0.99999.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
        assertEquals(
            "-10",
            10.Dc.div((-0.99999).Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.HALF_EVEN)).toString(),
            "operator (10.Dc.div -0.99999.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
        assertEquals(
            "-10",
            10.Dc.div((-0.99999).Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.DOWN)).toString(),
            "operator (10.Dc.div -0.99999.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
        assertEquals(
            "-1",
            0.3.Dc.div((-0.3).Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.DOWN)).toString(),
            "operator (0.3.Dc.div -0.3.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
    }
    @Test fun arithmeticModTests() {

        Decimal.setRoundingConfig(Decimal.RoundingConfig(15,Decimal.RoundingMode.UP))
        assertEquals(
            "4.00001",
            10.Dc.mod(5.99999.Dc).toString(),
            "operator (10.Dc.mod 5.99999.Dc, 15, Decimal.RoundingMode.UP) with autoRoundingMode"
        )

        Decimal.setRoundingConfig(Decimal.RoundingConfig(2,Decimal.RoundingMode.UP))
        assertEquals(
            "4",
            10.Dc.mod(5.99999.Dc).toString(),
            "operator (10.Dc.mod 5.99999.Dc, 2, Decimal.RoundingMode.UP) with AutoRoundingMode"
        )
        assertEquals(
            "4.00001",
            Decimal(10.0, true).mod(Decimal(5.99999, true), Decimal.RoundingConfig(15, Decimal.RoundingMode.UP)).toString(),
            "operator (10.Dc.mod 5.99999.Dc, 3, Decimal.RoundingMode.UP) with omitRounding and RoundingConfig Parameter"
        )
        assertEquals(
            "4",
            10.Dc.mod(5.99999.Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.DOWN)).toString(),
            "operator (10.Dc.mod 5.99999.Dc, 3, Decimal.RoundingMode.DOWN)"
        )

        Decimal.setRoundingConfig(Decimal.noRoundingConfig)
        assertEquals(
            "4.00001",
            10.Dc.mod(5.99999.Dc, Decimal.RoundingConfig(10, Decimal.RoundingMode.HALF_EVEN)).toString(),
            "operator (10.Dc.mod 5.99999.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
        assertEquals(
            "0.0001",
            10.Dc.mod(0.99999.Dc, Decimal.RoundingConfig(10, Decimal.RoundingMode.HALF_EVEN)).toString(),
            "operator (10.Dc.times 0.99999.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
        assertEquals(
            "-1",
            10.Dc.mod((-0.99999).Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.HALF_EVEN)).toString(),
            "operator (10.Dc.mod -0.99999.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
        assertEquals(
            "-0.999",
            10.Dc.mod((-0.99999).Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.DOWN)).toString(),
            "operator (10.Dc.mod -0.99999.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
        assertEquals(
            "0",
            0.3.Dc.mod((-0.3).Dc, Decimal.RoundingConfig(3, Decimal.RoundingMode.DOWN)).toString(),
            "operator (0.3.Dc.mod -0.3.Dc, 3, Decimal.RoundingMode.HALF_EVEN)"
        )
    }

}

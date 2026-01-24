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

}

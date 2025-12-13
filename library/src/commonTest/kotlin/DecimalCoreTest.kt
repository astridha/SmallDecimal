package io.github.astridha.decimal

import kotlin.test.Test
import kotlin.test.assertEquals

class DecimalCoreTest {

    @Test fun intConstructorTests() {
        assertEquals(
            "15",
            Decimal(15L).toPlainString(),
            "intConstructor: 15L"
        )
        assertEquals(
            "16",
            (16).Dc.toPlainString(),
            "Int.Dc Constructor: 16"
        )
        assertEquals(
            "17.5",
            (17.5).Dc.toPlainString(),
            "Double.Dc Constructor: 17.5"
        )
        Decimal.setMinDecimals(2)
        assertEquals(
            "18.5001",
            "18.5001".Dc.toString(),
            "String.Dc Constructor: 18.5001"
        )
        Decimal.setMinDecimals(0) // default
        assertEquals(
            "18500",
            "18500.000".Dc.toString(),
            "String.Dc Constructor: 18500.000"
        )
        Decimal.setPrecision(2)
        assertEquals(
            "18.5",
            "18.5001".Dc.toString(),
            "String.Dc Constructor: 18.5001"
        )
        Decimal.setPrecision(0)
        assertEquals(
            "19",
            "18.5001".Dc.toString(),
            "String.Dc Constructor: 18.5001"
        )
        Decimal.setPrecision(15) // default
    }

    @Test fun doubleConstructorTests() {

        Decimal.setPrecision(15) // default
        assertEquals(
            "100000.47",
            Decimal(100000.47).toPlainString(),
            "DoubleConstructor: 100000.47"
        )
        assertEquals(
            "15.00000001",
            Decimal(15.00000001).toPlainString(),
            "DoubleConstructor: 15"
        )
        assertEquals(
            "15.000000000001",
            Decimal(15.000000000001).toPlainString(),
            "DoubleConstructor: 15 (d=12)"
        )
        assertEquals(
            "15.0000000000001",
            Decimal(15.0000000000001).toPlainString(),
            "DoubleConstructor: 15 (d=13)"
        )
        assertEquals(
            "15.00000000000006",
            15.00000000000006.toString(),
            "15 Double (d=14) toString()"
        )
        assertEquals(
            "15.00000000000001",
            Decimal(15.00000000000001).toPlainString(),
            "DoubleConstructor: 15 (d=14)"
        )
        Decimal.setPrecision(5)
        assertEquals(
            "15.000001",
            15.000001.toString(),
            "15 Double (p=6) toString()"
        )
        assertEquals(
            "15",
            15.000001.Dc.toString(),  // 6 places when precision is 5!
            "15.000001 Double (d=6, p=5) toString()"
        )
        assertEquals(
            "15.00001",
            15.000009.Dc.toString(),  // 6 places when precision is 5!
            "15.000009 Double (d=6, p=5) toString()"
        )
        Decimal.setPrecision(6)
        assertEquals(
            "15.000001",
            15.000001.Dc.toString(),  // 6 places when precision is 6!
            "15.000001 Double (d=6, p=6) toString()"
        )
        assertEquals(
            "15",
            Decimal(15.000000000000009).toPlainString(),
            "DoubleConstructor: 15.000000000000009 (d=15, p=6)"
        )
        Decimal.setPrecision(15)
        assertEquals(
            "15.000000000000009",
            Decimal(15.000000000000009).toPlainString(),
            "DoubleConstructor: 15.000000000000009 (d=15, p=15)"
        )
        assertEquals(
            "15",
            Decimal(15).toPlainString(),
            "intConstructor: 15"
        )
    }

    @Test fun floatConstructorTests() {

        Decimal.setPrecision(15) // default
        assertEquals(
            "100000.47",
            Decimal(100000.47F).toPlainString(),
            "floatConstructor: 10000000.47"
        )
        assertEquals(
            "10000.47",
            (10000.47F).toDecimal().toPlainString(),
            "Float.toDecimal(): 10000.47"
        )
        assertEquals(
            "15.3",
            (15.3F).toString(),
            "Float Test 15.3F"
        )
        assertEquals(
            "15.3",
            (15.3F).toDecimal().toPlainString(),
            "float.toDecimal(): 15.3F"
        )

    }

    @Test fun stringConstructorTests() {
        assertEquals(
            "123",
            Decimal("123").toPlainString(),
            "stringConstructor: 123"
            )
        assertEquals(
            "123000",
            Decimal("123000").toPlainString(),
            "stringConstructor: 123000"
        )
        assertEquals(
            "123",
            Decimal("123.000").toPlainString(),
            "stringConstructor: 123.000"
        )
        assertEquals(
            "123.4",
            Decimal("123.4").toPlainString(),
            "stringConstructor: 123.4"
        )
        assertEquals(
            "-123.004",
            Decimal("-123.004").toPlainString(),
            "stringConstructor: -123.004"
        )
        assertEquals(
            "1.234",
            Decimal("1.234E0").toPlainString(),
            "stringConstructor: 1.234E0"
        )
        assertEquals(
            "123.4",
            Decimal("1.234E2").toPlainString(),
            "stringConstructor: 1.234E2"
        )
        assertEquals(
            "-123.4",
            Decimal("-1.234E2").toPlainString(),
            "stringConstructor: -1.234E2"
        )
        assertEquals(
            "0.01234",
            Decimal("1.234E-2").toPlainString(),
            "stringConstructor: 1.234E-2"
        )

    }

    @Test fun toPlainStringTests() {
        assertEquals(
            "123",
            Decimal(123L, 0).toPlainString(),
            "toPlainString: +mantissa 123L, 0 places 0"
        )
        assertEquals(
            "1.24",
            Decimal(124L, 2).toPlainString(),
            "toPlainString: +mantissa, 124L +places 2"
        )
        assertEquals(
            "12500",
            Decimal(125L, -2).toPlainString(),
            "toPlainString: +mantissa 125L, -places -2"
        )
        assertEquals(
            "-125",
            Decimal(-125L, 0).toPlainString(),
            "toPlainString: -mantissa -125L, 0 places"
        )
        assertEquals(
            "-1.25",
            Decimal(-125L, +2).toPlainString(),
            "toPlainString: -mantissa -125L, +places +2"
        )
        assertEquals(
            "12500",
            //Decimal(-125L, -2).toPlainString(),
            12500F.Dc.toPlainString(),
            "toPlainString: -mantissa -125L, -places -2"
        )
    }

    @Test fun toScientificStringTests() {
        assertEquals(
            "1.23E2",
            Decimal(123L, 0).toScientificString(),
            "toScientific: +mantissa, 0 places"
        )
        assertEquals(
            "1.24E-8",
            Decimal(124L, 10).toScientificString(),
            "toScientific: +mantissa, +places"
        )
        assertEquals(
            "1.25E4",
            Decimal(125L, -2).toScientificString(),
            "toScientific: +mantissa 125L, -places -2"
        )
        assertEquals(
            "-1.25E2",
            Decimal(-125L, 0).toScientificString(),
            "toScientific: -mantissa, 0places, '1.25E2'"
        )
        assertEquals(
            "-1.25E-8",
            Decimal(-125L, 10).toScientificString(),
            "toScientific: -mantissa, +places, '-1.25E-8'"
        )
        assertEquals(
            "-1.25E12",
            Decimal(-125L, -10).toScientificString(),
            "toScientific: -mantissa, -places, '1.25E12'"
        )

    }
}

package io.github.astridha.decimal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecimalCoreTest {

    @Test fun intConstructorTests() {
        assertEquals(
            "15",
            Decimal(15L).toRawDecimalString(),
            "intConstructor: 15L"
        )
        assertEquals(
            "16",
            (16).Dc.toRawDecimalString(),
            "Int.Dc Constructor: 16"
        )
        assertEquals(
            "17.5",
            (17.5).Dc.toRawDecimalString(),
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
        Decimal.setMaxDecimalPlaces(2)
        assertEquals(
            "18.5",
            "18.5001".Dc.toString(),
            "String.Dc Constructor: 18.5001"
        )
        Decimal.setMaxDecimalPlaces(0)
        assertEquals(
            "19",
            "18.5001".Dc.toString(),
            "String.Dc Constructor: 18.5001"
        )
        Decimal.setMaxDecimalPlaces(15) // default
    }

    @Test fun doubleConstructorTests() {

        Decimal.setMaxDecimalPlaces(15) // default
        assertEquals(
            "100000.47",
            Decimal(100000.47).toRawDecimalString(),
            "DoubleConstructor: 100000.47"
        )
        assertEquals(
            "15.00000001",
            Decimal(15.00000001).toRawDecimalString(),
            "DoubleConstructor: 15"
        )
        assertEquals(
            "15.000000000001",
            Decimal(15.000000000001).toRawDecimalString(),
            "DoubleConstructor: 15 (d=12)"
        )
        assertEquals(
            "15.0000000000001",
            Decimal(15.0000000000001).toRawDecimalString(),
            "DoubleConstructor: 15 (d=13)"
        )
        assertEquals(
            "15.00000000000006",
            15.00000000000006.toString(),
            "15 Double (d=14) toString()"
        )
        assertEquals(
            "15.00000000000001",
            Decimal(15.00000000000001).toRawDecimalString(),
            "DoubleConstructor: 15 (d=14)"
        )
        Decimal.setMaxDecimalPlaces(5)
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
        Decimal.setMaxDecimalPlaces(6)
        assertEquals(
            "15.000001",
            15.000001.Dc.toString(),  // 6 places when precision is 6!
            "15.000001 Double (d=6, p=6) toString()"
        )
        assertEquals(
            "15",
            Decimal(15.000000000000009).toRawDecimalString(),
            "DoubleConstructor: 15.000000000000009 (d=15, p=6)"
        )
        Decimal.setMaxDecimalPlaces(15)
        assertEquals(
            "15.000000000000009",
            Decimal(15.000000000000009).toRawDecimalString(),
            "DoubleConstructor: 15.000000000000009 (d=15, p=15)"
        )
        assertEquals(
            "15",
            Decimal(15).toRawDecimalString(),
            "intConstructor: 15"
        )
    }

    @Test fun floatConstructorTests() {

        Decimal.setMaxDecimalPlaces(15) // default
        assertEquals(
            "100000.47",
            Decimal(100000.47F).toRawDecimalString(),
            "floatConstructor: 10000000.47"
        )
        assertEquals(
            "10000.47",
            (10000.47F).toDecimal().toRawDecimalString(),
            "Float.toDecimal(): 10000.47"
        )
        assertEquals(
            "15.3",
            (15.3F).toString(),
            "Float Test 15.3F"
        )
        assertEquals(
            "15.3",
            (15.3F).toDecimal().toRawDecimalString(),
            "float.toDecimal(): 15.3F"
        )

    }

    @Test fun stringConstructorTests() {
        assertEquals(
            null,
            "abc".toDecimalOrNull()?.toRawDecimalString(),
            "string \"abc\".toDecimalOrNull()"
        )
        assertFailsWith(
            NumberFormatException::class,
            "string  \"abc\".toDecimal()",
            {"abc".toDecimal().toRawDecimalString()}
        )
        assertFailsWith(
            NumberFormatException::class,
            "stringConstructor: abc",
            {Decimal("abc").toRawDecimalString()}
        )
         assertEquals(
            "123",
            Decimal("123").toRawDecimalString(),
            "stringConstructor: 123"
        )
        assertEquals(
            "123000",
            Decimal("123000").toRawDecimalString(),
            "stringConstructor: 123000"
        )
        assertEquals(
            "123",
            Decimal("123.000").toRawDecimalString(),
            "stringConstructor: 123.000"
        )
        assertEquals(
            "123.4",
            Decimal("123.4").toRawDecimalString(),
            "stringConstructor: 123.4 (no rounding defined, no decimals defined)"
        )
        assertEquals(
            "-123.004",
            Decimal("-123.004").toRawDecimalString(),
            "stringConstructor: -123.004"
        )
        assertEquals(
            "1.234",
            Decimal("1.234E0").toRawDecimalString(),
            "stringConstructor: 1.234E0"
        )
        assertEquals(
            "123.4",
            Decimal("1.234E2").toRawDecimalString(),
            "stringConstructor: 1.234E2"
        )
        assertEquals(
            "-123.4",
            Decimal("-1.234E2").toRawDecimalString(),
            "stringConstructor: -1.234E2"
        )
        assertEquals(
            "0.01234",
            Decimal("1.234E-2").toRawDecimalString(),
            "stringConstructor: 1.234E-2"
        )
        assertEquals(
            "0.12345678901234",
            Decimal("0.12345678901234").toRawDecimalString(),
            "stringConstructor: \"0.12345678901234\""
        )
        assertEquals(
            "0.123456789012346",
            Decimal("0.1234567890123456").toRawDecimalString(),
            "stringConstructor: \"0.1234567890123456\", with rounding"
        )
        assertEquals(
            "123456.12345678",
            Decimal("123456.1234567800000000").toRawDecimalString(),
            "stringConstructor: \"123456.1234567800000000\", with rounding"
        )
        Decimal.setMaxDecimalPlaces(3)
        assertEquals(
            "123456.123",
            Decimal("123456.1234567890123456").toRawDecimalString(),
            "stringConstructor: \"123456.1234567890123456\", with rounding to 3 dplc"
        )
        Decimal.setMaxDecimalPlaces(15)
        assertFailsWith(
            ArithmeticException::class,
            "stringConstructor: \"123456.1234567890123456\", with rounding to 15 dplc",
            {Decimal("123456.1234567890123456").toRawDecimalString()}
        )
        /* assertEquals(
            "123456.12345678901235",
            Decimal("123456.1234567890123456").toRawDecimalString(),
            "stringConstructor: \"123456.1234567890123456\", with rounding to 15 dplc"
        )
        */
    }

    @Test fun toPlainStringTests() {
        assertEquals(
            "123",
            Decimal(123L, 0, true).toRawDecimalString(),
            "toPlainString: +mantissa 123L, 0 places 0"
        )
        assertEquals(
            "1.24",
            Decimal(124L, 2, true).toRawDecimalString(),
            "toPlainString: +mantissa, 124L +places 2"
        )
        assertEquals(
            "12500",
            Decimal(125L, -2, true).toRawDecimalString(),
            "toPlainString: +mantissa 125L, -places -2"
        )
        assertEquals(
            "-125",
            Decimal(-125L, 0, true).toRawDecimalString(),
            "toPlainString: -mantissa -125L, 0 places"
        )
        assertEquals(
            "-1.25",
            Decimal(-125L, +2, true).toRawDecimalString(),
            "toPlainString: -mantissa -125L, +places +2"
        )
        assertEquals(
            "12500",
            //Decimal(-125L, -2, true).toPlainString(),
            12500F.Dc.toRawDecimalString(),
            "toPlainString: -mantissa -125L, -places -2"
        )
    }

    @Test fun toScientificStringTests() {
        assertEquals(
            "1.23E2",
            Decimal(123L).toScientificString(),
            "toScientific: +mantissa, 0 places"
        )
        assertEquals(
            "1.24E-8",
            Decimal(124L, 10,true).toScientificString(),
            "toScientific: +mantissa, +places"
        )
        assertEquals(
            "1.25E4",
            Decimal(125L, -2, true).toScientificString(),
            "toScientific: +mantissa 125L, -places -2"
        )
        assertEquals(
            "-1.25E2",
            Decimal(-125L).toScientificString(),
            "toScientific: -mantissa, 0places, '1.25E2'"
        )
        assertEquals(
            "-1.25E-8",
            Decimal(-125L, 10, true).toScientificString(),
            "toScientific: -mantissa, +places, '-1.25E-8'"
        )
        assertEquals(
            "-1.25E12",
            Decimal(-125L, -10, true).toScientificString(),
            "toScientific: -mantissa, -places, '1.25E12'"
        )

    }
}

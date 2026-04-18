package io.github.astridha.smalldecimal

import io.github.astridha.smalldecimal.Decimal.Companion.noRounding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecimalCoreTest {

    @Test fun intConstructorTests() {
        assertEquals(
            "15",
            Decimal(15L).toString(),
            "intConstructor: 15L"
        )
        assertEquals(
            "16",
            (16).Dc.toString(),
            "Int.Dc Constructor: 16"
        )
        assertEquals(
            "17.5",
            (17.5).Dc.toString(),
            "Double.Dc Constructor: 17.5"
        )
        Decimal.setLocale(Decimal.Locale(null,'.',2))
        assertEquals(
            "18.5001",
            "18.5001".Dc.toString(),
            "String.Dc Constructor: 18.5001"
        )
         Decimal.setLocale(Decimal.Locale(null,'.',0))
        assertEquals(
            "18500",
            "18500.000".Dc.toString(),
            "String.Dc Constructor: 18500.000"
        )
         Decimal.setRounding(Decimal.Rounding(2, Decimal.RoundingMode.HALF_UP))
        assertEquals(
            "18.5",
            "18.5001".Dc.toString(),
            "String.Dc Constructor: 18.5001"
        )
        Decimal.setRounding(Decimal.Rounding(0, Decimal.RoundingMode.HALF_UP))
        assertEquals(
            "19",
            "18.5001".Dc.toString(),
            "String.Dc Constructor: 18.5001"
        )
         Decimal.setRounding(Decimal.Rounding(15, Decimal.RoundingMode.HALF_UP))
    }

    @Test fun doubleConstructorTests() {

        Decimal.setRounding(noRounding)
        assertEquals(
            "100000.47",
            Decimal(100000.47).toString(),
            "DoubleConstructor: 100000.47"
        )
        assertEquals(
            "15.00000001",
            Decimal(15.00000001).toString(),
            "DoubleConstructor: 15"
        )
        assertEquals(
            "15.000000000001",
            Decimal(15.000000000001).toString(),
            "DoubleConstructor: 15 (d=12)"
        )
        assertEquals(
            "15.0000000000001",
            Decimal(15.0000000000001).toString(),
            "DoubleConstructor: 15 (d=13)"
        )
        assertEquals(
            "15.00000000000006",
            15.00000000000006.Dc.toString(),
            "15 Double (d=14) toString()"
        )
        assertEquals(
            "15.00000000000001",
            Decimal(15.00000000000001).toString(),
            "DoubleConstructor: 15 (d=14)"
        )

        Decimal.setRounding(Decimal.Rounding(5, Decimal.RoundingMode.HALF_UP))

        assertEquals(
            "15.000001",
            15.000001.toString(),
            "15 Double!!! (p=6) toString()"
        )
        assertEquals(
            "15",
            15.000001.Dc.toString(),  // 6 places when precision is 5!
            "15.000001 DoubleConstructor (d=6, p=5) toString()"
        )
        assertEquals(
            "15.00001",
            15.000009.Dc.toString(),  // 6 places when precision is 5!
            "15.000009 DoubleConstructor (d=6, p=5) toString()"
        )
        Decimal.setRounding(Decimal.Rounding(6, Decimal.RoundingMode.HALF_UP))
        assertEquals(
            "15.000001",
            15.000001.Dc.toString(),  // 6 places when precision is 6!
            "15.000001 DoubleConstructor (d=6, p=6) toString()"
        )
        assertEquals(
            "15",
            Decimal(15.000000000000009).toString(),
            "DoubleConstructor: 15.000000000000009 (d=15, p=6)"
        )
        Decimal.setMaxDecimalPlaces(15)
        assertEquals(
            "15.000000000000009",
            Decimal(15.000000000000009).toString(),
            "DoubleConstructor: 15.000000000000009 (d=15, p=15)"
        )
        assertEquals(
            "15",
            Decimal(15).toString(),
            "intConstructor: 15"
        )
    }

    @Test fun floatConstructorTests() {

        Decimal.setMaxDecimalPlaces(15) // default
        assertEquals(
            "100000.47",
            Decimal(100000.47F).toString(),
            "floatConstructor: 10000000.47"
        )
        assertEquals(
            "10000.47",
            (10000.47F).toDecimal().toString(),
            "Float.toDecimal(): 10000.47"
        )
        assertEquals(
            "15.3",
            (15.3F).toString(),
            "Float Test 15.3F"
        )
        assertEquals(
            "15.3",
            (15.3F).toDecimal().toString(),
            "float.toDecimal(): 15.3F"
        )

    }

    @Test fun stringConstructorTests() {
        assertEquals(
            null,
            "abc".toDecimalOrNull()?.toString(),
            "string \"abc\".toDecimalOrNull()"
        )
        assertFailsWith(
            NumberFormatException::class,
            "string  \"abc\".toDecimal()",
            {"abc".toDecimal().toString()}
        )
        assertFailsWith(
            NumberFormatException::class,
            "stringConstructor: abc",
            {Decimal("abc").toString()}
        )
         assertEquals(
            "123",
            Decimal("123").toString(),
            "stringConstructor: 123"
        )
        assertEquals(
            "123000",
            Decimal("123000").toString(),
            "stringConstructor: 123000"
        )
        assertEquals(
            "123",
            Decimal("123.000").toString(),
            "stringConstructor: 123.000"
        )
        assertEquals(
            "123.4",
            Decimal("123.4").toString(),
            "stringConstructor: 123.4 (no rounding defined, no decimals defined)"
        )
        assertEquals(
            "-123.004",
            Decimal("-123.004").toString(),
            "stringConstructor: -123.004"
        )
        assertEquals(
            "1.234",
            Decimal("1.234E0").toString(),
            "stringConstructor: 1.234E0"
        )
        assertEquals(
            "123.4",
            Decimal("1.234E2").toString(),
            "stringConstructor: 1.234E2"
        )
        assertEquals(
            "-123.4",
            Decimal("-1.234E2").toString(),
            "stringConstructor: -1.234E2"
        )
        assertEquals(
            "0.01234",
            Decimal("1.234E-2").toString(),
            "stringConstructor: 1.234E-2"
        )
        assertEquals(
            "0.12345678901234",
            Decimal("0.12345678901234").toString(),
            "stringConstructor: \"0.12345678901234\""
        )
        assertEquals(
            "0.123456789012346",
            Decimal("0.1234567890123456").toString(),
            "stringConstructor: \"0.1234567890123456\", with rounding"
        )
        assertEquals(
            "123456.12345678",
            Decimal("123456.1234567800000000").toString(),
            "stringConstructor: \"123456.1234567800000000\", with rounding"
        )
       Decimal.setRounding(Decimal.Rounding(3))
        assertEquals(
            "123456.123",
            Decimal("123456.1234567890123456").toString(),
            "stringConstructor: \"123456.1234567890123456\", with rounding to 3 dplc"
        )
        Decimal.setRounding(Decimal.Rounding(15))
        assertEquals(
            "123456.123",
            Decimal("123..456,123",Decimal.Locale('.',',', 5)).toString(),
            "stringConstructor: \"123..456,123\", with grouping=dot and decimal=comma"
        )
        assertFailsWith(
            ArithmeticException::class,
            "stringConstructor: \"123456.1234567890123456\", with rounding to 15 dplc",
            {Decimal("123456.1234567890123456").toString()}
        )
        /* assertEquals(
            "123456.12345678901235",
            Decimal("123456.1234567890123456").toRawDecimalString(),
            "stringConstructor: \"123456.1234567890123456\", with rounding to 15 dplc"
        )
        */
    }

}

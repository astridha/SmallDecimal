package io.github.astridha.smalldecimal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecimalOutputTests {

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
            //Decimal(-125L, -2, true).toPlainString(),
            12500F.Dc.toPlainString(),
            "toRawString: -mantissa -125L, -places -2"
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
            Decimal(-125L).toScientificString(),
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

    @Test fun toStringTests() {
        assertEquals(
            "1234567890",
            Decimal(1234567890L).toString(),
            "toString:  default setting, Long"
        )
        assertEquals(
            "123456.789",
            Decimal(123456.7890).toString(),
            "toString:  default setting, Double (with comma)"
        )
        var displayFormat = Decimal.LocalConfig(null, '.',3)
        assertEquals(
            "1.000",
            Decimal(1L).toString(displayFormat),
            "toString:  decimal is dot (default)"
        )
        displayFormat = Decimal.LocalConfig(null, ',',3)
        assertEquals(
            "1,000",
            Decimal(1L).toString(displayFormat),
            "toString:  decimal is comma"
        )

        assertFailsWith(
            IllegalArgumentException::class,
            "toString:  identical thousands and decimal are invalid"
        ) {
            Decimal(1L).toString(Decimal.LocalConfig('*', '*', 3))
        }

        displayFormat = Decimal.LocalConfig('.', ',',0)
        assertEquals(
            "1.000.000",
            Decimal(1000000L).toString(displayFormat),
            "toString:  thousands is dot and decimal is comma (but no decimals)"
        )
        displayFormat = Decimal.LocalConfig('.', ',',3)
        assertEquals(
            "1.000.000,000",
            Decimal(1000000L).toString(displayFormat),
            "toString:  thousands is dot and decimal is comma"
        )
        assertEquals(
            "1.000.000,000",
            Decimal(1000000L).toString(displayFormat),
            "toString:  thousands is dot and decimal is comma"
        )
        assertEquals(
            "1.234.567,000",
            Decimal(1234567L).toString(displayFormat),
            "toString:  thousands is dot and decimal is comma"
        )
        assertEquals(
            "-1.234.567,000",
            Decimal(-1234567L).toString(displayFormat),
            "toString:  thousands is dot and decimal is comma"
        )
        displayFormat = Decimal.LocalConfig(':', ',',3)
        assertEquals(
            "1:234:567,1234567",
            Decimal(1234567.1234567).toString(displayFormat),
            "toString:  thousands is : and decimal is comma"
        )
        assertEquals(
            "-1:234:567,000",
            Decimal(-1234567).toString(displayFormat),
            "toString:  thousands is : and decimal is comma"
        )
    }


}

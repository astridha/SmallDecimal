package io.github.astridha.smalldecimal

import io.github.astridha.smalldecimal.DecimalArithmetics.Companion.equalizeDecimals
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

//import kotlin.reflect.jvm.jvmName


public class Decimal : Number, Comparable<Decimal> {

    // 60-bit long mantissa plus 4-Bit long exponent (decimal places):
    private var decimal64: Long = 0L


    public enum class RoundingMode {
        UP,
        DOWN,
        CEILING,
        FLOOR,
        HALF_UP,
        HALF_DOWN,
        HALF_EVEN,
        UNNECESSARY
    }

    public enum class Error { // max 14 Errors (plus NO_ERROR), for 4 Byte decimals field when mantissa == 0!
        NO_ERROR,
        NOT_A_NUMBER,
        PARSING_OVERFLOW,
        NUMERIC_OVERFLOW,
        ADD_OVERFLOW,
        SUBTRACT_OVERFLOW,
        MULTIPLY_OVERFLOW,
        DIVIDE_OVERFLOW,
        INC_OVERFLOW,
        DEC_OVERFLOW,
        OTHER_OVERFLOW,
        DIVISION_BY_0,
        POSITIVE_INFINITY,
        NEGATIVE_INFINITY,
        ROUNDING_FAILED,
        OTHER_ERROR;     // 15!

        // override fun toString(): String {
        //     return name.replace("_", " ")
        // }
    }

    /***********************  Secondary Constructors  ************************/

    @Throws(NumberFormatException::class, ArithmeticException::class)
    public constructor (rawNumberString: String, locale: Locale = autoLocale, omitRounding: Boolean = false) { // or explicit RoundingMode?
        val roundingConfig = if (omitRounding) noRounding; else autoRounding
        val parsedDecimalPair: Pair<Long, Int>? = mkDecimalParseOrNull(rawNumberString, roundingConfig, locale, false)
        if (parsedDecimalPair != null) {
            if (!isError(parsedDecimalPair.first, parsedDecimalPair.second)) {
                val (roundedMantissa, roundedDecimals) = roundWithMode(
                    parsedDecimalPair.first,
                    parsedDecimalPair.second,
                    roundingConfig
                )
                decimal64 = pack64(roundedMantissa, roundedDecimals)
            } else {
                decimal64 = pack64(parsedDecimalPair.first, parsedDecimalPair.second)
            }
        } else {
            // no exception. this branch won't happen because null was not allowed
            decimal64 = pack64(0, Error.NOT_A_NUMBER.ordinal)
        }
    }


    @Throws(ArithmeticException::class)
    public constructor (float: Float, omitRounding: Boolean = false) : this(float.toString(), noLocale, omitRounding)

    @Throws(ArithmeticException::class)
    public constructor (double: Double, omitRounding: Boolean = false) : this(double.toString(), noLocale, omitRounding)

    public constructor (other: Decimal) {
        decimal64 = other.decimal64   // or: clone()? difference?
    }

    internal constructor (mantissa: Long, decimalPlaces: Int) {
        decimal64 = pack64(mantissa, decimalPlaces)
    }

    @Throws(ArithmeticException::class)
    public constructor (long:Long) {
        if (abs(long) > MAX_MANTISSA_VALUE) {
            // a single value will overflow
            val errorCode = generateErrorCode(Error.NUMERIC_OVERFLOW, "$long cannot fit into a Decimal")
             decimal64 = pack64(0,errorCode)
         } else {
            decimal64 = pack64(long, 0)
        }
    }
    // Secondary constructors cannot call other secondary constructors.
    // So constructors based on other integer types cannot simply call constructor(int.toLong())
    // Therefore, see the work-around invoke expressions in the Companion object!

    /**************************** Packing / Unpacking Helper Methods  ********************************/

    internal fun unpack64(): Pair<Long, Int> {
        val decimals: Int = (decimal64 and MAX_DECIMAL_PLACES.toLong()).toInt()
        val mantissa: Long = (decimal64 shr 4)
        if ((mantissa == 0L) && (decimals != 0)) {
            val error = getError(decimals)
            generateErrorCode(error, "")
         }
        return Pair(mantissa, decimals)
    }

    internal fun pack64(pMantissa: Long, pDecimals: Int): Long {
        var mantissa = pMantissa
        //var decimals =  if (mantissa == 0L) 0; else p_decimals
        var decimals = pDecimals

        // paranoid last checks
        if (!((mantissa == 0L) && (decimals != 0))) {

            // most important, correct negative decimal places, as we don't support them!
            while (decimals < 0) {
                mantissa *= 10
                decimals++
            }

            // truncate any empty decimal places, will make room for longer mantissa
            while ((decimals > 0) && (mantissa != 0L) && ((mantissa % 10) == 0L)) {
                //mantissa = (mantissa+5) / 10
                mantissa /= 10
                decimals--
            }

            // still too long :(
            if ((abs(mantissa) > MAX_MANTISSA_VALUE) || (decimals > MAX_DECIMAL_PLACES)) {
                val errno = generateErrorCode(Error.OTHER_OVERFLOW,"mantissa $mantissa with $decimals decimals")
                mantissa = 0L
                decimals = errno
            }
        }

        return ((mantissa shl 4) or (decimals.toLong() and MAX_DECIMAL_PLACES.toLong()) )
    }

    /*******************  Rounding functions  *********************************/

    public fun ceil(desiredDecimals: Int = 0) : Decimal  {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(mantissa, decimals, Rounding(desiredDecimals, RoundingMode.CEILING))
        return Decimal(newMantissa, if (newMantissa == 0L)  0 else newDecimals)
    }

    public fun floor(desiredDecimals: Int = 0) : Decimal  {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(mantissa, decimals, Rounding(desiredDecimals, RoundingMode.FLOOR))
        return Decimal(newMantissa, if (newMantissa == 0L)  0 else newDecimals)
    }

    public fun truncate(desiredDecimals: Int = 0) : Decimal  {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(mantissa, decimals, Rounding(desiredDecimals, RoundingMode.DOWN))
        return Decimal(newMantissa, if (newMantissa == 0L)  0 else newDecimals)
    }

    public fun round(desiredDecimals: Int = 0) : Decimal  {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(mantissa, decimals, Rounding(desiredDecimals, RoundingMode.HALF_EVEN))
        return Decimal(newMantissa, if (newMantissa == 0L)  0 else newDecimals)
    }


    public fun setScale(desiredDecimals: Int = autoRounding.decimalPlaces, roundingMode: RoundingMode = autoRounding.roundingMode): Decimal {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val roundingDecimals = min(MAX_DECIMAL_PLACES, desiredDecimals)
        val (newMantissa, newDecimals) = roundWithMode(mantissa, decimals, Rounding(roundingDecimals, roundingMode))
        return Decimal(newMantissa, if (newMantissa == 0L)  0 else newDecimals)
    }

    /*******************  Operator Overloads  ******************/

    /***  unary operators ***/

    /*** operator unaryPlus (+) , unaryMinus (-) and not() (!) ***/

    public operator fun unaryPlus() : Decimal = this // or: clone()?

    public operator fun unaryMinus() : Decimal {
        if (isError()) return clone()
        var (mantissa, decimals) = unpack64()
        mantissa = (0L-mantissa)
        return Decimal(mantissa, decimals)
    }

    public operator fun not() : Boolean = (decimal64 == 0L)


    /***** operator unaryIncrement (++) , unaryDecrement (--)  *****/

    @Throws(ArithmeticException::class)
    public operator fun inc() : Decimal {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val step = getPower10(decimals)
        if (mantissa.isPositive()) {
            // increment might overflow!
            val space: Long = (MAX_MANTISSA_VALUE - mantissa)
            if (space < step) {
                return generateErrorDecimal(Error.INC_OVERFLOW, "$this + ${Decimal(step)} result does not fit into Decimal")
            }
        }
        return Decimal(mantissa+step, decimals)
    }

    @Throws(ArithmeticException::class)
    public operator fun dec() : Decimal {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val step = getPower10(decimals)
        if (mantissa.isNegative()) {
            // decrement might overflow!
            val space: Long = (MAX_MANTISSA_VALUE - abs(mantissa))
            if (space < step) {
                return generateErrorDecimal(Error.DEC_OVERFLOW, "$this - ${Decimal(step)} result does not fit into Decimal")
            }
        }

        return Decimal(mantissa-step, decimals)
    }

    /*********************  Arithmetic operator overloads  **************************/

    private fun Long.isNegative() = (this.sign < 0)
    private fun Long.isPositive() = (this.sign >= 0)

    /***** operator plus (+) *****/

    @Throws(ArithmeticException::class)
    public operator fun plus(other: Decimal) : Decimal {
        return DecimalArithmetics.arithmeticPlus(this, other)
    }
    public operator fun plus(otherDouble: Double) : Decimal = plus(otherDouble.toDecimal())
    public operator fun plus(otherFloat: Float) : Decimal = plus(otherFloat.toDecimal())
    public operator fun plus(otherLong: Long) : Decimal = plus(otherLong.toDecimal())
    public operator fun plus(otherInt: Int) : Decimal = plus(otherInt.toDecimal())
    public operator fun plus(otherShort: Short) : Decimal = plus(otherShort.toDecimal())
    public operator fun plus(otherByte: Byte) : Decimal = plus(otherByte.toDecimal())
    public operator fun plus(otherULong: ULong) : Decimal = plus(otherULong.toDecimal())
    public operator fun plus(otherUInt: UInt) : Decimal = plus(otherUInt.toDecimal())
    public operator fun plus(otherUShort: UShort) : Decimal = plus(otherUShort.toDecimal())
    public operator fun plus(otherUByte: UByte) : Decimal = plus(otherUByte.toDecimal())

    @Throws(ArithmeticException::class)
    public fun plus(other: Decimal, rounding: Rounding): Decimal {
        return DecimalArithmetics.arithmeticPlus(this, other, rounding)
    }
    public fun plus(otherDouble: Double, rounding: Rounding): Decimal = plus(otherDouble.toDecimal(),rounding)
    public fun plus(otherFloat: Float, rounding: Rounding): Decimal = plus(otherFloat.toDecimal(),rounding)
    public fun plus(otherLong: Long, rounding: Rounding): Decimal = plus(otherLong.toDecimal(),rounding)
    public fun plus(otherInt: Int, rounding: Rounding): Decimal = plus(otherInt.toDecimal(),rounding)
    public fun plus(otherShort: Short, rounding: Rounding): Decimal = plus(otherShort.toDecimal(),rounding)
    public fun plus(otherByte: Byte, rounding: Rounding): Decimal = plus(otherByte.toDecimal(),rounding)
    public fun plus(otherULong: ULong, rounding: Rounding): Decimal = plus(otherULong.toDecimal(),rounding)
    public fun plus(otherUInt: UInt, rounding: Rounding): Decimal = plus(otherUInt.toDecimal(),rounding)
    public fun plus(otherUShort: UShort, rounding: Rounding): Decimal = plus(otherUShort.toDecimal(),rounding)
    public fun plus(otherUByte: UByte, rounding: Rounding): Decimal = plus(otherUByte.toDecimal(),rounding)


    /***** operator minus (-) *****/

    @Throws(ArithmeticException::class)
    public operator fun minus(other: Decimal) : Decimal {
        return DecimalArithmetics.arithmeticMinus(this, other)
    }
    public operator fun minus(otherDouble: Double) : Decimal = minus(otherDouble.toDecimal())
    public operator fun minus(otherFloat: Float) : Decimal = minus(otherFloat.toDecimal())
    public operator fun minus(otherLong: Long) : Decimal = minus(otherLong.toDecimal())
    public operator fun minus(otherInt: Int) : Decimal = minus(otherInt.toDecimal())
    public operator fun minus(otherShort: Short) : Decimal = minus(otherShort.toDecimal())
    public operator fun minus(otherByte: Byte) : Decimal = minus(otherByte.toDecimal())
    public operator fun minus(otherULong: ULong) : Decimal = minus(otherULong.toDecimal())
    public operator fun minus(otherUInt: UInt) : Decimal = minus(otherUInt.toDecimal())
    public operator fun minus(otherUShort: UShort) : Decimal = minus(otherUShort.toDecimal())
    public operator fun minus(otherUByte: UByte) : Decimal = minus(otherUByte.toDecimal())

    @Throws(ArithmeticException::class)
    public fun minus(other: Decimal, rounding: Rounding): Decimal {
        return DecimalArithmetics.arithmeticMinus(this, other, rounding)
    }
    public fun minus(otherDouble: Double, rounding: Rounding): Decimal = minus(otherDouble.toDecimal(),rounding)
    public fun minus(otherFloat: Float, rounding: Rounding): Decimal = minus(otherFloat.toDecimal(),rounding)
    public fun minus(otherLong: Long, rounding: Rounding): Decimal = minus(otherLong.toDecimal(),rounding)
    public fun minus(otherInt: Int, rounding: Rounding): Decimal = minus(otherInt.toDecimal(),rounding)
    public fun minus(otherShort: Short, rounding: Rounding): Decimal = minus(otherShort.toDecimal(),rounding)
    public fun minus(otherByte: Byte, rounding: Rounding): Decimal = minus(otherByte.toDecimal(),rounding)
    public fun minus(otherULong: ULong, rounding: Rounding): Decimal = minus(otherULong.toDecimal(),rounding)
    public fun minus(otherUInt: UInt, rounding: Rounding): Decimal = minus(otherUInt.toDecimal(),rounding)
    public fun minus(otherUShort: UShort, rounding: Rounding): Decimal = minus(otherUShort.toDecimal(),rounding)
    public fun minus(otherUByte: UByte, rounding: Rounding): Decimal = minus(otherUByte.toDecimal(),rounding)


    /***** operator times (*) *****/

    @Throws(ArithmeticException::class)
    public operator fun times(other: Decimal) : Decimal {
        return DecimalArithmetics.arithmeticTimes(this, other)
    }
    public operator fun times(otherDouble: Double) : Decimal = times(otherDouble.toDecimal())
    public operator fun times(otherFloat: Float) : Decimal = times(otherFloat.toDecimal())
    public operator fun times(otherLong: Long) : Decimal = times(otherLong.toDecimal())
    public operator fun times(otherInt: Int) : Decimal = times(otherInt.toDecimal())
    public operator fun times(otherShort: Short) : Decimal = times(otherShort.toDecimal())
    public operator fun times(otherByte: Byte) : Decimal = times(otherByte.toDecimal())
    public operator fun times(otherULong: ULong) : Decimal = times(otherULong.toDecimal())
    public operator fun times(otherUInt: UInt) : Decimal = times(otherUInt.toDecimal())
    public operator fun times(otherUShort: UShort) : Decimal = times(otherUShort.toDecimal())
    public operator fun times(otherUByte: UByte) : Decimal = times(otherUByte.toDecimal())

    @Throws(ArithmeticException::class)
    public fun times(other: Decimal, rounding: Rounding): Decimal {
        return DecimalArithmetics.arithmeticTimes(this, other, rounding)
    }
    public fun times(otherDouble: Double, rounding: Rounding): Decimal = times(otherDouble.toDecimal(),rounding)
    public fun times(otherFloat: Float, rounding: Rounding): Decimal = times(otherFloat.toDecimal(),rounding)
    public fun times(otherLong: Long, rounding: Rounding): Decimal = times(otherLong.toDecimal(),rounding)
    public fun times(otherInt: Int, rounding: Rounding): Decimal = times(otherInt.toDecimal(),rounding)
    public fun times(otherShort: Short, rounding: Rounding): Decimal = times(otherShort.toDecimal(),rounding)
    public fun times(otherByte: Byte, rounding: Rounding): Decimal = times(otherByte.toDecimal(),rounding)
    public fun times(otherULong: ULong, rounding: Rounding): Decimal = times(otherULong.toDecimal(),rounding)
    public fun times(otherUInt: UInt, rounding: Rounding): Decimal = times(otherUInt.toDecimal(),rounding)
    public fun times(otherUShort: UShort, rounding: Rounding): Decimal = times(otherUShort.toDecimal(),rounding)
    public fun times(otherUByte: UByte, rounding: Rounding): Decimal = times(otherUByte.toDecimal(),rounding)


    /***** operator div (/) *****/

    @Throws(ArithmeticException::class)
    public operator fun div(other: Decimal) : Decimal {
        return DecimalArithmetics.arithmeticDiv(this, other)
    }
    public operator fun div(otherDouble: Double) : Decimal = div(otherDouble.toDecimal())
    public operator fun div(otherFloat: Float) : Decimal = div(otherFloat.toDecimal())
    public operator fun div(otherLong: Long) : Decimal = div(otherLong.toDecimal())
    public operator fun div(otherInt: Int) : Decimal = div(otherInt.toDecimal())
    public operator fun div(otherShort: Short) : Decimal = div(otherShort.toDecimal())
    public operator fun div(otherByte: Byte) : Decimal = div(otherByte.toDecimal())
    public operator fun div(otherULong: ULong) : Decimal = div(otherULong.toDecimal())
    public operator fun div(otherUInt: UInt) : Decimal = div(otherUInt.toDecimal())
    public operator fun div(otherUShort: UShort) : Decimal = div(otherUShort.toDecimal())
    public operator fun div(otherUByte: UByte) : Decimal = div(otherUByte.toDecimal())

    @Throws(ArithmeticException::class)
    public fun div(other: Decimal, rounding: Rounding): Decimal {
        return DecimalArithmetics.arithmeticDiv(this, other, rounding)
    }
    public fun div(otherDouble: Double, rounding: Rounding): Decimal = div(otherDouble.toDecimal(),rounding)
    public fun div(otherFloat: Float, rounding: Rounding): Decimal = div(otherFloat.toDecimal(),rounding)
    public fun div(otherLong: Long, rounding: Rounding): Decimal = div(otherLong.toDecimal(),rounding)
    public fun div(otherInt: Int, rounding: Rounding): Decimal = div(otherInt.toDecimal(),rounding)
    public fun div(otherShort: Short, rounding: Rounding): Decimal = div(otherShort.toDecimal(),rounding)
    public fun div(otherByte: Byte, rounding: Rounding): Decimal = div(otherByte.toDecimal(),rounding)
    public fun div(otherULong: ULong, rounding: Rounding): Decimal = div(otherULong.toDecimal(),rounding)
    public fun div(otherUInt: UInt, rounding: Rounding): Decimal = div(otherUInt.toDecimal(),rounding)
    public fun div(otherUShort: UShort, rounding: Rounding): Decimal = div(otherUShort.toDecimal(),rounding)
    public fun div(otherUByte: UByte, rounding: Rounding): Decimal = div(otherUByte.toDecimal(),rounding)


    /************* operator rem (%) ************/

    @Throws(ArithmeticException::class)
    public operator fun rem(other:Decimal) : Decimal {
        return DecimalArithmetics.arithmeticRem(this, other)
    }
    public operator fun rem(otherDouble: Double) : Decimal = rem(otherDouble.toDecimal())
    public operator fun rem(otherFloat: Float) : Decimal = rem(otherFloat.toDecimal())
    public operator fun rem(otherLong: Long) : Decimal = rem(otherLong.toDecimal())
    public operator fun rem(otherInt: Int) : Decimal = rem(otherInt.toDecimal())
    public operator fun rem(otherShort: Short) : Decimal = rem(otherShort.toDecimal())
    public operator fun rem(otherByte: Byte) : Decimal = rem(otherByte.toDecimal())
    public operator fun rem(otherULong: ULong) : Decimal = rem(otherULong.toDecimal())
    public operator fun rem(otherUInt: UInt) : Decimal = rem(otherUInt.toDecimal())
    public operator fun rem(otherUShort: UShort) : Decimal = rem(otherUShort.toDecimal())
    public operator fun rem(otherUByte: UByte) : Decimal = rem(otherUByte.toDecimal())

    @Throws(ArithmeticException::class)
    public fun rem(other: Decimal, rounding: Rounding): Decimal {
        return DecimalArithmetics.arithmeticRem(this, other, rounding)
    }
    public fun rem(otherDouble: Double, rounding: Rounding): Decimal = rem(otherDouble.toDecimal(),rounding)
    public fun rem(otherFloat: Float, rounding: Rounding): Decimal = rem(otherFloat.toDecimal(),rounding)
    public fun rem(otherLong: Long, rounding: Rounding): Decimal = rem(otherLong.toDecimal(),rounding)
    public fun rem(otherInt: Int, rounding: Rounding): Decimal = rem(otherInt.toDecimal(),rounding)
    public fun rem(otherShort: Short, rounding: Rounding): Decimal = rem(otherShort.toDecimal(),rounding)
    public fun rem(otherByte: Byte, rounding: Rounding): Decimal = rem(otherByte.toDecimal(),rounding)
    public fun rem(otherULong: ULong, rounding: Rounding): Decimal = rem(otherULong.toDecimal(),rounding)
    public fun rem(otherUInt: UInt, rounding: Rounding): Decimal = rem(otherUInt.toDecimal(),rounding)
    public fun rem(otherUShort: UShort, rounding: Rounding): Decimal = rem(otherUShort.toDecimal(),rounding)
    public fun rem(otherUByte: UByte, rounding: Rounding): Decimal = rem(otherUByte.toDecimal(),rounding)


    /************ infix operator mod (mod) ***********/

    @Throws(ArithmeticException::class)
    public infix fun mod(other:Decimal) : Decimal {
        return DecimalArithmetics.arithmeticMod(this, other)
    }

    public infix fun mod(otherDouble: Double) : Decimal = mod(otherDouble.toDecimal())
    public infix fun mod(otherFloat: Float) : Decimal = mod(otherFloat.toDecimal())
    public infix fun mod(otherLong: Long) : Decimal = mod(otherLong.toDecimal())
    public infix fun mod(otherInt: Int) : Decimal = mod(otherInt.toDecimal())
    public infix fun mod(otherShort: Short) : Decimal = mod(otherShort.toDecimal())
    public infix fun mod(otherByte: Byte) : Decimal = mod(otherByte.toDecimal())
    public infix fun mod(otherULong: ULong) : Decimal = mod(otherULong.toDecimal())
    public infix fun mod(otherUInt: UInt) : Decimal = mod(otherUInt.toDecimal())
    public infix fun mod(otherUShort: UShort) : Decimal = mod(otherUShort.toDecimal())
    public infix fun mod(otherUByte: UByte) : Decimal = mod(otherUByte.toDecimal())

    @Throws(ArithmeticException::class)
    public fun mod(other: Decimal, rounding: Rounding): Decimal {
        return DecimalArithmetics.arithmeticMod(this, other, rounding)
    }
    public fun mod(otherDouble: Double, rounding: Rounding): Decimal = mod(otherDouble.toDecimal(),rounding)
    public fun mod(otherFloat: Float, rounding: Rounding): Decimal = mod(otherFloat.toDecimal(),rounding)
    public fun mod(otherLong: Long, rounding: Rounding): Decimal = mod(otherLong.toDecimal(),rounding)
    public fun mod(otherInt: Int, rounding: Rounding): Decimal = mod(otherInt.toDecimal(),rounding)
    public fun mod(otherShort: Short, rounding: Rounding): Decimal = mod(otherShort.toDecimal(),rounding)
    public fun mod(otherByte: Byte, rounding: Rounding): Decimal = mod(otherByte.toDecimal(),rounding)
    public fun mod(otherULong: ULong, rounding: Rounding): Decimal = mod(otherULong.toDecimal(),rounding)
    public fun mod(otherUInt: UInt, rounding: Rounding): Decimal = mod(otherUInt.toDecimal(),rounding)
    public fun mod(otherUShort: UShort, rounding: Rounding): Decimal = mod(otherUShort.toDecimal(),rounding)
    public fun mod(otherUByte: UByte, rounding: Rounding): Decimal = mod(otherUByte.toDecimal(),rounding)


    /**********************  Other Math functions ***************************/

    // still missing: pow, pow(n), pow(Dc), sqrt

    public fun abs() : Decimal  {
        val (mantissa, decimals) = unpack64()
        return Decimal(abs(mantissa), decimals)
    }

    public val absoluteValue: Decimal
        get() = abs()

    public val sign : Decimal
        get() = Decimal(decimal64.sign)

    public val numDecimalPlaces : Int
        get() = (decimal64 and MAX_DECIMAL_PLACES.toLong()).toInt()


    /**********************  Converting to Standard Numeric Types ***************************/

    private fun truncatedMantissa() : Long  {
        val (mantissa, decimals) = unpack64()
        var shift: Long
        when {
            (decimals > 0) -> {
                shift = getPower10(decimals)
                return (mantissa / shift)
            }
            else -> return mantissa
        }
    }

    private fun roundedMantissa(roundingMode: RoundingMode) : Long  {
        val (mantissa, decimals) = unpack64()
        if (decimals == 0) return mantissa  // nothing to do
        val (rounded, _) = roundWithMode(mantissa, decimals, Rounding(0, roundingMode))
        return rounded
    }


    public override fun toDouble(): Double = this.toRawString().toDouble()
    public override fun toFloat(): Float = this.toRawString().toFloat()
    public override fun toLong(): Long = truncatedMantissa()
    public override fun toInt(): Int = truncatedMantissa().toInt()
    public override fun toShort(): Short = truncatedMantissa().toShort()
    public override fun toByte() : Byte = truncatedMantissa().toByte()
    public fun toULong(): ULong = truncatedMantissa().toULong()
    public fun toUInt(): UInt = truncatedMantissa().toUInt()
    public fun toUShort(): UShort = truncatedMantissa().toUShort()
    public fun toUByte(): UByte = truncatedMantissa().toUByte()
    public fun toLong(roundingMode: RoundingMode): Long = roundedMantissa(roundingMode)
    public fun toInt(roundingMode: RoundingMode): Int = roundedMantissa(roundingMode).toInt()
    public fun toShort(roundingMode: RoundingMode): Short = roundedMantissa(roundingMode).toShort()
    public fun toByte(roundingMode: RoundingMode): Byte = roundedMantissa(roundingMode).toByte()
    public fun toULong(roundingMode: RoundingMode): ULong = roundedMantissa(roundingMode).toULong()
    public fun toUInt(roundingMode: RoundingMode): UInt = roundedMantissa(roundingMode).toUInt()
    public fun toUShort(roundingMode: RoundingMode): UShort = roundedMantissa(roundingMode).toUShort()
    public fun toUByte(roundingMode: RoundingMode): UByte = roundedMantissa(roundingMode).toUByte()
    public fun roundToLong(): Long = roundedMantissa(RoundingMode.CEILING)
    public fun roundToInt(): Int = roundedMantissa(RoundingMode.CEILING).toInt()


    /********************  Unformatted or formatted Output to human-readable Strings  ****************************/


    public  fun toRawString() : String {
        if (isError()) return getError().toString()
        val (mantissa, decimals) = unpack64()
        return toRawString(mantissa, decimals)
    }

     public fun toScientificString() : String {
        if (isError()) return getError().toString()
        val (mantissa, decimals) = unpack64()
        if (mantissa == 0L) return "0E0"
        var decimalString: String
        val prefix : String
         when {
             (mantissa < 0) -> { decimalString = (0L - mantissa).toString(10); prefix = "-" }
             else ->           { decimalString = mantissa.toString(10); prefix = ""  }
         }

        val adjustedExp = (decimalString.count()-1) - decimals
        if (decimalString.count() > 1) decimalString = decimalString.take(1) + '.' + decimalString.substring(1).trimEnd('0')

        return prefix+decimalString+'E'+adjustedExp.toString(10)
    }

    public override fun toString() : String {
        return toString(autoLocale)
    }

    public fun toString(displayFormat: Locale) : String {
            if (isError()) return getError().toString()
        // inserts grouping delimiters between groups of 3 digits dynamically and adds the minimum of decimal places
        // i.e., needs no formatting string and supports no overall minimum width; but no India lakh/crore format
        val groupingSeparatorString = displayFormat.groupingSeparator?.toString() ?: ""
        val decimalsSeparatorString = displayFormat.decimalSeparator.toString()
        val minDecimalPlaces = displayFormat.minDecimalPlaces
        var rawString = this.toRawString()
        var integerPart: String
        var decimalPart: String
        val decimalPosition = rawString.indexOf(".")
        if (decimalPosition >= 0) {
            integerPart = rawString.take(decimalPosition)
            decimalPart = rawString.substring(decimalPosition+1)
        } else {
            integerPart = rawString
            decimalPart = ""
        }

        rawString = integerPart.reversed()
            .chunked(3)
            .joinToString(groupingSeparatorString)
            .reversed()
        if (decimalPosition >= 0) {
          rawString = buildString {
              append(rawString)
              append(decimalsSeparatorString)
              append(decimalPart)
          }
        }

        if (minDecimalPlaces > 0) {
            val decimals = decimalPart.length
            val  missingPlaces = minDecimalPlaces - decimals
            if (decimals <= 0) rawString += decimalsSeparatorString
            if (missingPlaces > 0) rawString += ("0".repeat(missingPlaces))
        }

        return rawString
    }


    // @JvmRecord
    public data class Rounding(val decimalPlaces: Int = MAX_DECIMAL_PLACES, val roundingMode: RoundingMode = RoundingMode.HALF_UP) {
        public constructor (decimalPlaces: Int): this(decimalPlaces, autoRounding.roundingMode)
        init {
            require(decimalPlaces >= (0-MAX_DECIMAL_PLACES)) { "decimal places must be greater or equal -$MAX_DECIMAL_PLACES" }
            require(decimalPlaces <= MAX_DECIMAL_PLACES) { "decimal places must not be be greater than $MAX_DECIMAL_PLACES, is: $decimalPlaces" }
        }
    }

    // @JvmRecord
    public data class Locale(val groupingSeparator: Char? = null, val decimalSeparator : Char = '.', val minDecimalPlaces: Int = 0) {
        init {
            if (groupingSeparator != null) {
                require((groupingSeparator != decimalSeparator)) { "Grouping separator and decimal separator may not be equal '$groupingSeparator'" }
            }
            require(minDecimalPlaces >= 0) { "Decimal places must be greater or equal 0" }
        }
    }




    /***********  Comparable interface, and equality operators  *************/

    /*****  Clone / Copy Functions  *****/

    public fun clone(): Decimal {
        val (mantissa, decimals) = unpack64()
        return Decimal(mantissa, decimals)
    }
    public fun copy(): Decimal = this.clone() // which one is better?

    /*****  Compare Functions  *****/

    public override operator fun compareTo(other: Decimal): Int {
        if (this.decimal64 == other.decimal64) return 0
        val (thisM, thisD) = unpack64()
        val (thatM, thatD) = other.unpack64()

        val (thisMantissa,thatMantissa, _) = equalizeDecimals(thisM, thisD, thatM, thatD)

        return when {
            (thisMantissa > thatMantissa) -> 1
            (thisMantissa < thatMantissa) -> -1
            else -> 0
        }
    }

    public override operator fun equals(other: Any?) : Boolean
        = ((other != null) && (other is Decimal) && (this.decimal64 == other.decimal64))

    public override fun hashCode(): Int {
        return ((this.decimal64 ushr 32).toInt() xor (this.decimal64 and 0x00000000FFFFFFFFL).toInt())

    }


    /***************************  Companion Object  **************************************/

    public companion object {
        // Simulating a constructor out of a single integer type
        public operator fun invoke(byte:Byte): Decimal = Decimal(byte.toLong())
        public operator fun invoke(ubyte:UByte): Decimal = Decimal(ubyte.toLong())
        public operator fun invoke(short:Short): Decimal = Decimal(short.toLong())
        public operator fun invoke(ushort:UShort): Decimal = Decimal(ushort.toLong())
        public operator fun invoke(int:Int): Decimal = Decimal(int.toLong())
        public operator fun invoke(uint:UInt): Decimal= Decimal(uint.toLong())
        //public operator fun invoke(long:Long): Decimal = Decimal(long)
        public operator fun invoke(ulong:ULong): Decimal = Decimal(ulong.toLong())

        internal fun mkDecimalOrNull(numberString: String, locale: Locale = autoLocale): Decimal? {
            val decimalPair: Pair<Long, Int>? = mkDecimalParseOrNull(numberString, autoRounding, locale, true)
            return if (decimalPair != null) {
                val (roundedMantissa, roundedDecimals) = roundWithMode(decimalPair.first, decimalPair.second, autoRounding)
                Decimal(roundedMantissa, roundedDecimals)
            } else {
                null
            }
        }

        public const val MAX_MANTISSA_VALUE: Long = +576460752303423487L
        public const val MIN_DECIMAL_LONG_VALUE: Long = -576460752303423487L
        //public const val NOT_A_NUMBER: Long = -576460752303423488L

        public const val MAX_DECIMAL_PLACES: Int = 15
        public const val MAX_DECIMAL_SIGNIFICANTS: Int = 18
        public const val MAX_DECIMAL_MANTISSA_AS_STRING: String = "576460752303423487"
        public const val MAX_LONG_SIGNIFICANTS: Int = 19
        public const val SAFE_LONG_SIGNIFICANTS: Int = MAX_LONG_SIGNIFICANTS - 1
        public const val MAX_LONG_VALUE_AS_STRING: String = "9223372036854775807"

        public val ONE: Decimal = Decimal(1,0)

        public val NaN: Decimal = Decimal(0, Error.NOT_A_NUMBER.ordinal)

        public val MAX_VALUE: Decimal = Decimal(MAX_MANTISSA_VALUE,0)
        public val MIN_VALUE: Decimal = Decimal(MIN_DECIMAL_LONG_VALUE,0)

        // static (common) variables and functions

        // throw exceptions, or encode error numbers in Decimal?
        internal var shallThrowOnError: Boolean = true
        public fun setThrowOnErrors(shallThrow: Boolean) {
            shallThrowOnError = shallThrow
        }
        public fun getThrowOnErrors(): Boolean = shallThrowOnError

        internal var autoRounding: Rounding = Rounding(MAX_DECIMAL_PLACES, RoundingMode.HALF_UP)
        internal val noRounding: Rounding = Rounding(MAX_DECIMAL_PLACES, RoundingMode.HALF_UP)
        internal var autoLocale: Locale = Locale(null, '.',0)
        internal val noLocale: Locale = Locale(null, '.',0)

        // for automatic rounding
        //internal var autoRoundingConfig.decimalPlaces: Int = MAX_DECIMAL_PLACES /* 0 - 15 */

        public fun setRounding(rounding: Rounding) {
            val setD = when {
                (rounding.decimalPlaces > MAX_DECIMAL_PLACES) -> MAX_DECIMAL_PLACES
                (rounding.decimalPlaces < 0) -> 0
                else -> rounding.decimalPlaces
            }
            autoRounding = Rounding(setD, rounding.roundingMode)
        }

        public fun setLocale(locale: Locale) {
            autoLocale = locale
        }

        public fun setMaxDecimalPlaces(maxDecimalPlaces: Int) {
            setRounding(Rounding(maxDecimalPlaces))
        }
        public fun getMaxDecimalPlaces(): Int = autoRounding.decimalPlaces

        // private var autoFormatString: String = "#,###,###,##0.00"
        // ??? important for India: lakh/crore system? otherwise toFormattedString() is sufficient

        public fun setRoundingMode(mode: RoundingMode) {
            setRounding(Rounding(autoRounding.decimalPlaces, mode))
        }

        public fun getRoundingMode():RoundingMode = autoRounding.roundingMode


        /***************************  Simple output core routine   ***************************/

        internal fun toRawString(mantissa: Long, decimals: Int) : String {
            if (mantissa == 0L) {
                if (decimals == 0) return "0"
                return getError(decimals).toString()
            }
            var decimalString: String
            val prefix : String
            when {
                (mantissa < 0) -> { decimalString = (0L - mantissa).toString(10); prefix = "-" }
                else ->           { decimalString = mantissa.toString(10); prefix = "" }
            }

            if (decimals > 0) { // decimal digits exist, insert a dot
                var missingDecimals = decimalString.count() - decimals
                if (missingDecimals <= 0) { // more than significant digits! prepend zeros!
                    decimalString = "0"+"0".repeat(0-missingDecimals)+decimalString
                    missingDecimals = 1
                }
                decimalString = decimalString.take(missingDecimals) + '.' + decimalString.substring(missingDecimals)
            }
            return prefix+decimalString
        }


        /**************************** Error Handling  ********************************/

        // If shallThrowOnError is false, errors are embedded into decimal places instead, while mantissa is 0
        // see this below:

        internal fun isError(mantissa: Long, decimalPlaces: Int) : Boolean {
            if (mantissa !=0L) return false
            return (decimalPlaces !=0)
        }

        public fun isError(decimal: Decimal): Boolean {
            return ((decimal.decimal64 > 0L) && (decimal.decimal64 <= MAX_DECIMAL_PLACES) )
        }

        public fun getError(errno: Int): Error {
            if ((errno > 0) && (errno <= MAX_DECIMAL_PLACES) && (errno < Error.entries.count())) return Error.entries[errno]
            return Error.NO_ERROR
        }

        // better inline for a more clear stack trace?
        @Suppress("NOTHING_TO_INLINE")
        @Throws(NumberFormatException::class, ArithmeticException::class)
        internal inline fun generateErrorCode(error: Error, info: String): Int {
            val errorText = "$error: $info"
            if (shallThrowOnError) throw if (error == Error.NOT_A_NUMBER) NumberFormatException(errorText) else ArithmeticException(errorText)
            return error.ordinal
        }


        // better inline for a more clear stack trace?
        @Suppress("NOTHING_TO_INLINE")
        internal inline fun generateErrorDecimal(error: Error, info: String): Decimal {
            val errorCode = generateErrorCode(error, info)
            return Decimal(0, errorCode)
        }



    }  // end of the companion object


    /**************************** Error Handling  ********************************/

    // If shallThrowOnError is false, errors are embedded into decimal places instead, while mantissa is 0
    // in other words, this means that decimal64 is greater 0, but lower than 16 (0x10)
    // this allows for only up to 14 error conditions, so better be thrifty with them

    public fun isError(): Boolean {
        return ((decimal64 > 0L) && (decimal64 <= MAX_DECIMAL_PLACES))
    }

    public fun getError(): Error {
        if ((decimal64 > 0L) && (decimal64 <= MAX_DECIMAL_PLACES) && (decimal64.toInt() < Error.entries.count())) return Error.entries[decimal64.toInt()]
        return Error.NO_ERROR
    }



}





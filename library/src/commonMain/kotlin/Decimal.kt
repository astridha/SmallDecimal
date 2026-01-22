package io.github.astridha.smalldecimal

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

//import kotlin.reflect.jvm.jvmName


public open class Decimal : Number, Comparable<Decimal> {

    // 60bit long mantissa plus 4 Bit int exponent (decimal places):
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
    public constructor (rawNumberString: String, omitRounding: Boolean = false) { // or explicit RoundingMode?
        val decimalPair: Pair<Long, Int>? = mkDecimalParseOrNull(rawNumberString, (if (omitRounding) MAX_DECIMAL_PLACES; else autoRoundingConfig.decimalPlaces), false)
        if (decimalPair != null) {
            if (!isError(decimalPair.first, decimalPair.second)) {
                val (roundedMantissa, roundedDecimals) = roundWithMode(
                    decimalPair.first,
                    decimalPair.second,
                    (if (omitRounding) MAX_DECIMAL_PLACES; else autoRoundingConfig.decimalPlaces),
                    autoRoundingConfig.roundingMode
                )
                decimal64 = pack64(roundedMantissa, roundedDecimals)
            } else {
                decimal64 = pack64(decimalPair.first, decimalPair.second)
            }
        } else {
            // no exception. this branch won't happen because null was not allowed
            decimal64 = pack64(0, Error.NOT_A_NUMBER.ordinal)
        }
    }


    @Throws(ArithmeticException::class)
    public constructor (float: Float) : this(float.toString())
    @Throws(ArithmeticException::class)
    public constructor (double: Double) : this(double.toString())

    public constructor (other: Decimal) {
        decimal64 = other.decimal64
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
    // Secondary contructors cannot call other secondary contructors.
    // So constructors based on other integer types cannot simply call constructor(int.toLong())
    // Therefore, see the work-around invoke expressions in Companion object!

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

        if (!((mantissa == 0L) && (decimals != 0))) {

            // paranoid last checks!

            // most important, correct negative decimal places, as we don't support them!
            while (decimals < 0) {
                mantissa *= 10
                decimals++
            }

            // truncate any empty decimal places
            while ((decimals > 0) && (mantissa != 0L) && ((mantissa % 10) == 0L)) {
                //mantissa = (mantissa+5) / 10
                mantissa /= 10
                decimals--
            }

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
        val (newMantissa, newDecimals) = roundWithMode(mantissa, decimals, desiredDecimals, RoundingMode.CEILING)
        return Decimal(newMantissa, if (newMantissa == 0L)  0 else newDecimals)
    }

    public fun floor(desiredDecimals: Int = 0) : Decimal  {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(mantissa, decimals, desiredDecimals, RoundingMode.FLOOR)
        return Decimal(newMantissa, if (newMantissa == 0L)  0 else newDecimals)
    }

    public fun truncate(desiredDecimals: Int = 0) : Decimal  {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(mantissa, decimals, desiredDecimals, RoundingMode.DOWN)
        return Decimal(newMantissa, if (newMantissa == 0L)  0 else newDecimals)
    }

    public fun round(desiredDecimals: Int = 0) : Decimal  {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(mantissa, decimals, desiredDecimals, RoundingMode.HALF_EVEN)
        return Decimal(newMantissa, if (newMantissa == 0L)  0 else newDecimals)
    }


    public fun setScale(desiredDecimals: Int = autoRoundingConfig.decimalPlaces, rounding: RoundingMode = autoRoundingConfig.roundingMode): Decimal {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val roundingDecimals = min(MAX_DECIMAL_PLACES, desiredDecimals)
        val (newMantissa, newDecimals) = roundWithMode(mantissa, decimals, roundingDecimals, rounding)
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
    internal data class EqualizedDecimals(val thisMantissa:Long, val thatMantissa: Long, val commonDecimal: Int)

    private fun equalizeDecimals(thisM:Long, thisD: Int, thatM: Long, thatD: Int): EqualizedDecimals {
        // aligns both mantissas to a common decimal for further processing
        // error handling still missing!
        var thisMantissa = thisM
        var thisDecimals = thisD
        var thatMantissa = thatM
        var thatDecimals = thatD

        // error handling still missing!
        while (thisDecimals < thatDecimals) {
            thisMantissa *= 10
            thisDecimals++
        }
        while (thatDecimals < thisDecimals) {
            thatMantissa *= 10
            thatDecimals++
        }

        return EqualizedDecimals(thisMantissa, thatMantissa, thisDecimals)
    }

    private fun Long.isNegative() = (this.sign < 0)
    private fun Long.isPositive() = (this.sign >= 0)

    /***** operator plus (+) *****/

    @Throws(ArithmeticException::class)
    public fun plus(other: Decimal, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal {
        return DecimalArithmetics.arithmeticPlus(this, other, roundToPlaces, roundingMode)
    }
    public fun plus( otherDouble: Double, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = plus(otherDouble.toDecimal(),roundToPlaces, roundingMode)
    public fun plus( otherFloat: Float, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = plus(otherFloat.toDecimal(),roundToPlaces, roundingMode)
    public fun plus( otherLong: Long, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = plus(otherLong.toDecimal(),roundToPlaces, roundingMode)
    public fun plus( otherInt: Int, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = plus(otherInt.toDecimal(),roundToPlaces, roundingMode)
    public fun plus( otherShort: Short, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = plus(otherShort.toDecimal(),roundToPlaces, roundingMode)
    public fun plus( otherByte: Byte, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = plus(otherByte.toDecimal(),roundToPlaces, roundingMode)
    public fun plus( otherULong: ULong, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = plus(otherULong.toDecimal(),roundToPlaces, roundingMode)
    public fun plus( otherUInt: UInt, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = plus(otherUInt.toDecimal(),roundToPlaces, roundingMode)
    public fun plus( otherUShort: UShort, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = plus(otherUShort.toDecimal(),roundToPlaces, roundingMode)
    public fun plus( otherUByte: UByte, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = plus(otherUByte.toDecimal(),roundToPlaces, roundingMode)

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


    /***** operator minus (-) *****/

    @Throws(ArithmeticException::class)
    public fun minus(other: Decimal, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal {
        return DecimalArithmetics.arithmeticMinus(this, other, roundToPlaces, roundingMode)
    }
    public fun minus( otherDouble: Double, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = minus(otherDouble.toDecimal(),roundToPlaces, roundingMode)
    public fun minus( otherFloat: Float, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = minus(otherFloat.toDecimal(),roundToPlaces, roundingMode)
    public fun minus( otherLong: Long, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = minus(otherLong.toDecimal(),roundToPlaces, roundingMode)
    public fun minus( otherInt: Int, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = minus(otherInt.toDecimal(),roundToPlaces, roundingMode)
    public fun minus( otherShort: Short, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = minus(otherShort.toDecimal(),roundToPlaces, roundingMode)
    public fun minus( otherByte: Byte, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = minus(otherByte.toDecimal(),roundToPlaces, roundingMode)
    public fun minus( otherULong: ULong, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = minus(otherULong.toDecimal(),roundToPlaces, roundingMode)
    public fun minus( otherUInt: UInt, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = minus(otherUInt.toDecimal(),roundToPlaces, roundingMode)
    public fun minus( otherUShort: UShort, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = minus(otherUShort.toDecimal(),roundToPlaces, roundingMode)
    public fun minus( otherUByte: UByte, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = minus(otherUByte.toDecimal(),roundToPlaces, roundingMode)

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


    /***** operator times (*) *****/

    @Throws(ArithmeticException::class)
    public fun times(other: Decimal, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal {
        return DecimalArithmetics.arithmeticTimes(this, other, roundToPlaces, roundingMode)
    }
    public fun times( otherDouble: Double, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = times(otherDouble.toDecimal(),roundToPlaces, roundingMode)
    public fun times( otherFloat: Float, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = times(otherFloat.toDecimal(),roundToPlaces, roundingMode)
    public fun times( otherLong: Long, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = times(otherLong.toDecimal(),roundToPlaces, roundingMode)
    public fun times( otherInt: Int, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = times(otherInt.toDecimal(),roundToPlaces, roundingMode)
    public fun times( otherShort: Short, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = times(otherShort.toDecimal(),roundToPlaces, roundingMode)
    public fun times( otherByte: Byte, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = times(otherByte.toDecimal(),roundToPlaces, roundingMode)
    public fun times( otherULong: ULong, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = times(otherULong.toDecimal(),roundToPlaces, roundingMode)
    public fun times( otherUInt: UInt, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = times(otherUInt.toDecimal(),roundToPlaces, roundingMode)
    public fun times( otherUShort: UShort, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = times(otherUShort.toDecimal(),roundToPlaces, roundingMode)
    public fun times( otherUByte: UByte, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = times(otherUByte.toDecimal(),roundToPlaces, roundingMode)

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


    /***** operator div (/) *****/

    @Throws(ArithmeticException::class)
    public fun div(other: Decimal, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal {
        return DecimalArithmetics.arithmeticDiv(this, other, roundToPlaces, roundingMode)
    }
    public fun div( otherDouble: Double, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = div(otherDouble.toDecimal(),roundToPlaces, roundingMode)
    public fun div( otherFloat: Float, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = div(otherFloat.toDecimal(),roundToPlaces, roundingMode)
    public fun div( otherLong: Long, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = div(otherLong.toDecimal(),roundToPlaces, roundingMode)
    public fun div( otherInt: Int, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = div(otherInt.toDecimal(),roundToPlaces, roundingMode)
    public fun div( otherShort: Short, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = div(otherShort.toDecimal(),roundToPlaces, roundingMode)
    public fun div( otherByte: Byte, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = div(otherByte.toDecimal(),roundToPlaces, roundingMode)
    public fun div( otherULong: ULong, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = div(otherULong.toDecimal(),roundToPlaces, roundingMode)
    public fun div( otherUInt: UInt, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = div(otherUInt.toDecimal(),roundToPlaces, roundingMode)
    public fun div( otherUShort: UShort, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = div(otherUShort.toDecimal(),roundToPlaces, roundingMode)
    public fun div( otherUByte: UByte, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = div(otherUByte.toDecimal(),roundToPlaces, roundingMode)

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


    /************* operator rem (%) ************/


    @Throws(ArithmeticException::class)
    public fun rem(other: Decimal, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal {
        return DecimalArithmetics.arithmeticRem(this, other, roundToPlaces, roundingMode)
    }
    public fun rem( otherDouble: Double, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = rem(otherDouble.toDecimal(),roundToPlaces, roundingMode)
    public fun rem( otherFloat: Float, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = rem(otherFloat.toDecimal(),roundToPlaces, roundingMode)
    public fun rem( otherLong: Long, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = rem(otherLong.toDecimal(),roundToPlaces, roundingMode)
    public fun rem( otherInt: Int, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = rem(otherInt.toDecimal(),roundToPlaces, roundingMode)
    public fun rem( otherShort: Short, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = rem(otherShort.toDecimal(),roundToPlaces, roundingMode)
    public fun rem( otherByte: Byte, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = rem(otherByte.toDecimal(),roundToPlaces, roundingMode)
    public fun rem( otherULong: ULong, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = rem(otherULong.toDecimal(),roundToPlaces, roundingMode)
    public fun rem( otherUInt: UInt, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = rem(otherUInt.toDecimal(),roundToPlaces, roundingMode)
    public fun rem( otherUShort: UShort, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = rem(otherUShort.toDecimal(),roundToPlaces, roundingMode)
    public fun rem( otherUByte: UByte, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = rem(otherUByte.toDecimal(),roundToPlaces, roundingMode)

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


    /************ infix operator mod (mod) ***********/

    @Throws(ArithmeticException::class)
    public fun mod(other: Decimal, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal {
        return DecimalArithmetics.arithmeticMod(this, other, roundToPlaces, roundingMode)
    }
    public fun mod( otherDouble: Double, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = mod(otherDouble.toDecimal(),roundToPlaces, roundingMode)
    public fun mod( otherFloat: Float, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = mod(otherFloat.toDecimal(),roundToPlaces, roundingMode)
    public fun mod( otherLong: Long, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = mod(otherLong.toDecimal(),roundToPlaces, roundingMode)
    public fun mod( otherInt: Int, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = mod(otherInt.toDecimal(),roundToPlaces, roundingMode)
    public fun mod( otherShort: Short, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = mod(otherShort.toDecimal(),roundToPlaces, roundingMode)
    public fun mod( otherByte: Byte, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = mod(otherByte.toDecimal(),roundToPlaces, roundingMode)
    public fun mod( otherULong: ULong, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = mod(otherULong.toDecimal(),roundToPlaces, roundingMode)
    public fun mod( otherUInt: UInt, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = mod(otherUInt.toDecimal(),roundToPlaces, roundingMode)
    public fun mod( otherUShort: UShort, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = mod(otherUShort.toDecimal(),roundToPlaces, roundingMode)
    public fun mod( otherUByte: UByte, roundToPlaces: Int, roundingMode: RoundingMode = autoRoundingConfig.roundingMode): Decimal = mod(otherUByte.toDecimal(),roundToPlaces, roundingMode)

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


    /**********************  Other Math functions ***************************/

    // still missing: pow, pow(n), pow(Dc), sqrt

    public fun abs() : Decimal  {
        val (mantissa, decimals) = unpack64()
        return Decimal(abs(mantissa), decimals)
    }
    public val absoluteValue: Decimal
        get() {
            return this.abs()
        }


    public val sign : Decimal
        get() {
            val (mantissa, _) = unpack64()
            val sign = mantissa.sign
            return Decimal(sign)
        }


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
        val (rounded, _) = roundWithMode(mantissa, decimals, 0, roundingMode)
        return rounded
    }


    public override fun toDouble(): Double = this.toString().toDouble()
    public override fun toFloat(): Float = this.toString().toFloat()
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


     public  override fun toString() : String {
        if (isError()) return getError().toString()
        val (mantissa, decimals) = unpack64()
        var decimalString = toRawString(mantissa, decimals)

        // only for display!: complete minimum decimal places with "0"
        if (autoDisplayFormat.minDecimalPlaces > 0) {
            val missingPlaces = autoDisplayFormat.minDecimalPlaces - decimals
            if (decimals == 0) decimalString += '.'
            if (missingPlaces > 0) decimalString += ("0".repeat(missingPlaces))
        }
        return decimalString
     }


    public fun toScientificString() : String {
        if (isError()) return getError().toString()
        val (mantissa, decimals) = unpack64()
        if (mantissa == 0L) return "0E0"
        var decimalString: String
        val prefix : String
        if (mantissa < 0) {
            decimalString = (0L - mantissa).toString(10); prefix = "-"
        }
        else {
            decimalString = mantissa.toString(10); prefix = ""
        }

        val adjustedExp = (decimalString.count()-1) - decimals
        if (decimalString.count() > 1) decimalString = decimalString.take(1) + '.' + decimalString.substring(1).trimEnd('0')

        return prefix+decimalString+'E'+adjustedExp.toString(10)
    }

    public fun toFormattedString() : String {
        return toFormattedString(autoDisplayFormat)
    }
    public fun toFormattedString(displayFormat: DisplayFormat) : String {
            if (isError()) return getError().toString()
        // inserts thousands delimiters between groups of 3 digits dynamically, and adds minimum of decimal places
        // i.e., needs no formatting string and supports no overall minimum width; but no India lakh/crore format
        val groupingChar = displayFormat.groupingSeparator
        val groupingSeparatorString = if (groupingChar != null) groupingChar.toString(); else ""
        val decimalsSeparatorString = displayFormat.decimalSeparator.toString()
        val minDecimalPlaces = displayFormat.minDecimalPlaces
        var rawString = this.toString()
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
    public data class RoundingConfig(val decimalPlaces: Int = MAX_DECIMAL_PLACES, val roundingMode: RoundingMode = RoundingMode.HALF_UP) {
        public constructor (decimalPlaces: Int): this(decimalPlaces, autoRoundingConfig.roundingMode)
        init {
            require(decimalPlaces >= (0-MAX_DECIMAL_PLACES)) { "decimal places must be greater or equal -$MAX_DECIMAL_PLACES" }
            require(decimalPlaces <= MAX_DECIMAL_PLACES) { "decimal places must not be be greater than $MAX_DECIMAL_PLACES, is: $decimalPlaces" }
        }
    }

    // @JvmRecord
    public data class DisplayFormat(val groupingSeparator: Char? = null, val decimalSeparator : Char = '.', val minDecimalPlaces: Int = 0) {
        init {
            if (groupingSeparator != null) {
                require((groupingSeparator != decimalSeparator)) { "Grouping separator and decimal separator may not be equal '$groupingSeparator'" }
            }
            //require((decimalSeparator != '.')) { "No dot as decimalSeparator, '$decimalSeparator'" }
            require(minDecimalPlaces >= 0) { "decimal places must be greater or equal 0" }
            require(minDecimalPlaces <= MAX_LONG_SIGNIFICANTS) { "decimal places must not be be greater than $MAX_LONG_SIGNIFICANTS), is: $minDecimalPlaces" }
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

        internal fun mkDecimalOrNull(numberString: String, omitRounding: Boolean = false): Decimal? {
            val decimalPair: Pair<Long, Int>? = mkDecimalParseOrNull(numberString, (if (omitRounding) MAX_DECIMAL_PLACES; else autoRoundingConfig.decimalPlaces), true)
            return if (decimalPair != null) {
                val (roundedMantissa, roundedDecimals) = roundWithMode(decimalPair.first, decimalPair.second, (if (omitRounding) MAX_DECIMAL_PLACES; else autoRoundingConfig.decimalPlaces), autoRoundingConfig.roundingMode)
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

        internal var autoRoundingConfig: RoundingConfig = RoundingConfig()
        internal var autoDisplayFormat: DisplayFormat = DisplayFormat()

        // for automatic rounding
        //internal var autoRoundingConfig.decimalPlaces: Int = MAX_DECIMAL_PLACES /* 0 - 15 */

        public fun setRoundingConfig(roundingConfig: RoundingConfig) {
            val setD = when {
                (roundingConfig.decimalPlaces > MAX_DECIMAL_PLACES) -> MAX_DECIMAL_PLACES
                (roundingConfig.decimalPlaces < 0) -> 0
                else -> roundingConfig.decimalPlaces
            }
            autoRoundingConfig = RoundingConfig(setD, roundingConfig.roundingMode)
        }

        public fun setDisplayFormat(displayFormat: DisplayFormat) {
            autoDisplayFormat = displayFormat
        }

        public fun setMaxDecimalPlaces(maxDecimalPlaces: Int) {
            setRoundingConfig(RoundingConfig(maxDecimalPlaces))
        }
        public fun getMaxDecimalPlaces(): Int = autoRoundingConfig.decimalPlaces

        // private var autoFormatString: String = "#,###,###,##0.00"
        // ??? important for India: lakh/crore system? otherwise toFormattedString() is sufficient

        public fun setRoundingMode(mode: RoundingMode) {
            setRoundingConfig(RoundingConfig(autoRoundingConfig.decimalPlaces, mode))
        }

        public fun getRoundingMode():RoundingMode = autoRoundingConfig.roundingMode

        /*
        // only for toString()! Remove when support for numeric formatting is added?
        private var autoMinDisplayDecimals: Int = 0 /*  0 - max */
        public fun setMinDecimals(mind: Int) {
            require(mind >= 0) { "minDecimals must be non-negative, was $mind" }
            autoMinDisplayDecimals = if (mind < 0) 0; else mind
        }
        public fun getMinDecimals(): Int = autoMinDisplayDecimals
        */

        /***************************  Simple output core routine   ***************************/

        internal fun toRawString(mantissa: Long, decimals: Int) : String {
            if (mantissa == 0L) {
                if (decimals == 0) return "0"
                return getError(decimals).toString()
            }
            var decimalString: String
            val prefix : String
            if (mantissa < 0) {
                decimalString = (0L - mantissa).toString(10); prefix = "-"
            }
            else {
                decimalString = mantissa.toString(10); prefix = ""
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
        // see also below

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



    }  // end of companion object


    /**************************** Error Handling  ********************************/

    // If shallThrowOnError is false, errors are embedded into decimal places instead, while mantissa is 0
    // which on other words means that decimal64 is greater 0, but lower the 16 (0x10)
    // this allows for only up to 14 error conditions, so better be thrifty with them

    public fun isError(): Boolean {
        return ((decimal64 > 0L) && (decimal64 <= MAX_DECIMAL_PLACES))
    }

    public fun getError(): Error {
        if ((decimal64 > 0L) && (decimal64 <= MAX_DECIMAL_PLACES) && (decimal64.toInt() < Error.entries.count())) return Error.entries[decimal64.toInt()]
        return Error.NO_ERROR
    }



}





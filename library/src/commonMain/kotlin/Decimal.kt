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

    public constructor (rawNumberString: String) { // or explicit RoundingMode?
        val decimalPair: Pair<Long, Int>? = mkDecimalParseOrNull(rawNumberString, autoDecimalPlaces, false)
        if (decimalPair != null) {
            if (!isError(decimalPair.first, decimalPair.second)) {
                val (roundedMantissa, roundedDecimals) = roundWithMode(
                    decimalPair.first,
                    decimalPair.second,
                    autoDecimalPlaces,
                    autoRoundingMode
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


    public constructor (input: Float) : this(input.toString())
    public constructor (input: Double) : this(input.toString())

    public constructor (other: Decimal) {
        decimal64 = other.decimal64
    }

    internal constructor (mantissa: Long, decimalPlaces: Int) {
        decimal64 = pack64(mantissa, decimalPlaces)
    }

    public constructor (mantissa:Long) {
        if (abs(mantissa) > MAX_MANTISSA_VALUE) {
            // a single value will overflow
            val errorCode = generateErrorCode(Error.NUMERIC_OVERFLOW, "$mantissa cannot fit into a Decimal")
             decimal64 = pack64(0,errorCode)
         } else {
            decimal64 = pack64(mantissa, 0)
        }
    }

    // for all constructors based on other integer types see also the invoke expressions in Companion object!

    /**************************** Packing / Unpacking Helper Methods  ********************************/

    private fun unpack64(): Pair<Long, Int> {
        val decimals: Int = (decimal64 and MAX_DECIMAL_PLACES.toLong()).toInt()
        val mantissa: Long = (decimal64 shr 4)
        if ((mantissa == 0L) && (decimals != 0)) {
            val error = getError(decimals)
            generateErrorCode(error, "")
         }
        return Pair(mantissa, decimals)
    }

    private fun pack64(pMantissa: Long, pDecimals: Int): Long {
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

    public fun ceil() : Decimal  {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(mantissa, decimals, 0, RoundingMode.CEILING)
        return Decimal(newMantissa, if (newMantissa == 0L)  0 else newDecimals)
    }

    public fun floor() : Decimal  {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(mantissa, decimals, 0, RoundingMode.FLOOR)
        return Decimal(newMantissa, if (newMantissa == 0L)  0 else newDecimals)
    }

    public fun truncate() : Decimal  {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(mantissa, decimals, 0, RoundingMode.DOWN)
        return Decimal(newMantissa, if (newMantissa == 0L)  0 else newDecimals)
    }

    public fun round() : Decimal  {
        if (isError()) return clone()
        val (mantissa, decimals) = unpack64()
        val (newMantissa, newDecimals) = roundWithMode(mantissa, decimals, 0, RoundingMode.HALF_EVEN)
        return Decimal(newMantissa, if (newMantissa == 0L)  0 else newDecimals)
    }


    public fun setScale(desiredDecimals: Int, rounding: RoundingMode = autoRoundingMode): Decimal {
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

    public operator fun plus(other: Decimal) : Decimal {
        if (isError(this) or isError(other)) return clone()
        val (thisMantissa, thisDecimals) = unpack64()
        if (thisMantissa == 0L) return other.clone()
        val (otherMantissa, otherDecimals) = other.unpack64()
        if (otherMantissa == 0L) return clone()
        val (equalizedThisMantissa,equalizedOtherMantissa, equalizedDecimals) = equalizeDecimals(thisMantissa, thisDecimals, otherMantissa, otherDecimals)
        println("Addition: this: $equalizedThisMantissa other: $equalizedOtherMantissa, sum: ${equalizedThisMantissa + equalizedOtherMantissa}")
        if (equalizedThisMantissa.isNegative() == equalizedOtherMantissa.isNegative() ) {
            // addition might overflow!
            val space: Long = MAX_MANTISSA_VALUE - abs(equalizedThisMantissa)
            if (space <= abs(equalizedOtherMantissa)) {
                return generateErrorDecimal(Error.ADD_OVERFLOW, "$this + $other result does not fit into Decimal")
             }
        }
        val equalizedMantissaSum = equalizedThisMantissa + equalizedOtherMantissa
        val (roundedMantissa, roundedDecimals) = roundWithMode(equalizedMantissaSum, equalizedDecimals,autoDecimalPlaces, autoRoundingMode)
        return Decimal(roundedMantissa, roundedDecimals)
    }
    public operator fun plus(other: Double) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: Float) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: Long) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: Int) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: Short) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: Byte) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: ULong) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: UInt) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: UShort) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: UByte) : Decimal = plus(other.toDecimal())


    /***** operator minus (-) *****/

    public operator fun minus(other: Decimal) : Decimal {
        if (isError(this) or isError(other)) return clone()
        val (thisMantissa, thisDecimals) = unpack64()
        if (thisMantissa == 0L) return other.clone()
        val (otherMantissa, otherDecimals) = other.unpack64()
        if (otherMantissa == 0L) return clone()
         val (equalizedThisMantissa, equalizedOtherMantissa, equalizedDecimals) = equalizeDecimals(
            thisMantissa,
            thisDecimals,
            otherMantissa,
            otherDecimals
        )
        println("Subtraction: this: $equalizedThisMantissa other: $equalizedOtherMantissa, diff: ${equalizedThisMantissa - equalizedOtherMantissa}")
        if (equalizedThisMantissa.isNegative() != equalizedOtherMantissa.isNegative()) {
            // subtraction is addition and might overflow!
            val space: Long = MAX_MANTISSA_VALUE - abs(equalizedThisMantissa)
            if (space <= abs(equalizedOtherMantissa)) {
                return generateErrorDecimal(Error.SUBTRACT_OVERFLOW, "$this - $other result does not fit into Decimal")
            }
        }
        val equalizedMantissaSum = equalizedThisMantissa - equalizedOtherMantissa
        val (roundedMantissa, roundedDecimals) = roundWithMode(
            equalizedMantissaSum,
            equalizedDecimals,
            autoDecimalPlaces,
            autoRoundingMode
        )
        return Decimal(roundedMantissa, roundedDecimals)
    }
    public operator fun minus(other: Double) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: Float) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: Long) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: Int) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: Short) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: Byte) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: ULong) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: UInt) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: UShort) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: UByte) : Decimal = minus(other.toDecimal())


    /***** operator times (*) *****/

    private fun willOverflowLong(a: Long, b: Long): Boolean{
        if ((a > 0) && (b > 0) && (a > (Long.MAX_VALUE / b))) return true
        if ((a > 0) && (b < 0) && (b < (Long.MIN_VALUE / a))) return true
        if ((a < 0) && (b > 0) && (a < (Long.MIN_VALUE / b))) return true
        return( (a < 0) && (b < 0) && (a < (Long.MAX_VALUE / b)))
    }

    private fun willOverflowMantissa(mantissa: Long, decimals: Int): Boolean {
        // this assumes later rounding to autoDecimalPlaces!
        // any tolerated overflow must be guaranteed to be removed later when being rounded away
        if ((abs(mantissa) > (MAX_MANTISSA_VALUE*10)) && (decimals <= (autoDecimalPlaces-2))) return true
        if ((abs(mantissa) > MAX_MANTISSA_VALUE ) && (decimals <= (autoDecimalPlaces-1))) return true
        return false
    }

    public operator fun times(other: Decimal) : Decimal {
        if (isError(this) or isError(other)) return clone()
        var (thisMantissa, thisDecimals) = unpack64()
        var (otherMantissa, otherDecimals) = other.unpack64()
        println("Multiplication: this: $thisMantissa other: $otherMantissa, product: ${thisMantissa * otherMantissa}")

        // 0, no rounding needed
        if ((thisMantissa == 0L) || (otherMantissa == 0L)) return Decimal(0,0)

        // temporary compression may help avoid overflow in some cases
        while ((thisMantissa % 10) == 0L) { thisMantissa /= 10; thisDecimals --  }
        while ((otherMantissa % 10) == 0L) { otherMantissa /= 10; otherDecimals --  }

       if (willOverflowLong(thisMantissa, otherMantissa)) {
           return generateErrorDecimal(Error.MULTIPLY_OVERFLOW, "$this * $other result does not fit into Decimal")
        }
        var resultMantissa = thisMantissa * otherMantissa
        var resultDecimals = thisDecimals + otherDecimals
        if (resultDecimals < 0) {
            val pw10 = getPower10(0-resultDecimals)
            resultMantissa *= pw10
            resultDecimals = 0
        }
        if (willOverflowMantissa(resultMantissa, resultDecimals)) {
            return generateErrorDecimal(Error.MULTIPLY_OVERFLOW, "$this * $other = ${toRawString(resultMantissa, resultDecimals)} result does not fit into Decimal")
        }
        val (roundedMantissa, roundedDecimals) = roundWithMode(resultMantissa, resultDecimals,autoDecimalPlaces, autoRoundingMode)
        return Decimal(roundedMantissa, roundedDecimals)
    }
    public operator fun times(other: Double) : Decimal = times(other.toDecimal())
    public operator fun times(other: Float) : Decimal = times(other.toDecimal())
    public operator fun times(other: Long) : Decimal = times(other.toDecimal())
    public operator fun times(other: Int) : Decimal = times(other.toDecimal())
    public operator fun times(other: Short) : Decimal = times(other.toDecimal())
    public operator fun times(other: Byte) : Decimal = times(other.toDecimal())
    public operator fun times(other: ULong) : Decimal = times(other.toDecimal())
    public operator fun times(other: UInt) : Decimal = times(other.toDecimal())
    public operator fun times(other: UShort) : Decimal = times(other.toDecimal())
    public operator fun times(other: UByte) : Decimal = times(other.toDecimal())


    /***** operator div (/) *****/

    public operator fun div(other: Decimal) : Decimal {
        if (isError(this) or isError(other)) return clone()
        var (thisMantissa, thisDecimals) = unpack64()
        val (otherMantissa, otherDecimals) = other.unpack64()
        println("Division: this: $thisMantissa other: $otherMantissa, result: ${if (otherMantissa==0L) "Error" else (thisMantissa / otherMantissa)}")
        if (otherMantissa == 0L) {
            return generateErrorDecimal(Error.DIVISION_BY_0, "$this is divided by 0")
         }
        // manual division
        while ((thisDecimals - otherDecimals) < MAX_DECIMAL_SIGNIFICANTS) {
            if ((otherMantissa * (thisMantissa / otherMantissa)) == thisMantissa) break // rest == 0, done
            if (abs(thisMantissa) > (Long.MAX_VALUE/10)) {
                // would otherwise overflow (but this is ok), stop now
                break
            }
            thisMantissa *=10; thisDecimals++
        }
        val resultMantissa = (thisMantissa / otherMantissa)
        val resultDecimals = (thisDecimals - otherDecimals)

        // rounding
        val (roundedMantissa, roundedDecimals) = roundWithMode(resultMantissa, resultDecimals, autoDecimalPlaces, autoRoundingMode)
        return Decimal(roundedMantissa, roundedDecimals)
    }
    public operator fun div(other: Double) : Decimal = div(other.toDecimal())
    public operator fun div(other: Float) : Decimal = div(other.toDecimal())
    public operator fun div(other: Long) : Decimal = div(other.toDecimal())
    public operator fun div(other: Int) : Decimal = div(other.toDecimal())
    public operator fun div(other: Short) : Decimal = div(other.toDecimal())
    public operator fun div(other: Byte) : Decimal = div(other.toDecimal())
    public operator fun div(other: ULong) : Decimal = div(other.toDecimal())
    public operator fun div(other: UInt) : Decimal = div(other.toDecimal())
    public operator fun div(other: UShort) : Decimal = div(other.toDecimal())
    public operator fun div(other: UByte) : Decimal = div(other.toDecimal())


    /***** operator rem (%), but what about modulo/mod ? *****/

    private fun integerDivision(other: Decimal) : Decimal {
        var (thisMantissa, thisDecimals) = unpack64()
        val (otherMantissa, otherDecimals) = other.unpack64()
        if (otherMantissa == 0L) {
            return generateErrorDecimal(Error.DIVISION_BY_0, "$this is divided by 0")
        }
        while (abs(thisMantissa) < abs(otherMantissa)){
            thisMantissa *=10; thisDecimals++
        }
        val resultMantissa = (thisMantissa / otherMantissa)
        val resultDecimals = (thisDecimals - otherDecimals)
        // rounding??? no rounding.
        return Decimal(resultMantissa, resultDecimals)
    }


    public operator fun rem(other:Decimal) : Decimal {
        if (isError(this) or isError(other)) return clone()
        val result = (this.integerDivision(other))
        println("Remainder: this: $this, other: $other, result: ${(this - (other * result))}")
        // rounding???
        return (this - (other * result))
    }
    public operator fun rem(other: Double) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: Float) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: Long) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: Int) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: Short) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: Byte) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: ULong) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: UInt) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: UShort) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: UByte) : Decimal = rem(other.toDecimal())

    /************ infix operator mod ***********/

     public infix fun mod(other:Decimal) : Decimal {
        if (isError(this) or isError(other)) return clone()
        val quotient = (this / other)
        val flooredQuotient = floor(quotient)
        println("Modulo: a: $this, m: $other, quotient: $quotient, flquotient: $flooredQuotient,  m*flquotient: ${other * flooredQuotient},  a-(m*flquotient): ${(this - (other * flooredQuotient))}")
        // rounding???
        return (this - (other * flooredQuotient))
    }

    public infix fun mod(other: Double) : Decimal = mod(other.toDecimal())
    public infix fun mod(other: Float) : Decimal = mod(other.toDecimal())
    public infix fun mod(other: Long) : Decimal = mod(other.toDecimal())
    public infix fun mod(other: Int) : Decimal = mod(other.toDecimal())
    public infix fun mod(other: Short) : Decimal = mod(other.toDecimal())
    public infix fun mod(other: Byte) : Decimal = mod(other.toDecimal())
    public infix fun mod(other: ULong) : Decimal = mod(other.toDecimal())
    public infix fun mod(other: UInt) : Decimal = mod(other.toDecimal())
    public infix fun mod(other: UShort) : Decimal = mod(other.toDecimal())
    public infix fun mod(other: UByte) : Decimal = mod(other.toDecimal())


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
        if (autoDeprecatedMinDecimals > 0) {
            val missingPlaces = autoDeprecatedMinDecimals - decimals
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

    public fun toFormattedString(thousandsDelimiter: Char = ',', decimalDelimiter: Char = '.', minDecimalPlaces: Int = autoDeprecatedMinDecimals) : String {
        if (isError()) return getError().toString()
        // inserts thousands delimiters between groups of 3 digits dynamically, and adds minimum of decimal places
        // i.e., needs no formatting string and supports no overall minimum width
        require (thousandsDelimiter != decimalDelimiter) {"Thousands separator and decimal separator must not be identical"}
        val thousandsString = thousandsDelimiter.toString()
        val decimalsString = decimalDelimiter.toString()
        var rawString = this.toString()
        var integerPart: String
        var decimalPart: String
        val decimalPosition = rawString.indexOf(".")
        if (decimalPosition >= 0) {
            decimalPart = rawString.substring(decimalPosition)
            integerPart = rawString.take(decimalPosition+1)
        } else {
            decimalPart = ""
            integerPart = rawString
        }

        rawString = integerPart.reversed()
            .chunked(3)
            .joinToString(thousandsString)
            .reversed()
        if (decimalPosition >= 0) {
          rawString = buildString {
              append(rawString)
              append(decimalsString)
              append(decimalPart)
          }
        }

        if (minDecimalPlaces > 0) {
            val decimals = decimalPart.length
            val  missingPlaces = minDecimalPlaces - decimals
            if (decimals <= 0) rawString += decimalDelimiter
            if (missingPlaces > 0) rawString += ("0".repeat(missingPlaces))
        }

        return rawString
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
        public operator fun invoke(input:Byte): Decimal = Decimal(input.toLong())
        public operator fun invoke(input:UByte): Decimal = Decimal(input.toLong())
        public operator fun invoke(input:Short): Decimal = Decimal(input.toLong())
        public operator fun invoke(input:UShort): Decimal = Decimal(input.toLong())
        public operator fun invoke(input:Int): Decimal = Decimal(input.toLong())
        public operator fun invoke(input:UInt): Decimal= Decimal(input.toLong())
        //public operator fun invoke(input:Long): Decimal = Decimal(input)
        public operator fun invoke(input:ULong): Decimal = Decimal(input.toLong())

        internal fun mkDecimalOrNull(numberString: String): Decimal? {
            val decimalPair: Pair<Long, Int>? = mkDecimalParseOrNull(numberString, autoDecimalPlaces, true)
            return if (decimalPair != null) {
                val (roundedMantissa, roundedDecimals) = roundWithMode(decimalPair.first, decimalPair.second, min(autoDecimalPlaces, MAX_DECIMAL_PLACES), autoRoundingMode)
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


        // for automatic rounding
        internal var autoDecimalPlaces: Int = 15 /* 0 - 15 */
        public fun setMaxDecimalPlaces(maxDecimalPlaces: Int) {
            require(maxDecimalPlaces >= 0) { "maxDecimalPlaces must be non-negative, was $maxDecimalPlaces" }
            // here: throw Exception if argument below 0?
            autoDecimalPlaces = if (maxDecimalPlaces < 0) {
                0
            } else if (maxDecimalPlaces > 15) {
                15
            } else maxDecimalPlaces
        }
        public fun getMaxDecimalPlaces(): Int = autoDecimalPlaces

        private var autoDecimalSeparator: Char = '.'
        private var autoGroupingSeparator: Char = ','
        private var autoFormatString: String = "#,###,###,##0.00"

        private var autoRoundingMode: RoundingMode = RoundingMode.HALF_UP
        public fun setRoundingMode(mode: RoundingMode) {
            autoRoundingMode = mode
        }
        public fun getRoundingMode():RoundingMode = autoRoundingMode


        // only for toString()! Remove when support for numeric formatting is added?
        private var autoDeprecatedMinDecimals: Int = 0 /*  0 - max */
        public fun setMinDecimals(mind: Int) {
            require(mind >= 0) { "minDecimals must be non-negative, was $mind" }
            autoDeprecatedMinDecimals = if (mind < 0) 0; else mind
        }
        public fun getMinDecimals(): Int = autoDeprecatedMinDecimals

        /***************************  Simple output routine   ***************************/

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

    internal fun getErrorName(): String {
        val err: Error = if (decimal64 == 0L) {
            Error.NO_ERROR
        } else if ((decimal64 > 0L) && (decimal64 <= MAX_DECIMAL_PLACES) && (decimal64.toInt() < Error.entries.count())) {
            Error.entries[decimal64.toInt()]
        } else {
            Error.NO_ERROR
        }
        return err.toString()
    }



}





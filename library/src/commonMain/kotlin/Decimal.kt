package io.github.astridha.decimal

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

    internal enum class ArithmeticErrors { // max 15, for 4 Byte decimals field when mantissa == 0!
        OK,
        NOT_A_NUMBER,
        PARSING_OVERFLOW,
        ADD_OVERFLOW,
        SUBTRACT_OVERFLOW,
        MULTIPLY_OVERFLOW,
        DIVIDE_OVERFLOW,
        INC_OVERFLOW,
        DEC_OVERFLOW,
        OTHER_OVERFLOW,
        DIVISION_BY_0,
        ROUNDING_FAILED

    }

    internal fun isDecimalError(mantissa: Long, decimalPlaces: Int) : Boolean {
        if (mantissa !=0L) return false
        return (decimalPlaces !=0)
    }

    /***********************  Secondary Constructors  ************************/

    // see also the invoke expressions in Companion object, for all constructors based on integer types!

    public constructor (rawNumberString: String) { // oder ecpliziter RoundingMode?
        val decimalPair: Pair<Long, Int>? = mkDecimalParseOrNull(rawNumberString, autoDecimalPlaces, false)
        if (decimalPair != null) {
            if (!Decimal.isDecimalError(decimalPair.first, decimalPair.second)) {
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
            decimal64 = pack64(0, ArithmeticErrors.NOT_A_NUMBER.ordinal)
        }
    }


    public constructor (input:Float): this(input.toString())
    public constructor (input:Double): this(input.toString())

    public constructor (other: Decimal) { decimal64 = other.decimal64 }

    internal constructor (mantissa: Long, decimalPlaces: Int, omitNormalize:Boolean)  {
        decimal64 = pack64(mantissa,decimalPlaces)
    }

    /**************************** Private Helper Methods  ********************************/



    private fun unpack64(): Pair<Long, Int> {
        val decimals: Int = (decimal64 and 0x0FL).toInt()
        val mantissa: Long = (decimal64 shr 4)
        if ((mantissa == 0L) and (decimals != 0) && shallThrowOnError) {
            when (decimals) {
                ArithmeticErrors.NOT_A_NUMBER.ordinal -> throw NumberFormatException("INVALID NUMBER FORMAT")
                ArithmeticErrors.OTHER_OVERFLOW.ordinal -> throw ArithmeticException("ARITHMETIC OVERFLOW")
                ArithmeticErrors.DIVISION_BY_0.ordinal -> throw ArithmeticException("DIVISION BY 0")
                ArithmeticErrors.ROUNDING_FAILED.ordinal -> throw ArithmeticException("ROUNDING FAILED")
            }
        }
        return Pair(mantissa, decimals)
    }

    private fun pack64(pmantissa: Long, pdecimals: Int): Long {
        var mantissa = pmantissa
        //var decimals =  if (mantissa == 0L) 0; else pdecimals
        var decimals = pdecimals

        if (!((mantissa == 0L) and (decimals != 0))) {

            // most important, correct negative decimal places, as we don't support them!
            while (decimals < 0) {
                mantissa *= 10
                decimals++
            }

            // truncate any empty decimal places
            while ((decimals > 0) and (mantissa != 0L) and ((mantissa % 10) == 0L)) {
                //mantissa = (mantissa+5) / 10
                mantissa /= 10
                decimals--
            }

            if ((abs(mantissa) > MAX_VALUE) or (decimals > MAX_DECIMAL_PLACES)) {
                if (shallThrowOnError) throw ArithmeticException("DECIMAL OVERFLOW: mantissa $mantissa with $decimals decimals")
                mantissa = 0L
                decimals = ArithmeticErrors.OTHER_OVERFLOW.ordinal
            }
        }

        return ((mantissa shl 4) or (decimals and MAX_DECIMAL_PLACES).toLong())
    }




    /*******************  Rounding functions  *********************************/

    public fun ceil() : Decimal  {
        val (mantissa, decimals) = unpack64()
        val (newmantissa, newdecimalplaces) = roundWithMode(mantissa, decimals, 0, RoundingMode.CEILING)
        return Decimal(newmantissa, if (newmantissa == 0L)  0 else newdecimalplaces,true)
    }

    public fun floor() : Decimal  {
        val (mantissa, decimals) = unpack64()
        val (newmantissa, newdecimalplaces) = roundWithMode(mantissa, decimals, 0, RoundingMode.FLOOR)
        return Decimal(newmantissa, if (newmantissa == 0L)  0 else newdecimalplaces,true)
    }

    public fun truncate() : Decimal  {
        val (mantissa, decimals) = unpack64()
        val (newmantissa, newdecimalplaces) = roundWithMode(mantissa, decimals, 0, RoundingMode.DOWN)
        return Decimal(newmantissa, if (newmantissa == 0L)  0 else newdecimalplaces,true)
    }

    public fun round() : Decimal  {
        val (mantissa, decimals) = unpack64()
        val (newmantissa, newdecimalplaces) = roundWithMode(mantissa, decimals, 0, RoundingMode.HALF_EVEN)
        return Decimal(newmantissa, if (newmantissa == 0L)  0 else newdecimalplaces,true)
    }


    public fun setScale(desiredDecimals: Int, rounding: RoundingMode = autoRoundingMode): Decimal {
        val (mantissa, decimals) = unpack64()
        val roundToDecimals = min(MAX_DECIMAL_PLACES, desiredDecimals) // or: min(MAX_DECIMALS, desiredprecision), and ignore autoPrecision?
        val (newmantissa, newdecimalplaces) = roundWithMode(mantissa, decimals, roundToDecimals, rounding)
        return Decimal(newmantissa, if (newmantissa == 0L)  0 else newdecimalplaces,true)
    }

    /*******************  Operator Overloads  ******************/

    /***  unary operators ***/

    /*** operator unaryPlus (+) , unaryMinus (-) and not() (!) ***/
    public operator fun unaryPlus() : Decimal = this

    public operator fun unaryMinus() : Decimal {
        var (mantissa, decimals) = unpack64()
        mantissa = (0-mantissa)
        return Decimal(mantissa, decimals, true)
    }

    public operator fun not() : Boolean = (decimal64 == 0L)


    /***** operator unaryIncrement (++) , unaryDecrement (--)  *****/

    public operator fun inc() : Decimal {
        val (mantissa, decimals) = unpack64()
        val decimalstep = getPower10(decimals)
        return Decimal(mantissa+decimalstep, decimals, true)
    }
    public operator fun dec() : Decimal {
        val (mantissa, decimals) = unpack64()
        val decimalstep = getPower10(decimals)
        return Decimal(mantissa-decimalstep, decimals, true)
    }

    /*********************  Arithmetic operator overloads  **************************/
    internal data class EqualizedDecimals(val thism:Long, val thatm: Long, val deci: Int)

    private fun equalizeDecimals(thism:Long, thisd: Int, thatm: Long, thatd: Int): EqualizedDecimals {
        // error handling still missing!
        var thismantissa = thism
        var thisdecimals = thisd
        var thatmantissa = thatm
        var thatdecimals = thatd

        // error handling still missing!
        while (thisdecimals < thatdecimals) {
            thismantissa *= 10
            thisdecimals++
        }
        while (thatdecimals < thisdecimals) {
            thatmantissa *= 10
            thatdecimals++
        }

        return EqualizedDecimals(thismantissa, thatmantissa, thisdecimals)
    }

    private fun Long.isNegative() = (this.sign < 0)
    private fun Long.isPositive() = (this.sign >= 0)

    /***** operator plus (+) *****/

    public operator fun plus(other: Decimal) : Decimal {
        if (isDecimalError(this) or isDecimalError(other)) return this
        val (thisMantissa, thisDecimals) = unpack64()
        val (otherMantissa, otherDecimals) = other.unpack64()
        val (equalizedThisMantissa,equalizedOtherMantissa, equalizedDecimals) = equalizeDecimals(thisMantissa, thisDecimals, otherMantissa, otherDecimals)
        println("Addition: this: $equalizedThisMantissa other: $equalizedOtherMantissa, sum: ${equalizedThisMantissa + equalizedOtherMantissa}")
        if (equalizedThisMantissa.isNegative() == equalizedOtherMantissa.isNegative() ) {
        //if (thisMantissa > 0 ) { // HACK!!!
            println("!")
            // a single value moght overflow
            // addition might overflow!
            var space: Long = MAX_VALUE - abs(equalizedThisMantissa)
            if (space <= equalizedOtherMantissa) {
                if (shallThrowOnError) throw ArithmeticException("OVERFLOW on addition: $this + $other")
                return Decimal(0, ArithmeticErrors.ADD_OVERFLOW.ordinal, true)
            }
        } else {
            // addition cannot overflow
        }
        var equalizedMantissaSum = equalizedThisMantissa + equalizedOtherMantissa

        val (roundedMantissa, roundedDecimals) = roundWithMode(equalizedMantissaSum, equalizedDecimals,autoDecimalPlaces, autoRoundingMode)
        return Decimal(roundedMantissa, roundedDecimals, true)
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
        if (isDecimalError(this) or isDecimalError(other)) return this
        val (thism, thisd) = unpack64()
        val (thatm, thatd) = other.unpack64()
        val (thismantissa,thatmantissa, decimals) = equalizeDecimals(thism, thisd, thatm, thatd)
        return Decimal(thismantissa-thatmantissa, decimals, true)
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

    public operator fun times(other: Decimal) : Decimal {
        if (isDecimalError(this) or isDecimalError(other)) return this
        val (thisMantissa, thisDecimals) = unpack64()
        val (otherMantissa, otherDecimals) = other.unpack64()

        val resultMantissa = thisMantissa * otherMantissa
        val resultDecimals = thisDecimals + otherDecimals
        if (resultMantissa/thisMantissa != otherMantissa) { // is this the best way to detect overflow?
            if (shallThrowOnError) throw ArithmeticException("Multiplication Overflow: $this * $other")
            return Decimal(0L, ArithmeticErrors.MULTIPLY_OVERFLOW.ordinal, true)
        }
        val (roundedMantissa, roundedDecimals) = roundWithMode(resultMantissa, resultDecimals,autoDecimalPlaces, autoRoundingMode)
        return Decimal(roundedMantissa, roundedDecimals, true)
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
        if (isDecimalError(this) or isDecimalError(other)) return this
        var (thisMantissa, thisDecimals) = unpack64()
        val (otherMantissa, otherDecimals) = other.unpack64()
        if (otherMantissa == 0L) {
            if (shallThrowOnError) throw ArithmeticException("Division by 0")
            return Decimal(0, ArithmeticErrors.DIVISION_BY_0.ordinal,true)
        }
        // manual devision
        while ((thisDecimals - otherDecimals) < MAX_DECIMAL_SIGNIFICANTS) {
            if ((otherMantissa * (thisMantissa / otherMantissa)) == thisMantissa) break // rest 0, done
            if (abs(thisMantissa) > (Long.MAX_VALUE/10)) {
                //println("Ups, OVERFLOW on division: $this / $other\"")
                // would otherwise overflow (this is ok)
                break
            }
            thisMantissa *=10; thisDecimals++
        }
        var resultMantissa = (thisMantissa / otherMantissa)
        var resultDecimals = (thisDecimals - otherDecimals)

        // rounding
        val (roundedMantissa, roundedDecimals) = roundWithMode(resultMantissa, resultDecimals, autoDecimalPlaces, autoRoundingMode)
        return Decimal(roundedMantissa, roundedDecimals, true)
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

    private fun integerdivided(other: Decimal) : Decimal {
        if (isDecimalError(this) or isDecimalError(other)) return this
        var (thisMantissa, thisDecimals) = unpack64()
        var (otherMantissa, otherDecimals) = other.unpack64()
        if (otherMantissa == 0L) {
            if (shallThrowOnError) throw ArithmeticException("Division by 0")
            return Decimal(0, ArithmeticErrors.DIVISION_BY_0.ordinal,true)
        }
        // preserve from running endlessly if thism cannot reach thatm!
        if (otherMantissa > (Long.MAX_VALUE/10)) {
            otherMantissa /= 10; otherDecimals--
        }
        while (thisMantissa < otherMantissa){
            //if ((thism * (thism / thatm)) == thatm) break
            thisMantissa *=10; thisDecimals++
        }
        // rounding???
        return Decimal(thisMantissa/otherMantissa, thisDecimals-otherDecimals, false)
    }

    public operator fun rem(other:Decimal) : Decimal {
       val divisionresult = (this.integerdivided(other))
        return (this - (other * divisionresult))
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


    /**********************  Other Math functions ***************************/

    // still missing: pow, pow(n), pow(Dc), sqrt

    public fun abs() : Decimal  {
        val (mantissa, decimals) = unpack64()
        return Decimal(abs(mantissa), decimals, true)
    }
    public val absoluteValue: Decimal
        get() {
            return this.abs()
        }


    public val sign : Decimal
        get() {
            val (mantissa, _) = unpack64()
            val msign = mantissa.sign
            return Decimal(msign)
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
        val (newmantissa, _) = roundWithMode(mantissa, decimals, 0, roundingMode)
        return newmantissa
    }


    public override fun toDouble(): Double = this.toRawDecimalString().toDouble()
    public override fun toFloat(): Float = this.toRawDecimalString().toFloat()
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
    public fun toLUong(roundingMode: RoundingMode): ULong = roundedMantissa(roundingMode).toULong()
    public fun toUInt(roundingMode: RoundingMode): UInt = roundedMantissa(roundingMode).toUInt()
    public fun toUShort(roundingMode: RoundingMode): UShort = roundedMantissa(roundingMode).toUShort()
    public fun toUByte(roundingMode: RoundingMode): UByte = roundedMantissa(roundingMode).toUByte()
    public fun roundToLong(): Long = roundedMantissa(RoundingMode.CEILING)
    public fun roundToInt(): Int = roundedMantissa(RoundingMode.CEILING).toInt()


    /********************  Unformatted or formatted Output to human-readable Strings  ****************************/


    public fun toRawDecimalString() : String {
        val (mantissa, decimals) = unpack64()
        if (mantissa == 0L) return "0"
        var decimalString: String
        val prefix : String
        if (mantissa < 0) {
            decimalString = (0L - mantissa).toString(10); prefix = "-"
        }
        else {
            decimalString = mantissa.toString(10); prefix = ""
        }

        if (decimals > 0) { // decimal digits exist, insert a dot
            var decimaldotpos = decimalString.count() - decimals
            if (decimaldotpos <= 0) { // more than significant digits! prepend zeros!
              decimalString = "0"+"0".repeat(0-decimaldotpos)+decimalString
                decimaldotpos = 1
            }
            decimalString = decimalString.take(decimaldotpos) + '.' + decimalString.substring(decimaldotpos)
        }
        return prefix+decimalString
    }


    public fun toScientificString() : String {
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

    public override fun toString() : String {
        val (_, decimals) = unpack64()
        var decimalstring = this.toRawDecimalString()
        // only for display!: complete minimum decimal places with "0"
        if (autoDeprecatedMinDecimals > 0) {
            val  missingplaces = autoDeprecatedMinDecimals - decimals
            if (decimals <= 0) decimalstring += '.'
            if (missingplaces > 0) decimalstring += ("0".repeat(missingplaces))
        }
        return decimalstring
    }

    public fun toFormatted(decim: String, thousands: String) : String {
        var rawstring = this.toRawDecimalString()
        var decimpart = ""
        val dotpos = rawstring.indexOf(".")
        if (dotpos >= 0) {
            decimpart = rawstring.substring(dotpos)
            rawstring = rawstring.take(dotpos+1)
        }

        rawstring = rawstring.reversed()
            .chunked(3)
            .joinToString(thousands)
            .reversed()
        if (dotpos >= 0) {
          rawstring = buildString {
              append(rawstring)
              append(decim)
              append(decimpart)
          }
        }

        if (autoDeprecatedMinDecimals > 0) {
            val decimals = decimpart.length
            val  missingplaces = autoDeprecatedMinDecimals - decimals
            if (decimals <= 0) rawstring += decim
            if (missingplaces > 0) rawstring += ("0".repeat(missingplaces))
        }

        return rawstring
    }


    /***********  Comparable interface, and equality operators  *************/

    /*****  Clone / Copy Functions  *****/

    public fun clone(): Decimal {
        val (mantissa, decimals) = unpack64()
        return Decimal(mantissa, decimals, true)
    }
    public fun copy(): Decimal = this.clone() // which one is better?

    /*****  Compare Functions  *****/

    public override operator fun compareTo(other: Decimal): Int {
        if (this.decimal64 == other.decimal64) return 0
        val (thism, thisd) = unpack64()
        val (thatm, thatd) = other.unpack64()

        val (thismantissa,thatmantissa, _) = equalizeDecimals(thism, thisd, thatm, thatd)

        return when {
            (thismantissa > thatmantissa) -> 1
            (thismantissa < thatmantissa) -> -1
            else -> 0
        }
    }

    public override operator fun equals(other: Any?) : Boolean
        = ((other != null) and (other is Decimal)  and (this.decimal64 == (other as Decimal).decimal64))

    public override fun hashCode(): Int {
        return ((this.decimal64 ushr 32).toInt() xor (this.decimal64 and 0x00000000FFFFFFFFL).toInt())

    }


    /***************************  Companion Object  **************************************/

    public companion object {
        // Simulating a constructor out of a single integer type
        public operator fun invoke(input:Byte): Decimal = Decimal(input.toLong(),0, true)
        public operator fun invoke(input:UByte): Decimal = Decimal(input.toLong(),0, true)
        public operator fun invoke(input:Short): Decimal = Decimal(input.toLong(),0,true)
        public operator fun invoke(input:UShort): Decimal = Decimal(input.toLong(),0, true)
        public operator fun invoke(input:Int): Decimal = Decimal(input.toLong(),0,true)
        public operator fun invoke(input:UInt): Decimal= Decimal(input.toLong(),0,true)
        public operator fun invoke(input:Long): Decimal = Decimal(input,0,true)
        public operator fun invoke(input:ULong): Decimal = Decimal(input.toLong(),0, true)

        internal fun mkDecimalOrNull(numberString: String): Decimal? {
            val decimalPair: Pair<Long, Int>? = mkDecimalParseOrNull(numberString, autoDecimalPlaces, true)
            return if (decimalPair != null) {
                val (roundedMantissa, roundedDecimals) = roundWithMode(decimalPair.first, decimalPair.second, min(autoDecimalPlaces, MAX_DECIMAL_PLACES), autoRoundingMode)
                Decimal(roundedMantissa, roundedDecimals, true)
            } else {
                null
            }
        }

        public const val MAX_VALUE: Long = +576460752303423487L
        public const val MIN_VALUE: Long = -576460752303423487L
        //public const val NOT_A_NUMBER: Long = -576460752303423488L

        public const val MAX_DECIMAL_PLACES: Int = 15
        public const val MAX_DECIMAL_SIGNIFICANTS: Int = 17
        public const val MAX_DECIMAL_VALUE_STRING: String = "576460752303423487"
        public const val MAX_LONG_SIGNIFICANTS: Int = 19
        public const val MAX_LONG_VALUE_STRING: String = "9223372036854775807"

        public val ONE: Decimal = Decimal(1,0,true)

        public val NaN: Decimal = Decimal(0, ArithmeticErrors.NOT_A_NUMBER.ordinal, true)
        // static (common) variables and functions

        // throw exceptions on all kind of errors?
        internal var shallThrowOnError: Boolean = true
        public fun setThrowOnErrors(shallThrow: Boolean) {
            shallThrowOnError = shallThrow
        }
        public fun getThrowOnErrors(): Boolean = shallThrowOnError


        // for automatic rounding
        internal var autoDecimalPlaces: Int = 15 /* 0 - 15 */
        public fun setMaxDecimalPlaces(maxDecimalPlaces: Int) {
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
            autoDeprecatedMinDecimals = if (mind < 0) 0; else mind
        }
        public fun getMinDecimals(): Int = autoDeprecatedMinDecimals

        internal fun isDecimalError(mantissa: Long, decimalPlaces: Int) : Boolean {
            if (mantissa !=0L) return false
            return (decimalPlaces !=0)
        }

        public fun isDecimalError(decimal: Decimal): Boolean {
            return ((decimal.decimal64 > 0) and (decimal.decimal64 <= MAX_DECIMAL_PLACES) )
            // val(mantissa, decimalPlaces) = decimal.unpack64()
            // return isDecimalError(mantissa, decimalPlaces)
        }


    }  // end of companion object



}





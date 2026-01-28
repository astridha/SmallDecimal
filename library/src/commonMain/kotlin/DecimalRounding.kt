package io.github.astridha.smalldecimal

import io.github.astridha.smalldecimal.Decimal.Error
import io.github.astridha.smalldecimal.Decimal.Companion.MAX_MANTISSA_VALUE
import kotlin.math.abs

internal fun getPower10(exponent: Int) : Long { // only for between 0 and 18!!!
    var power: Long = 1
    if (exponent < 0) return 0 // solve somehow else, but generally shouldn't happen
    if (exponent > Decimal.SAFE_LONG_SIGNIFICANTS) {
        // throws exception but cannot return Error, because return value is not decimal? or just return -1?
        Decimal.generateErrorDecimal(Error.ROUNDING_FAILED, "Rounding Overflow")
    }
    repeat (exponent) { power *= 10 } // or fixed long values? Size = 18 x 8 = 144 Bytes
    return power
}

// first: mantissa offset multiplicator, second: a hint for the rounding direction
internal fun getRoundingModeSpecificCalculation(roundingMode: Decimal.RoundingMode, isPositive: Boolean) : Pair<Int, Int> {
    return when (roundingMode) {
        Decimal.RoundingMode.UP -> if (isPositive) Pair(10, -1) else Pair(-10, 1)
        Decimal.RoundingMode.DOWN -> Pair(0, 0)
        Decimal.RoundingMode.CEILING -> if (isPositive) Pair(10, -1) else Pair(0, 0)
        Decimal.RoundingMode.FLOOR -> if (isPositive) Pair(0, 0) else Pair(-10, 1)
        Decimal.RoundingMode.HALF_UP -> if (isPositive) Pair(5, 0) else Pair(-5, 0)
        Decimal.RoundingMode.HALF_DOWN -> if (isPositive) Pair(5, -1) else Pair(-5, 1)
        Decimal.RoundingMode.HALF_EVEN -> if (isPositive) Pair(5, -0) else Pair(-5, 0)
        Decimal.RoundingMode.UNNECESSARY -> Pair(0, 0)
    }
}

// Cannot assume that raw values were previously stored in a Decimal, they may come from parsing!
// So the raw mantissa might be a full Long that will not fit into Decimal
// also, the raw decimal places might be more than MAX_DECIMAL_PLACES
// desiredDecimals can be below 0, which means that the lowest pre-comma places will also be rounded to 0
// but resulting decimal places must aim between 0 and 15, independent of  desired precision
// and long rawMantissa must also be handled and be shortened if greater than MAX_DECIMAL_VALUE/MIN_DECIMAL_VALUE
internal fun roundWithMode(rawMantissa: Long, rawDecimals: Int, rounding: Decimal.Rounding = Decimal.autoRounding): Pair<Long, Int> {
    if (rawMantissa == 0L) return Pair(0,0)
    val (desiredDecimals, roundingMode) = rounding
    var currentMantissa = rawMantissa
    var currentDecimals = rawDecimals
    // eliminate edge cases for better handling
    while ((currentMantissa != 0L)
            && (
                ((currentDecimals > 0) && ((currentMantissa % 10) == 0L))                 // empty decimal places (low-hanging fruits)
                || (currentDecimals > (desiredDecimals + Decimal.SAFE_LONG_SIGNIFICANTS))  // distance too far, must shorten first
                )
            ) {
        currentMantissa /= 10
        currentDecimals--
    }

    if (desiredDecimals >= currentDecimals) {
        // nothing to round
        return Pair(currentMantissa, currentDecimals)
    }

    if (roundingMode == Decimal.RoundingMode.UNNECESSARY) {
        // should round, but rounding is forbidden!
        val errno = Decimal.generateErrorCode(Error.ROUNDING_FAILED, "Rounding necessary but forbidden (RoundingMode.UNNECESSARY)")
        return Pair(0L, errno)
    }
//    println("\n old: mantissa:$currentMantissa, currentDecimals:$currentDecimals, desiredDecimals:$desiredDecimals, mode:$roundingMode")
    val wholeRoundingDistance: Int = currentDecimals - desiredDecimals
    if (wholeRoundingDistance > Decimal.MAX_LONG_SIGNIFICANTS) return Pair(0, 0) // more than mantissa width, nothing will be left

    val upperRoundingDistance: Int = if (desiredDecimals >= 0) {0} else {0 - desiredDecimals}
     val (offsetMultiplicator, offsetBias)  = getRoundingModeSpecificCalculation(roundingMode, currentMantissa >=0)

    val roundingDivisor = getPower10(wholeRoundingDistance)
    val roundingOffset = ((roundingDivisor * offsetMultiplicator)/ 10) + offsetBias

//    println("Distance: $wholeRoundingDistance, Mult:$offsetMultiplicator, Bias:$offsetBias => roundingDivisor: $roundingDivisor, roundingOffset: $roundingOffset")

    var halfEvenOffset = 0

    if (roundingMode == Decimal.RoundingMode.HALF_EVEN) {
        // find the neighboring even value only if exactly in the middle (half) = 5
        // this value can be found in roundingOffset, which is 5[00...]
        val isExactlyHalf = ((currentMantissa % roundingDivisor) == roundingOffset)
        println("HALF_EVEN: mantissaHalfFraction: ${(currentMantissa % roundingDivisor)}, roundingOffset: ${(roundingOffset)} => isExactlyHalf: $isExactlyHalf")
        if (isExactlyHalf) {
            val nextDigit = (((currentMantissa + roundingOffset) / roundingDivisor) % 10)
            //println("Peep! (($currentMantissa + $roundingOffset) / $roundingDivisor) = ${((currentMantissa+roundingOffset) / roundingDivisor)}, Next digit is: ${nextDigit}")
            if ((nextDigit % 2) != 0L) {halfEvenOffset  = if (currentMantissa < 0) 1 else -1}
//            println("Next upper neighbor digit is: ${nextDigit}, so decrement: ${halfEvenOffset}")
        }
    }

    var newMantissa = ((currentMantissa+roundingOffset) / roundingDivisor) + halfEvenOffset
    var newDecimals = if (desiredDecimals >= 0) desiredDecimals; else 0
//    println("new: m:$newMantissa, d:$newDecimals")

    if (upperRoundingDistance > 0) {
        val upperMultiplicator = getPower10(upperRoundingDistance)
        newMantissa *= upperMultiplicator
        newDecimals = 0

//        println("Upper Multiplicator: $upperMultiplicator")
        // and newDecimals stays 0 because rounding is left to comma!
//        println("new: m:$newMantissa, d:$newDecimals")
    }

    // truncate any empty decimal places that might have come though rounding
    while ((newDecimals > 0) && (newMantissa != 0L) && ((newMantissa % 10) == 0L)) {
        newMantissa /= 10
        newDecimals--
    }
    if (abs(newMantissa) > MAX_MANTISSA_VALUE) {
//        println("Ups!")
        val errno = Decimal.generateErrorCode(Error.NUMERIC_OVERFLOW, "\"Rounded Value won't fit into Decimal\"")
        newMantissa = 0L
        newDecimals = errno
    }

    return Pair(newMantissa, if (newMantissa == 0L) 0; else newDecimals)

}



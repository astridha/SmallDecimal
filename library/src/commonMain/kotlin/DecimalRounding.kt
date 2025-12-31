package io.github.astridha.decimal

internal fun getPower10(exponent: Int) : Long { // only for between 0 and 16!!!
    var power: Long = 1
    if (exponent < 0) return 0 // solve somehow else, but generally shouldn't happen
    repeat (exponent) { power *= 10 }
    return power
}

// first: multiplicator, second: a hint for the direction
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
// desiredDecimals can be below 0, which means that the lowest pre-comma places will also be rounded to 0
// but resulting decimal places must aim between 0 and 15, independent of autoprecision
// and long rawMantissa must also be handled and be shortened if greater than MAX_VALUE/MIN_VALUE
internal fun roundWithMode(rawMantissa: Long, rawDecimals: Int, desiredDecimals: Int, roundingMode: Decimal.RoundingMode): Pair<Long, Int> {
    var currentMantissa = rawMantissa
    var currentDecimals = rawDecimals

    if (desiredDecimals >= currentDecimals) {
        // nothing to round
        return Pair(currentMantissa, currentDecimals)
    }

    if (roundingMode == Decimal.RoundingMode.UNNECESSARY) {
        // should round, but rounding is forbidden
        throw ArithmeticException("Rounding is necessary")
    }
    println("\nold: mantissa:$currentMantissa, currentDecimals:$currentDecimals, desiredDecimals:$desiredDecimals, mode:$roundingMode")
    val wholeRoundingDistance: Int = currentDecimals - desiredDecimals
    if (wholeRoundingDistance > Decimal.MAX_LONG_SIGNIFICANTS) return Pair(0, 0) // more than mantissa width, nothing will be left

    val upperRoundingDistance: Int = if (desiredDecimals >= 0) {0} else {0 - desiredDecimals}
     val (mult, bias)  = getRoundingModeSpecificCalculation(roundingMode, currentMantissa >=0)

    val roundingDivisor = getPower10(wholeRoundingDistance)
    val roundingOffset = ((roundingDivisor * mult)/ 10) + bias

    println("Mult:$mult, Bias:$bias => roundingDivisor: $roundingDivisor, roundingOffset: $roundingOffset")

    var halfEvenOffset = 0

    if (roundingMode == Decimal.RoundingMode.HALF_EVEN) {
        // find the neighboring even value only if exactly in the middle (half) = 5
        // this value can be found in roundingOffset, which is 5[00...]
        val isExactlyHalf = ((currentMantissa % roundingDivisor) == roundingOffset)
        println("HALF_EVEN: trailingDigit: ${(currentMantissa % roundingDivisor)}, roundingOffset: ${(roundingOffset)} => isExactlyHalf: ${isExactlyHalf}")
        if (isExactlyHalf) {
            val nextDigit = (((currentMantissa + roundingOffset) / roundingDivisor) % 10)
            //println("Peep! (($currentMantissa + $roundingOffset) / $roundingDivisor) = ${((currentMantissa+roundingOffset) / roundingDivisor)}, Next digit is: ${nextDigit}")
            if ((nextDigit % 2) != 0L) {halfEvenOffset  = if (currentMantissa < 0) 1 else -1}
            println("Next upper neighbor digit is: ${nextDigit}, so decrement: ${halfEvenOffset}")
        }
    }

    var newMantissa = ((currentMantissa+roundingOffset) / roundingDivisor) + halfEvenOffset
    var newDecimals = if (desiredDecimals >= 0) desiredDecimals; else 0
    println("new: m:$newMantissa, d:$newDecimals")

    if (upperRoundingDistance > 0) {
        val upperMultiplicator = getPower10(upperRoundingDistance)
        newMantissa *= upperMultiplicator
        newDecimals = 0

        println("Upper Multiplicator: $upperMultiplicator")
        // and newDecimals stays 0 because rounding is left to comma!
        println("new: m:$newMantissa, d:$newDecimals")
    }

    // truncate any empty decimal places that might have come though rounding
    while ((newDecimals > 0) and (newMantissa != 0L) and ((newMantissa % 10) == 0L)) {
        newMantissa /= 10
        newDecimals--
    }

    return Pair(newMantissa, if (newMantissa == 0L) 0; else newDecimals)

}



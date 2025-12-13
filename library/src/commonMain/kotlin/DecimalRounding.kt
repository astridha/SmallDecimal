package io.github.astridha.decimal

internal fun getPower10(exponent: Int) : Long { // only for between 0 and 16!!!
    var power: Long = 1
    if (exponent < 0) return 0 // solve somehow else, but generally shouldn't happen
    repeat (exponent) { power *= 10 }
    return power
}

// first: multiplicator, second: a hint for the direction
internal fun getDivisor(roundingMode: Decimal.RoundingMode, pos: Boolean) : Pair<Int, Int> {
    when (roundingMode) {
        Decimal.RoundingMode.UP -> return if (pos) Pair(10, -1) else Pair(-10, 1)
        Decimal.RoundingMode.DOWN -> return Pair(0, 0)
        Decimal.RoundingMode.CEILING -> return if (pos) Pair(10, -1) else Pair(0, 0)
        Decimal.RoundingMode.FLOOR -> return if (pos) Pair(0, 0) else Pair(-10, 1)
        Decimal.RoundingMode.HALF_UP -> return if (pos) Pair(5, 0) else Pair(-5, 0)
        Decimal.RoundingMode.HALF_DOWN -> return if (pos) Pair(5, -1) else Pair(-5, 1)
        Decimal.RoundingMode.HALF_EVEN -> return  if (pos) Pair(5, -0) else Pair(-5, 0)
        Decimal.RoundingMode.UNNECESSARY -> return Pair(0, 0)
    }
}

// desired precision can be below 0, but decimalplaces is between 0 and 15, independent of autoprcision
internal fun roundWithMode(mantissa: Long, decimals: Int, desiredprecision: Int, roundingmode: Decimal.RoundingMode): Pair<Long, Int> {
    if (desiredprecision >= decimals) return Pair(mantissa, decimals) // won't improve
    if (roundingmode == Decimal.RoundingMode.UNNECESSARY) throw ArithmeticException("Rounding is necessary")
    println("\nold: m:$mantissa, d:$decimals, p:$desiredprecision, r:$roundingmode")
    var rounding_distance: Int = decimals - desiredprecision
    if (rounding_distance > 17) return Pair(0, 0) // more than mantissa width, nothing will be left

    var upper_rounding_distance: Int = if (desiredprecision >= 0) {0} else {0 - desiredprecision}
     val (mult, bias)  = getDivisor(roundingmode, mantissa >=0)

    val divisor = getPower10(rounding_distance)
    val biasoffset = ((divisor * mult)/10)+bias

    println("Mult:$mult, bias:$bias => Divisor: $divisor, biasoffset: $biasoffset")

    var even_decr: Int = 0

    if (roundingmode == Decimal.RoundingMode.HALF_EVEN) {
         var isMidpoint = ((mantissa % divisor) == biasoffset) // == 5...
        println("HALF_EVEN: leastdigit: ${(mantissa % divisor)}, biasoffset: ${(biasoffset)} => isMidpoint: ${isMidpoint}")
        if (isMidpoint) {
            var nextdigit = (((mantissa+biasoffset) / divisor) % 10)
            println("Peep! (($mantissa+$biasoffset) / $divisor) = ${((mantissa+biasoffset) / divisor)}, Next digit is: ${nextdigit}")
            if ((nextdigit % 2) != 0L) {even_decr  =if (mantissa < 0) 1 else -1}
            println("decrement: ${even_decr}")
        }
    }

    var newmantissa = ((mantissa+biasoffset) / divisor) + even_decr
    var newdecimals = if (desiredprecision >= 0) desiredprecision; else 0;
    println("new: m:$newmantissa, d:$newdecimals")

    if (upper_rounding_distance > 0) {

        val upper_multiplicator = getPower10(upper_rounding_distance)
        newmantissa *= upper_multiplicator
        newdecimals = 0

        println("Upper mult: $upper_multiplicator")
        // and newdecimals stays 0 because rounding is left to comma!
        println("new: m:$newmantissa, d:$newdecimals")
    }

    return Pair(newmantissa, newdecimals)

}



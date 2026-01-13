package io.github.astridha.smalldecimal

import kotlin.math.min
import io.github.astridha.smalldecimal.Decimal.*
import io.github.astridha.smalldecimal.Decimal.Companion.generateErrorCode
import io.github.astridha.smalldecimal.Decimal.Companion.generateErrorDecimal
import io.github.astridha.smalldecimal.Decimal.Companion.MAX_LONG_SIGNIFICANTS
//import System

private fun IsMantissaStringTooLong(mantissaString: String): Boolean {
    //  Calculates whether mantissa will fit into Long
    val mantissaLength = mantissaString.length
    if (mantissaLength > Decimal.MAX_LONG_SIGNIFICANTS) {
        return true
    }
    if ((mantissaLength == Decimal.MAX_LONG_SIGNIFICANTS)
        and (mantissaString.compareTo(Decimal.MAX_LONG_VALUESTRING) > 0)) {
        return true
    }
    return false
}

private fun NumMantissaStringDisposableDecimalPlaces(mantissaString: String, decimals:Int): Int {
    // count how many decimal places are outside of Long and should be cut before parsing and rounding
    val mantissaLength = mantissaString.length
    if (mantissaLength >= Decimal.MAX_LONG_SIGNIFICANTS) println("disposable: mantissaLength: $mantissaLength, decimals: $decimals => ${min(mantissaLength - (MAX_LONG_SIGNIFICANTS+1), decimals)}")
    if (mantissaLength > Decimal.MAX_LONG_SIGNIFICANTS) {
        return min(mantissaLength - MAX_LONG_SIGNIFICANTS, decimals)
    }
    if ((mantissaLength == Decimal.MAX_LONG_SIGNIFICANTS)
        and (mantissaString.compareTo(Decimal.MAX_LONG_VALUESTRING) > 0)) {
        return min(mantissaLength - (MAX_LONG_SIGNIFICANTS+1), decimals)
    }
    return 0
}

private fun IsMantissaStringWillOverflow(mantissaString: String, decimals: Int, desiredDecimals: Int): Boolean {
    // Calculates whether integer part and desired decimal part (after rounding) together will fit into Decimal
    val mantissaLength = mantissaString.length
    val preCommaLength = mantissaLength - decimals
    val postCommaLength = min(decimals, desiredDecimals)
    val significantLength =  preCommaLength + postCommaLength
    val significantString = mantissaString.take(significantLength)
    if (significantLength > Decimal.MAX_DECIMAL_SIGNIFICANTS) {
        return true
    }
    if ((significantLength == Decimal.MAX_DECIMAL_SIGNIFICANTS)
        and (significantString.compareTo(Decimal.MAX_DECIMAL_MANTISSASTRING) > 0)) {
        return true
    }
    return false
 }


internal fun mkDecimalParseOrNull (rawNumberString: String, desiredDecimalPlaces: Int = Decimal.autoDecimalPlaces, orNull: Boolean) : Pair <Long, Int>? {
    val cleanedNumberString = rawNumberString.replace("_","").replace(" ","")

    val decimalNumberPattern = """(?<prefix>[+-])?(?<integer>[+-]?\d*)(?:\.(?<fraction>\d*))?(?:[Ee](?<exponent>[+-]?\d+))?"""
    val decimalNumberRegex = Regex(decimalNumberPattern)

    val match = decimalNumberRegex.matchEntire(cleanedNumberString)

    if (match == null) {
        if (orNull) return null
        val errno = generateErrorCode(Decimal.Error.NOT_A_NUMBER,"\"$rawNumberString\" is no number")
        return Pair(0, errno)
    }

    val exponent = (match.groups["exponent"]?.value ?: "0").toInt()

    val fractionString = (match.groups["fraction"]?.value ?: "").trimEnd('0')
    var decimalPlaces = fractionString.length

    val integerString = (match.groups["integer"]?.value ?: "").trimStart('0')
    val prefixString = match.groups["prefix"]?.value ?: ""

    var mantissaString = prefixString + integerString + fractionString
    decimalPlaces -= exponent                 // exponent calculates reverse, 0 - exponent = decimal places!

    // detect whether mantissaString including desiredDecimals cannot fit
    if (IsMantissaStringWillOverflow(mantissaString, decimalPlaces, desiredDecimalPlaces)) {
        println("mantissa $mantissaString will overflow")
        val errno = generateErrorCode(Decimal.Error.PARSING_OVERFLOW,"\"$rawNumberString\" cannot fit into a Decimal")
       return Pair(0, errno)
    }

    // if necessary, ideally truncate to Long (but truncate only disposable decimal digits) and condense again
    val disposableDecimalPlaces = NumMantissaStringDisposableDecimalPlaces(mantissaString, decimalPlaces)
    if (disposableDecimalPlaces > 0) {
        println("dispose $disposableDecimalPlaces")
        mantissaString = mantissaString.dropLast(disposableDecimalPlaces)
        decimalPlaces -= disposableDecimalPlaces
        // once again remove possible trailing 0s from decimals part
        while ((decimalPlaces > 0) and (mantissaString.last()=='0')) {
            mantissaString = mantissaString.dropLast(1)
            decimalPlaces--
        }
    }

    // Give up if mantissa still too long to put it into Long mantissa for later rounding
    if (IsMantissaStringTooLong(mantissaString)) {
        println("I give up. \"$mantissaString\" still too long.")
        if (orNull) return null
        val errno = generateErrorCode(Decimal.Error.PARSING_OVERFLOW,"\"$rawNumberString\" cannot fit into a Decimal")
         return Pair(0, errno)
    }

    if (mantissaString in listOf("+", "- ", "")) mantissaString += "0"
    val mantissa: Long = mantissaString.toLong()

    return Pair(mantissa, decimalPlaces)
}

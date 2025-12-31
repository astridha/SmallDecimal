package io.github.astridha.decimal

@Throws(NumberFormatException::class)
internal fun mkDecimalParseOrNull (rawNumberString: String, orNull: Boolean) : Pair <Long, Int>? {
    val cleanedNumberString = rawNumberString.replace("_","").replace(" ","")

    val decimalNumberPattern = """(?<integer>[+-]?\d*)(?:\.(?<fraction>\d*))?(?:[Ee](?<exponent>[+-]?\d+))?"""
    val decimalNumberRegex = Regex(decimalNumberPattern)

    val match = decimalNumberRegex.matchEntire(cleanedNumberString)

    if (match == null) {
        if (orNull) return null
        if (Decimal.getThrowOnErrors()) throw NumberFormatException("INVALID DECIMAL FORMAT: \"$rawNumberString\"")
        return Pair(0, Decimal.ArithmeticErrors.NOT_A_NUMBER.ordinal)

    } else {

        val exponent = (match.groups["exponent"]?.value ?: "0").toInt()

        val fractionString = (match.groups["fraction"]?.value ?: "0").trimEnd('0')
        var decimalPlaces = fractionString.length

        val integerString = match.groups["integer"]?.value ?: ""

        var mantissaString = integerString + fractionString
        decimalPlaces -= exponent                 // exponent calculates reverse, 0 - exponent = decimal places!

        if (mantissaString in listOf("+", "- ", "")) mantissaString += "0"
        val mantissa: Long = mantissaString.toLong()

        return Pair(mantissa, decimalPlaces)

    }
}

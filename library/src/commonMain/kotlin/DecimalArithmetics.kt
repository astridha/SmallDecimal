package io.github.astridha.smalldecimal

import io.github.astridha.smalldecimal.Decimal.Companion.MAX_DECIMAL_SIGNIFICANTS
import io.github.astridha.smalldecimal.Decimal.Companion.MAX_MANTISSA_VALUE
import io.github.astridha.smalldecimal.Decimal.Companion.autoRoundingConfig
// import io.github.astridha.smalldecimal.Decimal.Companion.autoRoundingConfig.roundingMode
import io.github.astridha.smalldecimal.Decimal.Companion.generateErrorDecimal
import io.github.astridha.smalldecimal.Decimal.Companion.isError
import io.github.astridha.smalldecimal.Decimal.Companion.toRawString
import kotlin.math.abs
import kotlin.math.sign

internal class DecimalArithmetics {

    companion object {

        internal data class EqualizedDecimals(val thisMantissa: Long, val thatMantissa: Long, val commonDecimal: Int)

        internal fun equalizeDecimals(thisM: Long, thisD: Int, thatM: Long, thatD: Int): EqualizedDecimals {
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

        /********************************************** plus (+) ****************************************/

        internal fun arithmeticPlus(
            thisD: Decimal,
            other: Decimal,
            roundingConfig: Decimal.RoundingConfig = autoRoundingConfig
         ): Decimal {
            if (isError(thisD) or isError(other)) return thisD.clone()
            val (thisMantissa, thisDecimals) = thisD.unpack64()
            if (thisMantissa == 0L) return other.clone()
            val (otherMantissa, otherDecimals) = other.unpack64()
            if (otherMantissa == 0L) return thisD.clone()
             val (equalizedThisMantissa, equalizedOtherMantissa, equalizedDecimals) = equalizeDecimals(
                thisMantissa,
                thisDecimals,
                otherMantissa,
                otherDecimals
            )
            println("Addition: this: $equalizedThisMantissa other: $equalizedOtherMantissa, sum: ${equalizedThisMantissa + equalizedOtherMantissa}")
            if (equalizedThisMantissa.isNegative() == equalizedOtherMantissa.isNegative()) {
                // addition might overflow!
                val space: Long = MAX_MANTISSA_VALUE - abs(equalizedThisMantissa)
                if (space <= abs(equalizedOtherMantissa)) {
                    return generateErrorDecimal(
                        Decimal.Error.ADD_OVERFLOW,
                        "$this + $other result does not fit into Decimal"
                    )
                }
            }
            val equalizedMantissaSum = equalizedThisMantissa + equalizedOtherMantissa
            val (roundedMantissa, roundedDecimals) = roundWithMode(
                equalizedMantissaSum,
                equalizedDecimals,
                roundingConfig
            )
            return Decimal(roundedMantissa, roundedDecimals)
        }

        /********************************************** minus (-) ****************************************/

        internal fun arithmeticMinus(
            thisD: Decimal,
            other: Decimal,
            roundingConfig: Decimal.RoundingConfig = autoRoundingConfig
        ) : Decimal {
            if (isError(thisD) or isError(other)) return thisD.clone()
            val (thisMantissa, thisDecimals) = thisD.unpack64()
            if (thisMantissa == 0L) return other.clone()
            val (otherMantissa, otherDecimals) = other.unpack64()
            if (otherMantissa == 0L) return thisD.clone()
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
                    return generateErrorDecimal(Decimal.Error.SUBTRACT_OVERFLOW, "$this - $other result does not fit into Decimal")
                }
            }
            val equalizedMantissaSum = equalizedThisMantissa - equalizedOtherMantissa
            val (roundedMantissa, roundedDecimals) = roundWithMode(
                equalizedMantissaSum,
                equalizedDecimals,
                roundingConfig
            )
            return Decimal(roundedMantissa, roundedDecimals)
        }

        /********************************************** times (*) ****************************************/

        private fun willOverflowLong(a: Long, b: Long): Boolean{
            if ((a > 0) && (b > 0) && (a > (Long.MAX_VALUE / b))) return true
            if ((a > 0) && (b < 0) && (b < (Long.MIN_VALUE / a))) return true
            if ((a < 0) && (b > 0) && (a < (Long.MIN_VALUE / b))) return true
            return( (a < 0) && (b < 0) && (a < (Long.MAX_VALUE / b)))
        }

        private fun willOverflowMantissa(mantissa: Long, decimals: Int, roundingConfig: Decimal.RoundingConfig): Boolean {
            // this assumes later rounding to autoRoundingConfig.decimalPlaces!
            // any tolerated overflow must be guaranteed to be removed later when being rounded away
            if ((abs(mantissa) > (MAX_MANTISSA_VALUE*10)) && (decimals <= (roundingConfig.decimalPlaces-2))) return true
            if ((abs(mantissa) > MAX_MANTISSA_VALUE ) && (decimals <= (roundingConfig.decimalPlaces-1))) return true
            return false
        }

        internal fun arithmeticTimes(
            thisD: Decimal,
            other: Decimal,
            roundingConfig: Decimal.RoundingConfig = autoRoundingConfig
        ) : Decimal {
            if (isError(thisD) or isError(other)) return thisD.clone()
            var (thisMantissa, thisDecimals) = thisD.unpack64()
            var (otherMantissa, otherDecimals) = other.unpack64()
            println("Multiplication: this: $thisMantissa other: $otherMantissa, product: ${thisMantissa * otherMantissa}")

            // 0, no rounding needed
            if ((thisMantissa == 0L) || (otherMantissa == 0L)) return Decimal(0,0)

            // temporary compression may help avoid overflow in some cases
            while ((thisMantissa % 10) == 0L) { thisMantissa /= 10; thisDecimals --  }
            while ((otherMantissa % 10) == 0L) { otherMantissa /= 10; otherDecimals --  }

            if (willOverflowLong(thisMantissa, otherMantissa)) {
                return generateErrorDecimal(Decimal.Error.MULTIPLY_OVERFLOW, "$this * $other result does not fit into Decimal")
            }
            var resultMantissa = thisMantissa * otherMantissa
            var resultDecimals = thisDecimals + otherDecimals
            if (resultDecimals < 0) {
                val pw10 = getPower10(0-resultDecimals)
                resultMantissa *= pw10
                resultDecimals = 0
            }
            if (willOverflowMantissa(resultMantissa, resultDecimals, roundingConfig)) {
                return generateErrorDecimal(Decimal.Error.MULTIPLY_OVERFLOW, "$this * $other = ${toRawString(resultMantissa, resultDecimals)} result does not fit into Decimal")
            }
            val (roundedMantissa, roundedDecimals) = roundWithMode(resultMantissa, resultDecimals,roundingConfig)
            return Decimal(roundedMantissa, roundedDecimals)
        }

        /********************************************** div (/) ****************************************/

        internal fun arithmeticDiv(
            thisD: Decimal,
            other: Decimal,
            roundingConfig: Decimal.RoundingConfig = autoRoundingConfig
        ) : Decimal {
            if (isError(thisD) or isError(other)) return thisD.clone()
            var (thisMantissa, thisDecimals) = thisD.unpack64()
            val (otherMantissa, otherDecimals) = other.unpack64()
            println("Division: this: $thisMantissa other: $otherMantissa, result: ${if (otherMantissa==0L) "Error" else (thisMantissa / otherMantissa)}")
            if (otherMantissa == 0L) {
                return generateErrorDecimal(Decimal.Error.DIVISION_BY_0, "$this is divided by 0")
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
            val (roundedMantissa, roundedDecimals) = roundWithMode(resultMantissa, resultDecimals, roundingConfig)
            return Decimal(roundedMantissa, roundedDecimals)
        }


        /********************************************** rem (%) ****************************************/

        internal fun arithmeticRem(
            thisD: Decimal,
            other: Decimal,
            roundingConfig: Decimal.RoundingConfig = autoRoundingConfig
        ) : Decimal {
            if (isError(thisD) or isError(other)) return thisD.clone()

            var (thisMantissa, thisDecimals) = thisD.unpack64()
            val (otherMantissa, otherDecimals) = other.unpack64()
            if (otherMantissa == 0L) {
                return generateErrorDecimal(Decimal.Error.DIVISION_BY_0, "$this is divided by 0")
            }
            while (abs(thisMantissa) < abs(otherMantissa)){
                thisMantissa *=10; thisDecimals++
            }
            val resultMantissa = (thisMantissa / otherMantissa)
            val resultDecimals = (thisDecimals - otherDecimals)


            val result = Decimal(resultMantissa, resultDecimals)
            println("Remainder: this: $thisD, other: $other, result: ${(thisD - (other * result))}")
            return (thisD - (other * result)).setScale(roundingConfig.decimalPlaces, roundingConfig.roundingMode)
        }


        /********************************************** mod (mod) ****************************************/

        internal fun arithmeticMod(
            thisD: Decimal,
            other: Decimal,
            roundingConfig: Decimal.RoundingConfig = autoRoundingConfig
        ) : Decimal {
            if (isError(thisD) or isError(other)) return thisD.clone()
            val flooredQuotient = arithmeticDiv( thisD,other, Decimal.noRoundingConfig).floor()
            val subtractor = arithmeticTimes(other, flooredQuotient, Decimal.noRoundingConfig)
            val result = arithmeticMinus(thisD, subtractor, Decimal.noRoundingConfig)
            println("Modulo: a: $thisD, m: $other, flooredquotient: $flooredQuotient,  m*floor: ${other * flooredQuotient}, subt: $subtractor,  a-(m*floor): ${(thisD - (other * flooredQuotient))}, result: $result")
            return result.setScale(roundingConfig.decimalPlaces, roundingConfig.roundingMode)
         }


    }
}
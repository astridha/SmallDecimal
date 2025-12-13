package io.github.astridha.decimal

public fun abs(d: Decimal) : Decimal = d.abs()
public fun round(d: Decimal) : Decimal = d.setScale(0)
public fun ceil(d: Decimal) : Decimal = d.setScale(0, Decimal.RoundingMode.CEILING)
public fun floor(d: Decimal) : Decimal = d.setScale(0, Decimal.RoundingMode.FLOOR)
public fun max(a: Decimal, b:Decimal) : Decimal = if (a > b) a; else b
public fun min(a: Decimal, b:Decimal) : Decimal = if (a < b) a; else b
public fun sign(d: Decimal) : Decimal = d.sign

public fun String.toDecimal():Decimal = Decimal(this)
public fun Float.toDecimal():Decimal = Decimal(this)
public fun Double.toDecimal():Decimal = Decimal(this)

public fun Byte.toDecimal():Decimal = Decimal(this)
public fun UByte.toDecimal():Decimal = Decimal(this)
public fun Short.toDecimal():Decimal = Decimal(this)
public fun UShort.toDecimal():Decimal = Decimal(this)
public fun Int.toDecimal():Decimal = Decimal(this)
public fun UInt.toDecimal():Decimal = Decimal(this)
public fun Long.toDecimal():Decimal = Decimal(this)
public fun ULong.toDecimal():Decimal = Decimal(this)

public val String.Dc: Decimal inline get() = Decimal(this)
public val Double.Dc: Decimal inline get() = Decimal(this)
public val Float.Dc: Decimal inline get() = Decimal(this)

public val Long.Dc: Decimal inline get() = Decimal(this)
public val Int.Dc: Decimal inline get() = Decimal(this)
public val Short.Dc: Decimal inline get() = Decimal(this)
public val Byte.Dc: Decimal inline get() = Decimal(this)
public val ULong.Dc: Decimal inline get() = Decimal(this)
public val UInt.Dc: Decimal inline get() = Decimal(this)
public val UShort.Dc: Decimal inline get() = Decimal(this)
public val UByte.Dc: Decimal inline get() = Decimal(this)

//  Arithmetic

public operator fun Double.plus(other: Decimal) : Decimal = other.plus(this)
public operator fun Double.minus(other: Decimal) : Decimal = other.minus(this)
public operator fun Double.times(other: Decimal) : Decimal = other.times(this)
public operator fun Double.div(other: Decimal) : Decimal = other.div(this)
public operator fun Double.rem(other: Decimal) : Decimal = other.rem(this)

public operator fun Float.plus(other: Decimal) : Decimal = other.plus(this)
public operator fun Float.minus(other: Decimal) : Decimal = other.minus(this)
public operator fun Float.times(other: Decimal) : Decimal = other.times(this)
public operator fun Float.div(other: Decimal) : Decimal = other.div(this)
public operator fun Float.rem(other: Decimal) : Decimal = other.rem(this)

public operator fun Long.plus(other: Decimal) : Decimal = other.plus(this)
public operator fun Long.minus(other: Decimal) : Decimal = other.minus(this)
public operator fun Long.times(other: Decimal) : Decimal = other.times(this)
public operator fun Long.div(other: Decimal) : Decimal = other.div(this)
public operator fun Long.rem(other: Decimal) : Decimal = other.rem(this)

public operator fun Int.plus(other: Decimal) : Decimal = other.plus(this)
public operator fun Int.minus(other: Decimal) : Decimal = other.minus(this)
public operator fun Int.times(other: Decimal) : Decimal = other.times(this)
public operator fun Int.div(other: Decimal) : Decimal = other.div(this)
public operator fun Int.rem(other: Decimal) : Decimal = other.rem(this)

public operator fun Short.plus(other: Decimal) : Decimal = other.plus(this)
public operator fun Short.minus(other: Decimal) : Decimal = other.minus(this)
public operator fun Short.times(other: Decimal) : Decimal = other.times(this)
public operator fun Short.div(other: Decimal) : Decimal = other.div(this)
public operator fun Short.rem(other: Decimal) : Decimal = other.rem(this)

public operator fun Byte.plus(other: Decimal) : Decimal = other.plus(this)
public operator fun Byte.minus(other: Decimal) : Decimal = other.minus(this)
public operator fun Byte.times(other: Decimal) : Decimal = other.times(this)
public operator fun Byte.div(other: Decimal) : Decimal = other.div(this)
public operator fun Byte.rem(other: Decimal) : Decimal = other.rem(this)

public operator fun ULong.plus(other: Decimal) : Decimal = other.plus(this)
public operator fun ULong.minus(other: Decimal) : Decimal = other.minus(this)
public operator fun ULong.times(other: Decimal) : Decimal = other.times(this)
public operator fun ULong.div(other: Decimal) : Decimal = other.div(this)
public operator fun ULong.rem(other: Decimal) : Decimal = other.rem(this)

public operator fun UInt.plus(other: Decimal) : Decimal = other.plus(this)
public operator fun UInt.minus(other: Decimal) : Decimal = other.minus(this)
public operator fun UInt.times(other: Decimal) : Decimal = other.times(this)
public operator fun UInt.div(other: Decimal) : Decimal = other.div(this)
public operator fun UInt.rem(other: Decimal) : Decimal = other.rem(this)

public operator fun UShort.plus(other: Decimal) : Decimal = other.plus(this)
public operator fun UShort.minus(other: Decimal) : Decimal = other.minus(this)
public operator fun UShort.times(other: Decimal) : Decimal = other.times(this)
public operator fun UShort.div(other: Decimal) : Decimal = other.div(this)
public operator fun UShort.rem(other: Decimal) : Decimal = other.rem(this)

public operator fun UByte.plus(other: Decimal) : Decimal = other.plus(this)
public operator fun UByte.minus(other: Decimal) : Decimal = other.minus(this)
public operator fun UByte.times(other: Decimal) : Decimal = other.times(this)
public operator fun UByte.div(other: Decimal) : Decimal = other.div(this)
public operator fun UByte.rem(other: Decimal) : Decimal = other.rem(this)


//impossible: public operator fun Int.plusAssign(other: Decimal) : Unit = other.plusAssign(this)

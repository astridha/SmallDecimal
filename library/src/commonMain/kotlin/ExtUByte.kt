@file:JvmName("UByte")
package io.github.astridha.smalldecimal

import kotlin.jvm.JvmName

public fun UByte.toDecimal():Decimal = Decimal(this)

public val UByte.Dc: Decimal inline get() = Decimal(this)

public operator fun UByte.plus(other: Decimal) : Decimal = Decimal(this).plus(other)
public operator fun UByte.minus(other: Decimal) : Decimal = Decimal(this).minus(other)
public operator fun UByte.times(other: Decimal) : Decimal = Decimal(this).times(other)
public operator fun UByte.div(other: Decimal) : Decimal = Decimal(this).div(other)
public operator fun UByte.rem(other: Decimal) : Decimal = Decimal(this).rem(other)
public infix fun UByte.mod(other: Decimal) : Decimal = Decimal(this).mod(other)

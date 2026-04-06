@file:JvmName("ULong")
package io.github.astridha.smalldecimal

import kotlin.jvm.JvmName

public fun ULong.toDecimal():Decimal = Decimal(this)

public val ULong.Dc: Decimal inline get() = Decimal(this)

public operator fun ULong.plus(other: Decimal) : Decimal = Decimal(this).plus(other)
public operator fun ULong.minus(other: Decimal) : Decimal = Decimal(this).minus(other)
public operator fun ULong.times(other: Decimal) : Decimal = Decimal(this).times(other)
public operator fun ULong.div(other: Decimal) : Decimal = Decimal(this).div(other)
public operator fun ULong.rem(other: Decimal) : Decimal = Decimal(this).rem(other)
public infix fun ULong.mod(other: Decimal) : Decimal = Decimal(this).mod(other)

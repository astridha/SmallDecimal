@file:JvmName("Byte")
package io.github.astridha.smalldecimal

import kotlin.jvm.JvmName

public fun Byte.toDecimal():Decimal = Decimal(this)

public val Byte.Dc: Decimal inline get() = Decimal(this)

public operator fun Byte.plus(other: Decimal) : Decimal = Decimal(this).plus(other)
public operator fun Byte.minus(other: Decimal) : Decimal = Decimal(this).minus(other)
public operator fun Byte.times(other: Decimal) : Decimal = Decimal(this).times(other)
public operator fun Byte.div(other: Decimal) : Decimal = Decimal(this).div(other)
public operator fun Byte.rem(other: Decimal) : Decimal = Decimal(this).rem(other)
public infix fun Byte.mod(other: Decimal) : Decimal = Decimal(this).mod(other)

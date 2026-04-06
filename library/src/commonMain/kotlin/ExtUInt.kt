@file:JvmName("UInt")
package io.github.astridha.smalldecimal

import kotlin.jvm.JvmName

public fun UInt.toDecimal():Decimal = Decimal(this)

public val UInt.Dc: Decimal inline get() = Decimal(this)

public operator fun UInt.plus(other: Decimal) : Decimal = Decimal(this).plus(other)
public operator fun UInt.minus(other: Decimal) : Decimal = Decimal(this).minus(other)
public operator fun UInt.times(other: Decimal) : Decimal = Decimal(this).times(other)
public operator fun UInt.div(other: Decimal) : Decimal = Decimal(this).div(other)
public operator fun UInt.rem(other: Decimal) : Decimal = Decimal(this).rem(other)
public infix fun UInt.mod(other: Decimal) : Decimal = Decimal(this).mod(other)

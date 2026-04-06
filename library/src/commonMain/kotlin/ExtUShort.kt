@file:JvmName("UShort")
package io.github.astridha.smalldecimal

import kotlin.jvm.JvmName

public fun UShort.toDecimal():Decimal = Decimal(this)

public val UShort.Dc: Decimal inline get() = Decimal(this)

public operator fun UShort.plus(other: Decimal) : Decimal = Decimal(this).plus(other)
public operator fun UShort.minus(other: Decimal) : Decimal = Decimal(this).minus(other)
public operator fun UShort.times(other: Decimal) : Decimal = Decimal(this).times(other)
public operator fun UShort.div(other: Decimal) : Decimal = Decimal(this).div(other)
public operator fun UShort.rem(other: Decimal) : Decimal = Decimal(this).rem(other)
public infix fun UShort.mod(other: Decimal) : Decimal = Decimal(this).mod(other)

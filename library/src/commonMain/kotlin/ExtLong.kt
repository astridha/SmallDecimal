@file:JvmName("Long")
package io.github.astridha.smalldecimal

import kotlin.jvm.JvmName

public fun Long.toDecimal():Decimal = Decimal(this)

public val Long.Dc: Decimal inline get() = Decimal(this)

public operator fun Long.plus(other: Decimal) : Decimal = Decimal(this).plus(other)
public operator fun Long.minus(other: Decimal) : Decimal = Decimal(this).minus(other)
public operator fun Long.times(other: Decimal) : Decimal = Decimal(this).times(other)
public operator fun Long.div(other: Decimal) : Decimal = Decimal(this).div(other)
public operator fun Long.rem(other: Decimal) : Decimal = Decimal(this).rem(other)
public infix fun Long.mod(other: Decimal) : Decimal = Decimal(this).mod(other)

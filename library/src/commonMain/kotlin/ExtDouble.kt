@file:JvmName("Double")
package io.github.astridha.smalldecimal

import kotlin.jvm.JvmName

public fun Double.toDecimal():Decimal = Decimal(this)

public val Double.Dc: Decimal inline get() = Decimal(this)

public operator fun Double.plus(other: Decimal) : Decimal = Decimal(this).plus(other)
public operator fun Double.minus(other: Decimal) : Decimal = Decimal(this).minus(other)
public operator fun Double.times(other: Decimal) : Decimal = Decimal(this).times(other)
public operator fun Double.div(other: Decimal) : Decimal = Decimal(this).div(other)
public operator fun Double.rem(other: Decimal) : Decimal = Decimal(this).rem(other)
public infix fun Double.mod(other: Decimal) : Decimal = Decimal(this).mod(other)

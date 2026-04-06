@file:JvmName("Short")
package io.github.astridha.smalldecimal

import kotlin.jvm.JvmName

public fun Short.toDecimal():Decimal = Decimal(this)

public val Short.Dc: Decimal inline get() = Decimal(this)

public operator fun Short.plus(other: Decimal) : Decimal = Decimal(this).plus(other)
public operator fun Short.minus(other: Decimal) : Decimal = Decimal(this).minus(other)
public operator fun Short.times(other: Decimal) : Decimal = Decimal(this).times(other)
public operator fun Short.div(other: Decimal) : Decimal = Decimal(this).div(other)
public operator fun Short.rem(other: Decimal) : Decimal = Decimal(this).rem(other)
public infix fun Short.mod(other: Decimal) : Decimal = Decimal(this).mod(other)

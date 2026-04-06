@file:JvmName("Int")
package io.github.astridha.smalldecimal

import kotlin.jvm.JvmName

public fun Int.toDecimal():Decimal = Decimal(this)

public val Int.Dc: Decimal inline get() = Decimal(this)

public operator fun Int.plus(other: Decimal) : Decimal = Decimal(this).plus(other)
public operator fun Int.minus(other: Decimal) : Decimal = Decimal(this).minus(other)
public operator fun Int.times(other: Decimal) : Decimal = Decimal(this).times(other)
public operator fun Int.div(other: Decimal) : Decimal = Decimal(this).div(other)
public operator fun Int.rem(other: Decimal) : Decimal = Decimal(this).rem(other)
public infix fun Int.mod(other: Decimal) : Decimal = Decimal(this).mod(other)

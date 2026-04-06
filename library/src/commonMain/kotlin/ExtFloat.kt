@file:JvmName("Float")
package io.github.astridha.smalldecimal

import kotlin.jvm.JvmName

public fun Float.toDecimal():Decimal = Decimal(this)

public val Float.Dc: Decimal inline get() = Decimal(this)

public operator fun Float.plus(other: Decimal) : Decimal = Decimal(this).plus(other)
public operator fun Float.minus(other: Decimal) : Decimal = Decimal(this).minus(other)
public operator fun Float.times(other: Decimal) : Decimal = Decimal(this).times(other)
public operator fun Float.div(other: Decimal) : Decimal = Decimal(this).div(other)
public operator fun Float.rem(other: Decimal) : Decimal = Decimal(this).rem(other)
public infix fun Float.mod(other: Decimal) : Decimal = Decimal(this).mod(other)

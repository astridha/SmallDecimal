@file:JvmName("String")
package io.github.astridha.smalldecimal

import kotlin.jvm.JvmName


public fun String.toDecimal():Decimal = Decimal(this, Decimal.autoLocale)
public fun String.toDecimalOrNull():Decimal? = Decimal.mkDecimalOrNull(this,  Decimal.autoLocale)
public val String.Dc: Decimal inline get() = Decimal(this)


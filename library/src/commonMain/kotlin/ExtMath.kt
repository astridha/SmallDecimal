@file:JvmName("Math")
package io.github.astridha.smalldecimal

import kotlin.jvm.JvmName


/*********  Math-like implementations, optionally with the number of desired decimal places *************/

public fun round(d: Decimal) : Decimal = d.round()
public fun round(d: Decimal, toPlaces: Int) : Decimal = d.round(toPlaces)

public fun ceil(d: Decimal) : Decimal = d.ceil()
public fun ceil(d: Decimal, toPlaces: Int) : Decimal = d.ceil(toPlaces)

public fun floor(d: Decimal) : Decimal = d.floor()
public fun floor(d: Decimal, toPlaces: Int) : Decimal = d.floor(toPlaces)

public fun truncate(d: Decimal) : Decimal = d.truncate()
public fun truncate(d: Decimal, toPlaces: Int) : Decimal = d.truncate(toPlaces)
public fun abs(d: Decimal) : Decimal = d.abs()
public fun inc(d: Decimal) : Decimal = d.inc()
public fun dec(d: Decimal) : Decimal = d.dec()
public fun max(a: Decimal, b:Decimal) : Decimal = if (a > b) a; else b
public fun min(a: Decimal, b:Decimal) : Decimal = if (a < b) a; else b
public fun sign(d: Decimal) : Decimal = d.sign


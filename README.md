# A Small Decimal Type for KMP Multiplatform

This Library offers a fixed-size **Decimal** class with small exponents and a predictive footprint.  

Made for Kotlin Multiplatform.

The **Decimal** class implements Number and Comparable interfaces, with a 64 Bit footprint.   
It supports math binary operators **+**, **-**, <b>*</b>, **/**, and **%**, as well as unary operators **+**, **-**, **++**, and **--**.


## Characteristics   

The footprint of a **Decimal** instance consists of a 60 bit mantissa, and a 4 bit exponent.

It's mantissa range is from -576_460_752_303_423_487 to +576_460_752_303_423_487.  

So, 17 - 18 significant decimal digits with 0 - 15 decimal places are supported.

It's small fixed 64bit footprint makes it possible to store it as an (unsigned) Long variable type anywhere where 8-Byte places are available.
.


### Convenient usage

#### No verbose type or class declaration

Just use it like any other numeric type, just with the extension *".Dc"*.  
Like *5.Dc* or *17.48.Dc*.  
Or use a numeric String constructor like *"1228573.68".Dc* or *"12_28_573.68".Dc*.

When giving many decimal places, e.g. *15.000000000000001.Dc* (15 decimal places) rounding errors might occur, 
because of the inaccuracy and rounding problems of Float and Double numbers.  
Better use: *"15.000000000000001".Dc*. This avoids the Float/Double problems.

#### Arithmetical Operators are working

Use arithmetical operators conveniently, like  
*(7.5.Dc + 8.5.Dc) / 3.Dc*



### Precision and display

#### setRoundingConfig (precision: Int, roundingMode: RoundingMode)
sets the number of decimal places every Decimal will be rounded to automatically.  
The supported range is from 0 to 15.   
15 is the default value and the maximum supported precision.  
**setRoundingConfig (2, HALF_UP)** means that all Decimals will be rounded to two decimal places.  
**setRoundingConfig (0, HALF_EVEN)** means that only whole numbers will be generated, and rounded to the next even number.

#### setLocalConfig (groupingSeparator: Char?, decimalSeparator: Char, minDecimalPlaces: Int)
Configures how the Decimal will be formatted to with  **toString()**.
Sets grouping separator and decimal separator, and the number of minimum decimal places.  
The supported range is from 0 to any positive value.   
0 is the default value and means there are no printed mandatory decimal places.  
If this setting sets more decimal places than the Decimal value has, the remaining decimal places are filled with "0"s.  
**setLocalConfig ('.', '.', 2)** means that at least two decimal places will be shown.  
**setLocalConfig (null, '.', 0)** means that only necessary decimal places will be shown.


### Rounding

#### Rounding Modes
There are the same eight Rounding modes as in BigDecimal:

``` kotlin
public enum class RoundingMode {
UP,
DOWN,
CEILING,
FLOOR,
HALF_UP,
HALF_DOWN,
HALF_EVEN,
UNNECESSARY
}
``` 

There is an automatic rounding mode, which is per default *HALF_UP* (commercial rounding).
This can be changed via **Decimal.setRoundingMode(roundingMode: RoundingMode)**.

#### How to use

The rounding modes can be used in SetScale:
``` kotlin
setScale(desiredprecision: Int, rounding: RoundingMode = autoRoundingConfig.roundingMode): Decimal
```

Furthermore, there are four standard rounding functions that round to whole values (no decimal places):
``` kotlin
trunc(): Decimal  // RoundingMode.DOWN

floor(): Decimal  // RoundingMode.FLOOR

ceil(): Decimal  // RoundingMode.CEILING

round(): Decimal  // RoundingMode.HALF_EVEN
``` 



--------

### Usage (not yet active!)

Dependencies in build.gradle.kts:
``` kotlin
dependencies {
// ...
implementation("io.github.astridha:smalldecimal:0.5.0")
}
```

Import in source files:
``` kotlin
import io.github.astridha.smalldecimal.*
```


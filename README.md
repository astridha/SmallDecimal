# A Small Decimal Type for KMP Multiplatform

This Library offers a fixed-size **Decimal** class with small exponents and a predictive, smaller footprint than BigDecimal.  

Made for Kotlin Multiplatform, no restriction to JVM.

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

#### setMaxDecimalPlaces(Int)
sets the number of decimal places every Decimal will be rounded to automatically.  
The supported range is from 0 to 15.   
15 is the default value and the maximum supported precision.  
**setPrecision(2)** means that all Decimals will be rounded to two decimal places.
**setPrecision(0)** means that only whole numbers will be generated.

#### setMinDecimals(Int)
sets the number of minimum decimal places the Decimal will be formatted to with **toString()**.  
The supported range is from 0 to any positive value.   
0 is the default value and means there are no printed mandatory decimal places.  
If this setting sets more decimal places than the Decimal value has, the remaining decimal places are filled with "0"s.  
**setMinDecimals(2)** means that at least two decimal places are shown when using **toString()** (but more if the Decimal has more decimal places).


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
setScale(desiredprecision: Int, rounding: RoundingMode = autoRoundingMode): Decimal
```

Furthermore, there are four standard rounding functions that round to whole values (no decimal places):
``` kotlin
trunc(): Decimal  // RoundingMode.DOWN

floor(): Decimal  // RoundingMode.FLOOR

ceil(): Decimal  // RoundingMode.CEILING

round(): Decimal  // RoundingMode.HALF_EVEN
``` 



--------

### Import
``` kotlin
import io.github.astridha.smalldecimal.*
```

#### Usage (not yet active!)
Use maven dependency:

```xml
<dependency>
    <groupId>io.github.astridha</groupId>
    <artifactId>smalldecimal</artifactId>
    <version>1.0.0</version>
</dependency>
```


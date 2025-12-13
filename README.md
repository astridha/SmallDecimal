# Decimal type on a 64bit footprint

### A compact Decimal class with small exponents and a predictive, smaller footprint than BigDecimal.  

KMP Multiplatform, only Common, no platform dependencies (and no restriction to JVM).


The Decimal class implements Number and Comparable interfaces, with a 64 Bit footprint.

## Characteristics   

The footprint consists of a 60 bit mantissa, and a 4 bit exponent

It's value range is from -576460752303423487 to +576460752303423487.  

0 - 15 decimal places are supported.

It's small fixed 64bit footprint makes it possible to store it as an (unsigned) Long variable type anywhere where 8Byte places are available.
.


### Convenient usage
Just use it like any other numeric type, with the extension *".Dc"*.

Like *5.Dc* or *17.48.Dc*.

When giving many decimal places, e.g. *15.000000000000001.Dc* (15 decimal places) rounding errors
because of the inaccuracy and rounding problems of Float and Double numbers,
better use: *"15.000000000000001".Dc*. This is a String constructor and avoids the Float/Double problems.


### Import
``` kotlin
import io.github.astridha.decimal.*
```


### Precision and display

#### setPrecision(Int)
sets the number of decimal places every Decimal will be rounded to automatically.  
The supported range is from 0 to 15.   
15 is the default value and the maximum precision.  
**setPrecision(2)** means that all Decimals will be rounded half-up to two decimal places.
**setPrecision(0)** means that only whole numbers will be generated.

#### setMinDecimals(Int)
sets the number of minimum decimal places the Decimal will be formatted to with **toString()**.  
The supported range is from 0 to any positive value.   
0 is the default value and means there are no mandatory decimal places.  
If this setting sets more decimal places than the Decimal value has, the remaining decimal places are filled with "0"s.  
**setMinDecimals(2)** means that at least two decimal places are shown when using **toString()** (but more if the Decimal has more decimal places).

#### Usage (not yet active!)
Use maven dependency:

```xml
<dependency>
    <groupId>io.github.astridha</groupId>
    <artifactId>compact-decimal</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

Based on the official Multiplatform Library Template [https://github.com/Kotlin/multiplatform-library-template](https://github.com/Kotlin/multiplatform-library-template).


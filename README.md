# A Small Decimal Type for KMP Multiplatform

This platform-independent library offers a fixed-size **Decimal** class with small exponents and a predictive footprint.  

Made for Kotlin Multiplatform.

The **Decimal** class implements Number and Comparable interfaces, with a 64-Bit footprint.   
It supports math binary operators **+**, **-**, <b>*</b>, **/**, and **%**, as well as unary operators **+**, **-**, **++**, and **--**.  
Comparing like **<**, **>**, **<=**, **>=**, or **==** is also supported.


## Characteristics   

The footprint of a **Decimal** instance corresponds to the 64bit **Long** size. It consists of a 60-bit mantissa and a 4-bit exponent.

Its mantissa range is from -576_460_752_303_423_487 to +576_460_752_303_423_487.  

So, 17 to 18 significant decimal digits with 0 to 15 decimal places are supported.




### Convenient usage

#### No verbose type or class declaration

The Decimal type can be used much idiomatically like any other numeric type, just with the extension *".Dc"*.  
Like *5.Dc* or *17.48.Dc*.  
Or use a numeric String constructor like *"1228573.68".Dc* or *"12_28_573.68".Dc*.

When giving many decimal places, e.g. *15.000000000000001.Dc* (15 decimal places), rounding errors might occur  
because of the inaccuracy and rounding problems of Float and Double numbers.  
Better use: *"15.000000000000001".Dc*. This avoids the Float/Double problems.

#### Arithmetical Operators are working

Use arithmetical operators conveniently, like  
*(7.5.Dc + 8.5.Dc) / 3.Dc*

## How to start

### Configure before use
Before using, initialize the automatic rounding behavior as well as the standard output format.
``` kotlin
Decimal.initRounding(Decimal.Rounding(2, Decimal.RoundingMode.HALF_UP))

Decimal.initLocale(Decimal.Locale(null, '.', 2)) 
```
This example will configure the rounding mode to automatic commercial rounding with two decimal places.   
Moreover, using toString() or in String interpolation, all Decimals will be printed without a thousands separator, the decimal separator is a dot, and 
the fractional part is shown with at least two decimal places.

### The automatic rounding
This is necessary to fit the number's order of magnitude optimally within the overall restriction of the small footprint.  \
The default (and maximum supported) number of decimal places is 15. This will not be reserved for decimal places, but might be reached quickly through various subsequent arithmetical calculations.  
But as the width of the decimal's mantissa is limited to overall 17–18 digits, an overflow is imminent. Therefore, it is preferable to limit the share for decimal places that are really needed. E.g., for currencies this may be two digits.  
The limiting is done by automatically rounding the calculated value back to the desired decimal places with each calculaton.
The maximum decimal places that can be stored is 15.

Moreover, a decision must be made which rounding method shall be applied for this purpose.

A data class defines the required decimal places and the rounding mode that will be applied to ensure them.  
``` kotlin
 public data class Decimal.Rounding(decimalPlaces: Int, roundingMode: Decimal.RoundingMode)
 ```  
Configuring is done with a initRounding() call:  
``` Decimal.initRounding (rounding: Decimal.Rounding) ```   
See below for details.

### The automatic local formatting for input and output
When parsing or printing the **Decimal** type with ``` .toString()``` , a local formatting can automatically be applied.

It describes an optional grouping (i.e., thousands) separator, the decimal separator
and the minimum number of decimal places that will be printed.
A data class describing the local formatting can be used for configuring this.  
``` kotlin
public data class Decimal.Locale(groupSeparator: Char?,ar, minDecimalPlaces: Int)
```  
Configuring is done with a setLocale() call:  
``` Decimal.setLocale (locale: Decimal.Locale) ```   

---


### Configuring the default decimal places and rounding modes

#### The class ```Rounding``` contains details about desired decimal places and how to limit them
The default (and maximum supported) number of decimal places is 15. This will not be reserved for decimal places, but might be reached quickly through various subsequent arithmetical calculations.  
But as the width of the decimal's mantissa is limited to overall 17–18 digits, an overflow is imminent. Therefore, it is preferable to limit the share for decimal places that are really needed. E.g., for currencies this may be two digits.  
The limiting is done by automatically rounding the calculated value back to the desired decimal places with each calculaton.

``` kotlin
public class Rounding(decimalPlaces: Int, roundingMode: Decimal.RoundingMode)
```
This class denotes the maximum decimal places that all **Decimal**s shall automatically be rounded to (if necessary), as well as the rounding mode that will be used to achieve this.  
For the available rounding modes see below.

``` Rounding (2, Decimal.RoundingMode.HALF_UP)```  means that all Decimals will be rounded to two decimal places.  
``` Rounding (0, Decimal.RoundingMode.HALF_EVEN)```  means that only whole numbers will be generated, and will be rounded to the next even number.

``` kotlin
val roundingPreference: Rounding = Rounding(2,RoundingMode.HALF_EVEN)
```


#### Setting the automatic mode with ```initRounding (rounding: Rounding)```
sets the way in which every Decimal will be rounded to automatically after each arithmetic calculation.  

The supported range is from 0 to 15.   
15 is the default value and the maximum supported precision.  
``` initRounding (2, HALF_UP)```  means that all Decimals will be rounded to two decimal places.    
``` initRounding (0, HALF_EVEN)```  means that only whole numbers will be generated, and will be rounded to the next even number.

#### initLocale (groupingSeparator: Char?, decimalSeparator: Char, minDecimalPlaces: Int)
Configures how the Decimal will be formatted to with  **toString()**.
Sets grouping separator and decimal separator, and the number of minimum decimal places.  
The supported range is from zero to any positive value.   
0 is the default value and means there are no printed mandatory decimal places.  
If this setting sets more decimal places than the Decimal value has, the remaining decimal places are filled with "0"s.  
If there are more decimal places than ```minDecimalPlaces```, they are all shown
``` kotlin initLocale (',', '.', 2)```  means that the grouping separator is a comma, decimal point is a dot, and at least two decimal places will be shown.  
``` kotlin initLocale (null, '.', 0)```  means that no grouping separator is used, the decimal point is a comma, and only non-zero decimal places will be shown.


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

The automatic rounding mode, when unchanged, is *HALF_UP* (commercial rounding).


#### How to use

The rounding modes can be explicitly used in scale():
``` kotlin
Decimal.scale(desiredprecision: Int, roundingMode: RoundingMode): Decimal
```

Furthermore, there are four standard rounding functions that round to whole values (no decimal places):
``` kotlin
trunc(): Decimal  // RoundingMode.DOWN

floor(): Decimal  // RoundingMode.FLOOR

ceil(): Decimal  // RoundingMode.CEILING

round(): Decimal  // RoundingMode.HALF_EVEN
``` 

These functions are also usable with desired precision other than 0 (whole values):
``` kotlin
trunc(desiredprecision: Int): Decimal  // RoundingMode.DOWN

floor(desiredprecision: Int): Decimal  // RoundingMode.FLOOR

ceil(desiredprecision: Int): Decimal  // RoundingMode.CEILING

round(desiredprecision: Int): Decimal  // RoundingMode.HALF_EVEN
``` 



--------

### Usage (not yet active!)

Dependencies in build.gradle.kts:
``` kotlin
dependencies {
// ...
implementation("io.github.astridha:smalldecimal:0.8.5")
}
```

Import in source files:
``` kotlin
import io.github.astridha.smalldecimal.*
```


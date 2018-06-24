---
layout: documentation
---

{% include base.html %}

# Units Of Measurement

To express measured values in a scientific correct unit the framework supports units of measurement.
By using quantified decimal values in state updates and commands, the framework is able to automatically convert values to a desired unit which may be defined by the system locale or on a per-use-basis. 

## QuantityType 

Bindings use the `QuantityType` to post updates of sensor data with a quantifying unit. 
This way the framework and/or the user is able to convert the quantified value to other matching units:

A weather binding which reads temperature values in °C would use the `QuantityType` to indicate the unit as °C.
The framework is then able to convert the values to either °F or Kelvin according to the configuration of the system.
The default conversion the framework will use is locale based: 
Depended on the configured locale the framework tries to convert a `QuantityType` to the default unit of the matching measurement system. 
This is the imperial system for the United States (locale US) and Liberia (language tag "en-LR"). 
The metric system with SI units is used for the rest of the world. 
This conversion will convert the given `QuantityType` into a default unit for the specific dimension of the type. 
This is:

| Dimension     | default unit metric        | default unit imperial  |
|---------------|----------------------------|------------------------|
| Length        | Meter (m)                  | Inch (in)              |
| Temperature   | Celsius (°C)               | Fahrenheit (°F)        |
| Pressure      | Hectopascal (hPa)          | Inch of mercury (inHg) | 
| Speed         | Kilometers per hour (km/h) | Miles per hour (mph)   |
| Intensity     | Irradiance (W/m2)          | Irradiance (W/m2)      |
| Dimensionless | Abstract unit one (one)    | Abstract unit one (one)|
| Angle         | Degree (°)                 | Degree (°)             |

## NumberItem linked to QuantityType Channel

In addition to the automated conversion the `NumberItem` linked to a channel delivering `QuantityTypes` can be configured to always have state updates converted to a specific unit. 
The unit given in the state description is parsed and then used for conversion (if necessary).
The framework assumes that the unit to parse is always the last token in the state description.
If the parsing failed the locale based default conversion takes place.

    Number:Temperature temperature "Outside [%.2f °F]" { channel="...:current#temperature" }
    
In the example the `NumberItem` is specified to bind to channels which offer values from the dimension `Temperature`.
Without the dimension information the `NumberItem` only will receive updates of type `DecimalType` without a unit and any conversion.
The state description defines two decimal places for the value and the fix unit °F.
In case the state description should display the unit the binding delivers or the framework calculates through locale based conversion the pattern will look like this:
    
    "Outside [%.2f %unit%]"
    
The special placeholder `%unit%` will then be replaced by the actual unit symbol.
In addition the placeholder `%unit%` can be placed anywhere in the state description.
 
#### Defining ChannelTypes

In order to match `NumberItems` and channels and define a default state description with unit placeholder the channel also has to provide an item type which includes the dimension information:


    <channel-type id="temperature">
        <item-type>Number:Temperature</item-type>
        <label>Temperature</label>
        <description>Current temperature</description>
        <state readOnly="true" pattern="%.1f %unit%" />
    </channel-type>

The state description pattern "%.1f %unit%" describes the value format as floating point with one decimal place and also the special placeholder for the unit.

## Implementing UoM
When creating QuantityType states the framework offers some useful packages and classes:
The `org.eclipse.smarthome.core.library.unit` package contains the classes `SIUnits`, `ImperialUnits` and `SmartHomeUnits` which provide units unique to either of the measurement systems and common units used in both systems.
The `MetricPrefix` class provides prefixes like MILLI, CENTI, HECTO, etc. which are wrappers to create derived units.
The `org.eclipse.smarthome.core.library.dimension` and `javax.measure.quantity` packages provide interfaces which are used to type the generic QuantityType and units.


<h1>List of Units</h1>

List of the unit available with the UoM (Unit of Measurement) types


<h2>Imperial:</h2>

<table>
<tr><th>Type</th><th>Unit</th><th>Symbol</th></tr>
<tr><td>Pressure</td><td>Inch of Mercury</td><td>inHg</td></tr>
<tr><td>Temperature</td><td>Fahrenheit</td><td>°F</td></tr>
<tr><td>Speed</td><td>Miles per Hour</td><td>mph</td></tr>
<tr><td>Length</td><td>Inch</td><td>in</td></tr>
<tr><td>Length</td><td>Foot</td><td>ft</td></tr>
<tr><td>Length</td><td>Yard</td><td>yd</td></tr>
<tr><td>Length</td><td>Chain</td><td>ch</td></tr>
<tr><td>Length</td><td>Furlong</td><td>fur</td></tr>
<tr><td>Length</td><td>Mile</td><td>mi</td></tr>
<tr><td>Length</td><td>League</td><td>lea</td></tr>
</table>

<h2>SI:</h2>

<table>
<tr><th>Type</th><th>Unit</th><th>Symbol</th></tr>
<tr><td>Acceleration</td><td>Metre per square Second</td><td>m/s2</td></tr>
<tr><td>AmountOfSubstance</td><td>Mole</td><td>mol</td></tr>
<tr><td>Angle</td><td>Radian</td><td>rad</td></tr>
<tr><td>Angle</td><td>Degree</td><td>°</td></tr>
<tr><td>Angle</td><td>Minute Angle</td><td>'</td></tr>
<tr><td>Angle</td><td>Second Angle</td><td>''</td></tr>
<tr><td>Area</td><td>Square Metre</td><td>m2</td></tr>
<tr><td>ArealDensity</td><td>Dobson Unit</td><td>DU</td></tr>
<tr><td>CatalyticActivity</td><td>Katal</td><td>kat</td></tr>
<tr><td>Dimensionless</td><td>Percent</td><td>%</td></tr>
<tr><td>Dimensionless</td><td>Parts per Million</td><td>ppm</td></tr>
<tr><td>Dimensionless</td><td>Decibel</td><td>dB</td></tr>
<tr><td>ElectricalPotential</td><td>Volt</td><td>V</td></tr>
<tr><td>ElectricCapacitance</td><td>Farad</td><td>F</td></tr>
<tr><td>ElectricCharge</td><td>Coulomb</td><td>C</td></tr>
<tr><td>ElectricConductance</td><td>Siemens</td><td>S</td></tr>
<tr><td>ElectricCurrent</td><td>Ampere</td><td>A</td></tr>
<tr><td>ElectricInductance</td><td>Henry</td><td>H</td></tr>
<tr><td>ElectricResistance</td><td>Ohm</td><td>Ω</td></tr>
<tr><td>Energy</td><td>Joule</td><td>J</td></tr>
<tr><td>Energy</td><td>Watt Second</td><td>Ws</td></tr>
<tr><td>Energy</td><td>Watt Hour</td><td>Wh</td></tr>
<tr><td>Energy</td><td>KiloWatt Hour</td><td>kWh</td></tr>
<tr><td>Force</td><td>Newton</td><td>N</td></tr>
<tr><td>Frequency</td><td>Hertz</td><td>Hz</td></tr>
<tr><td>Illuminance</td><td>Lux</td><td>lx</td></tr>
<tr><td>Intensity</td><td>Irradiance</td><td>W/m²</td></tr>
</tr>
<tr><td>Length</td><td>Metre</td><td>m</td></tr>
<tr><td>Length</td><td>Kilometre</td><td>km</td></tr>
<tr><td>LuminousFlux</td><td>Lumen</td><td>lm</td></tr>
<tr><td>LuminousIntensity</td><td>Candela</td><td>cd</td></tr>
<tr><td>Temperature</td><td>Kelvin</td><td>K</td></tr>
<tr><td>Temperature</td><td>Celcius</td><td>°C</td></tr>
<tr><td>MagneticFlux</td><td>Weber</td><td>Wb</td></tr>
<tr><td>MagneticFluxDensity</td><td>Tesla</td><td>T</td></tr>
<tr><td>Mass</td><td>Kilogram</td><td>kg</td></tr>
<tr><td>Mass</td><td>Gram</td><td>g</td></tr>
<tr><td>Power</td><td>Watt</td><td>W</td></tr>
<tr><td>Pressure</td><td>Pascal</td><td>Pa</td></tr>
<tr><td>Pressure</td><td>hectoPascal</td><td>hPa</td></tr>
<tr><td>Pressure</td><td>Millimetre of Mercury</td><td>mmHg</td></tr>
<tr><td>Radioactivity</td><td>Becquerel</td><td>Bq</td></tr>
<tr><td>RadiationDoseAbsorbed</td><td>Gray</td><td>Gy</td></tr>
<tr><td>RadiationDoseEffective</td><td>Sievert</td><td>Sv</td></tr>
<tr><td>SolidAngle</td><td>Steradian</td><td>sr</td></tr>
<tr><td>Speed</td><td>Metre per Second</td><td>m/s</td></tr>
<tr><td>Speed</td><td>Kilometre per Hour</td><td>km/h</td></tr>
<tr><td>Speed</td><td>Knot</td><td>kn</td></tr>
<tr><td>Time</td><td>Second</td><td>s</td></tr>
<tr><td>Time</td><td>Minute</td><td>min</td></tr>
<tr><td>Time</td><td>Hour</td><td>h</td></tr>
<tr><td>Time</td><td>Day</td><td>d</td></tr>
<tr><td>Time</td><td>Week</td><td>week</td></tr>
<tr><td>Time</td><td>Year</td><td>y</td></tr>
<tr><td>Volume</td><td>Cubic Metre</td><td>m3</td></tr>
</table>

<h2>Prefixes:</h2>

⁻
<table>
<tr><th>Name</th><th>Symbol</th><th>Value</th></tr>
<tr><td>Yotta</td><td>Y</td><td>10²⁴</td></tr>
<tr><td>Zetta</td><td>Z</td><td>10²¹</td></tr>
<tr><td>Exa</td><td>E</td><td>10¹⁸</td></tr>
<tr><td>Peta</td><td>P</td><td>10¹⁵</td></tr>
<tr><td>Tera</td><td>T</td><td>10¹²</td></tr>
<tr><td>Giga</td><td>G</td><td>10⁹</td></tr>
<tr><td>Mega</td><td>M</td><td>10⁶</td></tr>
<tr><td>Kilo</td><td>k</td><td>10³</td></tr>
<tr><td>Hecto</td><td>h</td><td>10²</td></tr>
<tr><td>Deca</td><td>da</td><td>10</td></tr>
<tr><td>Deci</td><td>d</td><td>10⁻¹</td></tr>
<tr><td>Centi</td><td>c</td><td>10⁻²</td></tr>
<tr><td>Milli</td><td>m</td><td>10⁻³</td></tr>
<tr><td>Micro</td><td>µ</td><td>10⁻⁶</td></tr>
<tr><td>Nano</td><td>n</td><td>10⁻⁹</td></tr>
<tr><td>Pico</td><td>p</td><td>10⁻¹²</td></tr>
<tr><td>Femto</td><td>f</td><td>10⁻¹⁵</td></tr>
<tr><td>Atto</td><td>a</td><td>10⁻¹⁸</td></tr>
<tr><td>Zepto</td><td>z</td><td>10⁻²¹</td></tr>
<tr><td>Yocto</td><td>y</td><td>10⁻²⁴</td></tr>
</table>

To use the prefixes simply add the prefix to the unit symbol (Above).
Examples:
* milliAmpere - mA
* centiMetre - cm
* kiloWatt - kW

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
| Pressure      | Hektopascal (hPa)          | Inch of mercury (inHg) | 
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

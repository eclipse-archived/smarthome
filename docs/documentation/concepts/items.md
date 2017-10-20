---
layout: documentation
---

{% include base.html %}

# Items

Eclipse SmartHome has a strict separation between the physical world (the "things", see below) and the application, which is built around the notion of "items" (also called the virtual layer).

Items represent functionality that is used by the application (mainly user interfaces or automation logic).
Items have a state and are used through events.
  
The following item types are currently available (alphabetical order):

| Item Name      | Description | Command Types |
|----------------|-------------|---------------|
| Color          | Color information (RGB) | OnOff, IncreaseDecrease, Percent, HSB |
| Contact        | Item storing status of e.g. door/window contacts | OpenClose |
| DateTime       | Stores date and time | - |
| Dimmer         | Item carrying a percentage value for dimmers | OnOff, IncreaseDecrease, Percent |
| Group          | Item to nest other items / collect them in groups | - |
| Image          | Holds the binary data of an image | - |
| Location       | Stores GPS coordinates | Point |
| Number         | Stores values in number format | Decimal |
| Player         | Allows to control players (e.g. audio players) | PlayPause, NextPrevious, RewindFastforward |
| Rollershutter  | Typically used for blinds | UpDown, StopMove, Percent |
| String         | Stores texts | String |
| Switch         | Typically used for lights (on/off) | OnOff |

Group Items can derive their own state depending on their member items.

- AVG displays the average of the item states in the group.
- OR displays an OR of the group, typically used to display whether any item in a group has been set.
- other aggregations: AND, SUM, MIN, MAX, NAND, NOR

## State and Command Type Formatting

### StringType

`StringType` objects store a simple Java String.

### DateTimeType

`DateTimeType` objects are parsed using Java's `SimpleDateFormat.parse()` using the first matching pattern:

1. `yyyy-MM-dd'T'HH:mm:ss.SSSZ`
2. `yyyy-MM-dd'T'HH:mm:ss.SSSX`
3. `yyyy-MM-dd'T'HH:mm:ssz`
4. `yyyy-MM-dd'T'HH:mm:ss`

### DecimalType, PercentType

`DecimalType` and `PercentType` objects use Java's `BigDecimal` constructor for conversion.
`PercentType` values range from 0 to 100.

### QuantityType

A numerical type which carries a unit in addition to its value. 
Bindings use the `QuantityType` to post updates of sensor data with a quantifying unit. 
This way the framework and/or the user is able to convert the quantified values to other matching units:

A weather binding which reads temperature values in °C would use the `QuantityType` to indicate the unit as °C.
The framework is then able to convert the values to either °F or Kelvin according to the configuration of the system.
One conversion the framework will use is location based: 
depended on the configured locale the framework tries to convert a `QuantityType` to the default unit of the matching measurement system. 
This is the imperial system for the United States (locale US) and Liberia (language tag "en-LR"). 
The metric system with SI units is used for the rest of the world. 
This conversion will only convert the given `QuantityType` into a default unit for the specific dimension of the type. 
This is:

| Dimension   | default unit metric        | default unit imperial  |
|-------------|----------------------------|------------------------|
| Length      | Meter (m)                  | Inch (in)              |
| Temperature | Celsius (°C)               | Fahrenheit (°F)        |
| Pressure    | Hektopascal (hPa)          | Inch of mercury (inHg) | 
| Speed       | Kilometers per hour (km/h) | Miles per hour (mph)   |
| Intensity   | Irradiance (W/m2)          | Irradiance (W/m2)      |

In order to circumvent the default conversion a binding should give a hint to the framework which unit to use for the specific measurement system. 
This will guarantee the conversion happens to the correct unit for the sensor data. 
A weather binding which measures the amount of rain could provide a hint to convert the given value into millimeter (mm) in the metric system and inch (in) in the imperial system:

    public class MyWeatherBinding {
        public void handleCommand(ChannelUID channelUID, Command command) {
            DecimalDecimal perception = getPerceptionDay();
            QuantityType state = new QuantityType(perception, MILLI(Units.METRE), getConversionMap());
            updateState(channelUID.getId(), state);
        }
        
        private Map<MeasurementSystem, Unit<?>> getConversionMap() {
            Map<MeasurementSystem, Unit<?>> conversionMap = new HashMap<>(2);
            conversionMap.put(MeasurementSystem.SI, MILLI(Units.METRE));
            conversionMap.put(MeasurementSystem.US, ESHUnits.INCH));
            return conversionMap;
        }
    }
 
The framework uses the conversion map to determine if a locale based conversion is necessary and uses the specified units for the conversion.

In addition to the automated conversion the `NumberItem` bound to a channel delivering `QuantityTypes` can be configured to always have state updates converted to a specific unit. 
The unit given in the state description is parsed and then used for conversion (if necessary). The framework assumes that the unit is always the last token in the state description. If the parsing failed the locale based default conversion takes place.

    Number:Temperature temperature "Outside [%.2f °F]" { channel="...:current#temperature" }
    
In the example the `NumberItem` is specified to bind to channels which offer values from the dimension `Temperature`.
Without the dimension information the `NumberItem` only will receive updates of type `DecimalType` without a unit and any conversion.
The state description defines two decimal places for the value and the fix unit °F.
In case the state description should display the unit the binding delivers or the framework calculates through locale based conversion the pattern will look like this:
    
    "Outside [%.2f %unit%]"
    
The special placeholder `%unit%` will then be replaced by the actual unit symbol.

  1. Define the dimension of matching channels: This mandatory information marks a `NumberItem` for channels of a specific dimension, regardless their concrete unit. `NumberItems` without the _dimension_ binding will only receive updates of type `DecimalType`.
  
  2. Define the optional unit for this `NumberItem`. State updates to this `NumberItem` will then always be converted to the specified unit.
 
#### Defining ChannelTypes
In order to match `NumberItems` and channels and define a default state description with unit placeholder the channel type has to be provided with an additional _dimension_ tag:


    <channel-type id="temperature">
        <item-type>Number</item-type>
        <dimension>Temperature</dimension>
        <label>Temperature</label>
        <description>Current temperature</description>
        <state readOnly="true" pattern="%.1f %unit%" />
    </channel-type>

The state description pattern "%.1f %unit%" describes the value format as floating point with one decimal place and also a special placeholder for the unit. 
This way the unit can be placed anywhere in the description and will also be overridden by item or sitemap state description definitions.

### HSBType

HSB string values consist of three comma-separated values for hue (0-360°), saturation (0-100%), and value (0-100%) respectively, e.g. `240,100,100` for blue.

### PointType

`PointType` strings consist of three `DecimalType`s separated by commas, indicating latitude and longitude in degrees, and altitude in meters respectively.

### Enum Types

| Type                  | Supported Values        |
|-----------------------|-------------------------|
| IncreaseDecreaseType  | `INCREASE`, `DECREASE`  |
| NextPreviousType      | `NEXT`, `PREVIOUS`      |
| OnOffType             | `ON`, `OFF`             |
| OpenClosedType        | `OPEN`, `CLOSED`        |
| PlayPauseType         | `PLAY`, `PAUSE`         |
| RewindFastforwardType | `REWIND`, `FASTFORWARD` |
| StopMoveType          | `STOP`, `MOVE`          |
| UpDownType            | `UP`, `DOWN`            |

## A note on items which accept multiple state data types

There are a number of items which accept multiple state data types, for example `DimmerItem`, which accepts `OnOffType` and `PercentType`, `RollershutterItem`, which  accepts `PercentType` and `UpDownType`, or `ColorItem`, which accepts `HSBType`, `OnOffType` and `PercentType`.
Since an item has a SINGLE state, these multiple data types can be considered different views to this state.
The data type carrying the most information about the state is usually used to keep the internal state for the item, and other datatypes are converted from this main data type.
This main data type is normally the first element in the list returned by `Item.getAcceptedDataTypes()`.

Here is a short table demonstrating conversions for the examples above:

| Item Name     | Main Data Type | Additional Data Types Conversions |
|---------------|----------------|-----------------------------------|
| Color         | `HSBType`      | &bull; `OnOffType` - `OFF` if the brightness level in the `HSBType` equals 0, `ON` otherwise <br/> &bull; `PercentType` - the value for the brightness level in the `HSBType` |
| Dimmer        | `PercentType`  | `OnOffType` - `OFF` if the brightness level indicated by the percent type equals 0, `ON` otherwise |
| Rollershutter | `PercentType`  | `UpDownType` - `UP` if the shutter level indicated by the percent type equals 0, `DOWN` if it equals 100, and `UnDefType.UNDEF` for any other value|

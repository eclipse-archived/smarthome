---
layout: documentation
---

{% include base.html %}

# Items

Eclipse SmartHome has a strict separation between the physical world (the "things", see below) and the application, which is built around the notion of "items" (also called the virtual layer).

Items represent functionality that is used by the application (mainly user interfaces or automation logic). Items have a state and are used through events.
  
The following item types are currently available (alphabetical order):

<table>
  <tr><td><b>Itemname</b></td><td><b>Description</b></td><td><b>Command Types</b></td></tr>
  <tr><td>Color</td><td>Color information (RGB)</td><td>OnOff, IncreaseDecrease, Percent, HSB</td></tr>
  <tr><td>Contact</td><td>Item storing status of e.g. door/window contacts</td><td>OpenClose</td></tr>
  <tr><td>DateTime</td><td>Stores date and time</td><td></td></tr>
  <tr><td>Dimmer</td><td>Item carrying a percentage value for dimmers</td><td>OnOff, IncreaseDecrease, Percent</td></tr>
  <tr><td>Group</td><td>Item to nest other items / collect them in groups</td><td>-</td></tr>
  <tr><td>Number</td><td>Stores values in number format</td><td>Decimal</td></tr>
  <tr><td>Player</td><td>Allows to control players (e.g. audio players)</td><td>PlayPause, NextPrevious, RewindFastforward</td></tr>
  <tr><td>Rollershutter</td><td>Typically used for blinds</td><td>UpDown, StopMove, Percent</td></tr>
  <tr><td>String</td><td>Stores texts</td><td>String</td></tr>
  <tr><td>Switch</td><td>Typically used for lights (on/off)</td><td>OnOff</td></tr>
</table>

Group Items can derive their own state depending on their member items.

  - AVG displays the average of the item states in the group.
  - OR displays an OR of the group, typically used to display whether any item in a group has been set.
  - other aggregations:  AND, SUM, MIN, MAX, NAND, NOR


## State and Command Type Formatting

### DateTime

DateTime objects are parsed using Java's `SimpleDateFormat.parse()` using the first matching pattern:

1. `yyyy-MM-dd'T'HH:mm:ss.SSSZ`
2. `yyyy-MM-dd'T'HH:mm:ss.SSSX`
3. `yyyy-MM-dd'T'HH:mm:ssz`
4. `yyyy-MM-dd'T'HH:mm:ss`

### DecimalType, PercentType

`DecimalType` and `PercentType` objects use Java's `BigDecimal` constructor for conversion. `PercentType` values range from 0 to 100.

### HSBType

HSB string values consist of three comma-separated values for hue (0-360°), saturation (0-100%), and value (0-100%) respectively, e.g. `240,100,100` for blue.

### PointType

`PointType` strings consist of three `DecimalType`s separated by commas, indicating latitude and longitude in degrees, and altitude in meters respectively.

### Enum Types

| Type | Supported Values |
| --- | --- |
| IncreaseDecreaseType | `INCREASE`, `DECREASE` |
| NextPreviousType | `NEXT`, `PREVIOUS` |
| OnOffType | `ON`, `OFF` |
| OpenClosedType | `OPEN`, `CLOSED` |
| PlayPauseType | `PLAY`, `PAUSE` |
| RewindFastforwardType | `REWIND`, `FASTFORWARD` |
| StopMoveType | `STOP`, `MOVE` |
| UpDownType | `UP`, `DOWN` |

## A note on items which accept multiple state data types

There are a number of items which accept multiple state data types, for example `DimmerItem`, which accepts `OnOffType` and `PercentType`, `RollershutterItem`, which  accepts `PercentType` and `UpDownType`, or `ColorItem`, which accepts `HSBType`, `OnOffType` and `PercentType`. Since an item has a SINGLE state, these multiple data types can be considered different views to this state.
The data type carrying the most information about the state is usually used to keep the internal state for the item, and other datatypes are converted from this main data type. This main data type is normally the first element in the list returned by `Item.getAcceptedDataTypes()`.

Here is a short table demonstrating conversions for the examples above:

| Itemname | Main data type | Additional data types conversions |
| --- | --- | --- |
| Color | `HSBType` | &bull; `OnOffType` - `OFF` if the brightness level in the `HSBType` equals 0, `ON` otherwise <br/> &bull; `PercentType` - the value for the brightness level in the `HSBType` |
| Dimmer | `PercentType` | `OnOffType` - `OFF` if the brightness level indicated by the percent type equals 0, `ON` otherwise |
| Rollershutter | `PercentType` | `UpDownType` - `UP` if the shutter level indicated by the percent type equals 0, `DOWN` if it equals 100, and `UnDefType.UNDEF` for any other value|

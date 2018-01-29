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

## Group Items

Group items collect other items into groups.
Group items can themselves be members of other group items.
Cyclic membership is not forbidden but strongly not recommended.
User interfaces might display group items as single entries and provide navigation to its members.

Example for a Group item as a simple collection of other items:
```
    Group groundFloor
    Switch kitchenLight (groundFloor)
    Switch livingroomLight (groundFloor)
``` 

### Derive Group State from Member Items

Group items can derive their own state from their member items.
To derive a state the group item must be constructed using a base item and a group function.
When calculating the state, group functions recursively traverse the group's members and also take members of subgroups into account.
If a subgroup however defines a state on its own (having base item & group function set) traversal stops and the state of the subgroup member is taken. 

Available group functions:

| Function           | Parameters                    | Base Item                                   | Description                                                                                                                                     |
|--------------------|-------------------------------|---------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| EQUALITY           | -                             | \<all\>                                     | Sets the state of the members if all have equal state. Otherwise UNDEF is set.                                                                  |
| AND, OR, NAND, NOR | <activeState>, <passiveState> | \<all\> (must match active & passive state) | Sets the \<activeState\>, if the member state \<activeState\> evaluates to `true` under the boolean term. Otherwise the \<passiveState\> is set.|
| SUM, AVG, MIN, MAX | -                             | Number                                      | Sets the state according to the arithmetic function over all member states.                                                                     |
| COUNT              | <regular expression>          | Number                                      | Sets the state to the number of members matching the given regular expression with their states.                                                |


Examples for derived states on group items when declared in the item DSL:

- `Group:Number:COUNT(".*")` counts all members of the group matching the given regular expression, here any character or state (simply count all members).
- `Group:Number:AVG` calculates the average value over all member states which can be interpreted as `DecimalTypes`.
- `Group:Switch:OR(ON,OFF)` sets the group state to `ON` if any of its members has the state `ON`, `OFF` if all are off.    
- `Group:Switch:AND(ON,OFF)` sets the group state to `ON` if all of its members have the state `ON`, `OFF` if any of the group members has a different state than `ON`.

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

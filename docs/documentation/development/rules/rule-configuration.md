---
layout: documentation
---

{% include base.html %}

# SmartHome Rule Configuration

This document intends to describe the JSON meta definitions for the commonly used module types `ItemStateChangeTrigger`, `ItemStateCondition`, and `ItemCommandAction` in a more textual and intuitive way.

## Item State Change Trigger Configuration

Item state change triggers fire on state changes of a specified item defined in the `itemName` attribute. A trigger's type is to be set to `ItemStateChangeTrigger` when used as a state change trigger. Unlike the related `ItemStateUpdateTrigger`, this trigger requires the triggering state to have changed to a different value.

    {
      "id": "trigger_1",
      "label": "Item State Change Trigger",
      "description": "This triggers a rule if an items state changed",
      "configuration": {
        "itemName": "switchA"
      },
      "type": "ItemStateChangeTrigger"
    }

## Item State Condition Configuration

Rule conditions are usually represented by the following JSON object:

    {
      "inputs": {},
      "id": "condition_1",
      "label": "Item state condition",
      "description": "compares the items current state with the given",
      "configuration": {
        "itemName": "switchA",
        "state": "ON",
        "operator": "="
      },
      "type": "ItemStateCondition"
    }

`itemName` again holds the unique identifier of the polled item. `state` is one of the corresponding item type's supported state strings. The state string is automatically converted to a state object that fits its value and is supported by the corresponding item. For example, `ON` will be converted to an `OnOffType` and `120,100,100` will be converted to an `HSBType`. `operator` specifies a comparative operator, namely one of the following: `=`, `!=`, `<`, `>`

## Action Command Configuration

Similarly to `ItemStateCondition`s, action command configurations reference an item by name and an action string:

    {
      "inputs": {},
      "id": "action_1",
      "label": "Post command to an item",
      "description": "posts commands on items",
      "configuration": {
        "itemName": "switchB",
        "command": "OFF"
      },
      "type": "ItemPostCommandAction"
    }

The string used as the command depends on the item type and its corresponding supported command types, e.g. an HSB value of `120,100,100` to set a colored light's color to green. Similar to state change triggers, the correct state/action type is chosen automatically.

## Item Types

| Item Name | Supported State/Command Types |
| --- |
| Color | OnOff, IncreaseDecrease, Percent, HSB |
| Contact | OpenClose |
| DateTime | DateTime |
| Dimmer | OnOff, IncreaseDecrease, Percent |
| Group | |
| Number | Decimal |
| Player | PlayPause, NextPrevious, RewindFastforward |
| Rollershutter | UpDown, StopMove, Percent |
| String | String |
| Switch | OnOff |


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
| --- |
| IncreaseDecreaseType | `INCREASE`, `DECREASE` |
| NextPreviousType | `NEXT`, `PREVIOUS` |
| OnOffType | `ON`, `OFF` |
| OpenClosedType | `OPEN`, `CLOSED` |
| PlayPauseType | `PLAY`, `PAUSE` |
| RewindFastforwardType | `REWIND`, `FASTFORWARD` |
| StopMoveType | `STOP`, `MOVE` |
| UpDownType | `UP`, `DOWN` |

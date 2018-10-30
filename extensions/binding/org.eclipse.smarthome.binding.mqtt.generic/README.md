# MQTT Thing Binding

    MQTT is a machine-to-machine (M2M)/"Internet of Things" connectivity protocol. It was designed as an extremely lightweight publish/subscribe messaging transport.

This binding allows to link MQTT topics to Things.

## Supported Thing

The MQTT [Homie convention](https://github.com/homieiot/convention) version 3.x is supported by this binding.
It allows to automatically discover "homie" devices and present them as Things.
Your Homie base topic needs to be **homie**. The mapping is structured like this:


| Homie    | Framework     | Example MQTT topic                 |
|----------|---------------|------------------------------------|
| Device   | Thing         | homie/super-car                    |
| Node     | Channel Group | homie/super-car/engine             |
| Property | Channel       | homie/super-car/engine/temperature |

HomeAssistant MQTT Components are recognized as well. The base topic needs to be **homeassistant**. 
The mapping is structured like this:


| HA MQTT               | Framework     | Example MQTT topic                 |
|-----------------------|---------------|------------------------------------|
| Object                | Thing         | homeassistant/../../object         |
| Component+Node        | Channel Group | homeassistant/component/node/object|
| -> Component Features | Channel       | state/topic/defined/in/comp/config |

There is also a generic "topic" thing available, where you can manually add the following channels to:

## Supported Channels

* **String**: This channel can show the received text on the given topic and can send text to a given topic.
* **Number**: This channel can show the received number on the given topic and can send a number to a given topic. It can have a min, max and step values.
* **Dimmer**: This channel handles numeric values as percentages. It can have min, max and step values.
* **Contact**: This channel represents a open/close (on/off) state of a given topic.
* **Switch**: This channel represents a on/off state of a given topic and can send an on/off value to a given topic.
* **EnumSwitch**: This channel can represent one of a set of states.
* **Color**: This channel handles color values in RGB and HSB format.

## Thing and Channel configuration

All thing channels support JSON/XML unpacking: Usually a MQTT topic state represents a plain value like a text or a number. Some devices send a JSON/XML response instead.

### Common channel configuration parameters

* __stateTopic__: The MQTT topic that represents the state of the thing. This can be empty, the thing channel will be a state-less trigger then. You can use a wildcard topic like "sensors/+/event" to retrieve state from multiple MQTT topics. 
* __transformationPattern__: An optional transformation pattern like [JSONPath](http://goessner.net/articles/JsonPath/index.html#e2). Use http://jsonpath.com/ to verify your pattern for the latter case. An example would be JSONPATH:$.device.status.temperature for a received json input of `{device: {status: { temperature: 23.2 }}}` to extract the temperature value.
* __commandTopic__: The MQTT topic that commands are send to. This can be empty, the thing channel will be read-only then. Transformations are not applied for sending data.

### Channel Type "String"

* __allowedStates__: A comma separated list of allowed states. Example: "ONE,TWO,THREE"

You can connect this channel to a String item.

### Channel Type "Number"
 
* __min__: An optional minimum value
* __max__: An optional maximum value
* __step__: For decrease, increase commands the step needs to be known
* __isfloat__: If set to true the value is send as a decimal value, otherwise it is send as integer.

If any of the parameters is a float/double (has a decimal point value), then a float value is send to the MQTT topic otherwise an int value is send.

You can connect this channel to a Number item.

### Channel Type "Dimmer"
 
* __min__: A required minimum value.
* __max__: A required maximum value.
* __step__: For decrease, increase commands the step needs to be known

The value is internally stored as a percentage for a value between **min** and **max**.

You can connect this channel to a Rollershutter or Dimmer item.

### Channel Type "Contact", "Switch"

* __on__: A number (like 1, 10) or a string (like "ON"/"Open") that is recognised as on state.
* __off__: A number (like 0, -10) or a string (like "OFF"/"Close") that is recognised as off state.
* __inverse__: Inverse the meaning. A received "ON"/"Open" will switch the thing channel off/closed and vice versa.

The thing by default always recognises `"ON"`,`"1"`, `1` as on state and `"OFF"`, `"0"`, `0` as off state and if **on** and **off** are not configured it sends the integer values `1` for on and `0` for off.

You can connect this channel to a Contact or Switch item.

### Channel Type "EnumSwitch"

* __allowedStates__: A comma separated list of allowed states. Example: "ONE,TWO,THREE"

You can connect this channel to a String item.

### Channel Type "Color"

* __rgb__: Set this to true to use RGB format, otherwise HSB is used.

You can connect this channel to a Color item.

## Limitations

* This binding does not support Homie Node Instances.
* Homie Device Statistics (except from "interval") are not supported.
* A "$retained" attribute for properties is supported as an extension to the Homie convention. (https://github.com/homieiot/convention/issues/70)
* The following HomeAssistant MQTT Components are not implemented: Camera, Climate, Fan, Cover. The light component only supports a on/off switch and no color or brightness changes.

## Full Example

demo.Things:

```xtend
Thing mqtt:mybroker:topic:mything {
Channels:
    Type Switch : lamp "Kitchen Lamp" [ mqttstate="lamp/enabled", mqttcommand="lamp/enabled/set" ]
    Type Switch : fancylamp "Fancy Lamp" [ mqttstate="fancy/lamp/state", mqttcommand="fancy/lamp/command", on="i-am-on", off="i-am-off" ]
    Type EnumSwitch : alarmpanel "Alarm system" [ mqttstate="alarm/panel/state", mqttcommand="alarm/panel/set", allowedStates="ARMED_HOME,ARMED_AWAY,UNARMED" ]
    Type Color : lampcolor "Kitchen Lamp color" [ mqttstate="lamp/color", mqttcommand="lamp/color/set", rgb=true ]
    Type Dimmer : blind "Blind" [ mqttstate="blind/state", mqttcommand="blind/set", min=0, max=5, step=1 ]
}
```

demo.items:

```xtend
Switch Kitchen_Light "Kitchen Light" {channel="mqtt:mybroker:topic:mything:lamp" }
Rollershutter shutter "Blind" {channel="mqtt:mybroker:topic:mything:blind" }
```

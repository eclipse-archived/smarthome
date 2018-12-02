# MQTT Thing Binding

    MQTT is a machine-to-machine (M2M)/"Internet of Things" connectivity protocol. It was designed as an extremely lightweight publish/subscribe messaging transport.

This binding allows to link MQTT topics to Things.

## Supported Thing

The MQTT [Homie convention](https://homieiot.github.io/) version 3.x is supported by this binding.
It allows to automatically discover devices that follow the "Homie" convention and present them as Things.
Your Homie base topic needs to be **homie**. The mapping is structured like this:


| Homie    | Framework     | Example MQTT topic                 |
|----------|---------------|------------------------------------|
| Device   | Thing         | homie/super-car                    |
| Node     | Channel Group | homie/super-car/engine             |
| Property | Channel       | homie/super-car/engine/temperature |

System trigger channels are supported using non-retained properties, with enum datatype and with the following formats:
* Format: "PRESSED,RELEASED" -> system.rawbutton
* Format: "SHORT\_PRESSED,DOUBLE\_PRESSED,LONG\_PRESSED" -> system.button
* Format: "DIR1\_PRESSED,DIR1\_RELEASED,DIR2\_PRESSED,DIR2\_RELEASED" -> system.rawrocker

---

HomeAssistant MQTT Components are recognized as well. The base topic needs to be **homeassistant**. 
The mapping is structured like this:


| HA MQTT               | Framework     | Example MQTT topic                 |
|-----------------------|---------------|------------------------------------|
| Object                | Thing         | homeassistant/../../object         |
| Component+Node        | Channel Group | homeassistant/component/node/object|
| -> Component Features | Channel       | state/topic/defined/in/comp/config |

---

There is also a generic "topic" thing available.

You can manually add the following channels:

## Supported Channels

* **string**: This channel can show the received text on the given topic and can send text to a given topic.
* **number**: This channel can show the received number on the given topic and can send a number to a given topic. It can have a min, max and step values.
* **dimmer**: This channel handles numeric values as percentages. It can have min, max and step values.
* **contact**: This channel represents a open/close state of a given topic.
* **switch**: This channel represents a on/off state of a given topic and can send an on/off value to a given topic.
* **colorRGB**: This channel handles color values in RGB format.
* **colorHSB**: This channel handles color values in HSB format.

## Thing and Channel configuration

All things require a configured broker.

### Common channel configuration parameters

* __stateTopic__: The MQTT topic that represents the state of the thing. This can be empty, the thing channel will be a state-less trigger then. You can use a wildcard topic like "sensors/+/event" to retrieve state from multiple MQTT topics. 
* __transformationPattern__: An optional transformation pattern like [JSONPath](http://goessner.net/articles/JsonPath/index.html#e2).
* __commandTopic__: The MQTT topic that commands are send to. This can be empty, the thing channel will be read-only then. Transformations are not applied for sending data.
* __formatBeforePublish__: Format a value before it is published to the MQTT broker. The default is to just pass the channel/item state. If you want to apply a prefix, say "MYCOLOR,", you would use "MYCOLOR,%s". If you want to adjust the precision of a number to for example 4 digits, you would use "%.4f".

### Channel Type "string"

* __allowedStates__: An optional comma separated list of allowed states. Example: "ONE,TWO,THREE"

You can connect this channel to a String item.

### Channel Type "number"
 
* __min__: An optional minimum value
* __max__: An optional maximum value
* __step__: For decrease, increase commands the step needs to be known
* __isDecimal__: If set to true the value is send as a decimal value, otherwise it is send as integer.

If any of the parameters is a float/double (has a decimal point value), then a float value is send to the MQTT topic otherwise an int value is send.

You can connect this channel to a Number item.

### Channel Type "dimmer"
 
* __min__: A required minimum value.
* __max__: A required maximum value.
* __step__: For decrease, increase commands the step needs to be known

The value is internally stored as a percentage for a value between **min** and **max**.

You can connect this channel to a Rollershutter or Dimmer item.

### Channel Type "contact", "switch"

* __on__: A number (like 1, 10) or a string (like "ON"/"Open") that is recognised as on/open state.
* __off__: A number (like 0, -10) or a string (like "OFF"/"Close") that is recognised as off/closed state.

The contact channel by default recognises `"OPEN"` and `"CLOSED"`. You can connect this channel to a Contact item.
The switch channel by default recognises `"ON"` and `"OFF"`. You can connect this channel to a Switch item.

If **on** and **off** are not configured it publishes the strings mentioned before respectively.

You can connect this channel to a Contact or Switch item.

### Channel Type "colorRGB", "colorHSB"

You can connect this channel to a Color item.

This channel will publish the color as comma separated list to the MQTT broker,
e.g. "112,54,123" for an RGB channel (0-255 per component) and "360,100,100" for a HSB channel (0-359 for hue and 0-100 for saturation and brightness).

The channel expects values on the corresponding MQTT topic to be in this format as well. 

## Limitations

* This binding does not support Homie Node Instances.
* Homie Device Statistics (except from "interval") are not supported.
* The following HomeAssistant MQTT Components are not implemented: Camera, Climate, Fan, Cover. The light component only supports a on/off switch and no color or brightness changes.

## Incoming value transformation

All mentioned channels can have a configured optional transformation for an incoming MQTT topic value.

This is required if your received value is wrapped in a JSON or XML response.

Here are a few examples to unwrap a value from a complex response:

| Received value                                                      | Tr. Service | Transformation                            |
|---------------------------------------------------------------------|-------------|-------------------------------------------|
| `{device: {status: { temperature: 23.2 }}}`                         | JSONPATH    | "JSONPATH:$.device.status.temperature"    |
| `<device><status><temperature>23.2</temperature></status></device>` | XPath       | "XPath:/device/status/temperature/text()" |
| `THEVALUE:23.2°C`                                                    | REGEX       | "REGEX::(.*?)°"                          |

## Format before Publish

This feature is quite powerful in transforming an item state before it is published to the MQTT broker.
It has the syntax: `%[flags][width]conversion`.
Find the full documentation on the [Java](https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html) web page.

The default is "%s" which means: Output the item state as string.

Here are a few examples:

* All uppercase: "%S". Just use the upper case letter for the conversion argument.
* Apply a prefix: "myprefix%s"
* Apply a suffix: "%s suffix"
* Number precision: ".4f" for a 4 digit precision. Use the "+" flag to always add a sign: "+.4f".
* Decimal to Hexadecimal/Octal/Scientific: For example "60" with "%x", "%o", "%e" becomes "74", "3C", "60".
* Date/Time: To reference the item state multiple times, use "%1$". Use the "tX" conversion where "X" can be any of [h,H,m,M,I,k,l,S,p,B,b,A,a,y,Y,d,e].
  - For an output of *May 23, 1995* use "%1$**tb** %1$**te**,%1$**tY**".
  - For an output of *23.05.1995* use "%1$**td**.%1$**tm**.%1$**tY**".
  - For an output of *23:15* use "%1$**tH**:%1$**tM**".

## Full Example

demo.Things:

```xtend
Bridge mqtt:broker:myUnsecureBroker [ host="192.168.0.42",secure="OFF" ]
{
    Thing mqtt:topic:mything {
    Channels:
        Type switch : lamp "Kitchen Lamp" [ mqttstate="lamp/enabled", mqttcommand="lamp/enabled/set" ]
        Type switch : fancylamp "Fancy Lamp" [ mqttstate="fancy/lamp/state", mqttcommand="fancy/lamp/command", on="i-am-on", off="i-am-off" ]
        Type string : alarmpanel "Alarm system" [ mqttstate="alarm/panel/state", mqttcommand="alarm/panel/set", allowedStates="ARMED_HOME,ARMED_AWAY,UNARMED" ]
        Type color : lampcolor "Kitchen Lamp color" [ mqttstate="lamp/color", mqttcommand="lamp/color/set", rgb=true ]
        Type dimmer : blind "Blind" [ mqttstate="blind/state", mqttcommand="blind/set", min=0, max=5, step=1 ]
    }
}
```

demo.items:

```xtend
Switch Kitchen_Light "Kitchen Light" {channel="mqtt:mybroker:topic:mything:lamp" }
Rollershutter shutter "Blind" {channel="mqtt:mybroker:topic:mything:blind" }
```

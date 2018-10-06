# MQTT Thing Binding

    MQTT is a machine-to-machine (M2M)/"Internet of Things" connectivity protocol. It was designed as an extremely lightweight publish/subscribe messaging transport.

This binding allows to link MQTT topics to Things.

## Supported Thing

The MQTT [homie convention](https://github.com/homieiot/convention) version 3.x is supported by this binding.
It allows to automatically discover "homie" things.
Your homie base topic needs to be **homie**. The mapping is structured like this:


| Homie    | Framework     | Example MQTT topic                 |
|----------|---------------|------------------------------------|
| Device   | Thing         | homie/super-car                    |
| Node     | Channel Group | homie/super-car/engine             |
| Property | Channel       | homie/super-car/engine/temperature |

There is also a generic "topic" thing available, where you can manually add the following channels to:

## Supported Channels

* **String**: This channel can show the received text on the given topic and can send text to a given topic.
* **Number**: This channel can show the received number on the given topic and can send a number to a given topic. It can have a min, max and step values.
* **Dimmer**: This channel handles numeric values as percentages. It can have min, max and step values.
* **Contact**: This channel represents a open/close (on/off) state of a given topic.
* **Switch**: This channel represents a on/off state of a given topic and can send an on/off value to a given topic.
* **Color**: This channel handles color values in RGB and HSB format.

## Thing and Channel configuration

All thing channels support JSON/XML unpacking: Usually a MQTT topic state represents a plain value like a text or a number. Some devices send a JSON/XML response instead.

### Common channel configuration parameters

* __stateTopic__: The MQTT topic that represents the state of the thing. This can be empty, the thing channel will be a state-less trigger then. 
* __commandTopic__: The MQTT topic that commands are send to. This can be empty, the thing channel will be read-only then.
* __transformationPattern__: An optional transformation pattern like [JSONPath](http://goessner.net/articles/JsonPath/index.html#e2). Use http://jsonpath.com/ to verify your pattern for the latter case. An example would be JSONPATH:$.device.status.temperature for a received json input of `{device: {status: { temperature: 23.2 }}}` to extract the temperature value.

Please be aware, it is possible to extract a plain value from incoming JSON, XML and other supported formats, but the other way round does not work. This binding is not capable of packing a plain value into a formatted response for sending it to a MQTT command topic.

Also note, that things will utterly go wrong, if you define multiple channels for the same state topic.
 
### Channel Type "Number"
 
* __min__: A minimum value, necessary if the thing channel is used as a Rollershutter or Dimmer.
* __max__: A maximum value, necessary if the thing channel is used as a Rollershutter or Dimmer.
* __step__: Because Rollershutter and Dimmer can send decrease, increase commands, we need to know the step.
* __isfloat__: If set to true the value is send as a decimal value, otherwise it is send as integer.

If any of the parameters is a float/double (has a decimal point value), then a float value is send to the MQTT topic otherwise an int value is send.

You can connect this channel to a Number item.

### Channel Type "Dimmer"
 
* __min__: A minimum value, necessary if the thing channel is used as a Rollershutter or Dimmer.
* __max__: A maximum value, necessary if the thing channel is used as a Rollershutter or Dimmer.
* __step__: Because Rollershutter and Dimmer can send decrease, increase commands, we need to know the step.
* __isfloat__: If set to true the value is send as a decimal value, otherwise it is send as integer.

If any of the parameters is a float/double (has a decimal point value), then a float value is send to the MQTT topic otherwise an int value is send.

You can connect this channel to a Rollershutter or Dimmer item.

### Channel Type "Contact", "Switch"

* __on__: A number (like 1, 10) or a string (like "ON"/"Open") that is recognised as on state.
* __off__: A number (like 0, -10) or a string (like "OFF"/"Close") that is recognised as off state.
* __inverse__: Inverse the meaning. A received "ON"/"Open" will switch the thing channel off/closed and vice versa.

The thing by default always recognises `"ON"`,`"1"`, `1` as on state and `"OFF"`, `"0"`, `0` as off state and if **on** and **off** are not configured it sends the integer values `1` for on and `0` for off.

You can connect this channel to a Contact or Switch item.

### Channel Type "Color"

* __rgb__: Set this to true to use RGB format, otherwise HSB is used.

You can connect this channel to a Color item.

## Limitations

This binding does not support Homie Node Instances. Homie Device Statistics (except from "interval") are not supported.

## Full Example

demo.Things:

```xtend
mqtt:mybroker:topic:mything {
    text [ mqttstate="lamp/state", mqttcommand="lamp/command" ]
}
```

demo.items:

```xtend
Switch Kitchen_Light "Kitchen Light" {mqtt="<[...], >[...]" }
```

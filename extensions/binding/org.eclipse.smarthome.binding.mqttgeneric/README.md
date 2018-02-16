# MqttGeneric Binding

    MQTT is a machine-to-machine (M2M)/"Internet of Things" connectivity protocol. It was designed as an extremely lightweight publish/subscribe messaging transport.

This binding allows to bind a Mqtt topic to a Thing. A Broker connection needs to be already defined for example by using the MqttBroker binding.

If your Mqtt device follows a specific Mqtt topic specification, you may want to use a specialised Mqtt binding instead of this generic one, so that auto discovery of things is available.

## Supported Bridge

* **brokerconnection**: This bridge represents a broker connection. Because broker connections are not managed by this binding, you only need to provide the connection name for the bridge to work.

## Supported Thing

There is one thing available ("topic"), where you can add the following channels to:

## Supported Channels

* **text**: This channel can show the received text on the given topic and can send text to a given topic.
* **number**: This channel can show the received number on the given topic and can send a number to a given topic. The thing can have a min, max and step value.
* **onoff**: This channel represents a on/off state of a given topic and can send an on/off value to a given topic.

## Discovery

Broker connections are automatically discovered and presented as bridges. Mqtt provides no means to enumerate available topics though, therefore things representing topics cannot be automatically discovered.

If your Mqtt device follows a specific Mqtt topic specification, you may want to use a specialised Mqtt binding instead of this generic one, so that auto discovery of things is available.

## Thing and Channel configuration

The bridge only needs a connection **name**. A thing does not need configuration and can be used to organise your Mqtt topics.

All thing channels support JSON/XML unpacking: Usually a Mqtt topic state represents a plain value like a text or a number. Some devices send a JSON/XML response instead.

### Common channel configuration parameters

* __mqttstate__: The Mqtt topic that represents the state of the thing. This can be empty, the thing channel will be a state-less trigger then. 
* __mqttcommand__: The Mqtt topic that commands are send to. This can be empty, the thing channel will be read-only then.
* __transformpattern__: An optional transformation pattern like [JSONPath](http://goessner.net/articles/JsonPath/index.html#e2). Use http://jsonpath.com/ to verify your pattern for the latter case. An example would be JSONPATH:$.device.status.temperature for a received json input of `{device: {status: { temperature: 23.2 }}}` to extract the temperature value.

Please be aware, we are able to extract a plain value from incoming JSON, XML and other supported formats, but this binding is not capable of packing a plain value into a formatted response for sending it to a Mqtt command topic.
 
### Number channel
 
* __min__: A minimum value, necessary if the thing channel is used as a Rollershutter or Dimmer.
* __max__: A maximum value, necessary if the thing channel is used as a Rollershutter or Dimmer.
* __step__: Because Rollershutter and Dimmer can send decrease, increase commands, we need to know the step.
* __isfloat__: If set to true the value is send as a decimal value, otherwise it is send as integer.

If any of the parameters is a float/double (has a decimal point value), then a float value is send to the Mqtt topic otherwise an int value is send.

You can connect this channel to a Number, Rollershutter, Dimmer item.

### OnOff channel

* __on__: A number (like 1, 10) or a string (like ON) that is recognised as on state.
* __off__: A number (like 0, -10) or a string (like OFF) that is recognised as off state.
* __inverse__: Inverse the meaning. A received "ON" will switch the thing channel off and vice versa.

The thing by default always recognises `"ON"`,`"1"`, `1` as on state and `"OFF"`, `"0"`, `0` as off state and if **on** and **off** are not configured it sends the integer values 1 for on and 0 for off.

You can connect this channel to a Contact or Switch item.

## Full Example

demo.Things:

```xtend
mqttgeneric:brokerconnection:mybroker [ brokername="configured-broker" ]
mqttgeneric:mybroker:topic:mything {
    text [ mqttstate="lamp/state", mqttcommand="lamp/command" ]
}
```
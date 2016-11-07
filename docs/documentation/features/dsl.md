---
layout: documentation
---

{% include base.html %}

# Textual Configuration

Eclipse SmartHome provides the possibility to do fully text-based system setups. This is done using domain specific languages (DSLs) for the different kinds of artifacts.

## Thing Configuration DSL

Things can be configured with a Domain specific Language (DSL). It is recommended to use the Eclipse SmartHome Designer for editing the DSL files, because it supports validation and auto-completion.

Thing configuration files must be placed under the `things` folder inside the Eclipse SmartHome `conf` folder and must end with the suffix `.things`.

## Defining Things

Things can be defined as followed:

```
Thing yahooweather:weather:berlin [ location=638242 ]
Thing yahooweather:weather:losangeles "Los Angeles" @ "home" [ location=2442047, unit="us", refresh=120 ]
```

The first keyword defines whether the entry is a bridge or a thing. The next statement defines the UID of the thing which contains of the following three segments: binding id, thing type id, thing id. So the first two segments must match to thing type supported by a binding (e.g. `yahooweather:weatheryahooweather`), whereas the thing id can be freely defined. Optionally, you may provide a label in order to recognize it easily, otherwise the default label from the thing type will be displayed.

To help organizing your things, you also may define a location (here: "home"), which should point to an item. This item can either be a simple String item carrying e.g. the room name, or you may of course also use a Location item containing some geo coordinates.  

Inside the squared brackets configuration parameters of the thing are defined.

The type of the configuration parameter is determined by the binding and must be specified accordingly in the DSL. If the binding requires a text the configuration parameter must be specified as a decimal value: `location=2442047`. Other types are for example boolean values (`refreshEnabled=true`).

For each thing entry in the DSL the framework will create a thing by calling the ThingFactory of the according binding.

### Shortcut

It is possible to skip the `Thing` keyword: `yahooweather:weather:berlin [ location=638242 ]`

## Defining Bridges

The DSL also supports the definition of bridges and contained things. The following configuration shows the definition of a hue bridge with two hue lamps:

```
Bridge hue:bridge:mybridge [ ipAddress="192.168.3.123" ] {
	Thing 0210 bulb1 [ lightId="1" ]
	Thing 0210 bulb2 [ lightId="2" ]
}
```

Within the curly brackets things can be defined, that should be members of the bridge. For the contained thing only the thing type ID and thing ID must be defined (e.g. `0210 bulb1`). So the syntax is `Thing <thingTypeId> <thingId> []`. The resulting UID of the thing is `hue:0210:mybridge:bulb1`.

Bridges that are defined somewhere else can also be referenced in the DSL:

```
Thing hue:0210:mybridge:bulb (hue:bridge:mybridge) [lightId="3"]
```

The referenced bridge is specified in the parentheses. Please notice that the UID of the thing also contains the bridge ID as third segment. For the contained notation of things the UID will be inherited and the bridge ID is automatically taken as part of the resulting thing UID.

## Defining Channels

It is also possible to manually define channels in the DSL. Usually this is not needed, as channels will be automatically created by the binding based on the thing type description. But there might be some bindings, that require the manual definition of channels.

### State channels

```
Thing yahooweather:weather:losangeles [ location=2442047, unit="us", refresh=120 ] {
	Channels:
		State String : customChannel1 "My Custom Channel" [
			configParameter="Value"
		]
		State Number : customChannel2 []
}
```

Each channel definition must be placed inside the curly braces and begin with the keyword `State` followed by the accepted item type (e.g. String). After this the channel ID follows with the configuration of a channel. The framework will merge the list of channels coming from the binding and the user-defined list in the DSL.

As state channels are the default channels, you can omit the `State` keyword, the following example creates the same channels as the example above:

```
Thing yahooweather:weather:losangeles [ location=2442047, unit="us", refresh=120 ] {
	Channels:
		String : customChannel1 "My Custom Channel" [
			configParameter="Value"
		]
		Number : customChannel2 []
}
```

You may optionally give the channel a proper label (like "My Custom Channel" in the example above) so you can distinguish the channels easily. 


### Trigger channels

```
Thing yahooweather:weather:losangeles [ location=2442047, unit="us", refresh=120 ] {
	Channels:
		Trigger String : customChannel1 [
			configParameter="Value"
		]
}
```

Trigger channels are defined with the keyword `Trigger` and only support the type String.

### Referencing existing channel types

Many bindings provide standalone channel type definitions like this:  

```
<thing:thing-descriptions bindingId="yahooweather" [...]>
    <channel-type id="temperature">
        <item-type>Number</item-type>
        <label>Temperature</label>
        <description>Current temperature in degrees celsius</description>
        <category>Temperature</category>
        <state readOnly="true" pattern="%.1f °C">
        </state>
    </channel-type>
    [...]
</thing:thing-descriptions>
``` 

They can be referenced within a thing's channel definition, so that they need to be defined only once and can be reused for many channels. You may do so in the DSL as well:

```
Thing yahooweather:weather:losangeles [ location=2442047, unit="us", refresh=120 ] {
    Channels:
        Type temperature : my_yesterday_temperature "Yesterday's Temperature"
}
``` 

The `Type` keyword indicates a reference to an existing channel definition. The channel kind and accepted item types of course are takes from the channel definition, therefore they don't need to be specified here again. 

You may optionally give the channel a proper label (like "Yesterday's Temperature" in the example above) so you can distinguish the channels easily. If you decide not to, then the label from the referenced channel type definition will be used.

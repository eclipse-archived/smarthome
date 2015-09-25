---
layout: documentation
---

{% include base.html %}

# Textual Configuration

Eclipse SmartHome provides the possibility to do fully text-based system setups. This is done using domain specific languages (DSLs) for the different kind of artifacts.

## Thing Configuration DSL

Things can be configured with a Domain specific Language (DSL). It is recommended to use the Eclipse SmartHome Designer for editing the DSL files, because it supports validation and auto-completion.

Thing configuration files must be placed under the `things` folder inside the Eclipse SmartHome `conf` folder and must end with the suffix `.things`.

## Defining Things

Things can be defined as followed:

```
Thing yahooweather:weather:berlin [ location="638242" ]
Thing yahooweather:weather:losangeles [ location="2442047", unit="us", refresh=120 ]
```

The first keyword defines whether the entry is a bridge or a thing. The next statement defines the UID of the thing which contains of the following three segments: binding id, thing type id, thing id. So the first two segments must match to thing type supported by a binding (e.g. `yahooweather:weatheryahooweather`), whereas the thing id can be freely defined. Inside the squared brackets configuration parameter of the thing are defined.

The type of the configuration parameter is determined by the binding and must be specified accordingly in the DSL. If the binding requires a text the configuration parameter must be specified as a string: `location="2442047"`. Other types are decimal values (`refresh=12`) and boolean values (`refreshEnabled=true`).

For each thing entry in the DSL the framework will create a thing by calling the ThingFactory of the according binding.

### Shortcut

It is possible to skip the `Thing` keyword: `yahooweather:weather:berlin [ location="638242" ]`

## Defining Bridges

The DSL also supports the definition of bridges and contained things. The following configuration shows the definition of a hue bridge with two hue lamps:

```
Bridge hue:bridge:mybridge [ ipAddress="192.168.3.123" ] {
	Thing LCT001 bulb1 [ lightId="1" ]
	Thing LCT001 bulb2 [ lightId="2" ]
}
```

Within the curly brackets things can be defined, that should be members of the bridge. For the contained thing only the thing type ID and thing ID must be defined (e.g. `LCT001 bulb1`). So the syntax is `Thing <thingTypeId> <thingId> []`. The resulting UID of the thing is `hue:LCT001:mybridge:bulb1`.

Bridges that are defined somewhere else can also be referenced in the DSL:

```
Thing hue:LCT001:mybridge:bulb (hue:bridge:mybridge) [lightId="3"]
```

The referenced bridge is specified in the parentheses. Please notice that the UID of the thing also contains the bridge ID as third segment. For the contained notation of things the UID will be inherited and the bridge ID is automatically taken as part of the resulting thing UID.

## Defining Channels

It is also possible to manually define channels in the DSL. Usually this is not needed, as channels will be automatically created by the binding based on the thing type description. But there might be some bindings, that require the manual definition of channels.

```
Thing yahooweather:weather:losangeles [ location="2442047", unit="us", refresh=120 ] {
	Channels:
		String : customChannel1 [
			configParameter="Value"
		]
		Number : customChannel2 []
}
```

Each channel definition must be placed inside the curly braces and begin with the accepted item type (e.g. String). After this the channel ID follows with the configuration of a channel. The framework will merge the list of channels coming from the binding and the user defined list in the DSL.
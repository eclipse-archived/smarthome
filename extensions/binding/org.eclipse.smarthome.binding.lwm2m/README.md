# Lwm2m (Leightweight machine to machine) Binding

_[OMA’s LightweightM2M](http://openmobilealliance.org/iot/lightweight-m2m-lwm2m) is a device management and control protocol designed for low power sensor and actuator networks. The LwM2M protocol features a design based on REST, defines an extensible resource and data model and builds on the Constrained Application Protocol (CoAP)._

LwM2M is compareable to a MQTT convention and specifies predefined objects / ressources (for example LEDs, switches, etc) which can easily be mapped to Eclipse Smarthome things / channels.

This binding implements an LwM2M server, devices are called LwM2M clients. Clients that are bootstraped to connect to this server are automatically available in the discovery inbox. A client implements a so called *LwM2M Data Model*. The LwM2M data model is very strictly organized as a three-level tree.

 * Objects: *LwM2M objects* are defined in the [OMA LwM2M object registry](http://www.openmobilealliance.org/wp/OMNA/LwM2M/LwM2MRegistry.html). You usually do not interact with objects directly except for querying for object instances. An object could be a light bulb or a switch.
 * Object instance: If an object represents for example a light bulb, an instance correlates to an actual controllable light bulb. This way a device can provide multiple light bulb or switch instances to the server.
 * Object resource: A lightbulb can be turned on/off but may also have a hue, saturation and brightness value. A resource represents one one specific property and is very much compareable to a Eclipse Smarthome Thing Channel.
 
 *Lwm2m Objects* are modelled as Bridges, *Lwm2m Object Instances* as Things and *Lwm2m Ressources* are modelled as Channels. Each object instance is listed in the discovery inbox and will be added with the recognised channels.

## Supported Things

The things are automatically generated from the [OMA LwM2M object registry](http://www.openmobilealliance.org/wp/OMNA/LwM2M/LwM2MRegistry.html). Please check the PaperUI binding section and list the available things.

## Channels

The channels are automatically generated from the [OMA LwM2M object registry](http://www.openmobilealliance.org/wp/OMNA/LwM2M/LwM2MRegistry.html). Please check the PaperUI binding section and list the available channels.

## Full Example

We create a light bulb thing for the first light bulb oject instance on device with LwM2M client id "32:14:42:12:12:42".
The on/off switch is connected to the LwM2M On/Off resource (5850) of the light bulb thing.
A dimmer is connected to the LwM2M brightness resource (5851).

demo.Things:

```
lwm2m:3311:0 [ clientid="32:14:42:12:12:42" ]
```

demo.items:

```
Switch Lightbulb0 { channel="lwm2m:3311:0:5850" }
Dimmer Lightbulb0Dimmer { channel="lwm2m:3311:0:5851" }
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
	Frame {
		Switch item=Lightbulb0
		Dimmer item=Lightbulb0Dimmer
	}
}
```

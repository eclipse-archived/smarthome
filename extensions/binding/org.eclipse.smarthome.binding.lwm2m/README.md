# LwM2M (Leightweight machine to machine) Binding

_[OMA’s LightweightM2M](http://openmobilealliance.org/iot/lightweight-m2m-lwm2m) is a device management and control protocol designed for low power sensor and actuator networks. The LwM2M protocol features a design based on REST, defines an extensible resource and data model and builds on the Constrained Application Protocol (CoAP)._

LwM2M is comparable to a MQTT convention and specifies predefined objects / ressources (for example LEDs, switches, etc) which can easily be mapped to Eclipse Smarthome things / channels. We also need to start a server instance, like in MQTT.

This binding implements an LwM2M server, devices are called LwM2M clients. Clients that are bootstrapped to connect to this server are automatically available in the discovery inbox. A client implements a so called *LwM2M Data Model*. The LwM2M data model is very strictly organized as a three-level tree.

 * Objects: *LwM2M objects* are defined in the [OMA LwM2M object registry](http://www.openmobilealliance.org/wp/OMNA/LwM2M/LwM2MRegistry.html). You usually do not interact with objects directly except for querying for object instances. An object could be a light bulb or a switch.
 * Object instance: If an object represents for example a light bulb, an instance correlates to an actual controllable light bulb. This way a device can provide multiple light bulb or switch instances to the server.
 * Object resource: A light-bulb can be turned on/off but may also have a hue, saturation and brightness value. A resource represents one one specific property and is very much comparable to a Eclipse Smarthome Thing Channel.
 
 *LwM2M Objects* are modeled as Bridges, *LwM2M Object Instances* as Things and *LwM2M Resources* are modeled as Channels. Each object instance is listed in the discovery inbox and will be added with the recognized channels.

## Binding configuration

Because the binding needs so start a LwM2M server, there is some configuration available. The default values are fine for the start, but the secure parameters should be adapted if the network is accessible for others.

* **port**: LwM2M Server Port. The default is 5683.
* **port_secure**: LwM2M Server Port (secure). The default is 5684.
* **secure_method**: Can be one of "No encryption", "Elliptic curve (TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8)", "Pre-shared key (TLS_PSK_WITH_AES_128_CCM_8)". Default is no encryption.
* **secure_public_key**: The key used for DTLS. This is the private part if elliptic curves are used. Default is: `fcc28728c123b155be410fc1c0651da374fc6ebe7f96606e90d927d188894a73`.
* **secure_point_x**: Elliptic curve, x coordinate in Hexadecimal. Only used if elliptic curves are used. Default is: `d2ffaa73957d76984633fc1cc54d0b763ca0559a9dff9706e9f4557dacc3f52a`.
* **secure_point_y**: Elliptic curve, y coordinate in Hexadecimal. Only used if elliptic curves are used. Default is: `1dae121ba406802ef07c193c1ee4df91115aabd79c1ed7f4c0ef7ef6a5449400`.

## Supported Things

The things are automatically generated from the [OMA LwM2M object registry](http://www.openmobilealliance.org/wp/OMNA/LwM2M/LwM2MRegistry.html). Please check the PaperUI binding section and list the available things.

## Channels

The channels are automatically generated from the [OMA LwM2M object registry](http://www.openmobilealliance.org/wp/OMNA/LwM2M/LwM2MRegistry.html). Please check the PaperUI binding section and list the available channels.

## Full Example

We first need to define a bridge thing, that represents the LwM2M client device identified by the client id "32:14:42:12:12:42".
In this example the device is a light bulb device. The LwM2M object that represents a light-bulb is 3311 and we want the first light bulb object instance.
The on/off switch is connected to the LwM2M On/Off resource (5850) of the light bulb thing.
A dimmer is connected to the LwM2M brightness resource (5851).

demo.Things:

```
Bridge lwm2m:client:mydevice [ clientid="32:14:42:12:12:42" ] {
    3311i1 myDimmableBulb [ object=3311, instance=1 ]
}
```

demo.items:

```
Switch Lightbulb0 { channel="lwm2m:3311i1:mydevice:myDimmableBulb:5850" }
Dimmer Lightbulb0Dimmer { channel="lwm2m:3311i1:mydevice:myDimmableBulb:5851" }
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

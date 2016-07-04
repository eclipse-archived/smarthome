---
layout: documentation
---

{% include base.html %}

# Philips Hue Binding

This binding integrates the [Philips Hue Lighting system](http://www.meethue.com).
The integration happens through the Hue bridge, which acts as an IP gateway to the ZigBee devices.

![Philips Hue](doc/hue.jpg)


## Supported Things

The Hue bridge is required as a "bridge" for accessing any other Hue devies.

Almost all available Hue devices are supported by this binding. This includes not only the "friends of Hue", but also products like the LivingWhites adapter. Additionally, it is possible to use Osram Lightify bulbs as well as other ZigBee LightLink compatible products like e.g. the [GE bulb](http://gelinkbulbs.com/). Please note that the devices need to be registered with the Hue bridge before it is possible for this binding to use them.

The hue binding supports all five types of lights of the Hue system: [Supported lights](http://www.developers.meethue.com/documentation/supported-lights). These are:

| Light type              | ZigBee Device ID | Thing type |
|-------------------------|------------------|------------|
| On/off light            | 0x0000           | 0000       |
| Dimmable light          | 0x0100           | 0100       |
| Color light             | 0x0200           | 0200       |
| Extended Color light    | 0x0210           | 0210       |
| Color temperature light | 0x0220           | 0220       |

All different models of Hue or Osram bulbs nicely fit into one of these five categories.

## Discovery

The Hue bridge is discovered through UPnP in the local network. Once it is added as a Thing, its authentication button (in the middle) needs to be pressed in order to authorize the binding to access it. Once the binding is authorized, it automatically reads all devices that are set up on the Hue bridge and puts them in the Inbox.

## Thing Configuration

The Hue bridge requires the ip address as a configuration value in order for the binding to know where to access it.
In the thing file, this looks e.g. like

```
Bridge hue:bridge:1 [ ipAddress="192.168.0.64" ]
```

A user to authenticate against the Hue bridge is automatically generated. Please note that the generated user name cannot be written automatically to the .thing file, and has to be set manually. The generated user name can be found in the log files after pressing the authentication button on the bridge.
The user name can be set using the `userName` configuration value, e.g.:

```
Bridge hue:bridge:1 [ ipAddress="192.168.0.64", userName="qwertzuiopasdfghjklyxcvbnm1234" ]
```

The bulbs are identified by the number that the Hue bridge assigns to them (also shown in the Hue app as an identifier).
Thus, all if needs for manual configuration is this single value like

```
0210 bulb1 [ lightId="1" ]
```


## Channels

All devices support some of the following channels:

| Channel Type ID   | Item Type | Description                                                                                                                            | Thing types supporting this channel |
|-------------------|-----------|----------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| color             | Color     | This channel supports full color control with hue, saturation and brightness values.                                                   | 0200, 0210                          |  
| brightness        | Dimmer    | This channel supports adjusting the brightness value. Note that this is not available, if the color channel is supported.              | 0100, 0220                          |
| color_temperature | Dimmer    | This channel supports adjusting the color temperature from cold (0%) to warm (100%)                                                    | 0210, 0220                          |
| alert             | String    | This channel supports displaying alerts by flashing the bulb either once or multiple times. Valid values are: NONE, SELECT and LSELECT | 0000, 0100, 0200, 0210, 0220        |
| effect            | Switch    | This channel supports color looping.                                                                                                   | 0200, 0210, 0220                    |

## Full Example

In this example **Bulb1** is a standard Philips HUE bulb (LCT001) which supports color and color_temperature. Therefore it is a thing of type _0210_. **Bulb2** is a OSRAM tunable white bulb (PAR16 50 TW) supporting color_temperature and so the type is _0220_.

### demo.things:

```
Bridge hue:bridge:1 [ ipAddress="192.168.0.64" ] {
	0210 bulb1 [ lightId="1" ]
	0220 bulb2 [ lightId="2" ]
}
```

### demo.items:

```
// Bulb1
Switch	Light1_Toggle		{ channel="hue:0210:1:bulb1:color" }
Dimmer  Light1_Dimmer		{ channel="hue:0210:1:bulb1:color" }
Color 	Light1_Color		{ channel="hue:0210:1:bulb1:color" }
Dimmer 	Light1_ColorTemp	{ channel="hue:0210:1:bulb1:color_temperature" }

// Bulb2
Switch	Light2_Toggle		{channel="hue:0220:1:bulb2:brightness"}				
Dimmer	Light2_Dimm		{channel="hue:0220:1:bulb2:brightness"}
Dimmer	Light2_ColorTemp	{channel="hue:0220:1:bulb2:color_temperature"}
```

Note: The bridge ID is in this example **1** but can be different in each system.

### demo.sitemap:

```
sitemap demo label="Main Menu"
{
	Frame {
		// Bulb1
		Switch item=		Light1_Toggle
		Slider item=		Light1_Dimmer
		Colorpicker item=	Light1_Color
		Slider item=		Light1_ColorTemp

		// Bulb2
		Switch item=		Light2_Toggle
		Slider item=		Light2_Dimmer
		Slider item=		Light2_ColorTemp
	}
}
```

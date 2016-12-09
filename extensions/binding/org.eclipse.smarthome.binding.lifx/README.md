---
layout: documentation
---

{% include base.html %}

# LIFX Binding

This binding integrates the [LIFX LED Lights](http://www.lifx.com/). All LIFX lights are directly connected to the WLAN and the binding communicates with them over a UDP protocol.

![LIFX E27](doc/lifx_e27.jpg)

## Supported Things

The following table lists the thing types of the supported LIFX devices:

| Device Type                  | Thing Type   |
|------------------------------|--------------|
| Original 1000                | colorlight   |
| Color 650                    | colorlight   |
| Color 1000                   | colorlight   |
| Color 1000 BR30              | colorlight   |
| LIFX A19                     | colorlight   |
| LIFX BR30                    | colorlight   |
| LIFX Z                       | colorlight   |
|                              |
| LIFX+ A19                    | colorirlight |
| LIFX+ BR30                   | colorirlight |
|                              |              |
| White 800 (Low Voltage)      | whitelight   |
| White 800 (High Voltage)     | whitelight   |
| White 900 BR30 (Low Voltage) | whitelight   |

The thing type determines the capability of a device and with that the possible ways of interacting with it. The following matrix lists the capabilities (channels) for each type:

| Thing Type   | On/Off | Brightness | Color | Color Temperature | Infrared |
|--------------|:------:|:----------:|:-----:|:-----------------:|:--------:|
| colorlight   | X      |            | X     | X                 |          |
| colorirlight | X      |            | X     | X                 | X        |
| whitelight   | X      | X          |       | X                 |          |

## Discovery

The binding is able to auto-discover all lights in a network over the LIFX UDP protocol. Therefore all lights must be turned on.

*Note:* To get the binding working, all lights must be added to the WLAN network first with the help of the [LIFX smart phone applications](http://www.lifx.com/pages/go). The binding is NOT able to add or detect lights outside the network.

## Thing Configuration

Each light needs the device ID as a configuration parameter. The device ID is printed as a serial number on the light and can also be found within the native LIFX Android or iOS application. But usually the discovery works quite reliably, so that a manual configuration is not needed.

However, in the thing file, a manual configuration looks e.g. like

```
Thing lifx:colorlight:living [ deviceId="D073D5A1A1A1", fadetime=200 ]
```

The *fadetime* is an optional thing configuration parameter which configures the time to fade to a new color value (in ms). When the *fadetime* is not configured, the binding uses 300ms as default.


## Channels

All devices support some of the following channels:

| Channel Type ID | Item Type | Description                                                                          | Thing Types                          |
|-----------------|-----------|--------------------------------------------------------------------------------------|--------------------------------------|
| brightness      | Dimmer    | This channel supports adjusting the brightness value.                                | whitelight                           |
| color           | Color     | This channel supports full color control with hue, saturation and brightness values. | colorlight, colorirlight             |
| infrared        | Dimmer    | This channel supports adjusting the infrared value. *Note:* IR capable lights only activate their infrared LEDs when the brightness drops below a certain level. | colorirlight |
| temperature     | Dimmer    | This channel supports adjusting the color temperature from cold (0%) to warm (100%).  | colorlight, colorirlight, whitelight |

The *color* and *brightness* channels have a "Power on brightness" configuration option that is used to determine the brightness when a light is switched on. When it is left empty, the brightness of a light remains unchanged when a light is switched on or off.

## Full Example

In this example **living** is a Color 1000 light that has a *colorlight* thing type which supports *color* and *temperature* channels.

The **porch** light is a LIFX+ BR30 that has a *colorirlight* thing type which supports *color*, *temperature* and *infrared* channels.

Finally, **kitchen** is a White 800 (Low Voltage) light that has a *whitelight* thing type which supports *brightness* and *temperature* channels.

### demo.things:

```
Thing lifx:colorlight:living [ deviceId="D073D5A1A1A1" ] {
	Channels:
		Type color : color [ powerOnBrightness= ]
}

Thing lifx:colorirlight:porch [ deviceId="D073D5B2B2B2", fadetime=0 ] {
	Channels:
		Type color : color [ powerOnBrightness=75 ]
}

Thing lifx:whitelight:kitchen [ deviceId="D073D5C3C3C3", fadetime=150 ]

```

### demo.items:

```
// Living
Switch Living_Toggle { channel="lifx:colorlight:living:color" }
Dimmer Living_Dimmer { channel="lifx:colorlight:living:color" }
Color Living_Color { channel="lifx:colorlight:living:color" }
Dimmer Living_Temperature { channel="lifx:colorlight:living:temperature" }

// Porch
Switch Porch_Toggle { channel="lifx:colorirlight:porch:color" }
Dimmer Porch_Dimmer { channel="lifx:colorirlight:porch:color" }
Color Porch_Color { channel="lifx:colorirlight:porch:color" }
Dimmer Porch_Temperature { channel="lifx:colorirlight:porch:temperature" }
Dimmer Porch_Infrared { channel="lifx:colorirlight:porch:infrared" }

// Kitchen
Switch Kitchen_Toggle { channel="lifx:whitelight:kichen:brightness" }
Dimmer Kitchen_Brightness { channel="lifx:whitelight:kitchen:brightness" }
Dimmer Kitchen_Temperature { channel="lifx:whitelight:kitchen:temperature" }

```

### demo.sitemap:

```
sitemap demo label="Main Menu"
{
	Frame label="Living" {
		Switch item=Living_Toggle
		Slider item=Living_Dimmer
		Colorpicker item=Living_Color
		Slider item=Living_Temperature
	}

	Frame label="Porch" {
		Switch item=Porch_Toggle
		Slider item=Porch_Dimmer
		Colorpicker item=Porch_Color
		Slider item=Porch_Temperature
		Slider item=Porch_Infrared
	}

	Frame label="Kitchen" {
		Switch item=Kitchen_Toggle
		Slider item=Kitchen_Brightness
		Slider item=Kitchen_Temperature
	}
}
```

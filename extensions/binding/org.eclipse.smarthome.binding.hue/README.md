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

## Discovery

The Hue bridge is discovered through UPnP in the local network. Once it is added as a Thing, its authentication button (in the middle) needs to be pressed in order to authorize the binding to access it. Once the binding is authorized, it automatically reads all devices that are set up on the Hue bridge and puts them in the Inbox.

## Binding Configuration

The binding uses a default secret to authenticate against the Hue bridge, unless a different secret is configured. The default value can be set in the file [hue.cfg](cfg/hue.cfg).

## Thing Configuration

The Hue bridge requires the ip address as a configuration value in order for the binding to know where to access it.
In the thing file, this looks e.g. like
```
Bridge hue:bridge:1 [ ipAddress="192.168.0.64" ]
```
The bulbs are identified by the number that the Hue bridge assigns to them (also shown in the Hue app as an identifier).
Thus, all if needs for manual configuration is this single value like
```
LCT001 bulb1 [ lightId="1" ]
```

## Channels

All devices support some of the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|----------------- |------------- |
| color | Color       | This channel supports full color control with hue, saturation and brightness values. |
| brightness | Dimmer       | This channel supports adjusting the brightness value. Note that this is not available, if the color channel is supported. |
| color_temperature | Dimmer       | This channel supports adjusting the color temperature from cold (0%) to warm (100%) |


## Full Example

demo.things:
```
Bridge hue:bridge:1 [ ipAddress="192.168.0.64" ] {
	LCT001 bulb1 [ lightId="1" ]
	ZLL_Light geBulb [ lightId="2" ]
}
```

demo.items:
```
Color Light { channel="hue:LCT001:1:bulb1:color" }
Dimmer Light_ColorTemp { channel="hue:LCT001:1:bulb1:color_temperature" }
```

demo.sitemap:
```
sitemap demo label="Main Menu"
{
	Frame {
		Colorpicker item=Light
		Slider item=Light_ColorTemp
	}
}
```

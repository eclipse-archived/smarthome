---
layout: documentation
---

{% include base.html %}

# Belkin Wemo Binding

This binding integrates the [Belkin WeMo Family](http://www.belkin.com/us/Products/c/home-automation/).
The integration happens either through the WeMo-Link bridge, which acts as an IP gateway to the ZigBee devices or through WiFi connection to standalone devices.

## Supported Things

The WeMo Binding supports the Socket, Insight, Lightswitch, Motion and Maker devices, as well as the WeMo-Link bridge with WeMo LED bulbs.

## Discovery

The WeMo devices are discovered through UPnP discovery service in the network. Devices will show up in the inbox and can be easily added as Things.

## Binding Configuration

The binding does not need any special configuration

## Thing Configuration

For manual Thing configuration, one needs to know the UUID of a certain WeMo device.
In the thing file, this looks e.g. like
```
[wemo:Socket-1-0-xxxxxx:Socket]
```

## Channels

Devices support some of the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|----------------- |------------- |
| state | Switch       | This channel controls the actual binary State of a Device or represents Motion Detection. |
| lastChangedAt | DateTime | Representing the Date and Time the device was last turned on or of. |
| lastOnFor | Number       | Time in seconds an Insight device was last turned on for. |
| onToday   | Number       | Time in seconds an Insight device has been switched on today.   |
| onTotal   | Number       | Time in seconds an Insight device has been switched on totally. |
| timespan  | Number       | Time in seconds over which onTotal applies. Typically 2 weeks except first used. |
| averagePower | Number    | Average power consumption in Watts. 
| currentPower | Number    | Current power consumption of an Insight device. 0 if switched off. |
| energyToday | Number     | Energy in Wh used today. |
| energyTotal | Number     | Energy in Wh used in total. |
| standbyLimit | Number    | Minimum energy draw in W to register device as switched on (default 8W, configurable via WeMo App). |
| brightness   | Number    | Brightness of a WeMo LED. |


## Full Example

demo.things:
```
wemo:socket:11111111111
```

demo.items:
```
Switch DemoSwitch    { channel="wemo:socket:1234567:state" }
Switch LightSwitch   { channel="wemo:lightswitch:1234567:state" }
Switch MotionSensor  { channel="wemo:Motion:1234567:state" }
Number InsightPower  { channel="wemo:insight:1234567:currentpower" }
Number InsightLastOn { channel="wemo:insight:1234567:lastOnFor" }
Number InsightToday  { channel="wemo:insight:1234567:onToday" }
Number InsightTotal  { channel="wemo:insight:1234567:onTotal" }
```

demo.sitemap:
```
sitemap demo label="Main Menu"
{
		Frame label="WeMo" {
			Switch item=DemoSwitch
			Switch item=LightSwitch
			Switch item=MotionSensor
			Number item=InsightPower
			Number item=InsightLastOn
			Number item=InsightToday
			Number item=InsightTotal
		}
}
```

---
layout: documentation
---

{% include base.html %}

# Belkin Wemo Binding

This binding integrates the [Belkin WeMo Family](http://www.belkin.com/us/Products/c/home-automation/).
The integration happens either through the WeMo-Link bridge (feature still to come), which acts as an IP gateway to the ZigBee devices or through WiFi connection to standalone devices.

## Supported Things

At current development stage, the WeMo Binding supports the Socket, Insight, Lightswitch and Motion devices.
Future Versions will also allow to integrate the new WeMo-Link bridge to interact with WeMo LED bulbs.

## Discovery

The WeMo devices are discovered through an individual discovery service in the network. Devices will show up in the inbox and can be easily added as Things.

## Binding Configuration

The binding does not need any special configuration

## Thing Configuration

For manual Thing configuration, one needs to know the UUID of a certain WeMo device.
In the thing file, this looks e.g. like
```
[wemo:Socket-1-0-xxxxxx:Socket]
```

## Channels

All devices support some of the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|----------------- |------------- |
| state | Switch       | This channel controls the actual binary State of a Socket Device or represents Motion Detection. |
| currentPower | Number       | This channel shows the current power consumption of an Insight device. |
| lastOnFor | Number       | This channel shows the duration an Insight Device has been switched on for. |
| onToday	| Number       | This channel shows how long an Insight device has been switched on today.   |
| onTotal   | Number       | This channel shows how long an Insight device has been switched on totally. |


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

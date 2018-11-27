# Philips Hue Binding

This binding integrates the [Philips Hue Lighting system](http://www.meethue.com).
The integration happens through the Hue bridge, which acts as an IP gateway to the ZigBee devices.

![Philips Hue](doc/hue.jpg)

## Supported Things

The Hue bridge is required as a "bridge" for accessing any other Hue devices.

Almost all available Hue devices are supported by this binding.
This includes not only the "friends of Hue", but also products like the LivingWhites adapter.
Additionally, it is possible to use OSRAM Lightify devices as well as other ZigBee LightLink compatible products.
Please note that the devices need to be registered with the Hue bridge before it is possible for this binding to use them.

The Hue binding supports all seven types of lighting devices defined for ZigBee LightLink ([see page 24, table 2](https://www.nxp.com/documents/user_manual/JN-UG-3091.pdf).
These are:

| Device type              | ZigBee Device ID | Thing type |
|--------------------------|------------------|------------|
| On/Off Light             | 0x0000           | 0000       |
| On/Off Plug-in Unit      | 0x0010           | 0010       |
| Dimmable Light           | 0x0100           | 0100       |
| Dimmable Plug-in Unit    | 0x0110           | 0110       |
| Colour Light             | 0x0200           | 0200       |
| Extended Colour Light    | 0x0210           | 0210       |
| Colour Temperature Light | 0x0220           | 0220       |

All different models of Hue, OSRAM, or other bulbs nicely fit into one of these seven types.
This type also determines the capability of a device and with that the possible ways of interacting with it.
The following matrix lists the capabilities (channels) for each type:

| Thing type  | On/Off | Brightness | Color | Color Temperature |
|-------------|:------:|:----------:|:-----:|:-----------------:|
|  0000       |    X   |            |       |                   |    
|  0010       |    X   |            |       |                   |    
|  0100       |    X   |     X      |       |                   |
|  0110       |    X   |     X      |       |                   |
|  0200       |    X   |            |   X   |                   |
|  0210       |    X   |            |   X   |          X        |
|  0220       |    X   |     X      |       |          X        |

Beside bulbs and luminaires the Hue binding supports some ZigBee sensors.
Currently only Hue specific sensors are tested successfully (e.g. Hue Motion Sensor and Hue Dimmer Switch).
The Hue Motion Sensor registers a `ZLLLightLevel` sensor, a `ZLLPresence` sensor and a `ZLLTemperature` sensor in one device.

| Device type           | ZigBee Device ID | Thing type |
|-----------------------|------------------|------------|
| Light Sensor          | 0x0106           | 0106       |
| Occupancy Sensor      | 0x0107           | 0107       |
| Temperature Sensor    | 0x0302           | 0302       |
| Non-Colour Controller | 0x0820           | 0820       |

The type of a specific device can be found in the configuration section for things in the PaperUI.
It is part of the unique thing id which could look like:

```
hue:0210:00178810d0dc:1
```

The thing type is the second string behind the first colon and in this example it is **0210**.

## Discovery

The Hue bridge is discovered through UPnP in the local network.
Once it is added as a Thing, its authentication button (in the middle) needs to be pressed in order to authorize the binding to access it.
Once the binding is authorized, it automatically reads all devices that are set up on the Hue bridge and puts them in the Inbox.

## Thing Configuration

The Hue bridge requires the IP address as a configuration value in order for the binding to know where to access it.
In the thing file, this looks e.g. like

```
Bridge hue:bridge:1 [ ipAddress="192.168.0.64" ]
```

A user to authenticate against the Hue bridge is automatically generated.
Please note that the generated user name cannot be written automatically to the `.thing` file, and has to be set manually.
The generated user name can be found in the log files after pressing the authentication button on the bridge.
The user name can be set using the `userName` configuration value, e.g.:

```
Bridge hue:bridge:1 [ ipAddress="192.168.0.64", userName="qwertzuiopasdfghjklyxcvbnm1234" ]
```

The devices are identified by the number that the Hue bridge assigns to them (also shown in the Hue App as an identifier).
Thus, all it needs for manual configuration is this single value like

```
0210 bulb1 [ lightId="1" ]
```

## Channels

The devices support some of the following channels:

| Channel Type ID   | Item Type          | Description                                                                                                                             | Thing types supporting this channel |
|-------------------|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| switch            | Switch             | This channel supports switching the device on and off.                                                                                  | 0000, 0010                          |
| color             | Color              | This channel supports full color control with hue, saturation and brightness values.                                                    | 0200, 0210                          |
| brightness        | Dimmer             | This channel supports adjusting the brightness value. Note that this is not available, if the color channel is supported.               | 0100, 0110, 0220                    |
| color_temperature | Dimmer             | This channel supports adjusting the color temperature from cold (0%) to warm (100%).                                                    | 0210, 0220                          |
| alert             | String             | This channel supports displaying alerts by flashing the bulb either once or multiple times. Valid values are: NONE, SELECT and LSELECT. | 0000, 0100, 0200, 0210, 0220        |
| effect            | Switch             | This channel supports color looping.                                                                                                    | 0200, 0210, 0220                    |
| dimmer_switch     | Number             | This channel shows which button was last pressed on the dimmer switch.                                                                  | 0820                                |
| illuminance       | Number:Illuminance | This channel shows the current illuminance measured by the sensor.                                                                      | 0106                                |
| light_level       | Number             | This channel shows the current light level measured by the sensor. **Advanced**                                                         | 0106                                |
| dark              | Switch             | This channel indicates whether the light level is below the darkness threshold or not.                                                  | 0106                                |
| daylight          | Switch             | This channel indicates whether the light level is below the daylight threshold or not.                                                  | 0106                                |
| presence          | Switch             | This channel indicates whether a motion is detected by the sensor or not.                                                               | 0107                                |
| temperature       | Number:Temperature | This channel shows the current temperature measured by the sensor.                                                                      | 0302                                |
| last_updated      | DateTime           | This channel the date and time when the sensor was last updated.                                                                        | 0820, 0106, 0107, 0302              |
| battery_level     | Number             | This channel shows the battery level.                                                                                                   | 0820, 0106, 0107, 0302              |
| battery_low       | Switch             | This channel indicates whether the battery is low or not.                                                                               | 0820, 0106, 0107, 0302              |

### Trigger Channels

The dimmer switch additionally supports a trigger channel.

| Channel ID          | Description                      | Thing types supporting this channel |
|---------------------|----------------------------------|-------------------------------------|
| dimmer_switch_event | Event for dimmer switch pressed. | 0820                                |

The event can trigger one of the following events:

| Button              | State           | Event |
|---------------------|-----------------|-------|
| Button 1 (ON)       | INITIAL_PRESSED | 1000  |
|                     | HOLD            | 1001  |
|                     | SHORT RELEASED  | 1002  |
|                     | LONG RELEASED   | 1003  |
| Button 2 (DIM UP)   | INITIAL_PRESSED | 2000  |
|                     | HOLD            | 2001  |
|                     | SHORT RELEASED  | 2002  |
|                     | LONG RELEASED   | 2003  |
| Button 3 (DIM DOWN) | INITIAL_PRESSED | 3000  |
|                     | HOLD            | 3001  |
|                     | SHORT RELEASED  | 3002  |
|                     | LONG RELEASED   | 3003  |
| Button 4 (OFF)      | INITIAL_PRESSED | 4000  |
|                     | HOLD            | 4001  |
|                     | SHORT RELEASED  | 4002  |
|                     | LONG RELEASED   | 4003  |

## Full Example

In this example **Bulb1** is a standard Philips HUE bulb (LCT001) which supports `color` and `color_temperature`.
Therefore it is a thing of type **0210**.
**Bulb2** is an OSRAM tunable white bulb (PAR16 50 TW) supporting `color_temperature` and so the type is **0220**.

### demo.things:

```
Bridge hue:bridge:1 [ ipAddress="192.168.0.64" ] {
	0210 bulb1 [ lightId="1" ]
	0220 bulb2 [ lightId="2" ]
	0106 light-level-sensor [ sensorId="3" ]
	0107 motion-sensor [ sensorId="4" ]
	0302 temperature-sensor [ sensorId="5" ]
	0820 dimmer-switch [ sensorId="6" ]
}
```

### demo.items:

```
// Bulb1
Switch	Light1_Toggle		{ channel="hue:0210:1:bulb1:color" }
Dimmer	Light1_Dimmer		{ channel="hue:0210:1:bulb1:color" }
Color	Light1_Color		{ channel="hue:0210:1:bulb1:color" }
Dimmer	Light1_ColorTemp	{ channel="hue:0210:1:bulb1:color_temperature" }
String	Light1_Alert		{ channel="hue:0210:1:bulb1:alert" }
Switch	Light1_Effect		{ channel="hue:0210:1:bulb1:effect" }

// Bulb2
Switch	Light2_Toggle		{ channel="hue:0220:1:bulb2:brightness" }
Dimmer	Light2_Dimmer		{ channel="hue:0220:1:bulb2:brightness" }
Dimmer	Light2_ColorTemp	{ channel="hue:0220:1:bulb2:color_temperature" }

// Light Level Sensor
Number:Illuminance LightLevelSensorIlluminance { "channel="hue:0106:light-level-sensor:illuminance" }

// Motion Sensor
Switch   MotionSensorPresence     { channel="hue:0107:motion-sensor:presence" }
DateTime MotionSensorLastUpdate   { channel="hue:0107:motion-sensor:last_update" }
Number   MotionSensorBatteryLevel { channel="hue:0107:motion-sensor:battery_level" }
Switch   MotionSensorLowBattery   { channel="hue:0107:motion-sensor:battery_low" }

// Temperature Sensor
Number:Temperature TemperatureSensorTemperature { "channel="hue:0302:temperature-sensor:temperature" }
```

Note: The bridge ID is in this example **1** but can be different in each system.

### demo.sitemap:

```
sitemap demo label="Main Menu"
{
	Frame {
		// Bulb1
		Switch		item=		Light1_Toggle
		Slider		item=		Light1_Dimmer
		Colorpicker	item=		Light1_Color
		Slider		item=		Light1_ColorTemp
		Switch		item=		Light1_Alert		mappings=[NONE="None", SELECT="Alert", LSELECT="Long Alert"]
		Switch		item=		Light1_Effect

		// Bulb2
		Switch		item=		Light2_Toggle
		Slider		item=		Light2_Dimmer
		Slider		item=		Light2_ColorTemp

		// Motion Sensor
		Switch item=MotionSensorPresence
		Text item=MotionSensorLastUpdate
		Text item=MotionSensorBatteryLevel
		Switch item=MotionSensorLowBattery
	}
}
```

### Events

 ```php
rule "example trigger rule"
when
    Channel "hue:0820:dimmer-switch:dimmer_switch_event" triggered <EVENT>
then
    ...
end
```

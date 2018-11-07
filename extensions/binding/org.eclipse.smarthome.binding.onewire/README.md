# OneWire Binding

The OneWire binding integrates OneWire (also spelled 1-Wire) devices. 
OneWire is a serial bus developed by Dallas Semiconductor.
It provides cheap sensors for temperature, humidity, digital I/O and more.
  
## Supported Things

### Bridges

Currently only one bridge is supported. 

The OneWire File System (OWFS, http://owfs.org) provides an abstraction layer between the OneWire bus and this binding. 
The `owserver` is the bridge that connects to an existing OWFS installation. 

### Things

There are three types of things: the generic ones (`counter2`, `digitalio`, `digitalio2`, `digitalio8`, `ibutton`, `temperature`), multisensors built around the DS2438 chip (`ms-th`, `ms-tv`) and more advanced sensors from Elaborated Networks (www.wiregate.de) (`ams`, `bms`). 

## Discovery

Discovery is supported for things. You have to add the bridges manually.  

## Thing Configuration

It is strongly recommended to use discovery and Paper UI for thing configuration.
Please note that:

* All things need a bridge.
* The sensor id parameter supports only the dotted format, including the family id (e.g. `28.7AA256050000`).
* Refresh time is the minimum time in seconds between two checks of that thing.
It defaults to 300s for analog channels and 10s for digital channels.
* Some thing channels need additional configuration, please see below in the channels section.

### OWFS Bridge (`owserver`)

There are no configuration options for the owserver besides the network address.
It consists of two parts: `address` and `port`.

The `address` parameter is used to denote the location of the owserver instance. 
It supports both, a hostname or an IP address. 

The `port` parameter is used to adjust non-standard OWFS installations.
It defaults to `4304`, which is the default of each OWFS installation.  
  
### Counter (`counter2`)

The counter thing supports the DS2423 chip, a dual counter.
Two `counterX` channels are supported. 

It has two parameters: sensor id `id` and refresh time `refresh`.
 

### Digital I/O (`digitalio`, `digitalio2`, `digitalio8`) 

The digital I/O things support the DS2405, DS2406, DS2408 and DS2413 chips.
Depending on the chip, one (DS2405), two (DS2406/DS2413) or eight (DS2408) `digitalX`  channels are supported.

It has two parameters: sensor id `id` and refresh time `refresh`.

### iButton (`ibutton`)

The iButton thing supports only the DS2401 chips.
It is used for presence detection and therefore only supports the `present` channel.
It's value is `ON` if the device is detected on the bus and `OFF` otherwise.

It has two parameters: sensor id `id` and refresh time `refresh`.

### Multisensor with Humidity (`ms-th`)

The multisensor with humidity is build  around the DS2438 chipset. 
It provides a `temperature`, a `humidity` and a `supplyvoltage` channel.
The voltage input of the DS2438 is connected to a humidity sensor, several common types are supported (see below).

The generic sensor with humidity and temperature using DS1923 chipset. 
It provides a `temperature` and `humidity` channels.

It has two parameters: sensor id `id` and refresh time `refresh`.

### Multisensor with Voltage (`ms-tv`)

The multisensor with voltage is build  around the DS2438 chipset. 
It provides a `temperature`, a `voltage` and a `supplyvoltage` channel.

It has two parameters: sensor id `id` and refresh time `refresh`.

### Temperature sensor (`temperature`)

The temperature thing supports DS18S20, DS18B20 and DS1822 sensors.
It provides only the `temperature` channel.

It has two parameters: sensor id `id` and refresh time `refresh`. 

### Elaborated Networks Multisensors (`ams`, `bms`)

These things are complex devices from Elaborated networks. 
They consist of a DS2438 and a DS18B20 with additional circuitry on one PCB.
The AMS additionally has a second DS242438 and a DS2413 for digital I/O on-board.
Analog light sensors can optionally be attached to both sensors.

These sensors provide `temperature`, `humidity` and `supplyvoltage` channels.
If the light sensor is attached and configured, a `light` channel is provided, otherwise a `current` channel.
The AMS has an additional `voltage`and two `digitalX` channels.

It has two (`bms`) or four (`ams`) sensor ids (`id0` to `id3`).
The first id is always the main DS2438, the second id the DS18B20 temperature sensor.
In the case of the AMS, the third sensor id has to be the second DS2438 and the fourth the DS2413.

Additionally the refresh time `refresh` can be configured.
The AMS supports a `digitalrefresh` parameter for the refresh time of the digital channels.

Since both multisensors have two temperature sensors on-board, the `temperaturesensor` parameter allows to select `DS18B20` or `DS2438` to be used for temperature measurement.
This parameter has a default of `DS18B20` as this is considered more accurate.

The last parameter is the `lightsensor` option to configure if an ambient light sensor is attached.
It defaults to `false`.
In that mode, a `current`  channel is provided.
If set to `true`, a `light` channel is added to the thing.
The correct formula for the ambient light is automatically determined from the sensor version.

## Channels

| Type-ID         | Thing                       | Item    | readonly   | Description                                        |
|-----------------|-----------------------------|---------|------------|----------------------------------------------------|
| current         | multisensors                | Number  | yes        | current (if light option not installed)            |
| counter         | counter2                    | Number  | yes        | countervalue                                       |
| digital         | digitalX, AMS               | Switch  | no         | digital, can be configured as input or output      |
| humidity        | multisensors (except ms-tv) | Number  | yes        | relative humidity                                  |
| light           | ams, bms                    | Number  | yes        | lightness (if installed)                           |
| present         | all                         | Switch  | yes        | sensor found on bus                                |
| supplyvoltage   | multisensors                | Number  | yes        | sensor supplyvoltage                               |
| temperature     | not digitalX, ibutton       | Number  | yes        | environmental temperature                          |
| voltage         | ms-tv, ams                  | Number  | yes        | voltage input                                      |

### Digital I/O (`digitalX`)

The `digitalX` channels each have two parameters: `mode` and `logic`.

The `mode` parameter is used to configure this channels as `input` or `output`.

The `logic` parameter can be used to invert the channel.
In `normal` mode the channel is considered `ON` for logic high, and `OFF` for logic low.
In `inverted` mode `ON` is logic low and `OFF` is logic high.

### Humidity (`humidity`)

Depending on the sensor, the `humidity` channel may have the `humiditytype` parameter.
This is only needed for the `ms-th` sensors.
`ams` and `bms` sensors select the correct sensor type automatically.

Possible options are `/humidity` for HIH-3610 sensors, `/HIH4000/humidity` for HIH-4000 sensors, `/HTM1735/humidity` for HTM-1735 sensors and `/DATANAB/humidity` for sensors from Datanab.

### Temperature (`temperature`)

The `temperature` channel has three types: `temperature`, `temperature-por`and `temperature-por-res`.
If the channel-type is `temperature`, there is nothing else to configure.

Some sensors (e.g. DS18x20) report 85 °C as Power-On-Reset value.
In some installations this leads to errorneous temperature readings.
If the `ignorepor` parameter is set to `true` 85 °C values will be filtered.
The default is `false` as correct reading of 85 °C will otherwise be filtered, too.

A channel of type `temperature-por-res` has one parameter: `resolution`.
OneWire temperature sensors are capable of different resolutions: `9`, `10`, `11` and `12` bits.
This corresponds to 0.5 °C, 0.25 °C, 0.125 °C, 0.0625 °C respectively.
The conversion time is inverse to that and ranges from 95 ms to 750 ms.
For best performance it is recommended to set the resolution only as high as needed. 
 
The correct channel-type is selected automatically by the thing handler depending on the sensor type.
 
## Full Example

This is the configuration for a OneWire network consisting of an owserver as bridge (`onewire:owserver:mybridge`) and a temperature sensor as thing (`onewire:temperature:mybridge:mysensor`). 

### demo.things:

```
Bridge onewire:owserver:mybridge [ network-address="192.168.0.51" ] {
    temperature mysensor   [id="28.505AF0020000" ] 
}
```

### demo.items:

```
Number:Temperature MySensor "MySensor [%.1f %unit%]" { channel="onewire:temperature:mybridge:mysensor:temperature" }
```

### demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame {
        Text item=MySensor
    }
}
```

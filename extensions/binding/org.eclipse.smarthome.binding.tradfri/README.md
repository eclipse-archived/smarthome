# Trådfri Binding

This binding integrates the IKEA Trådfri gateway and devices connected to it (such as dimmable LED bulbs).

## Supported Things

Besides the gateway (thing type "gateway"), the binding currently supports dimmable warm white bulbs as well as white spectrum bulbs.

The thing type ids are defined according to the lighting devices defined for ZigBee LightLink ([see page 24, table 2](https://www.nxp.com/documents/user_manual/JN-UG-3091.pdf). These are:

| Device type              | ZigBee Device ID | Thing type |
|--------------------------|------------------|------------|
| Dimmable Light           | 0x0100           | 0100       |
| Colour Temperature Light | 0x0220           | 0220       |


The following matrix lists the capabilities (channels) for each of the supported lighting device types:

| Thing type  | On/Off | Brightness | Color | Color Temperature |
|-------------|:------:|:----------:|:-----:|:-----------------:|   
|  0100       |    X   |     X      |       |                   |
|  0220       |    X   |     X      |       |          X        |

## Thing Configuration

The gateway requires a `host` parameter for the hostname or IP address and a `code`, which is the security code that is printed on the bottom of the gateway. Optionally, a `port` can be configured, but any standard gateway uses the default port 5684.

The devices require only a single (integer) parameter, which is their instance id. Unfortunately, this is not displayed anywhere in the IKEA app, but it seems that they are sequentially numbered starting with 65537 for the first device. If in doubt, use the auto-discovered things to find out the correct instance ids.

## Channels

All devices support the `brightness` channel, while the white spectrum bulbs additionally also support the `color_temperature` channel (refer to the matrix above).

| Channel Type ID   | Item Type | Description                                 |
|-------------------|-----------|---------------------------------------------|
| brightness        | Dimmer    | The brightness of the bulb in percent       |
| color_temperature | Dimmer    | color temperature from 0%=cold to 100%=warm |

## Full Example

demo.things:

```
Bridge tradfri:gateway:mygateway [ host="192.168.0.177", code="EHPW5rIJKyXFgjH3" ] {
    0100 myDimmableBulb [ id=65537 ]    
    0220 myColorTempBulb [ id=65538 ]    
}
```

demo.items:

```
Dimmer Light { channel="tradfri:0100:mygateway:myDimmableBulb:brightness" } 
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame {
        Slider item=Light label="Brightness [%.1f %%]"
    }
}
```

Documentation of the Wemo binding bundle

## Introduction

The Eclipse SmartHome (ESH) Wemo binding allows to send commands Belkin Wemo Switches.

For installation of the binding, please see Wiki page [[Bindings]].

####This Binding will be available with openHAB version 2.x !!!

Wemo Binding needs no configuration in openhab.cfg


## Item Binding Configuration

In order to bind an item to the device, you need to provide configuration settings. The easiest way to do so is to add some binding information in your item file (in the folder configurations/items`). The syntax of the binding configuration strings accepted is the following:

    { channel="wemo:<Model>:<SerialNumber>:<channelType>"

<Model> describes the type of your WeMo device and can be one of the following : socket | insight | lightswitch
<SerialNumber> is the unique serial number of your device, shown at the bottom of the device.
<channelType> can be one of the following:

<state>			channel for reading binary state of your device and switching ON or OFF.

Examples, how to configure your items in your items file:

   Switch SocketSwitch 		{ channel="wemo:socket:123456:state" }
   Switch LightSwitch 		{ channel="wemo:lightswitch:123456:state" }
   Switch InsightSwitch 	{ channel="wemo:insight:123456:state" }
   
 


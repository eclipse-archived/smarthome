# Overview

Eclipse SmartHome is a framework for building smart home solutions. As such, it consists of a rich set of OSGi bundles that serve different purposes. Not all solutions that build on top of Eclipse SmartHome will require all of those bundles - instead they can choose what parts are interesting for them.

There are the following categories of bundles:

 - `config`: everything that is concerned with general configuration of the system like config files, xml parsing, discovery, etc.	
 - `core`: the main bundles for the logical operation of the system - based on the abstract item and event concepts.
 - `io`: all kinds of optional functionality that have to do with i/o like console commands, audio support or http/rest communication
 - `model`: support for domain specific languages (DSLs) 
 - `designer`: Eclipse RCP support for DSLs and other configuration files
 - `ui`: user interface related bundles that provide services that can be used by different UIs, such as charting or icons
  
# General Concepts
 
## Items and Events

Eclipse SmartHome has a strict separation between the physical world (the "things", see below) and the application, which is built around the notion of "items" (also called the virtual layer).

Items represent functionality that is used by the application (mainly user interfaces or automation logic). Items have a state and are used through events.
  
The event bus is THE base service of Eclipse SmartHome. All bundles that do not require stateful behaviour should use it to inform other bundles about events and to be updated by other bundles on external events.

There are mainly two types of events:

 - Commands, which trigger an action or a state change of some item.
 - State updates, which inform about a state change of some item (often as a response to a command)

The following item types are currently available (alphabetical order):

<table>
  <tr><td><b>Itemname</b></td><td><b>Description</b></td><td><b>Command Types</b></td></tr>
  <tr><td>Color</td><td>Color information (RGB)</td><td>OnOff, IncreaseDecrease, Percent, HSB</td></tr>
  <tr><td>Contact</td><td>Item storing status of e.g. door/window contacts</td><td>OpenClose</td></tr>
  <tr><td>DateTime</td><td>Stores date and time</td><td></td></tr>
  <tr><td>Dimmer</td><td>Item carrying a percentage value for dimmers</td><td>OnOff, IncreaseDecrease, Percent</td></tr>
  <tr><td>Group</td><td>Item to nest other items / collect them in groups</td><td>-</td></tr>
  <tr><td>Number</td><td>Stores values in number format</td><td>Decimal</td></tr>
  <tr><td>Player</td><td>Allows to control players (e.g. audio players)</td><td>PlayPause, NextPrevious, RewindFastforward</td></tr>
  <tr><td>Rollershutter</td><td>Typically used for blinds</td><td>UpDown, StopMove, Percent</td></tr>
  <tr><td>String</td><td>Stores texts</td><td>String</td></tr>
  <tr><td>Switch</td><td>Typically used for lights (on/off)</td><td>OnOff</td></tr>
</table>

Group Items can derive their own state depending on their member items.

  - AVG displays the average of the item states in the group.
  - OR displays an OR of the group, typically used to display whether any item in a group has been set.
  - other aggregations:  AND, SUM, MIN, MAX, NAND, NOR
 
It is important to note that Eclipse SmartHome is not meant to reside on (or near) actual hardware devices which would then have to remotely communicate with many other distributed instances. Instead, solutions based on Eclipse SmartHome serve as an integration hub between such devices and as a mediator between different protocols that are spoken between these devices. In a typical installation there will therefore be usually just one instance of Eclipse SmartHome running on some central server. Nonetheless, the events can also be exported through appropriate protocols such as MQTT, so that it is possible to connect several distributed Eclipse SmartHome instances.


## Things

Things are the entities that can physically be added to a system and which can potentially provide many functionalities in one. It is important to note that things do not have to be devices, but they can also represent a web service or any other manageable source of information and functionality.
From a user perspective, they are relevant for the setup and configuration process, but not for the operation.

Things can have configuration properties, which can be optional or mandatory. Such properties can be basic information like an IP address, an access token for a web service or a device specific configuration that alters its behavior.

Things provide "channels", which represent the different functions they provide. Channels are linked to items, where such links are the glue between the virtual and the physical layer. Once such a link is established, a thing reacts on events sent for an item that is linked to one of its channels. Likewise, it actively sends out events for items linked to its channels.

A special type of thing is a "bridge". Bridges are things that need to be added to the system in order to gain access to other things. A typical example of a bridge is an IP gateway for some non-IP based home automation system. 

As many things can be automatically discovered, there are special mechanisms available that deal with the handling of [automatically discovered things](discovery.md).

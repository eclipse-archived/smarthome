# EnOcean Binding

This binding uses the EnOcean base driver located in the protocol part.


## Supported Things

It supports 2 examples for now: Eltako smoke detector and a G-media switch plug to show an implemention of a command and an event

## Discovery

All is handled in the class EnOceanDiscoveryService. In the declarative services part, service is bound to each EnOceanDevice created by the base driver:

   <reference name="enoceanDevice" cardinality="0..n" policy="dynamic"
   interface="org.osgi.service.enocean.EnOceanDevice"
   bind="setEnoceanDevice" unbind="unsetEnoceanDevice"/>

So each time an EnOcean device is created an EnOcean thing will be created as well in the setEnoceanDevice method.


## Thing Configuration

No configuration is needed for these EnOcean devices as nothing can be set and thus no configuration to keep.

## Channels

The Eltako smoke detector is available as this channel:
Channel Type ID: alarm
Item Type: Switch
Description: alarm turned on or off

G-media switch plug:
Channel Type ID: onOff
Item Type: Switch
Description: the plug turned on or off


## EnOcean Thing handler

Command sending and events are handled in this class.

Commands are implemented in the classic Eclipse SmartHome way in the handleCommand method switching on the channel case:

public void handleCommand(ChannelUID channelUID, Command command) {
	if (command instanceof RefreshType) {
	    //
	    boolean success = true;
	    if (success) {
	       switch (channelUID.getId()) {
		    case CHANNEL_ELTAKO_SMOKE_DETECTOR:
		        // should not happen
		        break;
		    case CHANNEL_ON_OFF:
		        EnOceanRPC rpc = null;

	...

the Enocean device sends events using the event admin so the class EnOceanHandler must implements the interface EventHandler:

public class EnOceanHandler extends BaseThingHandler implements EventHandler {

...
    @Override
    public void handleEvent(Event event) {

    String chipId = (String) event.getProperty(EnOceanDevice.CHIP_ID);

    if (device.getChipId() == Integer.valueOf(chipId)) {
	if (thing.getThingTypeUID().equals(THING_TYPE_ELTAKO_SMOKE_DETECTOR)) {
	    EnOceanMessage data = (EnOceanMessage) event.getProperty(EnOceanEvent.PROPERTY_MESSAGE);
	    byte[] payload = data.getBytes();
...


Each thing handler has its EnoceanDevice as a private field

## EnOcean device

An EnOcean device can be identified by its unique chipId. This chip Id will be used to create the thingUID.
An EnOcean device type can be identified by 3 parameters: 

RORG: defines the telegram type
FUNC: defines the function of the device like temperature sensor
TYPE: defines a sub element of the function like temperature sensor 0-40°







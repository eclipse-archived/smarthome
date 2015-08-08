---
layout: documentation
---

{% include base.html %}

# Thing Type Definitions

In order to work with things, some meta information about them is needed. This is provided through 'ThingType' definitions, which describe details about their functionality and configuration options.

Technically, the thing types are provided by [ThingTypeProvider](https://github.com/eclipse/smarthome/blob/master/bundles/core/org.eclipse.smarthome.core.thing/src/main/java/org/eclipse/smarthome/core/thing/binding/ThingTypeProvider.java)s. Eclipse SmartHome comes with an implementation of such a provider that reads XML files from the folder `ESH-INF/thing` of bundles. Although we refer to this XML syntax in the following, you also have the option to provide directly object model instances through your own provider implementation.  

## Things

Things represent devices or services that can be individually added to, configured or removed from the system. They either contain a set of channels or a set of channel groups. A bridge is a specific type of thing as it can additionally provide access to other Things as well. Which Things can be associated through which bridge type is defined within the description of a thing:

```xml
    <thing-type id="thingTypeID">
        <supported-bridge-type-refs>
            <bridge-type-ref id="bridgeTypeID" />
        </supported-bridge-type-refs>
        <label>Sample Thing</label>
        <description>Some sample description</description>
		...
    </thing-type>
```


## Channels

A channel describes a specific functionality of a thing and can be linked to an item. So the basic information is, which command types the channel can handle and which state it sends to the linked item. This can be specified by the accepted item type. Inside the thing type description XML file a list of channels can be referenced. The channel type definition is specified on the same level as the thing type definition. That way channels can be reused in different things.

The following XML snippet shows a thing type definition with 2 channels and one referenced channel types:

```xml 
<thing-type id="thingTypeID">
    <label>Sample Thing</label>
    <description>Some sample description</description>
    <channels>
      <channel id="switch" typeId="switch" />
      <channel id="targetTemperature" typeId="temperatureActuator" />
    </channels>
</thing-type>
<channel-type id="temperatureActuator" advanced="true">
    <item-type>Number</item-type>
    <label>Temperature</label>
    <category>Temperature</category>
    <tags>
      <tag>weather</tag>
    </tags>
    <state min="12" max="30" step="0.5" pattern="%d °C" readOnly="false">
    </state>
</channel-type>
```

In order to reuse identical channels in different bindings a channeltype can be systemwide. A channel-type can be declared as systemwide by setting its `system` property to true and can then be referenced using a `system.` prefix in a `channel` `typeId` attribute in any binding.  

The following XML snippet shows a system channel-type definition and thing-type definition that references it:

```xml 
<thing-type id="thingTypeID">
    <label>Sample Thing</label>
    <description>Some sample description</description>
    <channels>
      <channel id="s" typeId="system.system-channel" />
    </channels>
</thing-type>
<channel-type id="system-channel" system="true">
    <item-type>Number</item-type>
    <label>System Channel</label>
    <category>QualityOfService</category>
</channel-type>
```

There exist systemwide channels that are available by default:

| Channel Type ID | Reference typeId       | Item Type    | Category         | Description  |
|-----------------|------------------------|--------------|----------------- |------------- |
| signal-strength | system.signal-strength | Number       | QualityOfService | Represents signal strength of a device as a Number with values 0, 1, 2, 3 or 4; 0 being worst strength and 4 being best strength.  |
| low-battery     | system.low-battery     | Switch       | Battery          | Represents a low battery warning with possible values on/off |
| battery-level   | system.battery-level   | Number       | Battery          | Represents the battery level as a percentage (0-100%). Bindings for things supporting battery level in a different format (eg 4 levels) should convert to a percentage to provide a consistent battery level reading. |


The `advanced` property indicates whether this channel is a basic or a more specific functionality of the thing. If `advanced` is set to `true` a user interface may hide this channel by default. The default value is `false` and thus will be taken if the `advanced` attribute is not specified. Especially for complex devices with a lot of channels, only a small set of channels - the most important ones - should be shown to the user to reduce complexity. Whether a channel should be declared as `advanced` depends on the device and can be decided by the binding developer. If a functionality is rarely used it should be better marked as `advanced`.

In the following sections the declaration and semantics of tags, state descriptions and channel categories will be explained in more detail. For a complete sample of the thing types XML file and a full list of possible configuration options please see the [XML Configuration Guide](xml-reference.html).

### Default Tags

The XML definition of a ThingType allows to assign default tags to channels. All items bound to this channel will automatically be tagged with these default tags. The following snippet shows an weather tag definition:

```xml 
<tags>
    <tag>weather</tag>
</tags>
```

### State Description

The state description allows to specify restrictions and additional information for the state of an item, that is linked to the channel. Some configuration options are only valid for specific item types. The following XML snippet shows the definition for a temperature actuator channel:

```xml 
<state min="12" max="30" step="0.5" pattern="%d °C" readOnly="false"></state>
```

The attributes `min` and `max` can only be declared for channel with the item type `Number`. It defines the range of the numeric value. The Java data type is a BigDecimal. For example user interfaces can create sliders with an appropriate scale based on this information. The `step` attribute can be declared for `Number` and `Dimmer` items and defines what is the minimal step size that can be used. The `readonly` attribute can be used for all item types and defines if the state of an item can be changed. For all sensors the `readonly` attribute should be set to `true`. The `pattern` attribute can be used for `Number` and  `String` items. It gives user interface a hint how to render the item. The format of the pattern must be compliant to the [Java Number Format](http://docs.oracle.com/javase/tutorial/java/data/numberformat.html). The pattern can be localized (see also [Internationalization](../../features/internationalization.html))

Some channels might have only a limited and countable set of states. These states can be specified as options. A `String` item must be used as item type. The following XML snippet defines a list of predefined state options:

```xml 
<state readOnly="true">
    <options>
        <option value="HIGH">High Pressure</option>
        <option value="MEDIUM">Medium Pressure</option>
        <option value="LOW">Low Pressure</option>
    </options>
</state>
```

The user interface can use these values to render labels for values or to provide a selection of states, when the channel is writable. The option labels can also be localized.

### Channel Categories

The channel type definition allows to specify a category. Together with the definition of the `readOnly` attribute in the state description, user interfaces get an idea how to render an item for this channel. A binding should classify each channel into one of the existing categories. This is a list of all predefined categories with their usual accessible mode and the according item type:

| Category      | Accessible Mode | Item Type              |
|---------------|-----------------|------------------------|
| Alarm         | R, RW           | Switch                 |
| Battery       | R               | Switch, Number         |
| Blinds        | RW              | Rollershutter          |
| ColorLight    | RW              | Color                  |
| Contact       | R               | Contact                |
| DimmableLight | RW              | Dimmer                 |
| CarbonDioxide | R               | Switch, Number         |
| Door          | R, RW           | Switch                 |
| Energy        | R               | Number                 |
| Fan           | RW              | Switch, Number, String |
| Fire          | R               | Switch                 |
| Flow          | R               | Number                 |
| GarageDoor    | RW              | String                 |
| Gas           | R               | Switch, Number         |
| Humidity      | R               | Number                 |
| Light         | R, RW           | Switch, Number         |
| Motion        | R               | Switch                 |
| MoveControl   | RW              | String                 |
| Player        | RW              | Player                 |
| PowerOutlet   | RW              | Switch                 |
| Pressure      | R               | Number                 |
| QualityOfService      | R       | Number                 |
| Rain          | R               | Switch, Number         |
| Recorder      | RW              | String                 |
| Smoke         | R               | Switch                 |
| SoundVolume   | R, RW           | Number                 |
| Switch        | RW              | Switch                 |
| Temperature   | R, RW           | Number                 |
| Water         | R               | Switch, Number         |
| Wind          | R               | Number                 |
| Window        | R, RW           | String, Switch         |
| Zoom          | RW              | String                 |

R=Read, RW=Read/Write

The accessible mode indicates whether a category could have `read only` flag configured to true or not. For example the `Motion` category can be used for sensors only, so `read only` can not be false. Temperature can be either measured or adjusted, so the accessible mode is R and RW, which means the read only flag can be `true` or `false`. In addition categories are related to specific item types. For example the 'Energy' category can only be used for `Number` items. But `Rain` could be either expressed as Switch item, where it only indicates if it rains or not, or as `Number`, which gives information about the rain intensity.

The list of categories may not be complete and not every device will fit into one of these categories. It is possible to define own categories. If the category is widely used, the list of predefined categories can be extended. Moreover not all user interfaces will support all categories. It is more important to specify the `read only` information and state information, so that default controls can be rendered, even if the category is not supported.

### Channel Groups

Some devices might have a lot of channels. There are also complex devices like a multi-channel actuator, which is installed inside the switchboard, but controls switches in other rooms. Therefore channel groups can be used to group a set of channels together into one logical block. A thing can only have direct channels or channel groups, but not both.

Inside the thing types XML file channel groups can be defined like this:

```xml 
<thing-type id="multiChannelSwitchActor">
    <!-- ... -->
    <channel-groups>
        <channel-group id="switchActor1" typeId="switchActor" />
        <channel-group id="switchActor2" typeId="switchActor" />
    </channel-groups>
    <!-- ... -->
</thing-type>    
```

The channel group type is defined on the same level as the thing types and channel types. The group type must have a label and an optional description. More over the list of contained channels must be specified:

```xml
<channel-group-type id="switchActor">
    <label>Switch Actor</label>
    <description>This is a single switch actor with a switch channel</description>
    <channels>
        <channel id="switch" typeId="switch" />
    </channels>
</channel-group-type> 
```

When a thing will be created for a thing type with channel groups, the channel UID will contain the group ID in the last segment divided by a hash (#). If an Item should be linked to channel within a group, the channel UID would be `binding:multiChannelSwitchActor:myDevice:switchActor1#switch` for the XML example before.

## Properties
Solutions based on Eclipse SmartHome might require meta data from a device. These meta data could include 

- general device information, e.g. the device vendor, the device series or the model ID, ...
- device characteristics, e.g. if it is battery based, which home automation protocol is used, what is the current firmware version or the serial number, ...
- physical descriptions, e.g. what is the size, the weight or the color of the device, ...
- any other meta data that should be made available for the solution by the binding

Depending on the solution the provided meta data can be used for different purposes. Among others the one solution could use the data during a device pairing process whereas another solution might use the data to group the devices/things by the vendors or by the home automation protocols on an user interface. To define such thing meta data the thing type definition provides the possibility to specify so-called `properties`:

```xml 
    <thing-type id="thingTypeId">
        ...
        <properties>
             <property name="vendor">MyThingVendor</property>
             <property name="modelId">thingTypeId</property>
             <property name="protocol">ZigBee</property>
             ...
        </properties>
		...
    </thing-type>
```

In general each `property` must have a name attribute which should be written in camel case syntax. The actual property value is defined as plain text and is placed as child node of the property element. It is recommended that at least the vendor and the model id properties are specified here since they should be definable for the most of the devices. In contrast to the properties defined in the 'ThingType' definitions the thing handler [documentation](thing-handler.html) explains how properties can be set during runtime.

## Formatting Labels and Descriptions
The label and descriptions for things, channels and config descriptions should follow the following format. The label should be short so that for most UIs it does not spread across multiple lines. The description can contain longer text to describe the thing in more detail. Limited use of HTML tags is permitted to enhance the description - if a long description is provided, the first line should be kept short, and a line break (```<br>```) placed at the end of the line to allow UIs to display a short description in limited space.

Configuration options should be kept short so that they are displayable in a single line in most UIs. If you want to provide a longer description of the options provided by a particular parameter, then this should be placed into the ```<description>``` of the parameter to keep the option label short. The description can include limited HTML to enhance the display of this information.

The following HTML tags are allowed -: ```<b>, <br>, <em>, <h1>, <h2>, <h3>, <h4>, <h5>, <h6>, <i>, <p>, <small>, <strong>, <sub>, <sup>, <ul>, <ol>, <li>```. These must be inside the XML escape sequence - eg - ```<description><![CDATA[ HTML marked up text here ]]></description>```.
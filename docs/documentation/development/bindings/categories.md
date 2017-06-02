---
layout: documentation
---

{% include base.html %}

# Categories

Categories in Eclipse SmartHome are used to provide meta information about things channels, etc. UIs can use this information to render specific icons or provide a search functionality to for example filter all things for a certain category.

## Thing Categories

The thing type definition allows to specify a category. User interfaces can parse this category to get an idea how to render this thing. A binding can classify each thing into one of the existing categories. The list of all predefined categories can be found in our categories overview:

| Category        | Description                                          |
|-----------------|------------------------------------------------------|
| Alarm           | Any device related to alarm systems, such as a Siren |
| BinarySensor    | General purpose sensor that returns a boolean value |
| Camera          | All kinds of cameras |
| Clock           | Devices that provide the current time |
| Cooling         | Devices that cool air, such as air conditioners, fans, etc. |
| Door            | Anything closely related to a door such as an Open/Close contact or Opener |
| Food            | Fridges, Mixers, etc. |
| Furniture       | Smart furniture |
| GeneralSensor   | General purpose sensor that does not fit in any category |
| Heating         | Devices that deal with heating such as radiators, boilers, temperature actuators, heaters, returnpipes, etc. |
| Light           | Devices that illuminate something, such as bulbs, etc. |
| Lock            | Devices whose primary pupose is locking something |
| Microphone      | Devices to record sound |
| Motion          | Motion sensors/detectors |
| MultiSwitch     | Device which controls MULTIPLE states (else see Switch) of something |
| Phone           | Anything related to telephony, such as mobile phones, landline phones, etc. |
| Power           | Anything related to power/electricity, for ex. power outlets, power plugs, etc. |
| Robots          | Vacuum cleaners such as cleaning robots, lawn mowers, etc. |
| RollerShutter   | Roller shutters |
| SolarPlant      | Devices related to solar |
| Smoke           | Smoke detectors |
| Speakers        | Devices that are able to play sounds |
| (Wall)Switch    | Any device that controls a BINARY status (else see MultiSwitch) of something, for ex. a light switch |
| Television      | TV devices, projectors, satelite or cable receivers, recorders, etc. |
| Temperature     | Devices related to Temperature, such as Temperature actuators, heaters etc. |
| Washing         | Devices related to washing, such as washing machines, dryers, etc. |
| Water           | Pumps, Softeners, Drainage, Cistern, etc. |
| Weather         | Rain sensors, Humidity sensors, Pressure sensors, wind sensors, etc. |
| Window          | Anything closely related to a window such as an Open/Close contact or Opener |

## Channel Categories

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
| Moisture      | R               | Number                 |
| Motion        | R               | Switch                 |
| MoveControl   | RW              | String                 |
| Noise         | R               | Number                 |
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

The list of categories may not be complete and not every device will fit into one of these categories. It is possible to define own categories. If the category is widely used, the list of predefined categories can be extended. Moreover, not all user interfaces will support all categories. It is more important to specify the `read only` information and state information, so that default controls can be rendered, even if the category is not supported.

## Group Channel Categories

Channel groups can be seen as a kind of `sub-device` as they combine certain (physical) abilities of a `thing` into one. For such `group channels` one can set a category from the `channel` category list.

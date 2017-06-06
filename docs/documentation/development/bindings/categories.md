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
| Aircondition    | Air condition devices incl. Fans |
| BinarySensor    | General purpose sensor that returns a boolean value, i.e. a contact that reports open/close on a door or window |
| Bridge          | Bridges/Gateway need to access other devices like used by Philips Hue for example |
| Boiler          | Devices that heat up water |
| Camera          | All kinds of cameras |
| Car             | Smart Cars |
| Cistern         | Cistern |
| Clock           | Devices that provide the current time |
| Coffee          | Coffee machines |
| Fridge          | Fridges, Freezers |
| Furniture       | Smart furniture |
| GarageDoor      | Garage doors |
| GeneralSensor   | General purpose sensor that does not fit in any category |
| Heating         | Devices that deal with heating |
| Hifi            | Devices related to TV/audio, such as satelite or cable receivers, recorders, etc.
| HomeApplicance  | Devices that look like Waschingmachines, Dishwashers, Dryers, etc. |
| Light           | Devices that illuminate something, such as bulbs, etc. |
| Lock            | Devices whose primary pupose is locking something |
| Microphone      | Devices to record sound |
| Mixer           | Kitchen devices for making cake |
| MotionSensor    | Motion sensors/detectors |
| MultiSwitch     | Device which controls MULTIPLE states (else see Switch) of something |
| Oven            | Ovens, microwaves, etc. |
| Phone           | Anything related to telephony, such as mobile phones, landline phones, etc. |
| PowerSocketDevice | Small devices to be plugged into a power socket in a wall which stick there |
| Projector       | Devices that project a picture somewhere |
| Pump            | Water pumps |
| Radiator        | Radiators that heat up rooms |
| Robots          | Vacuum cleaners such as cleaning robots, lawn mowers, etc. |
| RollerShutter   | Roller shutters |
| Screen          | Devices that are able to show a picture |
| Siren           | Siren used by Alarm systems |
| Softener        | Water softeners |
| SolarPlant      | Devices related to solar |
| Smoke           | Smoke detectors |
| Speakers        | Devices that are able to play sounds |
| (Wall)Switch    | Any device that controls a BINARY status (else see MultiSwitch) of something, for ex. a light switch |
| Weather         | Rain sensors, Humidity sensors, Pressure sensors, wind sensors, etc. |

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

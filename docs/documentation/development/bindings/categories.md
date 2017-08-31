---
layout: documentation
---

{% include base.html %}

# Categories

Categories in Eclipse SmartHome are used to provide meta information about things channels, etc. UIs can use this information to render specific icons or provide a search functionality to for example filter all things for a certain category.

## Differences between categories

We seperate the categories into `functional` and `visual`. Therefore we treat `thing categories` as how the physical device **looks like** and `channel categories` as something that describes the **functional purpose** of the channel.

## Thing Categories

The thing type definition allows to specify a category. User interfaces can parse this category to get an idea how to render this thing. A binding can classify each thing into one of the existing categories. The list of all predefined categories can be found in our categories overview:

| Category        | Description                                          |
|-----------------|------------------------------------------------------|
| AVR             | Audio/Video receivers, i.e. radio receivers, satelite or cable receivers, recorders, etc.
| Blinds          | Roller shutters, window blinds, etc. |
| Camera          | All kinds of cameras |
| Car             | Smart Cars |
| CleaningRobot   | Vacuum robots, mopping robots, etc. |
| Heating         | Devices that deal with heating |
| HVAC            | Air condition devices, Fans |
| Inverter        | Power inverter, such as solar inverters etc. |
| LawnMower       | Lawn mowing robots, etc. |
| Lightbulb       | Devices that illuminate something, such as bulbs, etc. |
| Lock            | Devices whose primary pupose is locking something |
| MotionDetector  | Motion sensors/detectors |
| NetworkAppliance| Bridges/Gateway need to access other devices like used by Philips Hue for example, Routers, Switches |
| PowerOutlet     | Small devices to be plugged into a power socket in a wall which stick there |
| Projector       | Devices that project a picture somewhere |
| RadiatorControl | Controls on radiators used to heat up rooms |
| Screen          | Devices that are able to show a picture |
| Sensor          | Device used to measure something |
| Siren           | Siren used by Alarm systems |
| SmokeDetector   | Smoke detectors |
| Speaker         | Devices that are able to play sounds |
| (Wall)Switch    | Any device that controls a BINARY status (else see MultiSwitch) of something, for ex. a light switch |
| WebService      | Account with credentials for a website |
| WhiteGood       | Devices that look like Waschingmachines, Dishwashers, Dryers, Fridges, Ovens, etc. |

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

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

| Category        | Description | Icon Example |
|-----------------|-------------|{% for category in site.data.categories_thing %}
|{{category.name}}|{{category.description}}|![{{category.icon}}](../features/ui/iconset/classic/icons/{{category.icon}}){:height="36px" width="36px"}|{% endfor %}

## Channel Categories

The channel type definition allows to specify a category. Together with the definition of the `readOnly` attribute in the state description, user interfaces get an idea how to render an item for this channel. A binding should classify each channel into one of the existing categories. This is a list of all predefined categories with their usual accessible mode and the according item type:

| Category        | Accessible Mode | Item Type | Icon Example |
|-----------------|-------------|{% for category in site.data.categories_channel %}
|{{category.name}}|{{category.access}}|{{category.itemType}}|![{{category.icon}}](../features/ui/iconset/classic/icons/{{category.icon}}){:height="36px" width="36px"}|{% endfor %}

R=Read, RW=Read/Write

The accessible mode indicates whether a category could have `read only` flag configured to true or not. For example the `Motion` category can be used for sensors only, so `read only` can not be false. Temperature can be either measured or adjusted, so the accessible mode is R and RW, which means the read only flag can be `true` or `false`. In addition categories are related to specific item types. For example the 'Energy' category can only be used for `Number` items. But `Rain` could be either expressed as Switch item, where it only indicates if it rains or not, or as `Number`, which gives information about the rain intensity.

The list of categories may not be complete and not every device will fit into one of these categories. It is possible to define own categories. If the category is widely used, the list of predefined categories can be extended. Moreover, not all user interfaces will support all categories. It is more important to specify the `read only` information and state information, so that default controls can be rendered, even if the category is not supported.

## Group Channel Categories

Channel groups can be seen as a kind of `sub-device` as they combine certain (physical) abilities of a `thing` into one. For such `group channels` one can set a category from the `channel` category list.

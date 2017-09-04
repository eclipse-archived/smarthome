#!/usr/bin/env sh

READMEMD="$(cd "$(dirname "$0")"; pwd)/README.md"

cat <<EOF > "$READMEMD"
# Classic Icon Set

This is a modernized version of the original icon set from openHAB 1.x.
The set is provided with the distribution in both the PNG and SVG file format.

EOF

for icon in $(ls icons/*.png | sort -V); do
  name=$(basename "$icon")
  echo "Adding icon '$name'"
  if [ "$name" = "none.png" ] || [ "$name" = "none.svg" ]; then continue; fi
  allIcons="$allIcons$name,"
done

allIcons=${allIcons:: -1}

cat <<EOF >> "$READMEMD"
{% assign allIconsStr = "$allIcons" %}
{% assign allIcons = allIconsStr | split: ',' %}

{% for icon in allIcons %}
  {% assign iconLower = icon | downcase | split: "." %}
  {% assign iconWithoutExt = iconLower[0] %}
  {% assign allIconsWithoutExtensionStr = allIconsWithoutExtensionStr | append: iconWithoutExt | append: ',' %}
{% endfor %}
{% assign allIconsWithoutExtension = allIconsWithoutExtensionStr | split: ',' %}

## Thing Categories

These are the icons that can be used on thing types.

{% for category in site.data.categories_thing %}
    {% assign thingCategoryNamesStr = thingCategoryNamesStr | append: category.name | downcase | append: ',' %}
{% endfor %}
{% assign thingCategoryNames = thingCategoryNamesStr | split: ',' %}

{% assign allIconsAndThingCategories = allIconsWithoutExtension | concat: thingCategoryNames | sort | uniq %}
<div id="iconset-preview-things">
{% for category in allIconsAndThingCategories %}
  {% assign showCategory = false %}
  {% for thingCategory in thingCategoryNames %}
    {% if thingCategory.size <= category.size %}
      {% assign categoryStart = category | truncate: thingCategory.size, "" %}
      {% if categoryStart == thingCategory %}
        {% assign showCategory = true %}
        {% break %}
      {% endif %}
    {% endif %}
  {% endfor %}

  {% if showCategory == true %}
    {% assign iconSrc = base | append: "/img/icon_no_category.png" %}
    {% if allIconsWithoutExtension contains category %}
      {% assign iconSrc = "icons/" | append: category | append: ".png" %}
    {% endif %}
    <figure style="width: 128px; display: inline-block; text-align: center; font-size: 0.8em; margin: 16px 8px;">
      <img src="{{iconSrc}}" alt="{{category}}" title="{{category}}">
      <figcaption>{{category}}</figcaption>
    </figure>
  {% endif %}
{% endfor %}
</div>

## Channel Categories

These are the icons that can be used on channel types.

{% for category in site.data.categories_channel %}
    {% assign channelCategoryNamesStr = channelCategoryNamesStr | append: category.name | downcase | append: ',' %}
{% endfor %}
{% assign channelCategoryNames = channelCategoryNamesStr | split: ',' %}

{% assign allIconsAndChannelCategories = allIconsWithoutExtension | concat: channelCategoryNames | sort | uniq %}
<div id="iconset-preview-channels">
{% for category in allIconsAndChannelCategories %}
  {% assign showCategory = false %}
  {% for channelCategory in channelCategoryNames %}
    {% if channelCategory.size <= category.size %}
      {% assign categoryStart = category | truncate: channelCategory.size, "" %}
      {% if categoryStart == channelCategory %}
        {% assign showCategory = true %}
        {% break %}
      {% endif %}
    {% endif %}
  {% endfor %}

  {% if showCategory == true %}
    {% assign iconSrc = base | append: "/img/icon_no_category.png" %}
    {% if allIconsWithoutExtension contains category %}
      {% assign iconSrc = "icons/" | append: category | append: ".png" %}
    {% endif %}
    <figure style="width: 128px; display: inline-block; text-align: center; font-size: 0.8em; margin: 16px 8px;">
      <img src="{{iconSrc}}" alt="{{category}}" title="{{category}}">
      <figcaption>{{category}}</figcaption>
    </figure>
  {% endif %}
{% endfor %}
</div>

## Other Categories

{% assign categoriesWithIcons = thingCategoryNames | concat: channelCategoryNames | sort | uniq %}

<div id="iconset-preview-other">
{% for icon in allIcons %}
  {% assign categoryLower = icon | downcase | split: "." %}
  {% assign plainCategory = categoryLower[0] %}

  {% assign otherIcon = true %}
  {% for catWithIcon in categoriesWithIcons %}
    {% if catWithIcon.size <= plainCategory.size %}
      {% assign plainCategoryStart = plainCategory | truncate: catWithIcon.size, "" %}
      {% if plainCategoryStart == catWithIcon %}
        {% assign otherIcon = false %}
        {% break %}
      {% endif %}
    {% endif %}
  {% endfor %}

  {% if otherIcon == false %}
    {% continue %}
  {% endif %}

  <figure style="width: 128px; display: inline-block; text-align: center; font-size: 0.8em; margin: 16px 8px;">
    <img src="icons/{{icon}}" alt="{{icon}}" title="{{icon}}">
    <figcaption>{{plainCategory}}</figcaption>
  </figure>
{% endfor %}
</div>

EOF

echo "Finished."

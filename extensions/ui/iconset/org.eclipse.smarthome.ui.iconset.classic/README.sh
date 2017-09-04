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

<div id="iconset-preview-things">
{% for thingCategory in thingCategoryNames %}
  {% assign iconSrc = base | append: "/img/icon_no_category.png" %}
  {% if allIconsWithoutExtension contains thingCategory %}
    {% assign iconSrc = "icons/" | append: thingCategory | append: ".png" %}
  {% endif %}
  <figure style="width: 128px; display: inline-block; text-align: center; font-size: 0.8em; margin: 16px 8px;">
    <img src="{{iconSrc}}" alt="{{thingCategory}}" title="{{thingCategory}}">
    <figcaption>{{thingCategory}}</figcaption>
  </figure>

  {% for icon in allIconsWithoutExtension %}
    {% if thingCategory.size < icon.size %}
      {% assign iconStart = icon | truncate: thingCategory.size, "" %}
      {% if iconStart == thingCategory %}
        {% unless icon contains "_" %}
          {% assign iconSrc = "icons/" | append: icon | append: ".png" %}
          <figure style="width: 128px; display: inline-block; text-align: center; font-size: 0.8em; margin: 16px 8px;">
            <img src="{{iconSrc}}" alt="{{icon}}" title="{{icon}}">
            <figcaption>{{icon}}</figcaption>
          </figure>
        {% endunless %}
      {% endif %}
    {% endif %}
  {% endfor %}
{% endfor %}
</div>

## Channel Categories

These are the icons that can be used on channel types.

{% for category in site.data.categories_channel %}
    {% assign channelCategoryNamesStr = channelCategoryNamesStr | append: category.name | downcase | append: ',' %}
{% endfor %}
{% assign channelCategoryNames = channelCategoryNamesStr | split: ',' %}

<div id="iconset-preview-channels">
{% for channelCategory in channelCategoryNames %}
  {% assign iconSrc = base | append: "/img/icon_no_category.png" %}
  {% if allIconsWithoutExtension contains channelCategory %}
    {% assign iconSrc = "icons/" | append: channelCategory | append: ".png" %}
  {% endif %}
  <figure style="width: 128px; display: inline-block; text-align: center; font-size: 0.8em; margin: 16px 8px;">
    <img src="{{iconSrc}}" alt="{{channelCategory}}" title="{{channelCategory}}">
    <figcaption>{{channelCategory}}</figcaption>
  </figure>

  {% for icon in allIconsWithoutExtension %}
    {% if channelCategory.size < icon.size %}
      {% assign iconStart = icon | truncate: channelCategory.size, "" %}
      {% if iconStart == channelCategory %}
        {% unless icon contains "_" %}
          {% assign iconSrc = "icons/" | append: icon | append: ".png" %}
          <figure style="width: 128px; display: inline-block; text-align: center; font-size: 0.8em; margin: 16px 8px;">
            <img src="{{iconSrc}}" alt="{{icon}}" title="{{icon}}">
            <figcaption>{{icon}}</figcaption>
          </figure>
        {% endunless %}
      {% endif %}
    {% endif %}
  {% endfor %}
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

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

## Thing Categories
{% assign thingCategoryNamesStr = "" %}

These are the icons that can be used on thing types.

<div id="iconset-preview-things">
{% for category in site.data.thingCategories %}
  {% assign thingCategoryNamesStr = thingCategoryNamesStr | append: category.name %}
  {% assign thingCategoryNamesStr = thingCategoryNamesStr | append: ',' %}

  {% assign iconSrc = category.icon %}
  {% if category.icon == nil %}
    {% assign iconSrc = "empty.png" %}
  {% endif %}

  <figure style="width: 128px; display: inline-block; text-align: center; font-size: 0.8em; margin: 16px 8px;">
    <img src="icons/{{iconSrc}}" alt="{{category.name}}" title="{{category.name}}">
    <figcaption>{{category.name}}</figcaption>
  </figure>
{% endfor %}
</div>

{% assign thingCategoryNames = thingCategoryNamesStr | split: ',' %}

## Channel Categories
{% assign channelCategoryNamesStr = "" %}

These are the icons that can be used on channel types.

<div id="iconset-preview-things">
{% for category in site.data.channelCategories %}
  {% assign channelCategoryNamesStr = channelCategoryNamesStr | append: category.name %}
  {% assign channelCategoryNamesStr = channelCategoryNamesStr | append: ',' %}

  {% assign iconSrc = category.icon %}
  {% if category.icon == nil %}
    {% assign iconSrc = "empty.png" %}
  {% endif %}

  <figure style="width: 128px; display: inline-block; text-align: center; font-size: 0.8em; margin: 16px 8px;">
    <img src="icons/{{iconSrc}}" alt="{{category.name}}" title="{{category.name}}">
    <figcaption>{{category.name}}</figcaption>
  </figure>
{% endfor %}
</div>

{% assign channelCategoryNames = channelCategoryNamesStr | split: ',' %}

{% assign categoriesWithIcons = thingCategoryNames | concat: channelCategoryNames | sort | uniq %}

## Other Categories

<div id="iconset-preview-other">
{% for category in allIcons %}
  {% assign tmp = category | split: "." %}
  {% assign plainCategory = tmp[0] %}

  {% assign otherIcon = true %}
  {% for cat in categoriesWithIcons %}
    {% assign catWithIcon = cat | downcase %}
    {% if catWithIcon == plainCategory %}
      {% assign otherIcon = false %}
      {% break %}
    {% endif %}
  {% endfor %}

  {% if otherIcon == false %}
    {% continue %}
  {% endif %}

  <figure style="width: 128px; display: inline-block; text-align: center; font-size: 0.8em; margin: 16px 8px;">
    <img src="icons/{{category}}" alt="{{category}}" title="{{category}}">
    <figcaption>{{plainCategory}}</figcaption>
  </figure>
{% endfor %}
</div>

EOF

echo "Finished."

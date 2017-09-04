# Classic Icon Set

This is a modernized version of the original icon set from openHAB 1.x.
The set is provided with the distribution in both the PNG and SVG file format.

{% assign allIconsStr = "alarm.png,attic.png,baby_1.png,baby_2.png,baby_3.png,baby_4.png,baby_5.png,baby_6.png,bath.png,battery.png,battery-0.png,battery-10.png,battery-20.png,battery-30.png,battery-40.png,battery-50.png,battery-60.png,battery-70.png,battery-80.png,battery-90.png,battery-100.png,battery-off.png,battery-on.png,bedroom.png,bedroom_blue.png,bedroom_orange.png,bedroom_red.png,blinds.png,blinds-0.png,blinds-10.png,blinds-20.png,blinds-30.png,blinds-40.png,blinds-50.png,blinds-60.png,blinds-70.png,blinds-80.png,blinds-90.png,blinds-100.png,bluetooth.png,boiler_viessmann.png,boy_1.png,boy_2.png,boy_3.png,boy_4.png,boy_5.png,boy_6.png,calendar.png,camera.png,carbondioxide.png,cellar.png,chart.png,cinema.png,cinemascreen.png,cinemascreen-0.png,cinemascreen-10.png,cinemascreen-20.png,cinemascreen-30.png,cinemascreen-40.png,cinemascreen-50.png,cinemascreen-60.png,cinemascreen-70.png,cinemascreen-80.png,cinemascreen-90.png,cinemascreen-100.png,cistern.png,cistern-0.png,cistern-10.png,cistern-20.png,cistern-30.png,cistern-40.png,cistern-50.png,cistern-60.png,cistern-70.png,cistern-80.png,cistern-90.png,cistern-100.png,climate.png,climate-on.png,clock.png,clock-on.png,colorlight.png,colorpicker.png,colorwheel.png,contact.png,contact-ajar.png,contact-closed.png,contact-open.png,corridor.png,dimmablelight.png,dimmablelight-0.png,dimmablelight-10.png,dimmablelight-20.png,dimmablelight-30.png,dimmablelight-40.png,dimmablelight-50.png,dimmablelight-60.png,dimmablelight-70.png,dimmablelight-80.png,dimmablelight-90.png,dimmablelight-100.png,door.png,door-closed.png,door-open.png,dryer.png,dryer-0.png,dryer-1.png,dryer-2.png,dryer-3.png,dryer-4.png,dryer-5.png,energy.png,error.png,fan.png,fan_box.png,fan_ceiling.png,faucet.png,fire.png,fire-off.png,fire-on.png,firstfloor.png,flow.png,flowpipe.png,frontdoor.png,frontdoor-closed.png,frontdoor-open.png,garage.png,garagedoor.png,garagedoor-0.png,garagedoor-10.png,garagedoor-20.png,garagedoor-30.png,garagedoor-40.png,garagedoor-50.png,garagedoor-60.png,garagedoor-70.png,garagedoor-80.png,garagedoor-90.png,garagedoor-100.png,garagedoor-ajar.png,garagedoor-closed.png,garagedoor-open.png,garage_detached.png,garage_detached_selected.png,garden.png,gas.png,girl_1.png,girl_2.png,girl_3.png,girl_4.png,girl_5.png,girl_6.png,grass.png,greenhouse.png,groundfloor.png,group.png,heating.png,heating-0.png,heating-20.png,heating-40.png,heating-60.png,heating-80.png,heating-100.png,heating-off.png,heating-on.png,house.png,humidity.png,humidity-0.png,humidity-10.png,humidity-20.png,humidity-30.png,humidity-40.png,humidity-50.png,humidity-60.png,humidity-70.png,humidity-80.png,humidity-90.png,humidity-100.png,incline.png,keyring.png,kitchen.png,light.png,lightbulb.png,light-off.png,light-on.png,line.png,line-decline.png,line-incline.png,line-stagnation.png,lock.png,lock-closed.png,lock-open.png,man_1.png,man_2.png,man_3.png,man_4.png,man_5.png,man_6.png,microphone.png,moon.png,motion.png,movecontrol.png,network.png,network-off.png,network-on.png,niveau.png,office.png,oil.png,outdoorlight.png,pantry.png,parents-off.png,parents_1_1.png,parents_1_2.png,parents_1_3.png,parents_1_4.png,parents_1_5.png,parents_1_6.png,parents_2_1.png,parents_2_2.png,parents_2_3.png,parents_2_4.png,parents_2_5.png,parents_2_6.png,parents_3_1.png,parents_3_2.png,parents_3_3.png,parents_3_4.png,parents_3_5.png,parents_3_6.png,parents_4_1.png,parents_4_2.png,parents_4_3.png,parents_4_4.png,parents_4_5.png,parents_4_6.png,parents_5_1.png,parents_5_2.png,parents_5_3.png,parents_5_4.png,parents_5_5.png,parents_5_6.png,parents_6_1.png,parents_6_2.png,parents_6_3.png,parents_6_4.png,parents_6_5.png,parents_6_6.png,party.png,pie.png,piggybank.png,player.png,poweroutlet.png,poweroutlet-off.png,poweroutlet-on.png,poweroutlet_au.png,poweroutlet_eu.png,poweroutlet_uk.png,poweroutlet_us.png,present.png,present-off.png,pressure.png,projector.png,pump.png,qualityofservice.png,qualityofservice-0.png,qualityofservice-1.png,qualityofservice-2.png,qualityofservice-3.png,qualityofservice-4.png,radiator.png,rain.png,receiver.png,receiver-off.png,receiver-on.png,recorder.png,returnpipe.png,rgb.png,rollershutter.png,rollershutter-0.png,rollershutter-10.png,rollershutter-20.png,rollershutter-30.png,rollershutter-40.png,rollershutter-50.png,rollershutter-60.png,rollershutter-70.png,rollershutter-80.png,rollershutter-90.png,rollershutter-100.png,screen.png,screen-off.png,screen-on.png,settings.png,sewerage.png,sewerage-0.png,sewerage-10.png,sewerage-20.png,sewerage-30.png,sewerage-40.png,sewerage-50.png,sewerage-60.png,sewerage-70.png,sewerage-80.png,sewerage-90.png,sewerage-100.png,shield.png,shield-0.png,shield-1.png,signal.png,signal-0.png,signal-1.png,signal-2.png,signal-3.png,signal-4.png,siren.png,siren-off.png,siren-on.png,slider.png,slider-0.png,slider-10.png,slider-20.png,slider-30.png,slider-40.png,slider-50.png,slider-60.png,slider-70.png,slider-80.png,slider-90.png,slider-100.png,smiley.png,smoke.png,sofa.png,softener.png,solarplant.png,soundvolume.png,soundvolume-0.png,soundvolume-33.png,soundvolume-66.png,soundvolume-100.png,soundvolume_mute.png,status.png,suitcase.png,sun.png,sunrise.png,sunset.png,sun_clouds.png,switch.png,switch-off.png,switch-on.png,temperature.png,temperature_cold.png,temperature_hot.png,terrace.png,text.png,toilet.png,vacation.png,video.png,wallswitch.png,wallswitch-off.png,wallswitch-on.png,wardrobe.png,washingmachine.png,washingmachine_2.png,washingmachine_2-0.png,washingmachine_2-1.png,washingmachine_2-2.png,washingmachine_2-3.png,water.png,whitegood.png,wind.png,window.png,window-ajar.png,window-closed.png,window-open.png,woman_1.png,woman_2.png,woman_3.png,woman_4.png,woman_5.png,woman_6.png,zoom.png" %}
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


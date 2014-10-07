# Internationalization

In this chapter the Eclipse SmartHome support for internationalization is described. 

## Internationalize Binding XML files

In Eclipse SmartHome a binding developer has to provide different XML files. In these XML files label and description texts must be specified. To Internationalize these texts, language files can be defined that follow the [Java Resource Bundle](http://docs.oracle.com/javase/6/docs/api/java/util/ResourceBundle.html) specification. All language files must be placer under the folder `ESH-INF/i18n`.

For each supported language one has to define a language file that contains the locale as file name suffix. For example the file name `language_fr.properties` will be used for french, where `language_de.properties` will be used for german texts. The localized text is resolved via the Java Resource Bundle mechanism. So if one requests a Locale `de_AT` the file `language_de.properties` will be used for example. The developer can define multiple language files, but the keys must be unique across the whole OSGi bundle.

For the the `binding.xml` and the `thing-types.xml` file Eclipse SmartHome defines a standard key scheme, that allows to easily reference the XML nodes. Inside the XML nodes the text must be specified in the default language english. Typically all texts for binding are put into one file with name of the binding, but they can also be splitted into multiple files.

### Binding Info

The following snippet shows the binding xml file of the Yahoo Weather Binding and its language file that localizes the binding name and description for the german language.

XML file (binding.xml):
```xml
<binding:binding id="yahooweather">
    <name>YahooWeather Binding</name>
    <description>The Yahoo Weather Binding requests the Yahoo Weather Service
		to show the current temperature, humidity and pressure.</description>
    <author>Kai Kreuzer</author>
</binding:binding>
```

Language file (yahooweather_de.properties):
```ini
binding.yahooweather.name = Yahoo Wetter Binding
binding.yahooweather.description = Das Yahoo Wetter Binding stellt verschiedene Wetterdaten wie die Temperatur, die Luftfeuchtigkeit und den Luftdruck für konfigurierbare Orte vom yahoo Wetterdienst bereit
```

So the key for referencing the name of a binding is `binding.<binding-id>.name` and `binding.<binding-id>.description` for the description text. 

### Thing Types

The following snippet shows an excerpt of the thing type definition xml file of the Yahoo Weather Binding and its language file that localizes labels and descriptions for the german language.

XML file (binding.xml):
```xml
<thing:thing-descriptions bindingId="yahooweather">
    <thing-type id="weather">
        <label>Weather Information</label>
        <description>Provides various weather data from the Yahoo service</description>
		<channels>
			<channel id="temperature" typeId="temperature" />
		</channels>
        <config-description>
            <parameter name="location" type="text">
                <label>Location</label>
                <description>Location for the weather information.
                    Syntax is WOEID, see https://en.wikipedia.org/wiki/WOEID.
                </description>
				<required>true</required>
            </parameter>
        </config-description>
    </thing-type>
    <channel-type id="temperature">
        <item-type>Number</item-type>
        <label>Temperature</label>
        <description>Current temperature in degrees celsius (metric) or fahrenheit (us)</description>
    </channel-type>
</thing:thing-descriptions>

```

Language file (yahooweather_de.properties):
```ini
thing-type.yahooweather.weather.label = Wetterinformation
thing-type.yahooweather.weather.description = Stellt verschiedene Wetterdaten vom yahoo Wetterdienst bereit
thing-type.config.yahooweather.weather.location.label = Ort
thing-type.config.yahooweather.weather.location.description = Ort der Wetterinformation.

channel-type.yahooweather.temperature.label = Temperatur
channel-type.yahooweather.temperature.description = Aktuelle Temperatur in Grad Celsius (metrisch) oder Grad Fahrenheit (us)
```

So the key for referencing a label of a defined thing type is `thing-type.<binding-id>.<thing-type-id>.label`. A label of a channel can be referenced with `channel-type.<binding-id>.<channel-type-id>.label`. And finally the config description parameter key is `thing-type.config.<binding-id>.<thing-type-id>.<parameter-name>.label`.

### Using custom Keys

In addition to the default keys the developer can also specify custom keys inside the XML file. But with this approach the XML file cannot longer contain the english texts. So it is mandatory to define a language file for the english language. The syntax for custom keys is `@text/<key>`.

The following snippet shows a binding xml that uses custom keys:

XML file (binding.xml):
```xml
<binding:binding id="yahooweather">
    <name>@text/bindingName</name>
    <description>@text/bindingName</description>
    <author>Kai Kreuzer</author>
</binding:binding>
```

Language file (yahooweather_en.properties):
```ini
bindingName = Yahoo Weather Binding
```

Language file (yahooweather_de.properties):
```ini
bindingName = Yahoo Wetter Binding
```

## I18n Text Provider API

To programmatically resolve texts for certain languages Eclipse SmartHome provides an OSGi service `I18nProvider`. The service parses every file inside the `ESH-INF/i18n` folder and caches all texts. A localized text can be retrieved via the method `getText(Bundle bundle, String key, String default, Locale locale)`, where bundle must be the reference to the bundle, in which the file is stored. The BundleContext from the Activator provides a method to get the bundle.

```java
String text = i18nProvider.getText(bundleContext.getBundle(), "my.key", "DefaultValue", Locale.GERMAN);
```

## Getting Thing Types and Binding Info in different languages

Thing Types can be retrieved through the `ThingTypeRegistry`. Every method takes a `Locale` as last argument. If no locale is specified the thing types are returned for the default locale which is determined by `Locale.getDefault()`, or the default text, which is specified in the XML, if no language file for the default locale exists.

The following snippet shows how to retrieve the list of Thing Types for the german locale:

```java
List<ThingType> thingTypes = thingTypeRegistry.getThingTypes(Locale.GERMAN);
```

If one binding supports the german language and another does not, it might occur that the languages of returned Thing Types are mixed.

For Binding Info and ConfigDescription, the localized objects can be retrieved via the `BindingInfoRegistry` and the `ConfigDescriptionRegistry` in the same manner as described for Thing Types.


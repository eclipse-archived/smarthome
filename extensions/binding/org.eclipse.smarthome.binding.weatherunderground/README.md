---
layout: documentation
---

{% include base.html %}

# WeatherUnderground Binding

This binding uses the [Weather Underground service](https://www.wunderground.com/weather/api/) for providing weather information for any location worldwide.

The Weather Underground API is provided by The Weather Underground, LLC (WUL) free of charge but there is a daily limit and minute rate limit to the number of requests that can be made to the API for free.
WUL will monitor your daily usage of the API to determine if you have exceeded the free-use threshold by using an API key. You may exceed this threshold only if you are or become a fee paying subscriber.
By using this binding, you confirm that you agree with the [Weather Underground API terms and conditions of use](https://www.wunderground.com/weather/api/d/terms.html).

To use this binding, you first need to [register and get your API key](https://www.wunderground.com/weather/api/d/pricing.html) .

## Supported Things

There is exactly one supported thing type, which represents the weather information for an observation location. It has the id `weather`.

## Discovery

If a system location is set, "Local Weather" will be automatically discovered for this location.

If the system location is changed, the background discovery updates the configuration of "Local Weather" automatically.

After adding this discovered thing, you will have to set the correct API key. 

## Binding Configuration
 
The binding has no configuration options, all configuration is done at Thing and Channel levels.
 
## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                              |
|-----------|------------------------------------------------------------------------- |
| apikey    | API key to access the Weather Underground service. Mandatory.            |
| location  | Location to be considered by the Weather Underground service. Mandatory. |
| language  | Language to be used by the Weather Underground service. Optional, the default is to use the language from the system locale. |
| refresh   | Refresh interval in minutes. Optional, the default value is 30 minutes and the minimum value is 5 minutes.  |

For the location parameter, different syntaxes are possible:

| Syntax                  | Example          |
|-------------------------|----------------- |
| US state/city           | CA/San_Francisco |
| US zipcode              | 60290            |
| country/city            | Australia/Sydney |
| latitude,longitude      | 37.8,-122.4      |
| airport code            | KJFK             |
| PWS id                  | pws:KCASANFR70   |

It can happen that the service is not able to determine the station to use, for example when you select as location a city in which several stations are registered. In this case, the thing configuration will fail because the service will not return the data expected by the binding. The best solution in this case is to use as location latitude and longitude, the service will automatically select a station from this position.

## Channels

The weather information that is retrieved is available as these channels:

| Channel Group ID | Channel ID | Item Type    | Description             | Configuration property |
|------------------|------------|--------------|-------------------------|---------------------------------- |
| Current | location | String | Weather observation location | |
| Current | stationId | String | Weather station identifier | |
| Current | observationTime | DateTime | Observation date and time | |
| Current | conditions | String | Weather conditions | |
| Current | temperature | Number | Temperature | SourceUnit: "C" for degrees Celsius or "F" for degrees Fahrenheit; default is "C" |
| Current | relativeHumidity | Number | Relative humidity in % | |
| Current | windDirection | String | Wind direction | |
| Current | windDirectionDegrees | Number | Wind direction in degrees | |
| Current | windSpeed | Number | Wind speed | SourceUnit: "kmh" or "mph"; default is "kmh" |
| Current | windGust | Number | Wind gust | SourceUnit: "kmh" or "mph"; default is "kmh" |
| Current | pressure | Number | Pressure | SourceUnit: "hPa" or "inHg"; default is "hPa" |
| Current | dewPoint | Number | Dew Point temperature | SourceUnit: "C" for degrees Celsius or "F" for degrees Fahrenheit; default is "C" |
| Current | heatIndex | Number | Heat Index | SourceUnit: "C" for degrees Celsius or "F" for degrees Fahrenheit; default is "C" |
| Current | windChill | Number | Wind chill temperature | SourceUnit: "C" for degrees Celsius or "F" for degrees Fahrenheit; default is "C" |
| Current | feelingTemperature | Number | Feeling temperature | SourceUnit: "C" for degrees Celsius or "F" for degrees Fahrenheit; default is "C" |
| Current | visibility | Number | Visibility | SourceUnit: "km" or "mi"; default is "km" |
| Current | solarRadiation | Number | Solar radiation in W/m2 | |
| Current | UVIndex | Number | UV Index | |
| Current | precipitationDay | Number | Rain fall during the day | SourceUnit: "mm" or "in"; default is "mm" |
| Current | precipitationHour | Number | Rain fall during the last hour | SourceUnit: "mm" or "in"; default is "mm" |
| Current | icon | Image | Icon representing the weather current conditions | |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | forecastTime | DateTime | Forecast date and time | |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | conditions | String | Weather forecast conditions | |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | minTemperature | Number | Minimum temperature | SourceUnit: "C" for degrees Celsius or "F" for degrees Fahrenheit; default is "C" |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | maxTemperature | Number | Maximum temperature | SourceUnit: "C" for degrees Celsius or "F" for degrees Fahrenheit; default is "C" |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | relativeHumidity | Number | Relative humidity in % | |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | probaPrecipitation | Number | Weather forecast conditions | |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | precipitationDay | Number | Rain fall | SourceUnit: "mm" or "in"; default is "mm" |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | snow | Number | Snow fall | SourceUnit: "cm" or "in"; default is "cm" |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | maxWindDirection | String | Maximum wind direction | |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | maxWindDirectionDegrees | Number | Maximum wind direction in degrees | |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | maxWindSpeed | Number | Maximum wind speed | SourceUnit: "kmh" or "mph"; default is "kmh" |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | averageWindDirection | String | Average wind direction | |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | averageWindDirectionDegrees | Number | Average wind direction in degrees | |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | averageWindSpeed | Number | Average wind speed | SourceUnit: "kmh" or "mph"; default is "kmh" |
| forecastToday forecastTomorrow forecastDay2 ... forecastDay9 | icon | Image | Icon representing the weather forecast conditions | |


## Full Example

demo.things:

```
Thing weatherunderground:weather:CDG "Météo Paris CDG" [ apikey="XXXXXXXXXXXX", location="CDG", language="FR", refresh=15 ] {
    Channels:
        Type temperature : current#temperature [ SourceUnit="C" ]
        Type windSpeed : current#windSpeed [ SourceUnit="kmh" ]
        Type windGust : current#windGust [ SourceUnit="kmh" ]
        Type pressure : current#pressure [ SourceUnit="hPa" ]
        Type dewPoint : current#dewPoint [ SourceUnit="C" ]
        Type heatIndex : current#heatIndex [ SourceUnit="C" ]
        Type windChill : current#windChill [ SourceUnit="C" ]
        Type feelingTemperature : current#feelingTemperature [ SourceUnit="C" ]
        Type visibility : current#visibility [ SourceUnit="km" ]
        Type rainDay : current#precipitationDay [ SourceUnit="mm" ]
        Type rainHour : current#precipitationHour [ SourceUnit="mm" ]
        Type minTemperature : forecastToday#minTemperature [ SourceUnit="C" ]
        Type maxTemperature : forecastToday#maxTemperature [ SourceUnit="C" ]
        Type rainDay : forecastToday#precipitationDay [ SourceUnit="mm" ]
        Type snow : forecastToday#snow [ SourceUnit="cm" ]
        Type maxWindSpeed : forecastToday#maxWindSpeed [ SourceUnit="kmh" ]
        Type averageWindSpeed : forecastToday#averageWindSpeed [ SourceUnit="kmh" ]
}
```

demo.items:

```
String Conditions "Conditions [%s]" {channel="weatherunderground:weather:CDG:current#conditions"}
Image Icon "Icon" {channel="weatherunderground:weather:CDG:current#icon"}
DateTime ObservationTime "Observation time [%1$tH:%1$tM]" <clock>  {channel="weatherunderground:weather:CDG:current#observationTime"}
String ObservationLocation "Location [%s]" {channel="weatherunderground:weather:CDG:current#location"}
String Station "Station [%s]" {channel="weatherunderground:weather:CDG:current#stationId"}

Number Temperature "Current temperature [%.1f °C]" <temperature> {channel="weatherunderground:weather:CDG:current#temperature"}
Number FeelTemp "Feeling temperature [%.1f °C]" <temperature>  {channel="weatherunderground:weather:CDG:current#feelingTemperature"}

Number Humidity "Humidity [%d %%]" <humidity> {channel="weatherunderground:weather:CDG:current#relativeHumidity"}
Number Pressure "Pressure [%.0f hPa]" {channel="weatherunderground:weather:CDG:current#pressure"}

Number RainD "Rain [%.1f mm]" <rain> {channel="weatherunderground:weather:CDG:current#precipitationDay"}
Number RainH "Rain [%.1f mm/h]" <rain> {channel="weatherunderground:weather:CDG:current#precipitationHour"}

String WindDirection "Wind direction [%s]" <wind> {channel="weatherunderground:weather:CDG:current#windDirection"}
Number WindDirection2 "Wind direction [%.0f °]" <wind>  {channel="weatherunderground:weather:CDG:current#windDirectionDegrees"}
Number WindSpeed "Wind speed [%.1f km/h]" <wind> {channel="weatherunderground:weather:CDG:current#windSpeed"}
Number WindGust "Wind gust [%.1f km/h]" <wind> {channel="weatherunderground:weather:CDG:current#windGust"}

Number DewPoint "Dew Point [%.1f °C]" <temperature>  {channel="weatherunderground:weather:CDG:current#dewPoint"}
Number HeatIndex "Heat Index [%.1f °C]" <temperature>  {channel="weatherunderground:weather:CDG:current#heatIndex"}
Number WindChill "Wind Chill [%.1f °C]" <temperature>  {channel="weatherunderground:weather:CDG:current#windChill"}
Number Visibility "Visibility [%.1f km]" {channel="weatherunderground:weather:CDG:current#visibility"}
Number SolarRadiation "Solar Radiation [%.2f] W/m2"  {channel="weatherunderground:weather:CDG:current#solarRadiation"}
Number UV "UV Index [%.1f]" {channel="weatherunderground:weather:CDG:current#UVIndex"}

DateTime ForecastTime "Forecast time [%1$tH:%1$tM]" <clock>  {channel="weatherunderground:weather:CDG:forecastToday#forecastTime"}
String ForecastCondition "Forecast conditions [%s]"  {channel="weatherunderground:weather:CDG:forecastToday#conditions"}
Image ForecastIcon "Forecast icon"  {channel="weatherunderground:weather:CDG:forecastToday#icon"}
Number ForecastTempMin "Forecast min temp [%.1f °C]" <temperature>  {channel="weatherunderground:weather:CDG:forecastToday#minTemperature"}
Number ForecastTempMax "Forecast max temp [%.1f °C]" <temperature>  {channel="weatherunderground:weather:CDG:forecastToday#maxTemperature"}
Number ForecastHumidity "Forecast Humidity [%d %%]" <humidity>  {channel="weatherunderground:weather:CDG:forecastToday#relativeHumidity"}
Number ForecastProbaPrecip "Proba precip [%d %%]" <rain>  {channel="weatherunderground:weather:CDG:forecastToday#probaPrecipitation"}
Number ForecastRain "Rain [%.1f mm]" <rain> {channel="weatherunderground:weather:CDG:forecastToday#precipitationDay"}
Number ForecastSnow "Snow [%.2f cm]" <rain> {channel="weatherunderground:weather:CDG:forecastToday#snow"}
String ForecastMaxWindDirection "Max wind direction [%s]" <wind>  {channel="weatherunderground:weather:CDG:forecastToday#maxWindDirection"}
Number ForecastMaxWindDirection2 "Max wind direction [%.0f °]" <wind>  {channel="weatherunderground:weather:CDG:forecastToday#maxWindDirectionDegrees"}
Number ForecastMaxWindSpeed "Max wind speed [%.1f km/h]" <wind>  {channel="weatherunderground:weather:CDG:forecastToday#maxWindSpeed"}
String ForecastAvgWindDirection "Avg wind direction [%s]" <wind>  {channel="weatherunderground:weather:CDG:forecastToday#averageWindDirection"}
Number ForecastAvgWindDirection2 "Avg wind direction [%.0f °]" <wind>  {channel="weatherunderground:weather:CDG:forecastToday#averageWindDirectionDegrees"}
Number ForecastAvgWindSpeed "Avg wind speed [%.1f km/h]" <wind>  {channel="weatherunderground:weather:CDG:forecastToday#averageWindSpeed"}
```


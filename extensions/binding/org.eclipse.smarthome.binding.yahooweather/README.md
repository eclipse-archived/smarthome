# YahooWeather Binding

This binding uses the [Yahoo Weather service](https://developer.yahoo.com/weather/) for providing current weather information.

_Note:_ The Yahoo Weather API is provided by Yahoo free of charge for personal, non-commercial uses, but it requires attribution and the acceptance of their terms of use.
By using this binding, you confirm that you agree with this - please read the details on [https://developer.yahoo.com/weather/](https://developer.yahoo.com/weather/).

## Supported Things

There is exactly one supported thing, which represents the weather service. It has the id `weather`.

## Thing Configuration

Besides the location (as ```location``` as a [WOEID](https://en.wikipedia.org/wiki/WOEID) number), the second configuration parameter is ```refresh``` which defines the refresh interval in seconds.

## Channels

The weather information that is retrieved is available as these channels:

| Channel Type ID | Item Type            | Description               |
|-----------------|----------------------|---------------------------|
| temperature     | Number:Temperature   | The current temperature   |
| humidity        | Number:Dimensionless | The current humidity in % |
| pressure        | Number:Pressure      | The current pressure      |


## Full Example

demo.things:

```
yahooweather:weather:berlin [ location=638242 ]
```

demo.items:

```
Number:Temperature Temperature 	"Outside Temperature [%.1f %unit%]" { channel="yahooweather:weather:berlin:temperature" }
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
	Frame {
		Text item=Temperature
	}
}
```

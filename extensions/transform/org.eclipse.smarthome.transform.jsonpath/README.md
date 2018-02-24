# JsonPath Transformation Service

Transforms a JSON structure on basis of the [JsonPath](https://github.com/jayway/JsonPath#jayway-jsonpath) expression to an JSON containing the requested data.

## Examples

### Basic Example

Given the JSON

```
[{ "device": { "location": "Outside", "status": { "temperature": 23.2 }}}]
```

the JsonPath expression `$.device.location` exstracts the JSON

```
[ "Outside" ]
```

the JsonPath expression `$.device.status.temperature` exstracts the JSON

```
[ 23.2 ]
```

### In Setup

**Item**

```csv
String  Temperature_json "Temperature [JSONPATH($.device.status.temperature):%s °C]" {...}
Number  Temperature "Temperature [%.1f °C]"
```

**Rule**

```php
rule "Convert JSON to Item Type Number"
  when
    Item Temperature_json changed
 then
    // use the transformation service to retrieve the value
    val newValue = transform("JSONPATH", ".$.device.status.temperature", Temperature_json.state.toString)

    // post the new value to the Number Item
    Temperature.postUpdate( newValue )
 end
```

Now the resulting Number can also be used in the label to [change the color](https://docs.openhab.org/configuration/sitemaps.html#label-and-value-colors) or in a rule as value to compare.

## Differences to standard JsonPath

Returns `null` if the JsonPath expression could not be found.
Compared to standard JSON the transformation it returns evaluated values when a single alement is retrieved from the querry.
Means it does not return a valid JSON `[ 23.2 ]` but `23.2`, `[ "Outside" ]` but `Outside`.
This makes it possible to use it in lables or output channel of things and get Numbers or strings instead of JSON arrays.
A querry which returns multiple elements as lsit is not supported.

## Further Reading
* An extended [introduction](https://www.w3schools.com/js/js_json_intro.asp) can be found at W3School.
* As JsonPath transformation is based on [Jayway](https://github.com/json-path/JsonPath) using a [online validator](https://jsonpath.herokuapp.com/) which also uses Jaway will give most similar results. 

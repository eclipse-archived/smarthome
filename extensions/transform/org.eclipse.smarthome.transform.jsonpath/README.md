# JsonPath Transformation Service

Extract an element of a JSON string using a [JsonPath expression](https://github.com/jayway/JsonPath#jayway-jsonpath).

Return `null` if the JsonPath expression could not be found.

## Example

Given the JsonPath expression `$.device.status.temperature`:

| input | output |
|-------|--------|
| `{ "device": { "status": { "temperature": 23.2 }}}` | `23.2` |

## Testing Tools

* [http://jsonpath.com/](http://jsonpath.com/)
* [http://www.jsonquerytool.com/](http://www.jsonquerytool.com/)

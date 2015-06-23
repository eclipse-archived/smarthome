# Remote events

In order to receive notice of important events outside of the Eclipse SmartHome framework they are exposed using the Server Sent Events (SSE) standard.

To subscribe to events a developer can listen to `/rest/events`. In general any SSE Consumer API can be used (for example HTML5 EventSource Object) to read from the event stream.

## Events

The framework broadcasts all events on the Eclipse SmartHome event bus also as an SEE event. A complete list of the framework event types can found in the [Core Event chapter](events.md).

All events are represented as JSON objects on the stream with the following format:

```json
{
    "topic": "smarthome/inbox/yahooweather:weather:12811438/added",
    "data": "{
        "flag": "NEW",
        "label": "Yahoo weather Berlin, Germany",
        "properties": {
            "location": "12811438"
        },
        "thingUID": "yahooweather:weather:12811438"
    }"
}
```

* `topic`: the event topic (see also [Runtime Events](events.md))
* `data`: String, which contains the payload of the Eclipse SmartHome event. For all core events, the payload will be in the JSON format. For example the `smarthome/items/item123/added` event will include the new item that was added and the `smarthome/items/item123/updated` event will include both old and new item.
  
## Filtering

By default when listening to `/rest/events` a developer will receive all events that are currently broadcasted. In order to listen for specific events the `topics` query parameter can be used.

For example while listening to `/services/events?topics=smarthome/items/*` a developer would receive notifications about item events only. The wildcard character(\*) can be used replacing one (or multiple) parts of the topic.

The `topics` query parameter allows for multiple filters to be specified using a comma(,) for a separator - `?topics=smarthome/items/*, smarthome/things/*`.

## Example

An example of listing events in JavaScript using the HTML5 EventSource object is provided below:

```js
var eventSource = new EventSource("/rest/events?topics=smarthome/*/added,smarthome/inbox/*");	

eventSource.addEventListener('message', function (event) {
    console.log(event.topic);
    console.log(event.data);		
});
```
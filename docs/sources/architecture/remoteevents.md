# Remote events 

In order for developers to receive notice of important events in Eclipse SmartHome they are exposed using the Server Sent Events(SSE) standard.
To subscribe to events a developer can listen to **/services/events** (or **/rest/events** by default. The base context path can be changed using OSGi configuration admin for the service.pid com.eclipsesource.jaxrs.connector and the property root).
In general any SSE Consumer API can be used(for example Jersey's https://jersey.java.net/documentation/latest/sse.html#d0e10392 or HTML5 EventSource Object) to read from the event stream.

## Events 
Currently the following events are available:

- smarthome/items/added - an item was added to the item registry;
- smarthome/items/removed - an item was removed from the item registry;
- smarthome/items/updated - an item was updated in the item registry;
- smarthome/things/added - a thing was added to the thing registry;
- smarthome/things/removed - a thing was removed from the thing registry;
- smarthome/things/updated - a thing was updated in the thing registry;
- smarthome/inbox/added - a discovery result was added to the inbox;
- smarthome/inbox/removed - a discovery result was removed from the inbox;
- smarthome/inbox/updated - a discovery result was updated in the inbox;
- smarthome/update - an item update event was sent;
- smarthome/command - an item command event was sent.

All events are represented as JSON objects on the stream with the following format:
```json
	{
		"eventType": "smarthome/inbox/added/12811438",
		"eventObject": [{
			"flag": "NEW",
			"label": "Yahoo weather Sevlievo, Bulgaria",
			"properties": {
				"location": "12811438"
			},
			"thingUID": "yahooweather:weather:12811438"
		}]
	}
```

where 
- eventType - contains both the event and an identifier of the object the event is about(if any).
- eventObject - array of objects that are relevant to the event. For example the *smarthome/items/added* event will include the new item that was added and the *smarthome/items/updated* event will include both old and new items.
  
## Filtering

By default when listening to **/services/events** a developer will receive all events that are currently broadcasted. In order to listen for specific events the **topics** query parameter can be used.

For example while listening to **/services/events?topics=smarthome/items/added** a developer would receive notifications only about items that were added to the item registry.

The **topics** query parameter allows for multiple filters to be specified using a comma(,) for a separator - **?topics=smarthome/items/added, smarthome/things/added**.

In addition to that every filter may contain a wildcard character(\*) replacing one(or all) of the filter components - **?topics=smarthome/items/\*, smarthome/things/\* **

## Example

An example of listing events using the HTML5 EventSource object is provided below:

```js
	eventSrc = new EventSource("/services/events?topics=smarthome/*/added,smarthome/inbox/removed");	

	eventSrc.addEventListener('message', function (event) {
		console.log(event.type);
		console.log(event.data);		
	});
```







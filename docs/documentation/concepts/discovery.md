---
layout: documentation
---

{% include base.html %}

# Thing Discovery and Inbox

Many technologies and systems can be automatically discovered on the network or browsed through some API. It therefore makes a lot of sense to use these features for a smart home solution.

In Eclipse SmartHome bindings can therefore implement _Discovery Services_ for things. As a solution might not want to make everything that is found on the network immediately available to the user and his applications, all _Discovery Results_ are regarded as suggestions that are first put into an _inbox_.

## Glossary

- _Discovery_: Search for available things in the smart home environment. 
- _Discovery Result_: Result of a _Discovery_ stored in the _Inbox_. 
- _Discovery Service_: Implements a service to discover things, typically based on a specialized protocol (e.g. UPnP). 
- _Inbox_: List of all discovered things, constantly updated by all running discoveries. 

## Inbox

The inbox represents a list of all discovered things (`DiscoveryResult`) from all known discovery services. Bindings can register new discovery services to discover new thing types (e.g. the Hue binding registers a new discovery service to search for Hue lights). Technically the inbox is an OSGi service which manages the discovery results. Notification about new things added to or things removed from the inbox will be sent as events. 

### Discovery Result 

A discovery result represents a discovered thing of a specific thing type, that could be instantiated as things in Eclipse SmartHome. The result usually contains properties that identify the discovered things further like IP address or a serial number. Each discovery result also has a timestamp when it was added to or updated in the inbox and it may also contain a time to live, indicating the time after which it will be automatically removed from the inbox. 

The following table gives an overview about the main parts of a `DiscoveryResult`: 

| Field | Description |
|-------|-------------|
| `thingUID` | The `thingUID` is the unique identifier of the specific discovered thing (e.g. a device's serial number). It  *must not* be constructed out of properties, that can change like IP addresses. A typical `thingUID` could look like this: `hue:bridge:001788141f1a` 
| `thingTypeUID` | Contrary to the `thingUID` is the `thingTypeUID` that specifies the type the discovered thing belongs to. It could be constructed from e.g. a product number. A typical `thingTypeUID` could be the following: `hue:bridge`. 
| `bridgeUID` | If the discovered thing belongs to a bridge, the `bridgeUID` contains the UID of that bridge. 
| `properties` | The `properties` of a `DiscoveryResult` contain the configuration for the newly created thing. 

Discovery results can either be ignored or approved, which means that a thing is created for them and they become available in the application. The configuration of that created thing contains the values from the `properties`of the approved `DiscoveryResult`. If an entry is ignored, it will be hidden in the inbox without creating a thing for it. 

### Active Scan vs. Background Discovery

There are different ways how a thing can be discovered:

- In protocols like UPnP or Bonjour/mDNS devices send announcements on the network that can be listened to. In Eclipse SmartHome we refer to such mechanisms as "background discovery", i.e. passive mechanisms where events come in and can be processed. Things can be therefore found any time and put into the inbox.

- There might be an API, which can be accessed to actively query all available things. In Eclipse SmartHome, this is called an "active scan" and thus configuration UIs must provide a way to trigger such a scan for a certain thing type. In general, it is not recommended to do any active discovery by the binding in the background as it can negatively impact the system performance. The only exception is that a scan can be triggered once at startup and if a bridge has been added, so that its attached things are directly discovered.

## Available Discovery Services

Eclipse SmartHome already comes with some discovery services. These are: 

- `UPnPDiscoveryService`: This discovery service discovers all IP devices using the UPnP protocol. The bindings must implement a `UpnpDiscoveryParticipant` to support this discovery service. The [UPnP discovery service documentation](../development/bindings/discovery-services.html#upnp-discovery) explains in detail, how to do that. 
- `MDNSDiscoveryService`: All devices supporting the mDNS protocol are discovered by this service. 

Bindings implement more discovery services, e.g. the search for Hue lights in the Hue binding or the search for the local weather in the Yahoo weather binding. 

The [Implement Discovery Service](../development/bindings/discovery-services.html) chapter describes how to implement DiscoveryServices in a binding.

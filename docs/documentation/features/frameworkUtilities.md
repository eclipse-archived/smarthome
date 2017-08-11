---
layout: documentation
---

# Framework Utilities

In this chapter useful services/utilities of the Eclipse SmartHome project are described. 

## Network Address Service

A user can configure his default network address via Paper UI under `Configuration -> System -> Network Settings`.
The `NetworkAddressService` is an OSGi service that can be used like any other OSGi service by adding a service reference to it.
One can obtain the configured address via the `getPrimaryIpv4HostAddress()` method on the service.
This service is useful for example in the `ThingHandlerFactory` or an `AudioSink` where one needs a specific IP address of the host system to provide something like a `callback` URL.
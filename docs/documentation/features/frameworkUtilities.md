---
layout: documentation
---

# Provided Services

In this chapter useful services of the Eclipse SmartHome project are described. 

## Network Address Service

A user can configure his default network address via Paper UI under `Configuration -> System -> Network Settings`.
To obtain this configured address the `ThingHandlerFactory` needs a `service reference` to the `NetworkAddressService` in its `MyHandlerFactory.java`:

```java
@Reference
protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
	this.networkAddressService = networkAddressService;
	}
protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
	this.networkAddressService = null;
}
```

Now the `MyHandlerFactory` can obtain the configured IP address via `networkAddressService.getPrimaryIpv4HostAddress()`.
This IP address can for example be used for things (i.e. AudioSinks) that require a callback URL which they can offer to a device.
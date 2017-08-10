---
layout: documentation
---

# Provided Services

In this chapter useful services of the Eclipse SmartHome project are described. 

## Obtaining the default IP address

Some things might require a `callback` URL which should be bound to a certain network interface. A user can configure his default network address via Paper UI under `Configuration -> System -> Network Settings`. To obtain this configured address the `ThingHandlerFactory` needs a `service reference` to the `NetworkInterfaceService` in its `MyHandlerFactory.java`:

```java
@Reference
protected void setNetworkAddressprovider(NetworkAddressprovider networkAddressProvider) {
	this.networkAddressProvider = networkAddressProvider;
	}
protected void unsetNetworkAddressprovider(NetworkAddressprovider networkAddressProvider) {
	this.networkAddressProvider = null;
}
```

Now the `MyHandlerFactory` can obtain the configured IP address via `networkAddressProvider.getPrimaryIpv4HostAddress()`. This IP address can for example be used in callback URLs offered to a device.
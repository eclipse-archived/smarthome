---
layout: documentation
---

# Framework Utilities

In this chapter useful services/utilities of the Eclipse SmartHome project are described. 

## Network Server TLS service

This service is responsible for providing SSLContexts that can be used all over ESH
for server applications like webservlets, Mqtt servers or any extension that need to
host a service. It can provide a SSLContext for TCP/TLS usage,
as well as Connector for UDP/DTLS use cases.

The SSLContext is configured to use either the user provided java keystore file
or if none is provided to generate a self-signed certificate and public/private key-pair.

The service can be asked to return different SSLContext or Connector objects based on a given
context string. The default context will be returned if `null` is passed as context or if the
given context is not configured.

If nothing is configured (first-start), the service will generate a self-signed certificate and public/private key-pair in `ESH_DIR/etc/default.keystore` and store a new configuration file in `ESH_DIR/etc/networkServerTlsProvider.cfg`, including the keystore access password and private key password.

You can provide your own java keystore files in `ESH_DIR/etc/yourKeystoreFile.keystore`. The name of the file is automatically the context name, e.g. you would request a `SSLContext` like this: `createSSLContext("yourKeystoreFile")`.
Don't forget to add the configuration for your context to the configuration file as explained in the next section.

The service can use ACME to automatically sign a public key by the Let's Encrypt initiative. 

### Configuration

The service accepts a configuration file `networkServerTlsProvider.cfg` with the following parameters:

For generating a self-signed certificate on the first start, the following parameters will be used:

* **organizationalUnit**: "Eclipse Smarthome"
* **organization**: "Eclipse Foundation, Inc."
* **city**: "Ottawa"
* **state**: "Ontario"
* **country**: for instance "US", the java environment will be asked for the current country code.
* **validityInDays**: 365 * 3

The following algorithm specific parameters can be set:

* **keysizeTLS**: 2048
* **keysizeDTLS**: 256
* **algorithmTLS**: RSA
* **algorithmDTLS**: EC
* **algorithmSigningTLS**: SHA256WithRSA
* **algorithmSigningDTLS**: SHA256withECDSA
* **algorithmKeyManager**: PKIX
* **sslContextProtocol**: TLSv1.2

And those ACME configurations are available:

* **useACME**: Default is false. ACME is used to sign generated public/private keys. This is not yet supported.
* **acmeProvider**: acme://letsencrypt.org/staging
* **acmeContact**: mailto:acme@example.com

As mentioned above, multiple contexts can be defined. The most important one is the default context, as this is used
throughout the framework for all kind of secure server services. A context will be defined like this:

```
[context]
domain=www.abc.org
preSharedKeyBase64=i0xLjUK
keystorePasswordBase64=JVBER
privateKeyPasswordBase64=i0xLjUK
autoRefreshInvalidCertificate=true
```

* **domain**: for instance "www.abc.org"
* **preSharedKeyBase64**: for instance "i0xLjUK" *¹
* **keystorePasswordBase64**: for instance "JVBER" *¹
* **privateKeyPasswordBase64**: for instance "i0xLjUK" *¹
* **autoRefreshInvalidCertificate**: Automatically create a new self-signed certificate after the current one for this context expired. Please be aware that your clients may need to be deployed with the new public key. Default is true for created self-signed certificates and false otherwise.

   *1: Base64 encoded password. Your password can contain any byte values and must be Base64 encoded to make it human read- and writable in the editor. You can create a base64 encoded string of your password on the command line of Linux/MacOS with `echo "abcdef" |base64` with _abcdef_ being your password.
  
Please be aware that if no public/private key and certificate can be found for the default context, the
service will create a configuration file or overwrite an existing one after creating the public/private key and certificate file.

### Usage examples

The most basic implementation for TCP/TLS would be:

```
public ServerSocket createServerSocket() {
   SSLContext s = provider.createSSLContext(null);
   ServerSocket serverAcceptSocket = s.getServerSocketFactory().createServerSocket();
   serverAcceptSocket.bind(new InetSocketAddress(0));
   return serverAcceptSocket;
}
```

If you want to react to updated contexts of the service, an implementation
could look like this:

```
class YourClass implements TLSContextChangedListener {
   final static String YOUR_CONTEXT = "default";
   @Override
   void tlsContextChanged(String context) {
      if (YOUR_CONTEXT.equals(context)) {
         restartYourService(createServerSocket());
      }
   }
}
```


### How to create a self-signed certificate with OpenSSL

1) Generate key with AES256

`openssl genrsa -aes256 -out server.key 2048`

2) Generate cert request for CA

`openssl req -x509 -sha256 -new -key server.key -out server.csr`

3) Generate self signed certificate with expiry-time of 3 years

`openssl x509 -sha256 -days 1095 -in server.csr -signkey server.key -out selfsigned.crt`

4) Create PKCS12 keystore from private key and certificate.

`openssl pkcs12 -export -name myservercert -in selfsigned.crt -inkey server.key -out keystore.p12`

5) Convert PKCS12 keystore into a JKS keystore

`keytool -importkeystore -destkeystore default.keystore -srckeystore keystore.p12 -srcstoretype pkcs12 -alias myservercert`


## Network Address Service

The `NetworkAddressService` is an OSGi service that can be used like any other OSGi service by adding a service reference to it. Its OSGi service name is `org.eclipse.smarthome.network`.
A user can configure his default network address via Paper UI under `Configuration -> System -> Network Settings`.
One can obtain the configured address via the `getPrimaryIpv4HostAddress()` method on the service.
This service is useful for example in the `ThingHandlerFactory` or an `AudioSink` where one needs a specific IP address of the host system to provide something like a `callback` URL.

Some static methods like `getAllBroadcastAddresses()` for retrieving all interface broadcast addresses or `getInterfaceAddresses()` for retrieving all assigned interface addresses might be usefull as well for discovery services.

## Caching

The framework provides some caching solutions for common scenarios.

### Simple expiring and reloading cache

A common usage case is in a `ThingHandler` to encapsulate one value of an internal state and attach an expire time on that value. A cache action will be called to refresh the value if it is expired. This is what `ExpiringCache` implements. If `handleCommand(ChannelUID channelUID, Command command)` is called with the "RefreshType" command, you just return `cache.getValue()`. 

It is a good practice to return as fast as possible from the `handleCommand(ChannelUID channelUID, Command command)` method to not block callers especially UIs.
Use this type of cache only, if your refresh action is a quick to compute, blocking operation. If you deal with network calls, consider the asynchronously reloading cache implementation instead.

### Expiring and asynchronously reloading cache

If we refreshed a value of the internal state in a `ThingHandler` just recently, we can return it immediately via the usual `updateState(channel, state)` method in response to a "RefreshType" command.
If the state is too old, we need to fetch it first and this may involve network calls, interprocess operations or anything else that will would block for a considerable amout of time.

A common usage case of the `ExpiringCacheAsync` cache type is in a `ThingHandler` to encapsulate one value of an internal state and attach an expire time on that value.


A **handleCommand** implementation with the interesting *RefreshType* could look like this:
```
public void handleCommand(ChannelUID channelUID, Command command) {
    if (command instanceof RefreshType) {
        switch (channelUID.getId()) {
            case CHANNEL_1:
                cache1.getValue(updater).thenAccept(value -> updateState(CHANNEL_1, value));
                break;
            ...
        }
    }
}
```

The interesting part is the `updater`. If the value is not yet expired, the returned CompletableFuture will complete immediately and the given code is executed.
If the value is expired, the updater will be used to request a refreshed value.

An updater can be any class or lambda that implements the funtional interface of `Supplier<CompletableFuture<VALUE_TYPE>>`.

In the following example the method `CompletableFuture<VALUE_TYPE> get()` is accordingly implemented. The example assumes that we deal
with a still very common callback based device refreshing method `doSuperImportantAsyncStuffHereToGetRefreshedValue(listener)`. The listener is the class
itself, which implements `DeviceStateUpdateListener`. We will be called back with a refreshed device state in `asyncCallbackFromDeviceStateRefresh`
and mark the Future as *complete*.

```
class FetchValueFromDevice implements Supplier<CompletableFuture<double>>, DeviceStateUpdateListener {
    CompletableFuture<double> c;
    
    @Override
    CompletableFuture<double> get() {
       if (c != null) {
          c = new CompletableFuture<double>();
          doSuperImportantAsyncStuffHereToGetRefreshedValue( (DeviceStateUpdateListener)this );
       }
       return c;
    }
    
    // Here you process the callback from your device refresh method
    @Override
    void asyncCallbackFromDeviceStateRefresh(double newValue) {
       // Notify the future that we have something
       if (c != null) {
          c.complete(newValue);
          c = null;
       }
    }
}
```
If you deal with a newer implementation with a CompletableFuture support, it is even easier. You would just return your CompletableFuture.

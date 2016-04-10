---
layout: documentation
---

{% include base.html %}

# Linux Bluetooth Transport

This bundle implements the transport for Linux Bluetooth API, using the BlueZ driver (the standard Bluetooth driver provided with most Linux distributions).


# Configuration

This bundle requires some configuration, and has some dependencies.

## Dependencies

1. BlueZ 5. A recent version of the BlueZ bluetooth layer is required - a minimum requirement of version 5. To check what version you have running, run `dpkg -s bluez`
2. The `org.eclipse.smarthome.io.transport.bluetooth` bundle
3. The `org.eclipse.smarthome.io.transport.dbus` bundle

## Setup

As of BlueZ version 5, GATT is considered experimental and therefore BlueZ needs to be run in experimental mode. This requires the -E option to be added to the command line options for bluetoothd. On Debian Linux, this is configured in the `/etc/init.d/bluetooth` file by setting `$SSD_OPTIONS -E`

## DBus Configuration

DBus is used for the communication between applications within Linux. It can be configured in a number of ways.

### Quick Configuration

The easiest way is to use the default configuration. This requires `libunix-java` to be available on the computer. On Debian, this can be installed with `apt-get install libunixsocket-java`. You may need to locate the `libunix-java.so` file and either ensure it is in the path, or move it to the SmartHome folder. In Debian, it is located in `/usr/lib/jni`.

The limitation of this configuration is that JNI is used and this has security implications.

### Alternative Configuration

Alternatively, DBus can be configured to use TCP, and avoid the additional libraries mentioned above, and also avoid the use of JNI. Configuration is however more complex, but can be achieved with the following steps. Note that IP addresses and port numbers must be configured for your system and can use `localhost` or a specific IP address. The file locations are for Debian and may be different for other systems.

Edit the file `/etc/dbus-1/system.conf`. Locate the following line `<listen>unix:path=/var/run/dbus/system_bus_socket</listen>` (note that different systems may configure this slightly differently).

Add the following line **above** this line...

    <listen>tcp:host=localhost,bind=*,port=6666,family=ipv4</listen>
    
and the following three lines **below** this line...

    <auth>ANONYMOUS</auth>
    <allow_anonymous/>
    <apparmor mode="disabled"/>
    
Edit the file `/lib/systemd/system/dbus.socket` and add `ListenStream=6666` to the `[Socket]` section.

Edit the file `/etc/dbus-1/system.d/bluetooth.conf`. At the bottom of this file you will find a line `<deny send_destination="org.bluez"/>` - change `deny` to `allow`.
    
In the Eclipse SmartHome startup script, add the following command line argument `-Dsmarthome.bluetooth.bluez.dbus="tcp:host=localhost,port=6666"`.


    
Functional and technical descriptions:

- The OSGi EnOcean Base Driver enables applications running on an OSGi software platform to discover and control EnOcean devices. EnOcean is a radio protocol used in Smart Home and Building markets. OSGi is an execution environment above Java virtual machine to run modular applications.

- The OSGi EnOcean Base Driver implements OSGi standard specification entitled EnOcean Device Service. OSGi EnOcean Device Service specification defines the Java API to discover and control EnOcean devices on the OSGi platform and according to OSGi service design patterns. This API maps the representation model of EnOcean entities defined by EnOcean Equipment Profiles standard into Java classes. OSGi service design patterns are used on the one hand for dynamic discovery, control and eventing of local and networked devices and on the other hand for dynamic network advertising and control of local OSGi services.

- The EnOcean Base Driver is entirely written in Java and uses serial communication drivers usually available on common OS, e.g., Linux. To work properly, the software needs to be installed on an OSGi platform R4 v4.0 (and above) running on a JVM 1.4 (and above) with RXTX Java library. It runs above Linux systems connected to an EnOcean TCM300 USB Gateway delivered as an internal chip or a USB dongle.


Tree
----

    - org.eclipse.smarthome.protocols.enocean.eeps.basic
                                       ; This bundle is an OSGi EnOcean bundle that contained the
                                       ; three EEPs (i.e. A5-02-05, D5-00-01, and F6-02-01) that 
                                       ; the EnOcean Alliance allowed the project to use.
 
    - org.eclipse.smarthome.protocols.enocean.basedriver.impl
                                       ; This bundle is the EnOcean Base Driver itself. This 
                                       ; version contains an EnOceanHost Implementation that 
                                       ; is able to communicate over the serial port.
                                       ; see https://github.com/osgi/design/tree/master/rfcs/rfc0199
 
    - org.eclipse.smarthome.protocols.enocean.sample.client
                                       ; This is a very simple sample application for using the 
                                       ; EnOcean base driver. It subscribes to both service and 
                                       ; event notifications regarding EnOcean events on OSGi.
 
    - maven_dependencies               ; Contains, among others, the OSGi RFC199 API bundle 
                                       ; (org.osgi.service.enocean-1.0.0.....jar) needed to 
                                       ; compile, and execute the project.
                                       ; see https://github.com/osgi/design/tree/master/rfcs/rfc0199
                                       ; for the related RFC199 document.
                                       ; Also contains driver-rx-tx.jar needed to compile, 
                                       ; and execute the project. It's a generic interface 
                                       ; bundle to the Serial port.
                                       ; see https://github.com/Orange-OpenSource/Driver-RX-TX
										 
    - README.md


Authors
-------
- Victor PERRON
- Mailys ROBIN
- Andre BOTTARO
- Antonin CHAZALET


Quick Start
-----------
* Get the code (e.g. in Eclipse).
* Compile it (via bnd).
* Get Apache Felix (e.g. version 4.4.1).
* Add the following bundles in the "bundle" folder of Felix:


```
#!shell
g! lb
START LEVEL 1
   ID|State      |Level|Name
    0|Active     |    0|System Bundle (4.4.1)
    1|Active     |    1|org.eclipse.smarthome.protocols.enocean.eeps.basic (0.8.0)
    2|Active     |    1|org.eclipse.smarthome.protocols.enocean.basedriver.impl (0.8.0)
    3|Active     |    1|org.eclipse.smarthome.protocols.enocean.sample.client (0.8.0)
    4|Active     |    1|Apache Felix Bundle Repository (2.0.2)
    5|Active     |    1|Apache Felix EventAdmin (1.3.2)
    6|Active     |    1|Apache Felix Gogo Command (0.14.0)
    7|Active     |    1|Apache Felix Gogo Runtime (0.12.1)
    8|Active     |    1|Apache Felix Gogo Shell (0.10.0)
    9|Active     |    1|org.osgi:org.osgi.service.enocean (1.0.0.201501061602)
   10|Active     |    1|osgi.cmpn (4.2.0.200908310645)
   11|Active     |    1|driver-rx-tx (0.0.0) - driver-rx-tx.jar

```
* Start Felix.


Note
----
Using the example app/bundle (org.eclipse.smarthome.protocols.enocean.sample.client) requires to 
put the following lines in the enocean_config.txt configuration file of ```org.eclipse.smarthome.
protocols.enocean.basedriver.impl```:

```
0x018c0874_RORG_FUNC_TYPE_FRIENDLYNAME_DESCRIPTION=F6##99##99##Eltako Smoke Detector##(F6)
0x01819912_RORG_FUNC_TYPE_FRIENDLYNAME_DESCRIPTION=F6##05##01##AfrisoLab Water Sensor##Liquid Leakage Sensor (mechanic energy harvester) (F6-05-01)
#0x11111111_RORG_FUNC_TYPE_FRIENDLYNAME_DESCRIPTION=D2##01##08##D2-01-08##Electronic switches and dimmers with Energy Measurement and Local Control
0x00851656_RORG_FUNC_TYPE_FRIENDLYNAME_DESCRIPTION=D2##A0##01##AfrisoLab Water Valve##Standard Control (BI-DIR) (D2-A0-01)
```

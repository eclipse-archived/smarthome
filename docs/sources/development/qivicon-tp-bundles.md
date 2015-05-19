## Overview
Since the [QIVICON](https://www.qivicon.com) project takes advantage of the core functionalities provided by Eclipse SmartHome, a [binding project](../howtos/bindings.md) developed for Eclipse SmartHome could also be compiled for QIVICON and run on the QIVICON runtime.  
Nevertheless, not all of the packages which are available during the compilation of an Eclipse SmartHome binding are present in the QIVICON target platform. 
Importing such packages may prevent your Eclipse SmartHome binding to compile with the QIVICON target platform.
  
Below you can find a list containing all of the bundles which are also available in QIVICON target platform.

## Bundles provided by Eclipse SmartHome

-  org.eclipse.smarthome.io.console
-  org.eclipse.smarthome.core.library
-  org.eclipse.smarthome.core.persistence
-  org.eclipse.smarthome.config.core
-  org.eclipse.smarthome.core.thing
-  org.eclipse.smarthome.core
-  org.eclipse.smarthome.core.autoupdate
-  org.eclipse.smarthome.config.discovery

## Bundles provided by Eclipse SmartHome target platform

-  com.google.inject
-  com.google.inject.source
-  com.google.guava
-  com.google.guava.source
-  com.google.gson
-  com.google.gson.source
-  com.ibm.icu.base
-  com.ibm.icu.base.source
-  com.sun.jersey
-  com.sun.jersey.source
-  javax.persistence
-  javax.persistence.source
-  javax.xml.stream
-  javax.xml
-  javax.annotation
-  javax.inject
-  javax.transaction
-  javax.servlet
-  javax.servlet.source
-  javax.activation
-  javax.activation.source
-  javax.ws.rs
-  javax.ws.rs.source
-  javax.xml
-  javax.xml.bind
-  org.apache.commons.exec
-  org.apache.commons.exec.source
-  org.apache.commons.codec
-  org.apache.commons.codec.source
-  org.apache.commons.collections.source
-  org.apache.commons.collections
-  org.apache.commons.httpclient
-  org.apache.commons.httpclient.source
-  org.apache.commons.io
-  org.apache.commons.io.source
-  org.apache.commons.net
-  org.apache.commons.net.source
-  org.apache.commons.lang.source
-  org.apache.commons.lang
-  org.apache.httpcomponents.httpclient
-  org.apache.httpcomponents.httpcore
-  org.apache.httpcomponents.httpcore.nio
-  org.codehaus.jackson.core
-  org.codehaus.jackson.core.source
-  org.codehaus.jackson.jaxrs
-  org.codehaus.jackson.jaxrs.source
-  org.codehaus.jackson.xc
-  org.codehaus.jackson.xc.source
-  org.codehaus.jackson.mapper
-  org.codehaus.jackson.mapper.source
-  org.hamcrest.core
-  org.hamcrest.core.source
-  org.mockito
-  org.mockito.source
-  org.objectweb.asm
-  org.objectweb.asm.source
-  ch.qos.logback.classic
-  ch.qos.logback.classic.source
-  ch.qos.logback.core
-  ch.qos.logback.core.source
-  ch.qos.logback.slf4j
-  ch.qos.logback.slf4j.source
-  org.objenesis
-  org.objenesis.source
-  org.slf4j.api
-  org.slf4j.ext
-  org.slf4j.ext.source
-  org.slf4j.api.source
-  org.slf4j.jul
-  org.slf4j.jul.source
-  org.slf4j.jcl
-  org.slf4j.jcl.source
-  org.slf4j.log4j
-  org.slf4j.log4j.source
-  org.apache.felix.gogo.runtime
-  org.eclipse.equinox.sdk.feature.group
-  org.jupnp.feature.feature.group
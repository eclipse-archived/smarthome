---
layout: documentation
---

{% include base.html %}

# Feature Overview

Eclipse SmartHome is a framework for building smart home solutions. With its very flexible architecture, it fosters the modularity provided by OSGi for Java applications.
As such, Eclipse SmartHome consists of a rich set of OSGi bundles that serve different purposes. Not all solutions that build on top of Eclipse SmartHome will require all of those bundles - instead they can choose what parts are interesting for them.

There are the following categories of bundles:

 - `config`: everything that is concerned with general configuration of the system like config files, xml parsing, discovery, etc.	
 - `core`: the main bundles for the logical operation of the system - based on the abstract item and event concepts.
 - `io`: all kinds of optional functionality that have to do with i/o like console commands, audio support or http/rest communication
 - `model`: support for domain specific languages (DSLs) 
 - `designer`: Eclipse RCP support for DSLs and other configuration files
 - `ui`: user interface related bundles that provide services that can be used by different UIs, such as charting or icons

## Runtime Services

### Optional Bundles

 - `org.eclipse.smarthome.core.id`: [Unique instance IDs](core/id.html)
 - `org.eclipse.smarthome.ui.icon`: [Icon support](ui/icons.html)

Besides the very core framework that is mandatory for all solutions, there are many optional features like the support for textual configurations (DSLs), the REST API or the sitemap support.

## Extensions

Being a framework, Eclipse SmartHome defines many extension types that allows building modular solutions with pluggable components (extensions). 

The list of extension types will grow over time and you are invited to discuss useful extension types in [our forum](https://www.eclipse.org/forums/eclipse.smarthome).

Note that many "existing" extension types like rule actions, persistence services, TTS modules, etc. are not covered in this documentation as it is planned to address and heavily refactor them in future - the current version is still from the initial contribution which came from openHAB 1 and thus is tight to textual configuration and not usable in a wider context.

### Bindings

A binding is an extension to the Eclipse SmartHome runtime that integrates an external system like a service, a protocol or a single device. Therefore the main purpose of a binding is to translate events from the Eclipse SmartHome event bus to the external system and vice versa. Learn about the internals of a binding in our [binding tutorial](../development/bindings/how-to.html).

Bindings can optionally include [discovery services](../concepts/discovery.html), which allow the system to automatically find accessible devices and services.

### User Interfaces

User interfaces normally use the REST API for communication, but if they are not client-side, but served from the runtime, they also have the option to use all local Java services.

Currently, the only available user interface in Eclipse SmartHome is the Classic UI, but further UIs will be available soon.

All user interfaces can share icon sets, so that these do not have to be included in every single user interface.
Eclipse SmartHome comes with the following iconsets:

 - `org.eclipse.smarthome.ui.iconset.classic`: [Classic Icon Set](ui/iconset/classic/readme.html)

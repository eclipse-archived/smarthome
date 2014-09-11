# Extensions

Being a framework, Eclipse SmartHome defines many extension types that allows building modular solutions with pluggable components (extensions). 

The list of extension types will grow over time and you are invited to discuss useful extension types in [our forum](http://eclipse.org/forums/eclipse.smarthome).

## Bindings

A binding is an extension to the Eclipse SmartHome runtime that integrates an external system like a service, a protocol or a single device. Therefore the main purpose of a binding is to translate events from the Eclipse SmartHome event bus to the external system and vice versa. Learn about the internals of a binding in our [binding tutorial](../howtos/bindings.md).

Bindings can optionally include [discovery services](discovery.md), which allow the system to automatically find accessible devices and services.

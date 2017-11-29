---
layout: post
title: "Less NPE's - We now use Eclipse Nullannotations!"
date:   2017-11-30
image: "2017-11-30-nullannotations.png"
published: true
---

Nullpointer Exceptions (aka NPE's) are pretty nasty since they are `RuntimeException`s and thus developers do not immediately notice them while coding in their IDE.
Hence, data-flow analysis where written that show potential accesses to nullpointers before they occur at runtime. Our project decided to make use of these ananlyses and get rid of (some) nullpointers.

<!--more-->

Two popular projects that offer such an analysis are the [Checker Framework](https://checkerframework.org) and the [Eclipse JDT compiler](https://wiki.eclipse.org/JDT_Core/Null_Analysis).
In order to obtain better results from these anylyses, developers can make use of `annotations` in the code.
In the current snapshots and in our next release we support such annotations in our provided Eclipse IDE setup and maven configuration.
We decided to use the implementation offered by the Eclipse JDT compiler.

#### Making JavaDoc comments explicit

Since we are a framework that will be integrated into solutions which are build by others, we have provided proper Javadoc documentation on our public interfaces in the past.
In these Javadoc comments we state that a method will never return `null`, a parameter should never be `null` or is optional and thus maybe `null`.
Maintaining these comments by hand is error-prone and subject to be ignored by developers not reading them.
Therefore we have decided to provide these comments in a machine readable way so the IDE can support developers in showing them errors or warnings in case they violate them.

As described in our [Coding Guidelines](https://www.eclipse.org/smarthome/documentation/development/guidelines.html#a-code-style) we annotate classes with the `@NonNullByDefault` annotations which declares that every field, return value, parameter, etc. defined in this class will always have a value unequal to `null`.
If a specific field, return value, or parameter in the class should be allowed to become `null` we annotate them with `@Nullable`.
Basically what these annotations are doing for us is translating the contract written in the Javadoc comment into something that can automatically be checked by the compiler.

We have already started to annotate our core packages so binding developers can benefit from them.

### Real life examples

One simple example is the [ThingHandler:handleCommand(ChannelUID channelUID, Command command)](https://github.com/eclipse/smarthome/blob/master/bundles/core/org.eclipse.smarthome.core.thing/src/main/java/org/eclipse/smarthome/core/thing/binding/ThingHandler.java#L99) which guarantees to binding developers that both arguments are **not** `null` and so they can simply implement their logic by going through the IDs via `switch (channelUID.getId())` without running into a NPE.

{:.center}
![NonNull annotation]({{ "/img/blog/2017-11-30-nullannotations_handleCommand.png" | absolute_url }})

For bindings which use bridge things and call [BaseThingHandler:getBridge()](https://github.com/eclipse/smarthome/blob/master/bundles/core/org.eclipse.smarthome.core.thing/src/main/java/org/eclipse/smarthome/core/thing/binding/BaseThingHandler.java#L584) we now have the `@Nullable` annotation in place that reminds developers to do a proper `null` check before using the return value of this method.
If you omit the `null` check, developers will get a warning as shown below.

{:.center}
![Nullable annotation with warning]({{ "/img/blog/2017-11-30-nullannotations_bridgeNullable.png" | absolute_url }})

After the `null` check has been implemented the warning is gone:

{:.center}
![Nullable annotation without warning]({{ "/img/blog/2017-11-30-nullannotations_bridgeNullableCorrected.png" | absolute_url }})

If developers violate the `null` specifications, the IDE will immediately show an error.
In the example below the `BaseThingHandler:updateState(String channelID, State state)` method requires both parameters to be unequal to `null`.
But since in the `else` branch the variable used as a second parameter in this method call is set to `null`, the IDE infers that `null` is passed as an argument to the method and shows an error.

{:.center}
![Null violation]({{ "/img/blog/2017-11-30-nullannotations_error.png" | absolute_url }})

If you are interested to see a fully annotated binding, please have a look at the [Philips Hue binding](https://github.com/eclipse/smarthome/tree/master/extensions/binding/org.eclipse.smarthome.binding.hue).


### Outlook: Third party code

If we make use of external libraries and use their return values in our code the compiler cannot decide whether these values are always unequal to `null` or might become `null` at some point.
This will generate a warning that is currently switched off by our setup.
In the future we are planing to support *external annotations* which are files that contain information about the method signatures of external libraries.
The [Last NPE project](http://last-npe.org) has started a crowd-sourcing initative to create these external annotations and we are planing to contribute our work to this project.

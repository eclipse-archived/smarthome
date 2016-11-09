---
layout: documentation
---

{% include base.html %}

# Thing Discovery

Many devices, technologies and systems can be automatically discovered on the network or browsed through some API. It therefore makes a lot of sense to use these features for a smart home solution.

In Eclipse SmartHome bindings therefore implement _Discovery Services_ for things, which provide _Discovery Results_. All _Discovery Results_ are regarded as suggestions to the user and are put into the _inbox_.

## Inbox

The inbox holds a list of all discovered things (`DiscoveryResult`) from all active discovery services. A discovery result represents a discovered thing of a specific thing type, that could be instantiated as a thing. The result usually contains properties that identify the discovered things further like IP address or a serial number. Each discovery result also has a timestamp when it was added to or updated in the inbox and it may also contain a time to live, indicating the time after which it is be automatically removed from the inbox. 

Discovery results can either be ignored or approved, where in the latter case a thing is created for them and they become available in the application. If an entry is ignored, it will be hidden in the inbox without creating a thing for it. 

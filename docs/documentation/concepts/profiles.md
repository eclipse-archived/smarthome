---
layout: documentation
---

# Profiles

The communication between the framework and the thing handlers is managed by the "Communication Manager", which in turn uses "Profiles"  to determined what exactly needs to be done. This provides some flexibility to influence these communication paths.

By their nature, profiles are correlated to links between items and channels (i.e. `ItemChannelLinks`). So if one channel is linked to several items it also will have several profile instances, each handling the communication to exactly one of these items. The same applies for the situation where one item is linked to multiple channels. 

Profiles are created by ProfileFactories and are retained for the lifetime of their link. This means that they are allowed to retain a transient state, like e.g. the timestamp of the the last event or the last state. With this, it is possible to take into account the temporal dimension when calculating the appropriate action in any situation.

There exist two different kinds of profiles: state and trigger profiles.

### State Profiles

State profiles are responsible for communication between items and their corresponding state channels (`ChannelKind.STATE`). Their purpose is to forward state updates and commands to and from the thing handlers.

### Trigger Profiles

Trigger channels (`ChannelKind.TRIGGER`) by themselves do not maintain a state (as by their nature they only fire events). With the help of trigger profiles they can be linked to items anyway. Hence the main purpose of a trigger profile is to calculate a state based on the fired events. This state then is forwarded to the linked item by sending `ItemStateEvents`. 

Trigger profiles are powerful means to implement some immediate, straight-forward logic without the need to write any rules. 

Apart from that, they do not pass any commands or state updates to and from the thing handler as by their nature trigger channels are not capable of handling these.

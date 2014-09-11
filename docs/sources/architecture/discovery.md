# Thing Discovery and Inbox

Many technologies and systems can be automatically discovered on the network or browsed through some API. It therefore makes a lot of sense to use these features for a smart home solution.

In Eclipse SmartHome bindings can therefore implement discovery services for things. As a solution might not want to make everything that is found on the network immediately available to the user and his applications, all discovery results are regarded as suggestions that are first put into an "inbox".

Inbox entries can then be either ignored or approved, which means that a thing is created for them and they become available in the application.

There are different ways how a thing discovery can be performed:
 - In protocols like UPnP or Bonjour/mDNS devices send announcements on the network that can be listened to. In Eclipse SmartHome we refer to such mechanisms as "background discovery", i.e. passive mechanisms where events come in and can be processed. Things can be therefore found any time and put into the inbox.
 - There might be an API, which can be accessed to actively query all available things. In Eclipse SmartHome, this is called an "active scan" and thus configuration UIs must provide a way to trigger such a scan for a certain thing type. In general, it is not recommended to do any active discovery by the binding in the background as it can negatively impact the system performance. The only exception is that a scan can be triggered once at startup and if a bridge has been added, so that its attached things are directly discovered.

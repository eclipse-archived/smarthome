---
layout: documentation
---

{% include base.html %}

# NTP Binding
 
The NTP binding is used for displaying the local date and time based update from an NTP server.
 
## Supported Things
 
This binding supports one ThingType: ntp
 
## Discovery
 
Discovery is used to place one default item in the inbox as a convenient way to add a Thing for the local time.
 
## Binding Configuration
 
The binding has no configuration options, all configuration is done at Thing level.
 
## Thing Configuration
 
The thing has a few configuration options:

| Option |  Description  |
|-----------------|--------------------------------------------------- |
| hostname | NTP host server, e.g. nl.pool.ntp.org |
| refreshInterval | Interval that new time updates are posted to the eventbus in seconds |
| refreshNtp | Number of updates between querying the NTP server (e.g. with refreshinterval = 60 (seconds) and refreshNtp = 30 the NTP server is queried each half hour. |
| timeZone | Timezone, can be left blank for using the default system one |
| locale | Locale, can be left blank for using the default system one |

 
## Channels
 
The ntp binding has two channels:
* `dateTime` which provides the data in a dateTime type
* `string` which provides the data in a string type. The string channel can be configured with the formatting of the date & time. This also allows proper representation of timezones other than the java machine default one.
See the [java documentation](http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html) for the detailed information on the formatting

 
 
## Full Example
 
Things:

```
ntp:ntp:demo  [ hostname="nl.pool.ntp.org", refreshInterval=60, refreshNtp=30 ]
```
Items:

```
DateTime Date  "Date [%1$tA, %1$td.%1$tm.%1$tY %1$tH:%1$tM]"  { channel="ntp:ntp:demo:dateTime" }

```

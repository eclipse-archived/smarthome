---
layout: documentation
---

{% include base.html %}

# Sonos Binding

This binding integrates the [Sonos Multi-Room Audio system](http://www.sonos.com).

## Supported Things

All available Sonos (playback) devices are supported by this binding. This includes the Play:1, Play:3, Play:5, Connect, Connect:Amp, Playbar, and Sub. The Bridge and Boost are not supported, but these devices do only have an auxiliary role in the Sonos network and do not have any playback capability. All supported Sonos devices are registered as an audio sink in the framework.

When being defined in a \*.things file, the specific thing types PLAY1, PLAY3, PLAY5, PLAYBAR, CONNECT and CONNECTAMP should be used.

Please note that these thing types are case sensitive (you need to define them in upper case).

## Discovery

The Sonos devices are discovered through UPnP in the local network and all devices are put in the Inbox. Beware that all Sonos devices have to be added to the local Sonos installation as described in the Sonos setup procedure, e.g. through the Sonos Controller software or smartphone app.

## Binding Configuration

The binding has the following configuration options, which can be set for "binding:sonos":

| Parameter | Name    | Description  | Required |
|-----------------|------------------------|--------------|------------ |
| opmlUrl | OPML Service URL | URL for the OPML/tunein.com service | no |
| callbackUrl | Callback URL | URL to use for playing notification sounds, e.g. http://192.168.0.2:8080 | no |

## Thing Configuration

The Sonos Thing requires the UPnP UDN (Unique Device Name) as a configuration value in order for the binding to know how to access it. All the Sonos UDN have the "RINCON_000E58D8403A0XXXX" format. Additionally, a refresh interval, used to poll the Sonos device, can be specified (in seconds)
In the thing file, this looks e.g. like
```
Thing sonos:PLAY1:1 [ udn="RINCON_000E58D8403A0XXXX", refresh=60]
```

## Channels

The devices support the following channels:

| Channel Type ID | Item Type    | Description  | Thing types |
|-----------------|------------------------|--------------|----------------- |------------- |---|
| add | String | Add a Zone Player to the group of the given Zone Player | all |
| alarm | Switch | Set the first occurring alarm either ON or OFF. Alarms first have to be defined through the Sonos Controller app | all |
| alarmproperties | String | Properties of the alarm currently running | all |
| alarmrunning | Switch | Set to ON if the alarm was triggered | all |
| control | Player       | This channel supports controlling the zoneplayer, e.g. start/stop/next/previous | all |
| coordinator | String | UDN of the coordinator for the current group | all |
| currentalbum | String | Name of the album currently playing | all |
| currentartist | String | Name of the artist currently playing | all |
| currenttitle | String | Title of the song currently playing | all |
| currenttrack | String       | This channel indicates the name of the track or radio station currently playing | all |
| currenttrackuri | String | URI of the current track | all |
| currenttransporturi | String | URI of the current AV transport | all |
| favorite | String | Play the given favorite entry. The favorite entry has to be predefined in the Sonos Controller app | all |
| led | Switch | Set or get the status of the white led on the front of the Zone Player | all |
| localcoordinator | Switch | Indicator set to ON if the this Zone Player is the Zone Group Coordinator | all |
| mute | Switch | Set or get the mute state of the master volume of the Zone Player | all |
| notificationsound | String | Play a notification sound by a given URI | all |
| notificationvolume | Dimmer | Set the volume applied to a notification sound | all |
| playlinein | String       | This channel supports playing the audio source connected to the line-in of the zoneplayer identified by the Thing UID or UPnP UDN provided by the String. | PLAY5, CONNECT, CONNECTAMP |
| playlist | String | Play the given playlist. The playlist has to predefined in the Sonos Controller app | all |
| playqueue | Switch | Play the songs from the current queue | all |
| playtrack | Number | Play the given track number from the current queue | all |
| playuri | String | Play the given URI | all |
| publicaddress | Switch | Put all Zone Players in one group, and stream audio from the line-in from the Zone Player that triggered the command | PLAY5, CONNECT, CONNECTAMP |
| radio | String | Play the given radio station. The radio station has to be predefined in the Sonos Controller app | all |
| remove | String | Remove the given Zone Player from the group of this Zone Player | all |
| repeat | String | Repeat the current track or queue. The accepted values are OFF, ONE and ALL | all |
| restore | Switch | Restore the state of the Zone Player | all |
| restoreall | Switch | Restore the state of all the Zone Players | all |
| volume | Dimmer       | This channel supports setting the master volume of the zoneplayer | all |
| save | Switch | Save the state of the Zone Player | all |
| saveall | Switch | Save the state of all the Zone Players | all |
| shuffle | Switch | Shuffle the queue playback | all |
| snooze | Switch | Snooze the running alarm, if any, with the given number of minutes | all |
| standalone | Switch | Make the Zone Player leave its Group and become a standalone Zone Player | all |
| state | String | The State channel contains state of the Zone Player, e.g. PLAYING, STOPPED,... | all |
| stop | Switch | Stop the Zone Player | all |
| zonegroup | String | XML formatted string with the current zonegroup configuration | all |
| zonegroupid | String | Id of the Zone Group the Zone Player belongs to | all |
| zonename | String | Name of the Zone Group the Zone Player belongs to | all |

## Audio Support

All supported Sonos devices are registered as an audio sink in the framework.
Audio streams are treated as notifications, i.e. they are fed into the `notificationsound` channel and changing the volume of the audio sink will change the `notificationvolume`, not the `volume`.
Note that the Sonos binding has a limit of 20 seconds for notification sounds. Any sound that is longer than that will be cut off.

URL audio streams (e.g. an Internet radio stream) are an exception and do not get sent to the `notificationsound` channel. Instead, these will be sent to the `playuri` channel.

## Full Example

demo.things:

```
Thing sonos:PLAY1:living [ udn="RINCON_000E58D8403A0XXXX", refresh=60]
```

demo.items:

```
Group Sonos <player>

Player Sonos_Controller   "Controller"                          (Sonos) {channel="sonos:PLAY1:living:control"}
Dimmer Sonos_Volume       "Volume [%.1f %%]" <soundvolume>      (Sonos) {channel="sonos:PLAY1:living:volume"}
Switch Sonos_Mute         "Mute"             <soundvolume_mute> (Sonos) {channel="sonos:PLAY1:living:mute"}
Switch Sonos_LED          "LED"              <switch>           (Sonos) {channel="sonos:PLAY1:living:led"}
String Sonos_CurrentTrack "Now playing [%s]" <text>             (Sonos) {channel="sonos:PLAY1:living:currenttrack"}
String Sonos_State        "Status [%s]"      <text>             (Sonos) {channel="sonos:PLAY1:living:state"}
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
		Frame label="Sonos" {
			Default item=Sonos_Controller
			Slider  item=Sonos_Volume
			Switch  item=Sonos_Mute
			Switch  item=Sonos_LED
			Text    item=Sonos_CurrentTrack		
			Text    item=Sonos_State
		}
}
```

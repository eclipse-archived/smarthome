/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.sonyaudio.internal.protocol;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.binding.sonyaudio.internal.SonyAudioEventListener;
import org.eclipse.smarthome.binding.sonyaudio.internal.protocol.SwitchNotifications.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link SonyAudioConnection} is responsible for communicating with SONY audio products
 * handlers.
 *
 * @author David Åberg - Initial contribution
 */
public class SonyAudioConnection implements SonyAudioClientSocketEventListener {
    private final Logger logger = LoggerFactory.getLogger(SonyAudioConnection.class);

    private String host;
    private int port;
    private String path;
    private URI base_uri;

    private SonyAudioClientSocket av_content_socket;
    private SonyAudioClientSocket audio_socket;
    private SonyAudioClientSocket system_socket;

    private SonyAudioEventListener listener;

    private int min_volume = 0;
    private int max_volume = 100;

    public SonyAudioConnection(String host, int port, String path, SonyAudioEventListener listener)
            throws MalformedURLException {
        this.host = host;
        this.port = port;
        this.path = path;
        this.listener = listener;
    }

    @Override
    public void handleEvent(JsonObject json) {
        int zone = 0;
        JsonObject param = json.get("params").getAsJsonArray().get(0).getAsJsonObject();

        if (param.has("output")) {
            String output_str = param.get("output").getAsString();
            Pattern pattern = Pattern.compile(".*zone=(\\d+)");
            Matcher m = pattern.matcher(output_str);
            if (m.matches()) {
                zone = Integer.parseInt(m.group(1));
            }
        }

        if (json.get("method").getAsString().equalsIgnoreCase("notifyPlayingContentInfo")) {
            String uri = param.get("uri").getAsString();
            if (param.has("broadcastFreq")) {
                int freq = param.get("broadcastFreq").getAsInt();
                listener.updateBroadcastFreq(freq);
                checkRadioPreset(uri);
            }
            listener.updateInputSource(zone, uri);
            listener.updateSeekStation("");
        }

        if (json.get("method").getAsString().equalsIgnoreCase("notifyVolumeInformation")) {
            int volume = param.get("volume").getAsInt();
            volume = Math.round(100 * (volume - min_volume) / (max_volume - min_volume));
            listener.updateVolume(zone, volume);

            String mute = param.get("mute").getAsString();
            listener.updateMute(zone, mute.equalsIgnoreCase("on"));
        }

        if (json.get("method").getAsString().equalsIgnoreCase("notifyPowerStatus")) {
            String power = param.get("status").getAsString();
            listener.updatePowerStatus(zone, power.equalsIgnoreCase("active"));
        }

        listener.updateConnectionState(true);
    }

    private void checkRadioPreset(String input) {
        Pattern pattern = Pattern.compile(".*contentId=(\\d+)");
        Matcher m = pattern.matcher(input);
        if (m.matches()) {
            listener.updateCurrentRadioStation(Integer.parseInt(m.group(1)));
        }
    }

    @Override
    public synchronized void onConnectionClosed() {
        listener.updateConnectionState(false);
    }

    private class Notifications {
        public List<Notification> enabled;
        public List<Notification> disabled;
    }

    private Notifications getSwitches(SonyAudioClientSocket socket, Notifications notifications) throws IOException {
        SwitchNotifications switchNotifications = new SwitchNotifications(notifications.enabled,
                notifications.disabled);
        JsonElement switches = socket.callMethod(switchNotifications);
        Gson mapper = new Gson();
        Type notificationListType = new TypeToken<List<Notification>>() {
        }.getType();
        notifications.enabled = mapper.fromJson(switches.getAsJsonArray().get(0).getAsJsonObject().get("enabled"),
                notificationListType);
        notifications.disabled = mapper.fromJson(switches.getAsJsonArray().get(0).getAsJsonObject().get("disabled"),
                notificationListType);

        return notifications;
    }

    @Override
    public synchronized void onConnectionOpened(URI resource) {
        try {
            Notifications notifications = new Notifications();
            notifications.enabled = Arrays.asList(new Notification[] {});
            notifications.disabled = Arrays.asList(new Notification[] {});

            if (av_content_socket.getURI().equals(resource)) {
                notifications = getSwitches(av_content_socket, notifications);

                for (Iterator<Notification> iter = notifications.disabled.listIterator(); iter.hasNext();) {
                    Notification a = iter.next();
                    if (a.name.equalsIgnoreCase("notifyPlayingContentInfo")) {
                        notifications.enabled.add(a);
                        iter.remove();
                    }
                }

                SwitchNotifications switchNotifications = new SwitchNotifications(notifications.enabled,
                        notifications.disabled);
                av_content_socket.callMethod(switchNotifications);
            }

            if (audio_socket.getURI().equals(resource)) {
                notifications = getSwitches(audio_socket, notifications);

                for (Iterator<Notification> iter = notifications.disabled.listIterator(); iter.hasNext();) {
                    Notification a = iter.next();
                    if (a.name.equalsIgnoreCase("notifyVolumeInformation")) {
                        notifications.enabled.add(a);
                        iter.remove();
                    }
                }

                SwitchNotifications switchNotifications = new SwitchNotifications(notifications.enabled,
                        notifications.disabled);
                audio_socket.callMethod(switchNotifications);
            }

            if (system_socket.getURI().equals(resource)) {
                notifications = getSwitches(system_socket, notifications);

                for (Iterator<Notification> iter = notifications.disabled.listIterator(); iter.hasNext();) {
                    Notification a = iter.next();
                    if (a.name.equalsIgnoreCase("notifyPowerStatus")) {
                        notifications.enabled.add(a);
                        iter.remove();
                    }
                }

                SwitchNotifications switchNotifications = new SwitchNotifications(notifications.enabled,
                        notifications.disabled);
                system_socket.callMethod(switchNotifications);
            }
            listener.updateConnectionState(true);
        } catch (IOException e) {
            listener.updateConnectionState(false);
        }
    }

    public synchronized void connect(ScheduledExecutorService scheduler) {
        try {
            base_uri = new URI(String.format("ws://%s:%d/%s", host, port, path)).normalize();

            URI wsAvContentUri = base_uri.resolve(base_uri.getPath() + "/avContent").normalize();
            av_content_socket = new SonyAudioClientSocket(this, wsAvContentUri, scheduler);
            av_content_socket.open();

            URI wsAudioUri = base_uri.resolve(base_uri.getPath() + "/audio").normalize();
            audio_socket = new SonyAudioClientSocket(this, wsAudioUri, scheduler);
            audio_socket.open();

            URI wsSystemUri = base_uri.resolve(base_uri.getPath() + "/system").normalize();
            system_socket = new SonyAudioClientSocket(this, wsSystemUri, scheduler);
            system_socket.open();
        } catch (Throwable t) {
            logger.error("exception during connect avContent to {}:{}", host, port, t);
        }
    }

    public synchronized void close() {
        if(av_content_socket != null) {
          av_content_socket.close();
        }
        av_content_socket = null;

        if(audio_socket != null) {
          audio_socket.close();
        }
        audio_socket = null;

        if(system_socket != null) {
          system_socket.close();
        }
        system_socket = null;
    }

    private boolean checkConnection(SonyAudioClientSocket socket) {
        if (!socket.isConnected()) {
            logger.debug("checkConnection: try to connect to {}", socket.getURI().toString());
            try {
                socket.open();
                return socket.isConnected();
            } catch (Throwable t) {
                logger.error("exception during connect to {}", socket.getURI().toString(), t);
                try {
                    socket.close();
                } catch (Exception e) {
                }
                return false;
            }
        }
        return true;
    }

    public boolean checkConnection() {
        return checkConnection(av_content_socket) && checkConnection(audio_socket) && checkConnection(system_socket);
    }

    public String getConnectionName() {
        return base_uri.toString();
    }

    public boolean getPower() throws IOException {
        GetPowerStatus getPowerStatus = new GetPowerStatus();
        JsonElement element = system_socket.callMethod(getPowerStatus);

        if (element.isJsonArray()) {
            String powerStatus = element.getAsJsonArray().get(0).getAsJsonObject().get("status").getAsString();
            return powerStatus.equalsIgnoreCase("active") ? true : false;
        }
        throw new IOException("Unexpected responces");
    }

    public boolean getPower(int zone) throws IOException {
        GetCurrentExternalTerminalsStatus getCurrentExternalTerminalsStatus = new GetCurrentExternalTerminalsStatus();
        JsonElement element = av_content_socket.callMethod(getCurrentExternalTerminalsStatus);

        if (element.isJsonArray()) {
            Iterator<JsonElement> terminals = element.getAsJsonArray().get(0).getAsJsonArray().iterator();
            while (terminals.hasNext()) {
                JsonObject terminal = terminals.next().getAsJsonObject();
                String uri = terminal.get("uri").getAsString();
                if (uri.equalsIgnoreCase("extOutput:zone?zone=" + Integer.toString(zone))) {
                    return terminal.get("active").getAsString().equalsIgnoreCase("active") ? true : false;
                }
            }
        }
        throw new IOException("Unexpected responces");
    }

    public void setPower(boolean power) throws IOException {
        SetPowerStatus setPowerStatus = new SetPowerStatus(power);
        system_socket.callMethod(setPowerStatus);
    }

    public void setPower(boolean power, int zone) throws IOException {
        SetActiveTerminal setActiveTerminal = new SetActiveTerminal(power, zone);
        av_content_socket.callMethod(setActiveTerminal);
    }

    public String getInput() throws IOException {
        GetPlayingContentInfo getPlayingContentInfo = new GetPlayingContentInfo();

        return getInput(getPlayingContentInfo);
    }

    public String getInput(int zone) throws IOException {
        GetPlayingContentInfo getPlayingContentInfo = new GetPlayingContentInfo(zone);

        return getInput(getPlayingContentInfo);
    }

    private String getInput(GetPlayingContentInfo getPlayingContentInfo) throws IOException {
        JsonElement element = av_content_socket.callMethod(getPlayingContentInfo);

        if (element.isJsonArray()) {
            JsonObject result = element.getAsJsonArray().get(0).getAsJsonArray().get(0).getAsJsonObject();
            String uri = result.get("uri").getAsString();
            checkRadioPreset(uri);
            return uri;
        }
        throw new IOException("Unexpected responces");
    }

    public void setInput(String input) throws IOException {
        SetPlayContent setPlayContent = new SetPlayContent(input);
        av_content_socket.callMethod(setPlayContent);
    }

    public void setInput(String input, int zone) throws IOException {
        SetPlayContent setPlayContent = new SetPlayContent(input, zone);
        av_content_socket.callMethod(setPlayContent);
    }

    public int getRadioFreq() throws IOException {
        GetPlayingContentInfo getPlayingContentInfo = new GetPlayingContentInfo();
        JsonElement element = av_content_socket.callMethod(getPlayingContentInfo);

        if (element.isJsonArray()) {
            JsonObject result = element.getAsJsonArray().get(0).getAsJsonArray().get(0).getAsJsonObject();
            if (result.has("broadcastFreq")) {
                int freq = result.get("broadcastFreq").getAsInt();
                return freq;
            }
            return 0;
        }
        throw new IOException("Unexpected responces");
    }

    public void radioSeekFwd() throws IOException {
        SeekBroadcastStation seekBroadcastStation = new SeekBroadcastStation(true);
        av_content_socket.callMethod(seekBroadcastStation);
    }

    public void radioSeekBwd() throws IOException {
        SeekBroadcastStation seekBroadcastStation = new SeekBroadcastStation(false);
        av_content_socket.callMethod(seekBroadcastStation);
    }

    public int getVolume() throws IOException {
        GetVolumeInformation getVolumeInformation = new GetVolumeInformation();
        return getVolume(getVolumeInformation);
    }

    public int getVolume(int zone) throws IOException {
        GetVolumeInformation getVolumeInformation = new GetVolumeInformation(zone);
        return getVolume(getVolumeInformation);
    }

    private int getVolume(GetVolumeInformation getVolumeInformation) throws IOException {
        if (!audio_socket.isConnected()) {
            return 0;
        }
        JsonElement element = audio_socket.callMethod(getVolumeInformation);

        if (element.isJsonArray()) {
            JsonObject result = element.getAsJsonArray().get(0).getAsJsonArray().get(0).getAsJsonObject();

            int volume = result.get("volume").getAsInt();
            min_volume = result.get("minVolume").getAsInt();
            max_volume = result.get("maxVolume").getAsInt();
            int vol = Math.round(100 * (volume - min_volume) / (max_volume - min_volume));
            if (vol < 0) {
                vol = 0;
            }
            return vol;
        }
        throw new IOException("Unexpected responces");
    }

    public void setVolume(int volume) throws IOException {
        SetAudioVolume setAudioVolume = new SetAudioVolume(volume, min_volume, max_volume);

        audio_socket.callMethod(setAudioVolume);
    }

    public void setVolume(int volume, int zone) throws IOException {
        SetAudioVolume setAudioVolume = new SetAudioVolume(zone, volume, min_volume, max_volume);

        audio_socket.callMethod(setAudioVolume);
    }

    public boolean getMute() throws IOException {
        GetVolumeInformation getVolumeInformation = new GetVolumeInformation();
        return getMute(getVolumeInformation);
    }

    public boolean getMute(int zone) throws IOException {
        GetVolumeInformation getVolumeInformation = new GetVolumeInformation(zone);
        return getMute(getVolumeInformation);
    }

    private boolean getMute(GetVolumeInformation getVolumeInformation) throws IOException {
        JsonElement element = audio_socket.callMethod(getVolumeInformation);

        if (element.isJsonArray()) {
            JsonObject result = element.getAsJsonArray().get(0).getAsJsonArray().get(0).getAsJsonObject();
            String mute = result.get("mute").getAsString();
            return mute.equalsIgnoreCase("on") ? true : false;
        }
        throw new IOException("Unexpected responces");
    }

    public void setMute(boolean mute) throws IOException {
        SetAudioMute setAudioMute = new SetAudioMute(mute);
        audio_socket.callMethod(setAudioMute);
    }

    public void setMute(boolean mute, int zone) throws IOException {
        SetAudioMute setAudioMute = new SetAudioMute(mute, zone);
        audio_socket.callMethod(setAudioMute);
    }

    public String getSoundField() throws IOException {
      GetSoundField getSoundField = new GetSoundField();
      JsonElement element = audio_socket.callMethod(getSoundField);

      if (element.isJsonArray()) {
        Iterator<JsonElement> iterator = element.getAsJsonArray().get(0).getAsJsonArray().iterator();
        while(iterator.hasNext()){
          JsonObject item = iterator.next().getAsJsonObject();
          if(item.get("target").getAsString().equalsIgnoreCase("soundField"))
            return item.get("currentValue").getAsString();
        }
      }
      throw new IOException("Unexpected responces");
    }

    public void setSoundField(String soundField) throws IOException {
        SetSoundField setSoundField = new SetSoundField(soundField);
        audio_socket.callMethod(setSoundField);
    }

    public boolean getPureDirect() throws IOException {
        GetPureDirect getPureDirect = new GetPureDirect();
        JsonElement element = audio_socket.callMethod(getPureDirect);

        if (element.isJsonArray()) {
          Iterator<JsonElement> iterator = element.getAsJsonArray().get(0).getAsJsonArray().iterator();
          while(iterator.hasNext()){
            JsonObject item = iterator.next().getAsJsonObject();
            if(item.get("target").getAsString().equalsIgnoreCase("pureDirect"))
              return item.get("currentValue").getAsString().equalsIgnoreCase("on") ? true : false;
          }
        }
        throw new IOException("Unexpected responces");
    }

    public void setPureDirect(boolean pureDirect) throws IOException {
        SetPureDirect setPureDirect = new SetPureDirect(pureDirect);
        audio_socket.callMethod(setPureDirect);
    }

    public boolean getClearAudio() throws IOException {
        GetClearAudio getClearAudio = new GetClearAudio();
        JsonElement element = audio_socket.callMethod(getClearAudio);

        if (element.isJsonArray()) {
          Iterator<JsonElement> iterator = element.getAsJsonArray().get(0).getAsJsonArray().iterator();
          while(iterator.hasNext()){
            JsonObject item = iterator.next().getAsJsonObject();
            if(item.get("target").getAsString().equalsIgnoreCase("clearAudio"))
              return item.get("currentValue").getAsString().equalsIgnoreCase("on") ? true : false;
          }
        }
        throw new IOException("Unexpected responces");
    }

    public void setClearAudio(boolean clearAudio) throws IOException {
        SetClearAudio serClearAudio = new SetClearAudio(clearAudio);
        audio_socket.callMethod(serClearAudio);
    }
}

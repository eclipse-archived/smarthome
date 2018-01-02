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

import java.util.List;

/**
 * The {@link SonyAudioMethod} base class for SONY API methods
 *
 * @author David Åberg - Initial contribution
 */
public abstract class SonyAudioMethod {
    protected int id = 1;
    protected String method;
    protected String version;

    public SonyAudioMethod(String method, String version) {
        this.method = method;
        this.version = version;
    }
};

/**
 * The {@link GetPowerStatus} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class GetPowerStatus extends SonyAudioMethod {
    public String[] params = new String[] {};

    public GetPowerStatus() {
        super("getPowerStatus", "1.1");
    }
}

/**
 * The {@link GetCurrentExternalTerminalsStatus} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class GetCurrentExternalTerminalsStatus extends SonyAudioMethod {
    public String[] params = new String[] {};

    public GetCurrentExternalTerminalsStatus() {
        super("getCurrentExternalTerminalsStatus", "1.0");
    }
}

/**
 * The {@link SetPowerStatus} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class SetPowerStatus extends SonyAudioMethod {
    public Param[] params;

    class Param {
        public String status;

        Param(boolean power) {
            status = power ? "active" : "off";
        }
    }

    SetPowerStatus(boolean power) {
        super("setPowerStatus", "1.1");
        Param param = new Param(power);
        params = new Param[] { param };
    }
}

/**
 * The {@link SetActiveTerminal} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class SetActiveTerminal extends SonyAudioMethod {
    public Param[] params;

    class Param {
        public String active;
        public String uri;

        Param(boolean power, int zone) {
            active = power ? "active" : "inactive";
            uri = "extOutput:zone?zone=" + Integer.toString(zone);
        }
    }

    SetActiveTerminal(boolean power, int zone) {
        super("setActiveTerminal", "1.0");
        Param param = new Param(power, zone);
        params = new Param[] { param };
    }
}

/**
 * The {@link GetPlayingContentInfo} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class GetPlayingContentInfo extends SonyAudioMethod {
    public Param[] params;

    class Param {
        String output = "";

        Param() {
        }

        Param(int zone) {
            output = "extOutput:zone?zone=" + Integer.toString(zone);
        }
    }

    GetPlayingContentInfo() {
        super("getPlayingContentInfo", "1.2");
        Param param = new Param();
        params = new Param[] { param };
    }

    GetPlayingContentInfo(int zone) {
        super("getPlayingContentInfo", "1.2");
        Param param = new Param(zone);
        params = new Param[] { param };
    }
}

/**
 * The {@link SetPlayContent} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class SetPlayContent extends SonyAudioMethod {
    public Param[] params;

    class Param {
        String output = "";
        String uri;

        Param(String input) {
            uri = input;
        }

        Param(String input, int zone) {
            uri = input;
            output = "extOutput:zone?zone=" + Integer.toString(zone);
        }
    }

    SetPlayContent(String input) {
        super("setPlayContent", "1.2");
        params = new Param[] { new Param(input) };
    }

    SetPlayContent(String input, int zone) {
        super("setPlayContent", "1.2");
        params = new Param[] { new Param(input, zone) };
    }
}

/**
 * The {@link GetVolumeInformation} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class GetVolumeInformation extends SonyAudioMethod {
    public Param[] params;

    class Param {
        String output = "";

        Param() {
        }

        Param(int zone) {
            output = "extOutput:zone?zone=" + Integer.toString(zone);
        }
    }

    GetVolumeInformation() {
        super("getVolumeInformation", "1.1");
        params = new Param[] { new Param() };
    }

    GetVolumeInformation(int zone) {
        super("getVolumeInformation", "1.1");
        params = new Param[] { new Param(zone) };
    }
}

/**
 * The {@link SetAudioVolume} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class SetAudioVolume extends SonyAudioMethod {
    public Param[] params;

    class Param {
        String output = "";
        String volume;

        Param(long new_volume) {
            volume = Long.toString(new_volume);
        }

        Param(long new_volume, int zone) {
            volume = Long.toString(new_volume);
            output = "extOutput:zone?zone=" + Integer.toString(zone);
        }
    }

    SetAudioVolume(int volume, int min, int max) {
        super("setAudioVolume", "1.1");
        long scaled_volume = scaleVolume(volume, min, max);
        params = new Param[] { new Param(scaled_volume) };
    }

    SetAudioVolume(int zone, int volume, int min, int max) {
        super("setAudioVolume", "1.1");
        long scaled_volume = scaleVolume(volume, min, max);
        params = new Param[] { new Param(scaled_volume, zone) };
    }

    long scaleVolume(int volume, int min, int max) {
        return Math.round(((max - min) * volume / 100.0) + min);
    }
}

/**
 * The {@link SetAudioMute} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class SetAudioMute extends SonyAudioMethod {
    public Param[] params;

    class Param {
        String output = "";
        String mute;

        Param(boolean mute) {
            this.mute = mute ? "on" : "off";
        }

        Param(boolean mute, int zone) {
            this.mute = mute ? "on" : "off";
            output = "extOutput:zone?zone=" + Integer.toString(zone);
        }
    }

    SetAudioMute(boolean mute) {
        super("setAudioMute", "1.1");
        params = new Param[] { new Param(mute) };
    }

    SetAudioMute(boolean mute, int zone) {
        super("setAudioMute", "1.1");
        params = new Param[] { new Param(mute, zone) };
    }
}

/**
 * Helper class
 *
 * @author David Åberg - Initial contribution
 */
class GetSoundSettings extends SonyAudioMethod {
    public Param[] params;

    class Param {
        String target;

        Param(String target) {
          this.target = target;
        }
    }

    GetSoundSettings() {
      super("getSoundSettings", "1.1");
    }

    GetSoundSettings(String target) {
        super("getSoundSettings", "1.1");
        params = new Param[] { new Param(target) };
    }
}

/**
 * The {@link GetSoundField} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class GetSoundField extends GetSoundSettings {
    GetSoundField() {
        super("soundField");
    }
}

/**
 * The {@link GetPureDirect} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class GetPureDirect extends GetSoundSettings {
    GetPureDirect() {
        super("pureDirect");
    }
}

/**
 * The {@link GetClearAudio} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class GetClearAudio extends GetSoundSettings {
    GetClearAudio() {
        super("clearAudio");
    }
}


/**
 * Helper class
 *
 * @author David Åberg - Initial contribution
 */
class SetSoundSettings extends SonyAudioMethod {
  public Param[] params;

  class Settings {
    String value;
    String target;

    Settings(String target, String value) {
      this.target = target;
      this.value = value;
    }
  }

  class Param {

      Settings[] settings;

      Param(Settings[] settings) {
          this.settings = settings;
      }
  }

  SetSoundSettings() {
    super("setSoundSettings", "1.1");
  }

  SetSoundSettings(String target, String value) {
      super("setSoundSettings", "1.1");
      Settings[] settings = { new Settings(target, value) };
      params = new Param[] { new Param(settings) };
  }
}

/**
 * The {@link SetSoundField} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class SetSoundField extends SetSoundSettings {
    SetSoundField(String soundField) {
        super("soundField", soundField);
    }
}

/**
 * The {@link SetPureDirect} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class SetPureDirect extends SetSoundSettings {
    SetPureDirect(boolean pureDirect) {
      super("pureDirect", pureDirect ? "on" : "off");
    }
}

/**
 * The {@link SetClearAudio} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class SetClearAudio extends SetSoundSettings {
    SetClearAudio(boolean clearAudio) {
      super("clearAudio", clearAudio ? "on" : "off");
    }
}


/**
 * The {@link SwitchNotifications} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class SwitchNotifications extends SonyAudioMethod {
    public Param[] params;

    class Notification {
        String name;
        String version;
    }

    class Param {
        List<Notification> enabled;
        List<Notification> disabled;

        Param(List<Notification> enabled, List<Notification> disabled) {
            this.enabled = enabled;
            this.disabled = disabled;
        }
    }

    SwitchNotifications(List<Notification> enabled, List<Notification> disabled) {
        super("switchNotifications", "1.0");
        params = new Param[] { new Param(enabled, disabled) };
    }
}

/**
 * The {@link SeekBroadcastStation} SONY Audio control API method
 *
 * @author David Åberg - Initial contribution
 */
class SeekBroadcastStation extends SonyAudioMethod {
    public Param[] params;

    class Param {
        String tuning = "auto";
        String direction;

        Param(boolean forward) {
            this.direction = forward ? "fwd" : "bwd";
        }
    }

    SeekBroadcastStation(boolean forward) {
        super("seekBroadcastStation", "1.0");
        params = new Param[] { new Param(forward) };
    }
}

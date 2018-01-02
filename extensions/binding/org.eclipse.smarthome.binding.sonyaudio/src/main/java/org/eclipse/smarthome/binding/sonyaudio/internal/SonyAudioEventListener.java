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
 package org.eclipse.smarthome.binding.sonyaudio.internal;

import java.util.EventListener;

/**
 * The {@link SonyAudioEventListener} event listener interface
 * handlers.
 *
 * @author David - Initial contribution
 */
public interface SonyAudioEventListener extends EventListener {
    void updateConnectionState(boolean connected);

    void updateInputSource(int zone, String source);

    void updateBroadcastFreq(int freq);

    void updateSeekStation(String seek);

    void updateCurrentRadioStation(int radioStation);

    void updateVolume(int zone, int volume);

    void updateMute(int zone, boolean mute);

    void updatePowerStatus(int zone, boolean power);
}

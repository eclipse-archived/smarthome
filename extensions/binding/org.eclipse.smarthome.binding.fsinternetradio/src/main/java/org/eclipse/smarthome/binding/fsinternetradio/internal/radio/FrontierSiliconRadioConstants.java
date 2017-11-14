/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.fsinternetradio.internal.radio;

/**
 * Internal constants for the frontier silicon radio.
 *
 * @author Markus Rathgeb - Moved the constants to separate class
 */
public class FrontierSiliconRadioConstants {

    public static final String REQUEST_SET_POWER = "SET/netRemote.sys.power";
    public static final String REQUEST_GET_POWER = "GET/netRemote.sys.power";
    public static final String REQUEST_GET_MODE = "GET/netRemote.sys.mode";
    public static final String REQUEST_SET_MODE = "SET/netRemote.sys.mode";
    public static final String REQUEST_SET_VOLUME = "SET/netRemote.sys.audio.volume";
    public static final String REQUEST_GET_VOLUME = "GET/netRemote.sys.audio.volume";
    public static final String REQUEST_SET_MUTE = "SET/netRemote.sys.audio.mute";
    public static final String REQUEST_GET_MUTE = "GET/netRemote.sys.audio.mute";
    public static final String REQUEST_SET_PRESET = "SET/netRemote.nav.state";
    public static final String REQUEST_SET_PRESET_ACTION = "SET/netRemote.nav.action.selectPreset";
    public static final String REQUEST_GET_PLAY_INFO_TEXT = "GET/netRemote.play.info.text";
    public static final String REQUEST_GET_PLAY_INFO_NAME = "GET/netRemote.play.info.name";

    /** URL path, must begin with a slash (/) */
    public static final String CONNECTION_PATH = "/fsapi";

    private FrontierSiliconRadioConstants() {
    }
}

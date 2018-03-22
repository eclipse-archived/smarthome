/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.binding.sonos.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SonosBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - Changed ESH-PREFIX and cleaned up warnings
 */
public class SonosBindingConstants {

    public static final String BINDING_ID = "sonos";
    public static final String ESH_PREFIX = "smarthome-";

    // List of all Thing Type UIDs
    // Column (:) is not used for PLAY:1, PLAY:3, PLAY:5 and CONNECT:AMP because of
    // ThingTypeUID and device pairing name restrictions
    public static final ThingTypeUID ONE_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "One");
    public static final ThingTypeUID PLAY1_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "PLAY1");
    public static final ThingTypeUID PLAY3_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "PLAY3");
    public static final ThingTypeUID PLAY5_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "PLAY5");
    public static final ThingTypeUID PLAYBAR_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "PLAYBAR");
    public static final ThingTypeUID CONNECT_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "CONNECT");
    public static final ThingTypeUID CONNECTAMP_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "CONNECTAMP");
    public static final ThingTypeUID ZONEPLAYER_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "zoneplayer");

    public static final Set<ThingTypeUID> WITH_LINEIN_THING_TYPES_UIDS = Stream
            .of(PLAY5_THING_TYPE_UID, PLAYBAR_THING_TYPE_UID, CONNECT_THING_TYPE_UID, CONNECTAMP_THING_TYPE_UID)
            .collect(Collectors.toSet());

    public static final Set<ThingTypeUID> SUPPORTED_KNOWN_THING_TYPES_UIDS = Stream
            .of(ONE_THING_TYPE_UID, PLAY1_THING_TYPE_UID, PLAY3_THING_TYPE_UID, PLAY5_THING_TYPE_UID,
                    PLAYBAR_THING_TYPE_UID, CONNECT_THING_TYPE_UID, CONNECTAMP_THING_TYPE_UID)
            .collect(Collectors.toSet());

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            SUPPORTED_KNOWN_THING_TYPES_UIDS);
    static {
        SUPPORTED_THING_TYPES_UIDS.add(ZONEPLAYER_THING_TYPE_UID);
    }

    // List of all Channel ids
    public static final String ADD = "add";
    public static final String ALARM = "alarm";
    public static final String ALARMPROPERTIES = "alarmproperties";
    public static final String ALARMRUNNING = "alarmrunning";
    public static final String CLEARQUEUE = "clearqueue";
    public static final String CONTROL = "control";
    public static final String COORDINATOR = "coordinator";
    public static final String CURRENTALBUM = "currentalbum";
    public static final String CURRENTALBUMART = "currentalbumart";
    public static final String CURRENTALBUMARTURL = "currentalbumarturl";
    public static final String CURRENTARTIST = "currentartist";
    public static final String CURRENTTITLE = "currenttitle";
    public static final String CURRENTTRACK = "currenttrack";
    public static final String CURRENTTRACKURI = "currenttrackuri";
    public static final String CURRENTTRANSPORTURI = "currenttransporturi";
    public static final String FAVORITE = "favorite";
    public static final String LED = "led";
    public static final String LINEIN = "linein";
    public static final String LOCALCOORDINATOR = "localcoordinator";
    public static final String MUTE = "mute";
    public static final String NOTIFICATIONSOUND = "notificationsound";
    public static final String PLAYLINEIN = "playlinein";
    public static final String PLAYLIST = "playlist";
    public static final String PLAYQUEUE = "playqueue";
    public static final String PLAYTRACK = "playtrack";
    public static final String PLAYURI = "playuri";
    public static final String PUBLICADDRESS = "publicaddress";
    public static final String RADIO = "radio";
    public static final String REMOVE = "remove";
    public static final String REPEAT = "repeat";
    public static final String RESTORE = "restore";
    public static final String RESTOREALL = "restoreall";
    public static final String SAVE = "save";
    public static final String SAVEALL = "saveall";
    public static final String SHUFFLE = "shuffle";
    public static final String SLEEPTIMER = "sleeptimer";
    public static final String SNOOZE = "snooze";
    public static final String STANDALONE = "standalone";
    public static final String STATE = "state";
    public static final String STOP = "stop";
    public static final String TUNEINSTATIONID = "tuneinstationid";
    public static final String VOLUME = "volume";
    public static final String ZONEGROUPID = "zonegroupid";
    public static final String ZONENAME = "zonename";
    public static final String MODELID = "modelId";

    // List of properties
    public static final String IDENTIFICATION = "identification";
    public static final String MAC_ADDRESS = "macAddress";
    public static final String IP_ADDRESS = "ipAddress";

}

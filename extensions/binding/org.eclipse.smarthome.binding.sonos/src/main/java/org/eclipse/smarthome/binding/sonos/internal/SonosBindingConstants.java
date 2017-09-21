/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    public final static ThingTypeUID PLAY1_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "PLAY1");
    public final static ThingTypeUID PLAY3_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "PLAY3");
    public final static ThingTypeUID PLAY5_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "PLAY5");
    public final static ThingTypeUID PLAYBAR_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "PLAYBAR");
    public final static ThingTypeUID CONNECT_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "CONNECT");
    public final static ThingTypeUID CONNECTAMP_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "CONNECTAMP");
    public final static ThingTypeUID ZONEPLAYER_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "zoneplayer");

    public static final Set<ThingTypeUID> SUPPORTED_KNOWN_THING_TYPES_UIDS = Stream
            .of(PLAY1_THING_TYPE_UID, PLAY3_THING_TYPE_UID, PLAY5_THING_TYPE_UID, PLAYBAR_THING_TYPE_UID,
                    CONNECT_THING_TYPE_UID, CONNECTAMP_THING_TYPE_UID)
            .collect(Collectors.toSet());

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            SUPPORTED_KNOWN_THING_TYPES_UIDS);
    static {
        SUPPORTED_THING_TYPES_UIDS.add(ZONEPLAYER_THING_TYPE_UID);
    }

    // List of all Channel ids
    public final static String ADD = "add";
    public final static String ALARM = "alarm";
    public final static String ALARMPROPERTIES = "alarmproperties";
    public final static String ALARMRUNNING = "alarmrunning";
    public final static String CLEARQUEUE = "clearqueue";
    public final static String CONTROL = "control";
    public final static String COORDINATOR = "coordinator";
    public final static String CURRENTALBUM = "currentalbum";
    public final static String CURRENTALBUMART = "currentalbumart";
    public final static String CURRENTALBUMARTURL = "currentalbumarturl";
    public final static String CURRENTARTIST = "currentartist";
    public final static String CURRENTTITLE = "currenttitle";
    public final static String CURRENTTRACK = "currenttrack";
    public final static String CURRENTTRACKURI = "currenttrackuri";
    public final static String CURRENTTRANSPORTURI = "currenttransporturi";
    public final static String FAVORITE = "favorite";
    public final static String LED = "led";
    public final static String LINEIN = "linein";
    public final static String LOCALCOORDINATOR = "localcoordinator";
    public final static String MUTE = "mute";
    public final static String NOTIFICATIONSOUND = "notificationsound";
    public final static String NOTIFICATIONVOLUME = "notificationvolume";
    public final static String PLAYLINEIN = "playlinein";
    public final static String PLAYLIST = "playlist";
    public final static String PLAYQUEUE = "playqueue";
    public final static String PLAYTRACK = "playtrack";
    public final static String PLAYURI = "playuri";
    public final static String PUBLICADDRESS = "publicaddress";
    public final static String RADIO = "radio";
    public final static String REMOVE = "remove";
    public final static String REPEAT = "repeat";
    public final static String RESTORE = "restore";
    public final static String RESTOREALL = "restoreall";
    public final static String SAVE = "save";
    public final static String SAVEALL = "saveall";
    public final static String SHUFFLE = "shuffle";
    public final static String SLEEPTIMER = "sleeptimer";
    public final static String SNOOZE = "snooze";
    public final static String STANDALONE = "standalone";
    public final static String STATE = "state";
    public final static String STOP = "stop";
    public final static String TUNEINSTATIONID = "tuneinstationid";
    public final static String VOLUME = "volume";
    public final static String ZONEGROUP = "zonegroup";
    public final static String ZONEGROUPID = "zonegroupid";
    public final static String ZONENAME = "zonename";
    public final static String MODELID = "modelId";

    // List of properties
    public static final String IDENTIFICATION = "identification";
    public static final String MAC_ADDRESS = "macAddress";
    public static final String IP_ADDRESS = "ipAddress";

}

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
package org.eclipse.smarthome.binding.bosesoundtouch.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.binding.bosesoundtouch.handler.BoseSoundTouchHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The {@link XMLResponseProcessor} class handles the XML mapping
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer - Initial contribution
 */
public class XMLResponseProcessor {
    private BoseSoundTouchHandler handler;

    private Map<XMLHandlerState, Map<String, XMLHandlerState>> stateSwitchingMap;

    public XMLResponseProcessor(BoseSoundTouchHandler handler) {
        this.handler = handler;
        init();
    }

    public void handleMessage(String msg) throws SAXException, IOException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(new XMLResponseHandler(handler, stateSwitchingMap));
        reader.parse(new InputSource(new StringReader(msg)));
    }

    // initializes our XML parsing state machine
    private void init() {
        stateSwitchingMap = new HashMap<>();

        Map<String, XMLHandlerState> msgInitMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.INIT, msgInitMap);
        msgInitMap.put("msg", XMLHandlerState.Msg);
        msgInitMap.put("SoundTouchSdkInfo", XMLHandlerState.Unprocessed);
        msgInitMap.put("userActivityUpdate", XMLHandlerState.Unprocessed); // ignored..

        Map<String, XMLHandlerState> msgBodyMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.MsgBody, msgBodyMap);
        msgBodyMap.put("info", XMLHandlerState.Info);
        msgBodyMap.put("volume", XMLHandlerState.Volume);
        msgBodyMap.put("presets", XMLHandlerState.Presets);
        msgBodyMap.put("key", XMLHandlerState.Unprocessed); // only confirmation of our key presses...
        msgBodyMap.put("status", XMLHandlerState.Unprocessed); // only confirmation of commands sent to device...
        msgBodyMap.put("zone", XMLHandlerState.Zone); // only confirmation of our key presses...
        msgBodyMap.put("bass", XMLHandlerState.Bass);
        msgBodyMap.put("sources", XMLHandlerState.Sources);
        msgBodyMap.put("bassCapabilities", XMLHandlerState.BassCapabilities);

        // info message states
        Map<String, XMLHandlerState> infoMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Info, infoMap);
        infoMap.put("components", XMLHandlerState.Unprocessed); // TODO read software version and serial number
        infoMap.put("name", XMLHandlerState.InfoName);
        infoMap.put("type", XMLHandlerState.InfoType);
        infoMap.put("networkInfo", XMLHandlerState.Unprocessed);
        infoMap.put("margeAccountUUID", XMLHandlerState.Unprocessed);
        infoMap.put("margeURL", XMLHandlerState.Unprocessed);
        infoMap.put("moduleType", XMLHandlerState.Unprocessed);
        infoMap.put("variant", XMLHandlerState.Unprocessed);
        infoMap.put("variantMode", XMLHandlerState.Unprocessed);
        infoMap.put("countryCode", XMLHandlerState.Unprocessed);
        infoMap.put("regionCode", XMLHandlerState.Unprocessed);

        Map<String, XMLHandlerState> updatesMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Updates, updatesMap);
        updatesMap.put("clockDisplayUpdated", XMLHandlerState.Unprocessed); // can we get anything useful of that?
        updatesMap.put("connectionStateUpdated", XMLHandlerState.UnprocessedNoTextExpected);
        updatesMap.put("infoUpdated", XMLHandlerState.Unprocessed);
        updatesMap.put("nowPlayingUpdated", XMLHandlerState.MsgBody);
        updatesMap.put("nowSelectionUpdated", XMLHandlerState.Unprocessed); // TODO this seems to be quite a useful info
                                                                            // what is currently played..
        updatesMap.put("recentsUpdated", XMLHandlerState.Unprocessed);
        updatesMap.put("volumeUpdated", XMLHandlerState.MsgBody);
        updatesMap.put("zoneUpdated", XMLHandlerState.ZoneUpdated); // just notifies but dosn't provide details
        updatesMap.put("bassUpdated", XMLHandlerState.BassUpdated);

        Map<String, XMLHandlerState> volume = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Volume, volume);
        volume.put("targetvolume", XMLHandlerState.VolumeTarget);
        volume.put("actualvolume", XMLHandlerState.VolumeActual);
        volume.put("muteenabled", XMLHandlerState.VolumeMuteEnabled);

        Map<String, XMLHandlerState> nowPlayingMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.NowPlaying, nowPlayingMap);
        nowPlayingMap.put("album", XMLHandlerState.NowPlayingAlbum);
        nowPlayingMap.put("art", XMLHandlerState.NowPlayingArt);
        nowPlayingMap.put("artist", XMLHandlerState.NowPlayingArtist);
        nowPlayingMap.put("ContentItem", XMLHandlerState.ContentItem);
        nowPlayingMap.put("description", XMLHandlerState.NowPlayingDescription);
        nowPlayingMap.put("playStatus", XMLHandlerState.NowPlayingPlayStatus);
        nowPlayingMap.put("rateEnabled", XMLHandlerState.NowPlayingRateEnabled);
        nowPlayingMap.put("skipEnabled", XMLHandlerState.NowPlayingSkipEnabled);
        nowPlayingMap.put("skipPreviousEnabled", XMLHandlerState.NowPlayingSkipPreviousEnabled);
        nowPlayingMap.put("stationLocation", XMLHandlerState.NowPlayingStationLocation);
        nowPlayingMap.put("stationName", XMLHandlerState.NowPlayingStationName);
        nowPlayingMap.put("track", XMLHandlerState.NowPlayingTrack);
        nowPlayingMap.put("connectionStatusInfo", XMLHandlerState.Unprocessed); // TODO active when Source==Bluetooth
        // TODO active when Source==Pandora and maybe also other sources - seems to be rating related
        nowPlayingMap.put("time", XMLHandlerState.Unprocessed);
        nowPlayingMap.put("rating", XMLHandlerState.Unprocessed);
        nowPlayingMap.put("rateEnabled", XMLHandlerState.Unprocessed);

        // ContentItem specifies a resource (that also could be bookmarked in a preset)
        Map<String, XMLHandlerState> contentItemMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.ContentItem, contentItemMap);
        contentItemMap.put("itemName", XMLHandlerState.ContentItemItemName);
        contentItemMap.put("containerArt", XMLHandlerState.Unprocessed);

        Map<String, XMLHandlerState> presetMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Preset, presetMap);
        presetMap.put("ContentItem", XMLHandlerState.ContentItem);

        Map<String, XMLHandlerState> zoneMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Zone, zoneMap);
        zoneMap.put("member", XMLHandlerState.ZoneMember);

        Map<String, XMLHandlerState> bassMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Bass, bassMap);
        bassMap.put("targetbass", XMLHandlerState.BassTarget);
        bassMap.put("actualbass", XMLHandlerState.BassActual);

        Map<String, XMLHandlerState> sourceMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.Sources, sourceMap);

        Map<String, XMLHandlerState> bassCapabilitiesMap = new HashMap<>();
        stateSwitchingMap.put(XMLHandlerState.BassCapabilities, bassCapabilitiesMap);
        bassCapabilitiesMap.put("bassAvailable", XMLHandlerState.BassAvailable);
        bassCapabilitiesMap.put("bassMin", XMLHandlerState.BassMin);
        bassCapabilitiesMap.put("bassMax", XMLHandlerState.BassMax);
        bassCapabilitiesMap.put("bassDefault", XMLHandlerState.BassDefault);
    }

}
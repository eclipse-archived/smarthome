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

import static org.eclipse.smarthome.binding.bosesoundtouch.BoseSoundTouchBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

import org.eclipse.smarthome.binding.bosesoundtouch.BoseSoundTouchConfiguration;
import org.eclipse.smarthome.binding.bosesoundtouch.handler.BoseSoundTouchHandler;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The {@link XMLResponseHandler} class handles the XML communication with the Soundtouch
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer - Initial contribution
 * @author Kai Kreuzer - code clean up
 */
public class XMLResponseHandler extends DefaultHandler {

    private final Logger logger = LoggerFactory.getLogger(XMLResponseHandler.class);

    private BoseSoundTouchHandler handler;
    private CommandExecutor commandExecutor;

    private Map<XMLHandlerState, Map<String, XMLHandlerState>> stateSwitchingMap;

    private Stack<XMLHandlerState> states;
    private XMLHandlerState state;
    private boolean msgHeaderWasValid;

    private ContentItem contentItem;
    private boolean volumeMuteEnabled;
    private OnOffType rateEnabled;
    private OnOffType skipEnabled;
    private OnOffType skipPreviousEnabled;

    private State nowPlayingSource;

    private BoseSoundTouchConfiguration masterDeviceId;
    String deviceId;

    private Map<Integer, ContentItem> playerPresets;

    /**
     * Creates a new instance of this class
     *
     * @param handler
     * @param stateSwitchingMap the stateSwitchingMap is the XMLState Map, that says which Flags are computed
     */
    public XMLResponseHandler(BoseSoundTouchHandler handler,
            Map<XMLHandlerState, Map<String, XMLHandlerState>> stateSwitchingMap) {
        this.handler = handler;
        this.commandExecutor = handler.getCommandExecutor();
        this.stateSwitchingMap = stateSwitchingMap;
        init();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        logger.trace("{}: startElement('{}'; state: {})", handler.getDeviceName(), localName, state);
        states.push(state);
        XMLHandlerState curState = state; // save for switch statement
        Map<String, XMLHandlerState> stateMap = stateSwitchingMap.get(state);
        state = XMLHandlerState.Unprocessed; // set default value; we avoid default in select to have the compiler
                                             // showing a
        // warning for unhandled states
        switch (curState) {
            case INIT:
                if ("updates".equals(localName)) {
                    // it just seems to be a ping - havn't seen any data on it..
                    if (checkDeviceId(localName, attributes, false)) {
                        state = XMLHandlerState.Updates;
                    } else {
                        state = XMLHandlerState.Unprocessed;
                    }
                } else {
                    state = stateMap.get(localName);
                    if (state == null) {
                        if (logger.isDebugEnabled()) {
                            logger.warn("{}: Unhandled XML entity during {}: '{}", handler.getDeviceName(), curState,
                                    localName);
                        }
                        state = XMLHandlerState.Unprocessed;
                    }
                }
                break;
            case Msg:
                if ("header".equals(localName)) {
                    // message
                    if (checkDeviceId(localName, attributes, false)) {
                        state = XMLHandlerState.MsgHeader;
                        msgHeaderWasValid = true;
                    } else {
                        state = XMLHandlerState.Unprocessed;
                    }
                } else if ("body".equals(localName)) {
                    if (msgHeaderWasValid) {
                        state = XMLHandlerState.MsgBody;
                    } else {
                        state = XMLHandlerState.Unprocessed;
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.warn("{}: Unhandled XML entity during {}: '{}'", handler.getDeviceName(), curState,
                                localName);
                    }
                    state = XMLHandlerState.Unprocessed;
                }
                break;
            case MsgHeader:
                if ("request".equals(localName)) {
                    state = XMLHandlerState.Unprocessed; // TODO implement request id / response tracking...
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.warn("{}: Unhandled XML entity during {}: '{}'", handler.getDeviceName(), curState,
                                localName);
                    }
                    state = XMLHandlerState.Unprocessed;
                }
                break;
            case MsgBody:
                if ("nowPlaying".equals(localName)) {
                    /*
                     * if (!checkDeviceId(localName, attributes, true)) {
                     * state = XMLHandlerState.Unprocessed;
                     * break;
                     * }
                     */
                    rateEnabled = OnOffType.OFF;
                    skipEnabled = OnOffType.OFF;
                    skipPreviousEnabled = OnOffType.OFF;
                    state = XMLHandlerState.NowPlaying;
                    String source = attributes.getValue("source");
                    if (nowPlayingSource == null || !nowPlayingSource.toString().equals(source)) {
                        // source changed
                        nowPlayingSource = new StringType(source);
                        // reset enabled states
                        updateRateEnabled(OnOffType.OFF);
                        updateSkipEnabled(OnOffType.OFF);
                        updateSkipPreviousEnabled(OnOffType.OFF);

                        // clear all "nowPlaying" details on source change...
                        updateNowPlayingAlbum(UnDefType.NULL);
                        updateNowPlayingArtwork(UnDefType.NULL);
                        updateNowPlayingArtist(UnDefType.NULL);
                        updateNowPlayingDescription(UnDefType.NULL);
                        updateNowPlayingGenre(UnDefType.NULL);
                        updateNowPlayingItemName(UnDefType.NULL);
                        updateNowPlayingStationLocation(UnDefType.NULL);
                        updateNowPlayingStationName(UnDefType.NULL);
                        updateNowPlayingTrack(UnDefType.NULL);
                    }
                } else if ("zone".equals(localName)) {
                    state = XMLHandlerState.Zone;
                } else if ("presets".equals(localName)) {
                    // reset the current playerPrests
                    playerPresets = new HashMap<>();
                    for (int i = 1; i <= 6; i++) {
                        playerPresets.put(i, null);
                    }
                    state = XMLHandlerState.Presets;
                } else if ("group".equals(localName)) {
                    this.masterDeviceId = new BoseSoundTouchConfiguration();
                    state = stateMap.get(localName);
                } else {
                    state = stateMap.get(localName);
                    if (state == null) {
                        if (logger.isDebugEnabled()) {
                            logger.warn("{}: Unhandled XML entity during {}: '{}", handler.getDeviceName(), curState,
                                    localName);
                        }
                        state = XMLHandlerState.Unprocessed;
                    } else if (state != XMLHandlerState.Volume && state != XMLHandlerState.Presets
                            && state != XMLHandlerState.Group
                            && state != XMLHandlerState.Unprocessed) {
                        if (!checkDeviceId(localName, attributes, false)) {
                            state = XMLHandlerState.Unprocessed;
                            break;
                        }
                    }
                }
                break;
            case Presets:
                if ("preset".equals(localName)) {
                    state = XMLHandlerState.Preset;
                    String id = attributes.getValue("id");
                    if (contentItem == null) {
                        contentItem = new ContentItem();
                    }
                    contentItem.setPresetID(Integer.parseInt(id));
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.warn("{}: Unhandled XML entity during {}: '{}'", handler.getDeviceName(), curState,
                                localName);
                    }
                    state = XMLHandlerState.Unprocessed;
                }
                break;
            case Sources:
                if ("sourceItem".equals(localName)) {
                    state = XMLHandlerState.Unprocessed;
                    String source = attributes.getValue("source");
                    String sourceAccount = attributes.getValue("sourceAccount");
                    String status = attributes.getValue("status");
                    if (status.equals("READY")) {
                        if (source.equals("AUX")) {
                            if (sourceAccount.equals("AUX")) {
                                commandExecutor.setAUXAvailable(true);
                            }
                            if (sourceAccount.equals("AUX1")) {
                                commandExecutor.setAUX1Available(true);
                            }
                            if (sourceAccount.equals("AUX2")) {
                                commandExecutor.setAUX2Available(true);
                            }
                            if (sourceAccount.equals("AUX3")) {
                                commandExecutor.setAUX3Available(true);
                            }
                        }
                        if (source.equals("STORED_MUSIC")) {
                            commandExecutor.setStoredMusicAvailable(true);
                        }
                        if (source.equals("INTERNET_RADIO")) {
                            commandExecutor.setInternetRadioAvailable(true);
                        }
                        if (source.equals("BLUETOOTH")) {
                            commandExecutor.setBluetoothAvailable(true);
                        }
                        if (source.equals("PRODUCT")) {
                            if (sourceAccount.equals("TV")) {
                                commandExecutor.setTVAvailable(true);
                            }
                            if (sourceAccount.equals("HDMI_1")) {
                                commandExecutor.setHDMI1Available(true);
                            }
                        }
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.warn("{}: Unhandled XML entity during {}: '{}'", handler.getDeviceName(), curState,
                                localName);
                    }
                    state = XMLHandlerState.Unprocessed;
                }
                break;
            // auto go trough the state map
            case Group:
            case Zone:
            case Bass:
            case ContentItem:
            case MasterDeviceId:
            case GroupName:
            case DeviceId:
            case DeviceIp:
            case Info:
            case NowPlaying:
            case Preset:
            case Updates:
            case Volume:
            case BassCapabilities:
                state = nextState(stateMap, curState, localName);
                break;
            // all entities without any children expected..
            case BassTarget:
            case BassActual:
            case BassUpdated:
            case BassMin:
            case BassMax:
            case BassDefault:
            case ContentItemItemName:
            case ContentItemContainerArt:
            case InfoName:
            case InfoType:
            case NowPlayingAlbum:
            case NowPlayingArt:
            case NowPlayingArtist:
            case NowPlayingGenre:
            case NowPlayingDescription:
            case NowPlayingPlayStatus:
            case NowPlayingRateEnabled:
            case NowPlayingSkipEnabled:
            case NowPlayingSkipPreviousEnabled:
            case NowPlayingStationLocation:
            case NowPlayingStationName:
            case NowPlayingTrack:
            case VolumeTarget:
            case VolumeActual:
            case VolumeUpdated:
            case VolumeMuteEnabled:
            case ZoneMember:
            case ZoneUpdated: // currently this dosn't provide any zone details..
                if (logger.isDebugEnabled()) {
                    logger.warn("{}: Unhandled XML entity during {}: '{}'", handler.getDeviceName(), curState,
                            localName);
                }
                state = XMLHandlerState.Unprocessed;
                break;
            case BassAvailable:
                if (logger.isDebugEnabled()) {
                    logger.warn("{}: Unhandled XML entity during {}: '{}'", handler.getDeviceName(), curState,
                            localName);
                }
                state = XMLHandlerState.Unprocessed;
                break;
            case Unprocessed:
                // all further things are also unprocessed
                state = XMLHandlerState.Unprocessed;
                break;
            case UnprocessedNoTextExpected:
                state = XMLHandlerState.UnprocessedNoTextExpected;
                break;
        }
        if (state == XMLHandlerState.ContentItem) {
            if (contentItem == null) {
                contentItem = new ContentItem();
            }
            contentItem.setSource(attributes.getValue("source"));
            contentItem.setSourceAccount(attributes.getValue("sourceAccount"));
            contentItem.setLocation(attributes.getValue("location"));
            contentItem.setPresetable(Boolean.parseBoolean(attributes.getValue("isPresetable")));
            for (int attrId = 0; attrId < attributes.getLength(); attrId++) {
                String attrName = attributes.getLocalName(attrId);
                if ("source".equalsIgnoreCase(attrName)) {
                    continue;
                }
                if ("location".equalsIgnoreCase(attrName)) {
                    continue;
                }
                if ("sourceAccount".equalsIgnoreCase(attrName)) {
                    continue;
                }
                if ("isPresetable".equalsIgnoreCase(attrName)) {
                    continue;
                }
                contentItem.setAdditionalAttribute(attrName, attributes.getValue(attrId));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        logger.trace("{}: endElement('{}')", handler.getDeviceName(), localName);
        final XMLHandlerState prevState = state;
        state = states.pop();
        switch (prevState) {
            case Info:
                commandExecutor.getInformations(APIRequest.VOLUME);
                commandExecutor.getInformations(APIRequest.PRESETS);
                commandExecutor.getInformations(APIRequest.NOW_PLAYING);
                commandExecutor.getInformations(APIRequest.GET_ZONE);
                commandExecutor.getInformations(APIRequest.BASS);
                commandExecutor.getInformations(APIRequest.SOURCES);
                commandExecutor.getInformations(APIRequest.BASSCAPABILITIES);
                commandExecutor.getInformations(APIRequest.GET_GROUP);
                break;
            case ContentItem:
                if (state == XMLHandlerState.NowPlaying) {
                    // update now playing name...
                    updateNowPlayingItemName(new StringType(contentItem.getItemName()));
                    commandExecutor.setCurrentContentItem(contentItem);
                }
                break;
            case Preset:
                if (state == XMLHandlerState.Presets) {
                    playerPresets.put(contentItem.getPresetID(), contentItem);
                    contentItem = null;
                }
                break;
            case NowPlaying:
                if (state == XMLHandlerState.MsgBody) {
                    updateRateEnabled(rateEnabled);
                    updateSkipEnabled(skipEnabled);
                    updateSkipPreviousEnabled(skipPreviousEnabled);
                }
                break;
            // handle special tags..
            case BassUpdated:
                // request current bass level
                commandExecutor.getInformations(APIRequest.BASS);
                break;
            case VolumeUpdated:
                commandExecutor.getInformations(APIRequest.VOLUME);
                break;
            case NowPlayingRateEnabled:
                rateEnabled = OnOffType.ON;
                break;
            case NowPlayingSkipEnabled:
                skipEnabled = OnOffType.ON;
                break;
            case NowPlayingSkipPreviousEnabled:
                skipPreviousEnabled = OnOffType.ON;
                break;
            case Volume:
                OnOffType muted = volumeMuteEnabled ? OnOffType.ON : OnOffType.OFF;
                commandExecutor.setCurrentMuted(volumeMuteEnabled);
                commandExecutor.postVolumeMuted(muted);
                break;
            case ZoneUpdated:
                commandExecutor.getInformations(APIRequest.GET_ZONE);
                break;
            case Presets:
                commandExecutor.updatePresetContainerFromPlayer(playerPresets);
                playerPresets = null;
                break;
            case Group:
                handler.handleGroupUpdated(masterDeviceId);
                break;
            default:
                // no actions...
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        logger.trace("{}: Text data during {}: '{}'", handler.getDeviceName(), state, new String(ch, start, length));
        super.characters(ch, start, length);
        switch (state) {
            case INIT:
            case Msg:
            case MsgHeader:
            case MsgBody:
            case Bass:
            case BassUpdated:
            case Updates:
            case Volume:
            case VolumeUpdated:
            case Info:
            case Preset:
            case Presets:
            case NowPlaying:
            case NowPlayingRateEnabled:
            case NowPlayingSkipEnabled:
            case NowPlayingSkipPreviousEnabled:
            case ContentItem:
            case UnprocessedNoTextExpected:
            case Zone:
            case ZoneUpdated:
            case Sources:
                logger.debug("{}: Unexpected text data during {}: '{}'", handler.getDeviceName(), state,
                        new String(ch, start, length));
                break;
            case BassMin: // @TODO - find out how to dynamically change "channel-type" bass configuration
            case BassMax: // based on these values...
            case BassDefault:
            case BassTarget:
            case VolumeTarget:
                // this are currently unprocessed values.
                break;
            case BassCapabilities:
                logger.debug("{}: Unexpected text data during {}: '{}'", handler.getDeviceName(), state,
                        new String(ch, start, length));
                break;
            case Unprocessed:
                // drop quietly..
                break;
            case BassActual:
                commandExecutor.updateBassLevelGUIState(new DecimalType(new String(ch, start, length)));
                break;
            case InfoName:
                setConfigOption(DEVICE_INFO_NAME, new String(ch, start, length));
                break;
            case InfoType:
                setConfigOption(DEVICE_INFO_TYPE, new String(ch, start, length));
                break;
            case BassAvailable:
                boolean bassAvailable = Boolean.parseBoolean(new String(ch, start, length));
                commandExecutor.setBassAvailable(bassAvailable);
                break;
            case NowPlayingAlbum:
                updateNowPlayingAlbum(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingArt:
                String url = new String(ch, start, length);
                if (url.startsWith("http")) {
                    // We download the cover art in a different thread to not delay the other operations
                    handler.getScheduler().submit(() -> {
                        RawType image = HttpUtil.downloadImage(url, true, 500000);
                        if (image != null) {
                            updateNowPlayingArtwork(image);
                        } else {
                            updateNowPlayingArtwork(UnDefType.UNDEF);
                        }
                    });
                } else {
                    updateNowPlayingArtwork(UnDefType.UNDEF);
                }
                break;
            case NowPlayingArtist:
                updateNowPlayingArtist(new StringType(new String(ch, start, length)));
                break;
            case ContentItemItemName:
                contentItem.setItemName(new String(ch, start, length));
                break;
            case ContentItemContainerArt:
                contentItem.setContainerArt(new String(ch, start, length));
                break;
            case NowPlayingDescription:
                updateNowPlayingDescription(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingGenre:
                updateNowPlayingGenre(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingPlayStatus:
                String playPauseState = new String(ch, start, length);
                if ("PLAY_STATE".equals(playPauseState) || "BUFFERING_STATE".equals(playPauseState)) {
                    commandExecutor.updatePlayerControlGUIState(PlayPauseType.PLAY);
                } else if ("STOP_STATE".equals(playPauseState) || "PAUSE_STATE".equals(playPauseState)) {
                    commandExecutor.updatePlayerControlGUIState(PlayPauseType.PAUSE);
                }
                break;
            case NowPlayingStationLocation:
                updateNowPlayingStationLocation(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingStationName:
                updateNowPlayingStationName(new StringType(new String(ch, start, length)));
                break;
            case NowPlayingTrack:
                updateNowPlayingTrack(new StringType(new String(ch, start, length)));
                break;
            case VolumeActual:
                commandExecutor.updateVolumeGUIState(new PercentType(Integer.parseInt(new String(ch, start, length))));
                break;
            case VolumeMuteEnabled:
                volumeMuteEnabled = Boolean.parseBoolean(new String(ch, start, length));
                commandExecutor.setCurrentMuted(volumeMuteEnabled);
                break;
            case MasterDeviceId:
                if (masterDeviceId != null) {
                    masterDeviceId.macAddress = new String(ch, start, length);
                }
                break;
            case GroupName:
                if (masterDeviceId != null) {
                    masterDeviceId.groupName = new String(ch, start, length);
                }
                break;
            case DeviceId:
                deviceId = new String(ch, start, length);
                break;
            case DeviceIp:
                if (masterDeviceId != null && Objects.equals(masterDeviceId.macAddress, deviceId)) {
                    masterDeviceId.host = new String(ch, start, length);
                }
                break;
            default:
                // do nothing
                break;
        }
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        super.skippedEntity(name);
    }

    private boolean checkDeviceId(String localName, Attributes attributes, boolean allowFromMaster) {
        String deviceID = attributes.getValue("deviceID");
        if (deviceID == null) {
            logger.warn("{}: No device-ID in entity {}", handler.getDeviceName(), localName);
            return false;
        }
        if (deviceID.equals(handler.getMacAddress())) {
            return true;
        }
        logger.warn("{}: Wrong device-ID in entity '{}': Got: '{}', expected: '{}'", handler.getDeviceName(), localName,
                deviceID, handler.getMacAddress());
        return false;
    }

    private void init() {
        states = new Stack<>();
        state = XMLHandlerState.INIT;
        nowPlayingSource = null;
    }

    private XMLHandlerState nextState(Map<String, XMLHandlerState> stateMap, XMLHandlerState curState,
            String localName) {
        XMLHandlerState state = stateMap.get(localName);
        if (state == null) {
            if (logger.isDebugEnabled()) {
                logger.warn("{}: Unhandled XML entity during {}: '{}'", handler.getDeviceName(), curState, localName);
            }
            state = XMLHandlerState.Unprocessed;
        }
        return state;
    }

    private void setConfigOption(String option, String value) {
        Map<String, String> prop = handler.getThing().getProperties();
        String cur = prop.get(option);
        if (cur == null || !cur.equals(value)) {
            logger.debug("{}: Option '{}' updated: From '{}' to '{}'", handler.getDeviceName(), option, cur, value);
            handler.getThing().setProperty(option, value);
        }
    }

    private void updateNowPlayingAlbum(State state) {
        handler.updateState(CHANNEL_NOWPLAYING_ALBUM, state);
    }

    private void updateNowPlayingArtwork(State state) {
        handler.updateState(CHANNEL_NOWPLAYING_ARTWORK, state);
    }

    private void updateNowPlayingArtist(State state) {
        handler.updateState(CHANNEL_NOWPLAYING_ARTIST, state);
    }

    private void updateNowPlayingDescription(State state) {
        handler.updateState(CHANNEL_NOWPLAYING_DESCRIPTION, state);
    }

    private void updateNowPlayingGenre(State state) {
        handler.updateState(CHANNEL_NOWPLAYING_GENRE, state);
    }

    private void updateNowPlayingItemName(State state) {
        handler.updateState(CHANNEL_NOWPLAYING_ITEMNAME, state);
    }

    private void updateNowPlayingStationLocation(State state) {
        handler.updateState(CHANNEL_NOWPLAYING_STATIONLOCATION, state);
    }

    private void updateNowPlayingStationName(State state) {
        handler.updateState(CHANNEL_NOWPLAYING_STATIONNAME, state);
    }

    private void updateNowPlayingTrack(State state) {
        handler.updateState(CHANNEL_NOWPLAYING_TRACK, state);
    }

    private void updateRateEnabled(OnOffType state) {
        handler.updateState(CHANNEL_RATEENABLED, state);
    }

    private void updateSkipEnabled(OnOffType state) {
        handler.updateState(CHANNEL_SKIPENABLED, state);
    }

    private void updateSkipPreviousEnabled(OnOffType state) {
        handler.updateState(CHANNEL_SKIPPREVIOUSENABLED, state);
    }
}
/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.handler;

import java.util.Set;

import org.eclipse.smarthome.binding.digitalstrom.DigitalSTROMBindingConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager.StructureManager;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.InternalScene;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link SceneHandler} is responsible for handling commands, which are sent to the channel of an
 * DigitalSTROM-Scene.<br>
 * For that it uses the {@link BridgeHandler} to execute the actual command and implements the
 * {@link SceneStatusListener} to get informed about changes from the accompanying {@link InternalScene}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public class SceneHandler extends BaseThingHandler implements SceneStatusListener {

    private Logger logger = LoggerFactory.getLogger(SceneHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(
            DigitalSTROMBindingConstants.THING_TYPE_APP_SCENE, DigitalSTROMBindingConstants.THING_TYPE_GROUP_SCENE,
            DigitalSTROMBindingConstants.THING_TYPE_ZONE_SCENE, DigitalSTROMBindingConstants.THING_TYPE_NAMED_SCENE);

    /**
     * Configured scene does not exist or cannot be used.
     */
    public final static String SCENE_WRONG = "sceneWrong";
    /**
     * Configured zone does not exist.
     */
    public final static String ZONE_WRONG = "zoneWrong";
    /**
     * Configured group does not exist.
     */
    public final static String GROUP_WRONG = "groupWrong";
    /**
     * StructureManager in BridgeHandler is null
     */
    public final static String NO_STRUC_MAN = "noStrucMan";
    /**
     * Configured scene is null.
     */
    public final static String NO_SCENE = "noScene";
    /**
     * BridgeHandler is null.
     */
    public final static String NO_BRIDGE = "noBridge";

    private BridgeHandler bridgeHandler = null;
    private InternalScene scene;
    private String sceneThingID = null;

    public SceneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SceneHandler");
        if (getBridge() != null) {
            bridgeStatusChanged(getBridge().getStatusInfo());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed... unregistering SceneStatusListener");
        if (sceneThingID != null) {
            BridgeHandler dssBridgeHandler = getBridgeHandler();
            if (dssBridgeHandler != null) {
                getBridgeHandler().unregisterSceneStatusListener(this);
            }
            sceneThingID = null;
            scene = null;
        }
    }

    @Override
    public void handleRemoval() {
        if (getBridgeHandler() != null) {
            this.bridgeHandler.childThingRemoved(sceneThingID);
        }
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            if (getBridgeHandler() != null) {
                String sceneID = getSceneID(getConfig(), bridgeHandler);
                switch (sceneID) {
                    case SCENE_WRONG:
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Configured scene '" + getConfig().get(DigitalSTROMBindingConstants.SCENE_ID)
                                        + "' does not exist or cannot be used, please check the configuration.");
                        break;
                    case ZONE_WRONG:
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Configured zone '" + getConfig().get(DigitalSTROMBindingConstants.SCENE_ZONE_ID)
                                        + "' does not exist, please check the configuration.");
                        break;
                    case GROUP_WRONG:
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Configured group '" + getConfig().get(DigitalSTROMBindingConstants.SCENE_GROUP_ID)
                                        + "' does not exist, please check the configuration.");
                        break;
                    case NO_STRUC_MAN:
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                                "Waiting for building digitalSTROM model.");
                        break;
                    case NO_SCENE:
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No Scene-ID is set!");
                        break;
                    default:
                        this.sceneThingID = sceneID;
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                                "Waiting for listener registration");
                        logger.debug("Set status on {}", getThing().getStatus());
                        this.bridgeHandler.registerSceneStatusListener(this);
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Checks the configuration and returns a unique Scene-ID or error string.<br>
     * The {@link StructureManager} of the {@link BridgeHandler} is used for checking the existing configured zone and
     * group. The {@link SceneEnum} will be used to check, if the configured scene exists and is allowed to use.<br>
     * If the check succeed the scene-ID will be returned in format "[zoneID]-[groupID]-[SceneID]", otherwise one of the
     * following errors {@link String}s will returned:
     * <li>{@link #SCENE_WRONG}: Configured scene does not exist or cannot be used.</li>
     * <li>{@link #ZONE_WRONG} Configured zone does not exist.</li>
     * <li>{@link #GROUP_WRONG}: Configured group does not exist.</li>
     * <li>{@link #NO_STRUC_MAN}: StructureManager in BridgeHandler is null.</li>
     * <li>{@link #NO_SCENE}: Configured scene is null.</li>
     * <li>{@link #NO_BRIDGE}: BridgeHandler is null.</li><br>
     *
     * @param configuration (must not be null)
     * @param bridgeHandler (can be null)
     * @return unique Scene-ID or error string
     */
    public static String getSceneID(Configuration configuration, BridgeHandler bridgeHandler) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration cannot be null");
        }
        if (bridgeHandler == null) {
            return NO_BRIDGE;
        }
        String configZoneID;
        String configGroupID;
        String configSceneID;

        short sceneID;
        int zoneID;
        short groupID;

        if (configuration.get(DigitalSTROMBindingConstants.SCENE_ZONE_ID) != null) {
            configZoneID = configuration.get(DigitalSTROMBindingConstants.SCENE_ZONE_ID).toString();
        } else {
            configZoneID = "";
        }
        if (configuration.get(DigitalSTROMBindingConstants.SCENE_GROUP_ID) != null) {
            configGroupID = configuration.get(DigitalSTROMBindingConstants.SCENE_GROUP_ID).toString();
        } else {
            configGroupID = "";
        }
        if (configuration.get(DigitalSTROMBindingConstants.SCENE_ID) != null) {
            configSceneID = configuration.get(DigitalSTROMBindingConstants.SCENE_ID).toString();
        } else {
            configSceneID = "";
        }
        if (!configSceneID.isEmpty()) {
            try {
                sceneID = Short.parseShort(configSceneID);
                if (!SceneEnum.containsScene(sceneID)) {
                    return SCENE_WRONG;
                }
            } catch (NumberFormatException e) {
                try {
                    sceneID = SceneEnum.valueOf(configSceneID.replace(" ", "_").toUpperCase()).getSceneNumber();
                } catch (IllegalArgumentException e1) {
                    return SCENE_WRONG;
                }
            }

            StructureManager strucMan = bridgeHandler.getStructureManager();
            if (strucMan != null) {
                if (configZoneID.isEmpty()) {
                    zoneID = 0;
                } else {
                    try {
                        zoneID = Integer.parseInt(configZoneID);
                        if (!strucMan.checkZoneID(zoneID)) {
                            return ZONE_WRONG;
                        }
                    } catch (NumberFormatException e) {
                        zoneID = strucMan.getZoneId(configZoneID);
                        if (zoneID == -1) {
                            return ZONE_WRONG;
                        }
                    }
                }

                if (configGroupID.isEmpty()) {
                    groupID = 0;
                } else {
                    try {
                        groupID = Short.parseShort(configGroupID);
                        if (!strucMan.checkZoneGroupID(zoneID, groupID)) {
                            return GROUP_WRONG;
                        }

                    } catch (NumberFormatException e) {
                        String zoneName = strucMan.getZoneName(zoneID);
                        groupID = strucMan.getZoneGroupId(zoneName, configGroupID);
                    }

                    if (groupID == -1) {
                        return GROUP_WRONG;
                    }
                }
                return zoneID + "-" + groupID + "-" + sceneID;
            } else {
                return NO_STRUC_MAN;
            }
        } else {
            return NO_SCENE;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        BridgeHandler dssBridgeHandler = getBridgeHandler();
        if (dssBridgeHandler == null) {
            logger.warn("BridgeHandler not found. Cannot handle command without bridge.");
            return;
        }

        if (channelUID.getId().equals(DigitalSTROMBindingConstants.CHANNEL_ID_SCENE)) {
            if (command instanceof OnOffType) {
                if (OnOffType.ON.equals(command)) {
                    this.bridgeHandler.sendSceneComandToDSS(scene, true);
                } else {
                    this.bridgeHandler.sendSceneComandToDSS(scene, false);
                }
            }
        } else {
            logger.warn("Command sent to an unknown channel id: {}", channelUID);
        }

    }

    private synchronized BridgeHandler getBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.debug("Bridge cannot be found");
                return null;
            }
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof BridgeHandler) {
                this.bridgeHandler = (BridgeHandler) handler;
            } else {
                logger.debug("BridgeHandler cannot be found");
                return null;
            }
        }
        return this.bridgeHandler;
    }

    @Override
    public void onSceneStateChanged(boolean flag) {
        if (flag) {
            updateState(new ChannelUID(getThing().getUID(), DigitalSTROMBindingConstants.CHANNEL_ID_SCENE),
                    OnOffType.ON);
        } else {
            updateState(new ChannelUID(getThing().getUID(), DigitalSTROMBindingConstants.CHANNEL_ID_SCENE),
                    OnOffType.OFF);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (scene != null && channelUID.getId().equals(DigitalSTROMBindingConstants.CHANNEL_ID_SCENE)) {
            onSceneStateChanged(scene.isActive());
        }
    }

    @Override
    public void onSceneRemoved(InternalScene scene) {
        scene = null;
        updateStatus(ThingStatus.OFFLINE);
        logger.debug("Set status on {}", getThing().getStatus());
    }

    @Override
    public void onSceneAdded(InternalScene scene) {
        logger.debug("Scene {} added", scene.getID());
        if (this.bridgeHandler != null) {
            ThingStatusInfo statusInfo = this.bridgeHandler.getThing().getStatusInfo();
            updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
            logger.debug("Set status on {}", getThing().getStatus());
        }
        this.scene = scene;
        onSceneStateChanged(scene.isActive());
    }

    @Override
    public String getSceneStatusListenerID() {
        return this.sceneThingID;
    }
}

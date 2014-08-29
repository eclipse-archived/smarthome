/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.handler;

import nl.q42.jue.FullLight;
import nl.q42.jue.HueBridge;
import nl.q42.jue.Light;
import nl.q42.jue.StateUpdate;

import org.eclipse.smarthome.binding.hue.config.HueLightConfiguration;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HueLightHandler} is the handler for a hue light. It uses the
 * {@link HueBridgeHandler} to execute the actual command.
 * 
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Oliver Libutzki
 * 
 */
public class HueLightHandler extends BaseThingHandler implements
        LightStatusListener {

    /**
     * ID of the color channel
     */
    public static final String CHANNEL_ID_COLOR = "color";

    /**
     * ID of the color temperature channel
     */
    public static final String CHANNEL_ID_COLOR_TEMPERATURE = "color_temperature";

    private final LightStateConverter lightStateConverter = new LightStateConverter();

    private Light light;
    private String lightId;

    private Logger logger = LoggerFactory.getLogger(HueLightHandler.class);

	private HueBridgeHandler bridgeHandler;

    public HueLightHandler(Thing hueLight) {
        super(hueLight);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hue light handler.");
        final String configLightId = getConfigAs(HueLightConfiguration.class).lightId;
        if (configLightId != null) {
            lightId = configLightId;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposes. Unregistering listener.");
        if (lightId != null) {
            HueBridgeHandler bridgeHandler = getHueBridgeHandler();
        	if(bridgeHandler!=null) {
        		getHueBridgeHandler().unregisterLightStatusListener(this);
        	}
            lightId = null;
        }
    }

    private Light getLight() {
        if (light == null) {
            HueBridgeHandler bridgeHandler = getHueBridgeHandler();
        	if(bridgeHandler!=null) {
        		light = bridgeHandler.getLightById(lightId);
        	}
        }
        return light;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        HueBridgeHandler hueBridge = getHueBridgeHandler();
        if (hueBridge == null) {
            logger.warn("hue bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        switch (channelUID.getId()) {

        case CHANNEL_ID_COLOR_TEMPERATURE:
            if (command instanceof PercentType) {
                StateUpdate lightState = lightStateConverter
                        .toColorTemperatureLightState((PercentType) command);
                hueBridge.updateLightState(getLight(), lightState);
            } else if (command instanceof OnOffType) {
                StateUpdate lightState = lightStateConverter
                        .toColorLightState((OnOffType) command);
                hueBridge.updateLightState(getLight(), lightState);
            }
            // TODO: support increase and decrease
            break;
        case CHANNEL_ID_COLOR:
            if (command instanceof HSBType) {
                StateUpdate lightState = lightStateConverter.toColorLightState((HSBType) command);
                hueBridge.updateLightState(getLight(), lightState);
            } else if (command instanceof PercentType) {
                StateUpdate lightState = lightStateConverter
                        .toColorLightState((PercentType) command);
                hueBridge.updateLightState(getLight(), lightState);
            } else if (command instanceof OnOffType) {
                StateUpdate lightState = lightStateConverter
                        .toColorLightState((OnOffType) command);
                hueBridge.updateLightState(getLight(), lightState);
            }
            // TODO: support increase and decrease
            break;
        default:
            logger.warn("Command send to an unknown channel id: " + channelUID);
            break;
        }

    }


    private synchronized HueBridgeHandler getHueBridgeHandler() {
    	if(this.bridgeHandler==null) {
	    	Bridge bridge = getBridge();
	        if (bridge == null) {
	            return null;
	        }
	        ThingHandler handler = bridge.getHandler();
	        if (handler instanceof HueBridgeHandler) {
	        	this.bridgeHandler = (HueBridgeHandler) handler;
	        	this.bridgeHandler.registerLightStatusListener(this);
	        } else {
	            return null;
	        }
    	}
        return this.bridgeHandler;
    }

    @Override
    public void onLightStateChanged(HueBridge bridge, FullLight fullLight) {
        if (fullLight.getId().equals(lightId)) {
            HSBType hsbType = lightStateConverter.toHSBType(fullLight.getState());
            updateState(new ChannelUID(getThing().getUID(),  CHANNEL_ID_COLOR), hsbType);

            PercentType percentType = lightStateConverter.toColorTemperaturePercentType(fullLight
                    .getState());
            updateState(new ChannelUID(getThing().getUID(),  CHANNEL_ID_COLOR_TEMPERATURE), percentType);
        }

    }

    @Override
    public void onLightRemoved(HueBridge bridge, FullLight light) {
        if (light.getId().equals(lightId)) {
            dispose();
        }
    }

    @Override
    public void onLightAdded(HueBridge bridge, FullLight light) {
        // do nothing
    }


}

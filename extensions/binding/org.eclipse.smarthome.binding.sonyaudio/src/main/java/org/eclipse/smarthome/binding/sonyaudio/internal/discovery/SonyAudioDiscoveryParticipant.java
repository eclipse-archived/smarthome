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
package org.eclipse.smarthome.binding.sonyaudio.internal.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.sonyaudio.SonyAudioBindingConstants;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class identifies SONY products by their Upnp service information.
 *
 * @author David Åberg - Initial contribution
 */
@Component(immediate = true)
public class SonyAudioDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(SonyAudioDiscoveryParticipant.class);

    private Set<ThingTypeUID> supportedThingTypes;

    public SonyAudioDiscoveryParticipant() {
        this.supportedThingTypes = SonyAudioBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return supportedThingTypes;
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        DiscoveryResult result = null;

        ThingUID thingUid = getThingUID(device);
        if (thingUid != null) {
            String label = StringUtils.isEmpty(device.getDetails().getFriendlyName()) ? device.getDisplayString()
                    : device.getDetails().getFriendlyName();
            String host = device.getIdentity().getDescriptorURL().getHost();
            int port = device.getIdentity().getDescriptorURL().getPort();
            String path = device.getIdentity().getDescriptorURL().getPath();
            try {
                Map<String, Object> properties = getDescription(host, port, path);
                properties.put(SonyAudioBindingConstants.HOST_PARAMETER,
                        device.getIdentity().getDescriptorURL().getHost());
                result = DiscoveryResultBuilder.create(thingUid).withLabel(label).withProperties(properties).build();
            } catch (IOException e) {
                return null;
            }
        }
        return result;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        ThingUID result = null;

        if (!StringUtils.containsIgnoreCase(device.getDetails().getManufacturerDetails().getManufacturer(),
                SonyAudioBindingConstants.MANUFACTURER)) {
            return result;
        }

        logger.debug("Manufacturer matched: search: {}, device value: {}.", SonyAudioBindingConstants.MANUFACTURER,
                device.getDetails().getManufacturerDetails().getManufacturer());
        if (!StringUtils.containsIgnoreCase(device.getType().getType(), SonyAudioBindingConstants.UPNP_DEVICE_TYPE)) {
            return result;
        }
        logger.debug("Device type matched: search: {}, device value: {}.", SonyAudioBindingConstants.UPNP_DEVICE_TYPE,
                device.getType().getType());
        logger.debug("Device services: {}", device.getServices().toString());
        String deviceModel = device.getDetails().getModelDetails() != null
                ? device.getDetails().getModelDetails().getModelName()
                : null;
        logger.debug("Device model: {}.", deviceModel);
        ThingTypeUID thingTypeUID = findThingType(deviceModel);
        if (thingTypeUID != null) {
            result = new ThingUID(thingTypeUID, device.getIdentity().getUdn().getIdentifierString());
        }
        return result;
    }

    private ThingTypeUID findThingType(String deviceModel) {
        ThingTypeUID thingTypeUID = null;
        for (ThingTypeUID thingType : SonyAudioBindingConstants.SUPPORTED_THING_TYPES_UIDS) {
            if (thingType.getId().equalsIgnoreCase(deviceModel)) {
                return thingType;
            }
        }

        return thingTypeUID;
    }

    private Map<String, Object> getDescription(String host, int port, String path) throws IOException {
        Map<String, Object> properties = new HashMap<>(2, 1);
        URL url = new URL("http", host, port, path);
        logger.debug("URL: {}", url.toString());
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String s;
            StringBuilder builder = new StringBuilder();
            while ((s = bufferedReader.readLine()) != null) {
                builder.append(s);
            }
            Pattern ScalarWebAPImatch = Pattern.compile("<av:X_ScalarWebAPI_BaseURL>(.*)</av:X_ScalarWebAPI_BaseURL>");
            Pattern baseURLmatch = Pattern.compile("http://(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d+)([^<]*)");

            Matcher tagmatch = ScalarWebAPImatch.matcher(builder.toString());
            if (tagmatch.find()) {
                Matcher matcher = baseURLmatch.matcher(tagmatch.group());
                matcher.find();
                // String scalar_host = matcher.group(0);
                int scalar_port = Integer.parseInt(matcher.group(2));
                String scalar_path = matcher.group(3);

                properties.put(SonyAudioBindingConstants.SCALAR_PORT_PARAMETER, scalar_port);
                properties.put(SonyAudioBindingConstants.SCALAR_PATH_PARAMETER, scalar_path);
            }
            return properties;
        }
    }
}

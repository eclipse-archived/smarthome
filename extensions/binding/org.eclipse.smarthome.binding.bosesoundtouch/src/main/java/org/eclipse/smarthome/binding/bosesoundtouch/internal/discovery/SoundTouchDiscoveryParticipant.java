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
package org.eclipse.smarthome.binding.bosesoundtouch.internal.discovery;

import static org.eclipse.smarthome.binding.bosesoundtouch.BoseSoundTouchBindingConstants.*;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.smarthome.binding.bosesoundtouch.BoseSoundTouchConfiguration;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoundTouchDiscoveryParticipant} is responsible processing the
 * results of searches for mDNS services of type _soundtouch._tcp.local.
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer - Initial contribution
 */
@Component(immediate = true, configurationPid = "discovery.bosesoundtouch")
public class SoundTouchDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(SoundTouchDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public DiscoveryResult createResult(ServiceInfo info) {
        DiscoveryResult result = null;
        ThingUID uid = getThingUID(info);
        if (uid != null) {

            // remove the domain from the name
            InetAddress[] addrs = info.getInetAddresses();

            Map<String, Object> properties = new HashMap<>(2);
            
            String label = null;
            if (BST_10_THING_TYPE_UID.equals(uid.getThingTypeUID())) {
                try {
                    String group = DiscoveryUtil
                            .executeUrl("http://" + addrs[0].getHostAddress() + ":8090/getGroup");
                    label = DiscoveryUtil.getContentOfFirstElement(group, "name");
                } catch (IOException e) {
                    logger.debug("Can't obtain label for group. Will use the default one");
                }
            }

            if (label == null || label.isEmpty()) {
                label = info.getName();
            }
            
            if (label == null || label.isEmpty()) {
                label = "Bose SoundTouch";
            }

            // we expect only one address per device..
            if (addrs.length > 1) {
                logger.warn("Bose SoundTouch device {} ({}) reports multiple addresses - using the first one: {}",
                        info.getName(), label, Arrays.toString(addrs));
            }

            properties.put(BoseSoundTouchConfiguration.HOST, addrs[0].getHostAddress());
            if (getMacAddress(info) != null) {
                properties.put(BoseSoundTouchConfiguration.MAC_ADDRESS, new String(getMacAddress(info)));
            }
            return DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label).withTTL(600).build();
        }
        return result;
    }

    @Override
    public ThingUID getThingUID(ServiceInfo info) {
        logger.trace("ServiceInfo: {}", info);
        ThingTypeUID typeUID = getThingTypeUID(info);
        if (typeUID != null) {
            if (info.getType() != null) {
                if (info.getType().equals(getServiceType())) {
                    logger.trace("Discovered a Bose SoundTouch thing with name '{}'", info.getName());
                    byte[] mac = getMacAddress(info);
                    if (mac != null) {
                        return new ThingUID(typeUID, new String(mac));
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getServiceType() {
        return "_soundtouch._tcp.local.";
    }

    private ThingTypeUID getThingTypeUID(ServiceInfo info) {
        InetAddress[] addrs = info.getInetAddresses();
        if (addrs.length > 0) {
            String ip = addrs[0].getHostAddress();
            String deviceId = null;
            byte[] mac = getMacAddress(info);
            if (mac != null) {
                deviceId = new String(mac);
            }
            String deviceType;
            try {
                String content = DiscoveryUtil.executeUrl("http://" + ip + ":8090/info");
                deviceType = DiscoveryUtil.getContentOfFirstElement(content, "type");
            } catch (IOException e) {
                return null;
            }

            if (deviceType.toLowerCase().contains("soundtouch 10")) {
                // Check if it's a Stereo Pair
                try {
                    String group = DiscoveryUtil.executeUrl("http://" + ip + ":8090/getGroup");
                    String masterDevice = DiscoveryUtil.getContentOfFirstElement(group, "masterDeviceId");

                    if (Objects.equals(deviceId, masterDevice)) {
                        // Stereo Pair - Master Device
                        return BST_10_THING_TYPE_UID;
                    } else if (!masterDevice.isEmpty()) {
                        // Stereo Pair - Secondary Device - should not be paired
                        return null;
                    } else {
                        // Single player
                        return BST_10_THING_TYPE_UID;
                    }
                } catch (IOException e) {
                    return null;
                }
            }
            if (deviceType.toLowerCase().contains("soundtouch 20")) {
                return BST_20_THING_TYPE_UID;
            }
            if (deviceType.toLowerCase().contains("soundtouch 300")) {
                return BST_300_THING_TYPE_UID;
            }
            if (deviceType.toLowerCase().contains("soundtouch 30")) {
                return BST_30_THING_TYPE_UID;
            }
            if (deviceType.toLowerCase().contains("soundtouch wireless link adapter")) {
                return BST_WLA_THING_TYPE_UID;
            }
            if (deviceType.toLowerCase().contains("wave")) {
                return BST_WSMS_THING_TYPE_UID;
            }
            if (deviceType.toLowerCase().contains("amplifier")) {
                return BST_SA5A_THING_TYPE_UID;
            }
            return null;
        }
        return null;
    }

    private byte[] getMacAddress(ServiceInfo info) {
        if (info != null) {
            // sometimes we see empty messages - ignore them
            if (!info.hasData()) {
                return null;
            }
            byte[] mac = info.getPropertyBytes("MAC");
            if (mac == null) {
                logger.warn("SoundTouch Device {} delivered no MAC address!", info.getName());
                return null;
            }
            if (mac.length != 12) {
                BigInteger bi = new BigInteger(1, mac);
                logger.warn("SoundTouch Device {} delivered an invalid MAC address: 0x{}", info.getName(),
                        String.format("%0" + (mac.length << 1) + "X", bi));
                return null;
            }
            return mac;
        }
        return null;
    }
}

/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.fsinternetradio.internal;

import static org.eclipse.smarthome.binding.fsinternetradio.FSInternetRadioBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;

/**
 * This is the discovery service for internet radios based on the fontier silicon chipset. Unfortunately, it is not
 * easily possible to detect from the upnp information which devices are supported. So currently, discovery only works
 * for medion internet radios. {@link FSInternetRadioDiscoveryParticipant#getThingUID(RemoteDevice)} must be extended to
 * add further supported devices!
 *
 * @author Patrick Koenemann - Initial contribution
 * @author Mihaela Memova - removed the getLabel(RemoteDevice device) method due to its unreachable code lines
 */
public class FSInternetRadioDiscoveryParticipant implements UpnpDiscoveryParticipant {

    /** Map from UPnP manufacturer to model number for supported radios; filled in static initializer below. */
    private static final Map<String, Set<String>> SUPPORTED_RADIO_MODELS = new HashMap<String, Set<String>>();

    static {
        // to allow case-insensitive match: add all values UPPER-CASE!

        // format: MANUFACTURER -> MODEL NAME, as shown e.g. by UPnP Tester as explained here:
        // https://community.openhab.org/t/internet-radio-i-need-your-help/2131

        // list of medion internet radios taken from: http://internetradio.medion.com/
        final Set<String> medionRadios = new HashSet<String>();
        SUPPORTED_RADIO_MODELS.put("MEDION AG", medionRadios);
        medionRadios.add("MD83813");
        medionRadios.add("MD84017");
        medionRadios.add("MD85651");
        medionRadios.add("MD86062");
        medionRadios.add("MD86250");
        medionRadios.add("MD86562");
        medionRadios.add("MD86672");
        medionRadios.add("MD86698");
        medionRadios.add("MD86869");
        medionRadios.add("MD86891");
        medionRadios.add("MD86955");
        medionRadios.add("MD86988");
        medionRadios.add("MD87090");
        medionRadios.add("MD87180");
        medionRadios.add("MD87238");
        medionRadios.add("MD87267");

        // list of hama internet radios taken from:
        // https://www.hama.com/action/searchCtrl/search?searchMode=1&q=Internet%20Radio
        final Set<String> hamaRadios = new HashSet<String>();
        SUPPORTED_RADIO_MODELS.put("HAMA", hamaRadios);
        hamaRadios.add("IR100");
        hamaRadios.add("IR110");
        hamaRadios.add("IR250");
        hamaRadios.add("IR320");
        hamaRadios.add("DIR3000");
        hamaRadios.add("DIR3100");
        hamaRadios.add("DIR3110");

        // as reported in: https://community.openhab.org/t/internet-radio-i-need-your-help/2131/5
        final Set<String> ttmicroRadios = new HashSet<String>();
        SUPPORTED_RADIO_MODELS.put("TTMICRO AS", ttmicroRadios);
        ttmicroRadios.add("PINELL SUPERSOUND");

        // as reported in: https://community.openhab.org/t/internet-radio-i-need-your-help/2131/7
        final Set<String> revoRadios = new HashSet<String>();
        SUPPORTED_RADIO_MODELS.put("REVO TECHNOLOGIES LTD", revoRadios);
        revoRadios.add("SUPERCONNECT");

        // as reported in: https://community.openhab.org/t/internet-radio-i-need-your-help/2131/10
        final Set<String> robertsRadios = new HashSet<String>();
        SUPPORTED_RADIO_MODELS.put("ROBERTS RADIO LIMITED", robertsRadios);
        robertsRadios.add("ROBERTS STREAM 93I");

        // as reported in: https://community.openhab.org/t/internet-radio-i-need-your-help/2131/11
        final Set<String> AUNARadios = new HashSet<String>();
        SUPPORTED_RADIO_MODELS.put("AUNA", AUNARadios);
        AUNARadios.add("CONNECT 150");
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(THING_TYPE_RADIO);
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        final ThingUID uid = getThingUID(device);
        if (uid != null) {
            final Map<String, Object> properties = new HashMap<>(1);
            final String ip = getIp(device);
            if (ip != null) {
                properties.put(CONFIG_PROPERTY_IP, ip);

                // add manufacturer and model, if provided
                final String manufacturer = getManufacturer(device);
                if (manufacturer != null) {
                    properties.put(PROPERTY_MANUFACTURER, manufacturer);
                }
                final String model = getModel(device);
                if (model != null) {
                    properties.put(PROPERTY_MODEL, model);
                }

                final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                        .withLabel(device.getDisplayString()).build();
                return result;
            }
        }
        return null;
    }

    private String getManufacturer(RemoteDevice device) {
        final DeviceDetails details = device.getDetails();
        if (details != null) {
            if (details.getManufacturerDetails() != null) {
                return details.getManufacturerDetails().getManufacturer();
            }
        }
        return null;
    }

    private String getModel(RemoteDevice device) {
        final DeviceDetails details = device.getDetails();
        if (details != null) {
            if (details.getModelDetails() != null) {
                return details.getModelDetails().getModelNumber();
            }
        }
        return null;
    }

    private String getIp(RemoteDevice device) {
        final DeviceDetails details = device.getDetails();
        if (details != null) {
            if (details.getBaseURL() != null) {
                return details.getBaseURL().getHost();
            }
        }
        final RemoteDeviceIdentity identity = device.getIdentity();
        if (identity != null) {
            if (identity.getDescriptorURL() != null) {
                return identity.getDescriptorURL().getHost();
            }
        }
        return null;
    }

    /**
     * If <code>device</code> is a supported device, a unique thing ID (e.g. serial number) must be returned. Further
     * supported devices should be added here, based on the available UPnP information.
     */
    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        final DeviceDetails details = device.getDetails();
        if (details != null) {
            final ManufacturerDetails manufacturerDetails = details.getManufacturerDetails();
            final String manufacturer = manufacturerDetails == null ? null : manufacturerDetails.getManufacturer();
            final ModelDetails modelDetails = details.getModelDetails();
            if (modelDetails != null) {
                // check manufacturer and model number
                final String modelNumber = modelDetails.getModelNumber();
                if (modelNumber != null) {
                    if (manufacturer != null) {
                        final Set<String> supportedRadios = SUPPORTED_RADIO_MODELS
                                .get(manufacturer.trim().toUpperCase());
                        if (supportedRadios != null && supportedRadios.contains(modelNumber.toUpperCase())) {
                            return new ThingUID(THING_TYPE_RADIO, details.getSerialNumber());
                        }
                    }
                    // check model name and number
                    final String modelName = modelDetails.getModelName();
                    if (modelName != null) {
                        final Set<String> supportedRadios = SUPPORTED_RADIO_MODELS.get(modelName.trim().toUpperCase());
                        if (supportedRadios != null && supportedRadios.contains(modelNumber.toUpperCase())) {
                            return new ThingUID(THING_TYPE_RADIO, details.getSerialNumber());
                        }
                    }
                }
            }
            // maybe we can add further indicators, whether the device is a supported one
        }
        // device not supported
        return null;
    }
}

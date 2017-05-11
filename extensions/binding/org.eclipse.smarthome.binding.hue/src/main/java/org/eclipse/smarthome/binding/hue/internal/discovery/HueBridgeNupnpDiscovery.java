/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal.discovery;

import static org.eclipse.smarthome.binding.hue.HueBindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link HueBridgeNupnpDiscovery} is responsible for discovering new hue bridges. It uses the 'NUPnP service
 * provided by Philips'.
 *
 * @author Awelkiyar Wehabrebi - Initial contribution
 * @author Christoph Knauf - Refactorings
 * @author Andre Fuechsel - make {@link #startScan()} asynchronous
 */
public class HueBridgeNupnpDiscovery extends AbstractDiscoveryService {

    private static final String MODEL_NAME_PHILIPS_HUE = "<modelName>Philips hue";

    protected static final String BRIDGE_INDICATOR = "fffe";

    private static final String DISCOVERY_URL = "https://www.meethue.com/api/nupnp";

    protected static final String LABEL_PATTERN = "Philips hue (IP)";

    private static final String DESC_URL_PATTERN = "http://HOST/description.xml";

    private static final int REQUEST_TIMEOUT = 5000;

    private static final int DISCOVERY_TIMEOUT = 10;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private final Logger logger = LoggerFactory.getLogger(HueBridgeNupnpDiscovery.class);

    public HueBridgeNupnpDiscovery() {
        super(SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT, false);
    }

    @Override
    protected void startScan() {
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                discoverHueBridges();
            }
        }, 0, TimeUnit.SECONDS);
    }

    /**
     * Discover available Hue Bridges and then add them in the discovery inbox
     */
    private void discoverHueBridges() {
        for (BridgeJsonParameters bridge : getBridgeList()) {
            if (isReachableAndValidHueBridge(bridge)) {
                String host = bridge.getInternalIpAddress();
                String serialNumber = bridge.getId().substring(0, 6) + bridge.getId().substring(10);
                ThingUID uid = new ThingUID(THING_TYPE_BRIDGE, serialNumber);
                DiscoveryResult result = DiscoveryResultBuilder.create(uid)
                        .withProperties(buildProperties(host, serialNumber))
                        .withLabel(LABEL_PATTERN.replace("IP", host)).withRepresentationProperty(SERIAL_NUMBER).build();
                thingDiscovered(result);
            }
        }
    }

    /**
     * Builds the bridge properties.
     *
     * @param host the ip of the bridge
     * @param serialNumber the id of the bridge
     * @return the bridge properties
     */
    private Map<String, Object> buildProperties(String host, String serialNumber) {
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(HOST, host);
        properties.put(SERIAL_NUMBER, serialNumber);
        return properties;
    }

    /**
     * Checks if the Bridge is a reachable Hue Bridge with a valid id.
     *
     * @param bridge the {@link BridgeJsonParameters}s
     * @return true if Bridge is a reachable Hue Bridge with a id containing
     *         BRIDGE_INDICATOR longer then 10
     */
    private boolean isReachableAndValidHueBridge(BridgeJsonParameters bridge) {
        String host = bridge.getInternalIpAddress();
        String id = bridge.getId();
        String description;
        if (host == null) {
            logger.debug("Bridge not discovered: ip is null");
            return false;
        }
        if (id == null) {
            logger.debug("Bridge not discovered: id is null");
            return false;
        }
        if (id.length() < 10) {
            logger.debug("Bridge not discovered: id {} is shorter then 10.", id);
            return false;
        }
        if (!id.substring(6, 10).equals(BRIDGE_INDICATOR)) {
            logger.debug(
                    "Bridge not discovered: id {} does not contain bridge indicator {} or its at the wrong position.",
                    id, BRIDGE_INDICATOR);
            return false;
        }
        try {
            description = doGetRequest(DESC_URL_PATTERN.replace("HOST", host));
        } catch (IOException e) {
            logger.debug("Bridge not discovered: Failure accessing description file for ip: {}", host);
            return false;
        }
        if (!description.contains(MODEL_NAME_PHILIPS_HUE)) {
            logger.debug("Bridge not discovered: Description does not containing the model name: {}", description);
            return false;
        }
        return true;
    }

    /**
     * Use the Philips Hue NUPnP service to find Hue Bridges in local Network.
     *
     * @return a list of available Hue Bridges
     */
    private List<BridgeJsonParameters> getBridgeList() {
        try {
            Gson gson = new Gson();
            String json = doGetRequest(DISCOVERY_URL);
            return gson.fromJson(json, new TypeToken<List<BridgeJsonParameters>>() {
            }.getType());
        } catch (IOException e) {
            logger.debug("Philips Hue NUPnP service not reachable. Can't discover bridges");
        } catch (JsonParseException je) {
            logger.debug("Invalid json respone from Hue NUPnP service. Can't discover bridges");
        }
        return new ArrayList<>();
    }

    /**
     * Introduced in order to enable testing.
     *
     * @param url the url
     * @return the http request result as String
     * @throws IOException if request failed
     */
    protected String doGetRequest(String url) throws IOException {
        return HttpUtil.executeUrl("GET", url, REQUEST_TIMEOUT);
    }

}

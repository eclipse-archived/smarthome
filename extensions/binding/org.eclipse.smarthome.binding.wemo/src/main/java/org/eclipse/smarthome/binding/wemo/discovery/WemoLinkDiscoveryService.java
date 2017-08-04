/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.discovery;

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.*;

import java.io.StringReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.wemo.handler.WemoBridgeHandler;
import org.eclipse.smarthome.binding.wemo.internal.http.WemoHttpCall;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * The {@link WemoLinkDiscoveryService} is responsible for discovering new and
 * removed WeMo devices connected to the WeMo Link Bridge.
 *
 * @author Hans-Jörg Merk - Initial contribution
 *
 */
public class WemoLinkDiscoveryService extends AbstractDiscoveryService implements UpnpIOParticipant {

    private Logger logger = LoggerFactory.getLogger(WemoLinkDiscoveryService.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_MZ100);

    public static final String NORMALIZE_ID_REGEX = "[^a-zA-Z0-9_]";

    /**
     * Maximum time to search for devices in seconds.
     */
    private final static int SEARCH_TIME = 20;

    /**
     * Initial delay for scanning job in seconds.
     */
    private final static int INITIAL_DELAY = 5;

    /**
     * Scan interval for scanning job in seconds.
     */
    private final static int SCAN_INTERVAL = 120;

    /**
     * The handler for WeMo Link bridge
     */
    private WemoBridgeHandler wemoBridgeHandler;

    /**
     * Job which will do the background scanning
     */
    private WemoLinkScan scanningRunnable;

    /**
     * Schedule for scanning
     */
    private ScheduledFuture<?> scanningJob;

    /**
     * The Upnp service
     */
    private UpnpIOService service;

    public WemoLinkDiscoveryService(WemoBridgeHandler wemoBridgeHandler, UpnpIOService upnpIOService) {
        super(SEARCH_TIME);
        this.wemoBridgeHandler = wemoBridgeHandler;

        if (upnpIOService != null) {
            this.service = upnpIOService;
        } else {
            logger.debug("upnpIOService not set.");
        }

        this.scanningRunnable = new WemoLinkScan();
        if (wemoBridgeHandler == null) {
            logger.warn("no bridge handler for scan given");
        }
        this.activate(null);
    }

    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startScan() {

        logger.trace("Starting WeMoEndDevice discovery on WeMo Link {}", wemoBridgeHandler.getThing().getUID());
        try {

            String devUDN = "uuid:" + wemoBridgeHandler.getThing().getConfiguration().get(UDN).toString();
            logger.trace("devUDN = '{}'", devUDN);

            String soapHeader = "\"urn:Belkin:service:bridge:1#GetEndDevices\"";
            String content = "<?xml version=\"1.0\"?>"
                    + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                    + "<s:Body>" + "<u:GetEndDevices xmlns:u=\"urn:Belkin:service:bridge:1\">" + "<DevUDN>" + devUDN
                    + "</DevUDN><ReqListType>PAIRED_LIST</ReqListType>" + "</u:GetEndDevices>" + "</s:Body>"
                    + "</s:Envelope>";

            URL descriptorURL = service.getDescriptorURL(this);

            if (descriptorURL != null) {
                String deviceURL = StringUtils.substringBefore(descriptorURL.toString(), "/setup.xml");
                String wemoURL = deviceURL + "/upnp/control/bridge1";

                String endDeviceRequest = WemoHttpCall.executeCall(wemoURL, soapHeader, content);

                if (endDeviceRequest != null) {
                    logger.trace("endDeviceRequest answered '{}'", endDeviceRequest);

                    try {
                        String stringParser = StringUtils.substringBetween(endDeviceRequest, "<DeviceLists>",
                                "</DeviceLists>");

                        stringParser = StringEscapeUtils.unescapeXml(stringParser);

                        // check if there are already paired devices with WeMo Link
                        if ("0".equals(stringParser)) {
                            logger.debug("There are no devices connected with WeMo Link. Exit discovery");
                            return;
                        }

                        // Build parser for received <DeviceList>
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        InputSource is = new InputSource();
                        is.setCharacterStream(new StringReader(stringParser));

                        Document doc = db.parse(is);
                        NodeList nodes = doc.getElementsByTagName("DeviceInfo");

                        // iterate the devices
                        for (int i = 0; i < nodes.getLength(); i++) {
                            Element element = (Element) nodes.item(i);

                            NodeList deviceIndex = element.getElementsByTagName("DeviceIndex");
                            Element line = (Element) deviceIndex.item(0);
                            logger.trace("DeviceIndex: {}", getCharacterDataFromElement(line));

                            NodeList deviceID = element.getElementsByTagName("DeviceID");
                            line = (Element) deviceID.item(0);
                            String endDeviceID = getCharacterDataFromElement(line);
                            logger.trace("DeviceID: {}", endDeviceID);

                            NodeList friendlyName = element.getElementsByTagName("FriendlyName");
                            line = (Element) friendlyName.item(0);
                            String endDeviceName = getCharacterDataFromElement(line);
                            logger.trace("FriendlyName: {}", endDeviceName);

                            NodeList vendor = element.getElementsByTagName("Manufacturer");
                            line = (Element) vendor.item(0);
                            String endDeviceVendor = getCharacterDataFromElement(line);
                            logger.trace("Manufacturer: {}", endDeviceVendor);

                            NodeList model = element.getElementsByTagName("ModelCode");
                            line = (Element) model.item(0);
                            String endDeviceModelID = getCharacterDataFromElement(line);
                            endDeviceModelID = endDeviceModelID.replaceAll(NORMALIZE_ID_REGEX, "_");

                            logger.trace("ModelCode: {}", endDeviceModelID);

                            if (SUPPORTED_THING_TYPES.contains(new ThingTypeUID(BINDING_ID, endDeviceModelID))) {
                                logger.debug("Discovered a WeMo LED Light thing with ID '{}'", endDeviceID);

                                ThingUID bridgeUID = wemoBridgeHandler.getThing().getUID();
                                ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, endDeviceModelID);

                                if (thingTypeUID.equals(THING_TYPE_MZ100)) {
                                    String thingLightId = endDeviceID;
                                    ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, thingLightId);

                                    Map<String, Object> properties = new HashMap<>(1);
                                    properties.put(DEVICE_ID, endDeviceID);

                                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                            .withProperties(properties)
                                            .withBridge(wemoBridgeHandler.getThing().getUID()).withLabel(endDeviceName)
                                            .build();

                                    thingDiscovered(discoveryResult);
                                }

                            } else {
                                logger.debug("Discovered an unsupported device :");
                                logger.debug("DeviceIndex : {}", getCharacterDataFromElement(line));
                                logger.debug("DeviceID    : {}", endDeviceID);
                                logger.debug("FriendlyName: {}", endDeviceName);
                                logger.debug("Manufacturer: {}", endDeviceVendor);
                                logger.debug("ModelCode   : {}", endDeviceModelID);
                            }

                        }
                    } catch (Exception e) {
                        logger.error("Failed to parse endDevices for bridge '{}'",
                                wemoBridgeHandler.getThing().getUID(), e);
                    }
                }

            }
        } catch (Exception e) {
            logger.error("Failed to get endDevices for bridge '{}'", wemoBridgeHandler.getThing().getUID(), e);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.trace("Start WeMo device background discovery");

        if (scanningJob == null || scanningJob.isCancelled()) {
            this.scanningJob = AbstractDiscoveryService.scheduler.scheduleWithFixedDelay(this.scanningRunnable,
                    INITIAL_DELAY, SCAN_INTERVAL, TimeUnit.SECONDS);
        } else {
            logger.trace("scanningJob active");
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("Stop WeMo device background discovery");

        if (scanningJob != null && !scanningJob.isCancelled()) {
            scanningJob.cancel(true);
            scanningJob = null;
        }
    }

    @Override
    public String getUDN() {
        return (String) this.wemoBridgeHandler.getThing().getConfiguration().get(UDN);
    }

    @Override
    public void onServiceSubscribed(String service, boolean succeeded) {
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {
    }

    @Override
    public void onStatusChanged(boolean status) {
    }

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "?";
    }

    public class WemoLinkScan implements Runnable {
        @Override
        public void run() {
            startScan();
        }
    }

}

/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.jupnp.UpnpService;
import org.jupnp.model.message.header.RootDeviceHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WemoDiscoveryService} is a {@link DiscoveryService} implementation, which can find WeMo UPnP devices in
 * the network.
 *
 * @author Hans-Jörg Merk - Initial contribution
 *
 */
public class WemoDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(WemoDiscoveryService.class);

    public WemoDiscoveryService() {
        super(5);
    }

    private UpnpService upnpService;

    protected void setUpnpService(UpnpService upnpService) {
        this.upnpService = upnpService;
    }

    protected void unsetUpnpService(UpnpService upnpService) {
        this.upnpService = null;
    }

    public void activate() {
        logger.debug("Starting WeMo UPnP discovery...");
        startScan();
    }

    @Override
    public void deactivate() {
        logger.debug("Stopping WeMo UPnP discovery...");
        stopScan();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting UPnP RootDevice search...");
        if (upnpService != null) {
            upnpService.getControlPoint().search(new RootDeviceHeader());
        } else {
            logger.debug("upnpService not set");
        }
    }

    @Override
    protected synchronized void stopScan() {
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
        if (!isBackgroundDiscoveryEnabled()) {
        }
    }

}

/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.binding.hue.internal.HttpClient.Result;
import org.eclipse.smarthome.binding.hue.internal.exceptions.ApiException;

import com.google.gson.Gson;

/**
 * Various ways of discovering bridges.
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding, minor code cleanup
 */
public class BridgeDiscovery {
    private BridgeDiscovery() {
    }

    /**
     * Search for bridges on the local network using UPnP, and
     * more specifically the SSDP protocol.
     *
     * It usually takes a while for devices to respond, so a
     * typical timeout would be 30 seconds. This function will
     * only return bridge objects with the IP address set, you
     * still need to authenticate manually.
     *
     * You can optionally specify a callback to get bridges as
     * they're being discovered. Pass null if you don't need that.
     *
     * @param timeout time to search in milliseconds
     * @param callback function to call when a new bridge is discovered (can be null)
     * @return list of bridges discovered
     */
    public static List<HueBridge> searchUPnP(int timeout, BridgeDiscoveryCallback callback) throws IOException {
        // Send out SSDP discovery broadcast
        String ssdpBroadcast = "M-SEARCH * HTTP/1.1\nHOST: 239.255.255.250:1900\nMAN: ssdp:discover\nMX: 8\nST:SsdpSearch:all";
        DatagramSocket upnpSock = new DatagramSocket();
        upnpSock.setSoTimeout(100);
        upnpSock.send(new DatagramPacket(ssdpBroadcast.getBytes(), ssdpBroadcast.length(),
                new InetSocketAddress("239.255.255.250", 1900)));

        // Start waiting
        HashSet<String> ips = new HashSet<>();
        ArrayList<HueBridge> bridges = new ArrayList<>();

        long start = System.currentTimeMillis();
        long nextBroadcast = start + 5000;

        while (System.currentTimeMillis() - start < timeout) {
            // Send a new discovery broadcast every 5 seconds just in case
            if (System.currentTimeMillis() > nextBroadcast) {
                upnpSock.send(new DatagramPacket(ssdpBroadcast.getBytes(), ssdpBroadcast.length(),
                        new InetSocketAddress("239.255.255.250", 1900)));
                nextBroadcast = System.currentTimeMillis() + 5000;
            }

            byte[] responseBuffer = new byte[1024];

            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);

            try {
                upnpSock.receive(responsePacket);
            } catch (SocketTimeoutException e) {
                if (System.currentTimeMillis() - start > timeout) {
                    break;
                } else {
                    continue;
                }
            }

            final String ip = responsePacket.getAddress().getHostAddress();
            final String response = new String(responsePacket.getData());

            if (!ips.contains(ip)) {
                Matcher m = Pattern.compile("LOCATION: (.*)", Pattern.CASE_INSENSITIVE).matcher(response);

                if (m.find()) {
                    final String description = new HttpClient().get(m.group(1)).getBody();

                    // Parsing with RegEx allowed here because the output format is fairly strict
                    final String modelName = Util.quickMatch("<modelName>(.*?)</modelName>", description);

                    // Check from description if we're dealing with a hue bridge or some other device
                    if (modelName.toLowerCase().contains("philips hue bridge")) {
                        try {
                            final HueBridge bridgeToBe = new HueBridge(ip);
                            bridgeToBe.getConfig();

                            bridges.add(bridgeToBe);

                            if (callback != null) {
                                callback.onBridgeDiscovered(bridgeToBe);
                            }
                        } catch (ApiException e) {
                            // Do nothing, this basically serves as an extra check to see if it's really a hue bridge
                        }
                    }
                }

                // Ignore subsequent packets
                ips.add(ip);
            }
        }

        return bridges;
    }

    /**
     * Search for local bridges using the portal API.
     * See: http://developers.meethue.com/5_portalapi.html
     *
     * @return list of bridges
     */
    public static List<HueBridge> searchPortal() throws IOException {
        Result result = new HttpClient().get("http://www.meethue.com/api/nupnp");

        if (result.getResponseCode() == 200) {
            return new Gson().fromJson(result.getBody(), PortalDiscoveryResult.gsonType);
        } else {
            throw new IOException();
        }
    }

    /**
     * Callback for intermediary bridge search results.
     */
    public interface BridgeDiscoveryCallback {
        /**
         * Called when a new bridge has been discovered by UPnP.
         * No attempt to authenticate will be made, so you'll
         * have to call authenticate() on the object manually.
         *
         * @param bridge Object representing found bridge
         */
        void onBridgeDiscovered(HueBridge bridge);
    }
}

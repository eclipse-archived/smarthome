/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.wemo.discovery;

import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.LOCATION;
import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.UDN;
import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.WEMO_INSIGHT_TYPE_UID;
import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.WEMO_LIGHTSWITCH_TYPE_UID;
import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.WEMO_MOTION_TYPE_UID;
import static org.eclipse.smarthome.binding.wemo.WemoBindingConstants.WEMO_SOCKET_TYPE_UID;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.wemo.handler.WemoHandler;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WemoDiscoveryService} is responsible for discovering new and
 * removed Wemo devices.
 *
 * @author Hans-Jörg Merk - Initial contribution
 *
 */
public class WemoDiscoveryService extends AbstractDiscoveryService {


	private Logger logger = LoggerFactory.getLogger(WemoDiscoveryService.class);

	/**
	 * Connection timeout
	 */
	private static int TIMEOUT = 5000;
	/**
	 * Port to send discovery message to
	 */
    private static final int SSDP_PORT = 1900;
	/**
	 * Port to use for sending discovery message
	 */
    private static final int SSDP_SEARCH_PORT = 1901;
	/**
	 * Broadcast address to use for sending discovery message
	 */
	private static final String SSDP_IP = "239.255.255.250";

    
    public InetAddress address;

	/**
	 * When true we will keep sending out packets
	 */

	static boolean discoveryRunning = false;
	
	/**
	 * The refresh interval for discovering WeMo devices
	 */
	private long refreshInterval = 600;
	private ScheduledFuture<?> wemoDiscoveryJob;
	private Runnable wemoDiscoveryRunnable = new Runnable () {
			@Override
			public void run() {
				discoverWemo();		}
	};
			
	public WemoDiscoveryService() {
		super(WemoHandler.SUPPORTED_THING_TYPES, 15, true);
	}
 
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return WemoHandler.SUPPORTED_THING_TYPES;
    }
 
	protected void startScan() {
		logger.debug("Starting WeMo Device discovery");
		discoverWemo();
	}

	@Override
	protected void startBackgroundDiscovery() {
		logger.trace("Start WeMo device background discovery");
		if (wemoDiscoveryJob == null || wemoDiscoveryJob.isCancelled()) {
			wemoDiscoveryJob = scheduler.scheduleAtFixedRate(wemoDiscoveryRunnable, 0, refreshInterval, TimeUnit.SECONDS);
		}
	}

	@Override
	protected void stopBackgroundDiscovery() {
		logger.debug("Stop WeMo device background discovery");
		if (wemoDiscoveryJob != null && !wemoDiscoveryJob.isCancelled()) {
			wemoDiscoveryJob.cancel(true);
			wemoDiscoveryJob = null;
		}
	}

	/**
	 * Scans for WeMo devices
	 */
	private synchronized void discoverWemo() {
		logger.debug("Run Wemo device discovery");
		sendWemoDiscoveryMessage();
		logger.trace("Done sending WeMo broadcast discovery message");
		//receiveWemoDiscoveryMessage();
		logger.trace("Done receiving WeMo broadcast discovery message");
	}

	
	public void sendWemoDiscoveryMessage() {
		logger.debug("wemoDiscovery() is called!");
		try {

			InetAddress localhost = InetAddress.getLocalHost();
			InetSocketAddress srcAddress = new InetSocketAddress(localhost,	SSDP_SEARCH_PORT);
			
			InetSocketAddress dstAddress = new InetSocketAddress(InetAddress.getByName(SSDP_IP), SSDP_PORT);

			// Request-Packet-Constructor
			StringBuffer discoveryMessage = new StringBuffer();
			discoveryMessage.append("M-SEARCH * HTTP/1.1\r\n");
			discoveryMessage.append("HOST: " + SSDP_IP + ":" + SSDP_PORT + "\r\n");
			discoveryMessage.append("ST: upnp:rootdevice\r\n");
			discoveryMessage.append("MAN: \"ssdp:discover\"\r\n");
			discoveryMessage.append("MX: 5\r\n");
			discoveryMessage.append("\r\n");
		    logger.trace("Request: {}", discoveryMessage.toString());
			byte[] discoveryMessageBytes = discoveryMessage.toString().getBytes();
			DatagramPacket discoveryPacket = new DatagramPacket(
					discoveryMessageBytes, discoveryMessageBytes.length, dstAddress);

			// Send multi-cast packet
			MulticastSocket multicast = null;
			try {
				multicast = new MulticastSocket(null);
				multicast.bind(srcAddress);
				logger.trace("Source-Address = '{}'", srcAddress);
				multicast.setTimeToLive(4);
				logger.debug("Send multicast request.");
				multicast.send(discoveryPacket);
			} finally {
				logger.trace("Multicast ends. Close connection.");
				multicast.disconnect();
				multicast.close();
			}

			// Response-Listener
			DatagramSocket wemoReceiveSocket = null;
			DatagramPacket receivePacket = null;
			try {
				wemoReceiveSocket = new DatagramSocket(SSDP_SEARCH_PORT);
				wemoReceiveSocket.setSoTimeout(TIMEOUT);
				logger.debug("Send datagram packet.");
				wemoReceiveSocket.send(discoveryPacket);

				while (true) {
					try {
						receivePacket = new DatagramPacket(new byte[1536], 1536);
						wemoReceiveSocket.receive(receivePacket);
						final String message = new String(receivePacket.getData());
						logger.trace("Received message: {}", message);
				
						new Thread(new Runnable() {
							@Override
							public void run() {
					            String label = "WeMo Device";
								String wemoUDN = null;
								String wemoLocation = null;
								String wemoFriendlyName = null;
								String wemoModelName = null;
								ThingUID uid = null;
								
								if (message != null) {
									if (message.contains("Socket-1_0") || message.contains("Insight-1_0") || message.contains("Motion-1_0") || message.contains("Lightswitch-1_0")) {
										wemoUDN = StringUtils.substringBetween(message, "USN: uuid:", "::upnp:rootdevice");
										logger.debug("Wemo device with UDN '{}' found", wemoUDN);
										wemoLocation = StringUtils.substringBetween(message, "LOCATION: ", "/setup.xml");
										if (wemoLocation != null) {
											try {
												int timeout = 5000;
												String response = HttpUtil.executeUrl("GET", wemoLocation+"/setup.xml", timeout);
												wemoFriendlyName = StringUtils.substringBetween(response, "<friendlyName>", "</friendlyName>");
												logger.debug("Wemo device '{}' found at '{}'", wemoFriendlyName, wemoLocation);
												wemoModelName = StringUtils.substringBetween(response, "<modelName>", "</modelName>");
												logger.trace("Wemo device '{}' has model name '{}'", wemoFriendlyName, wemoModelName);
												label = "Wemo" + wemoModelName;
												
												switch(wemoModelName) {
												case "Socket":
													logger.debug("Creating ThingUID for device model '{}' with UDN '{}'", wemoModelName, wemoUDN);
													uid = new ThingUID(WEMO_SOCKET_TYPE_UID, wemoUDN);
													
													break;
												case "Insight":
													logger.trace("Creating ThingUID for device model '{}' with UDN '{}'", wemoModelName, wemoUDN);
													uid = new ThingUID(WEMO_INSIGHT_TYPE_UID, wemoUDN);
													break;
												case "LightSwitch":
													logger.trace("Creating ThingUID for device model '{}' with UDN '{}'", wemoModelName, wemoUDN);
													uid = new ThingUID(WEMO_LIGHTSWITCH_TYPE_UID, wemoUDN);
													break;
												case "Motion":
													logger.trace("Creating ThingUID for device model '{}' with UDN '{}'", wemoModelName, wemoUDN);
													uid = new ThingUID(WEMO_MOTION_TYPE_UID, wemoUDN);
													break;
												}
									            Map<String, Object> properties = new HashMap<>(4);
									            properties.put(UDN, wemoUDN);
									            properties.put(LOCATION, wemoLocation);

									            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
									                    .build();
									            thingDiscovered(result);

											} catch (Exception te) {
												logger.error("Could not discover WeMo device", te);
											}
										}
									}
								}
							}
						}).start();

					} catch (SocketTimeoutException e) {
						logger.debug("Message receive timed out.");
						break;
					}
				}
			} finally {
				if (wemoReceiveSocket != null) {
					wemoReceiveSocket.disconnect();
					wemoReceiveSocket.close();
				}
			}
			
		} catch (Exception e) {
			logger.error("Could not send Wemo device discovery", e);
		}
		
	}
}	

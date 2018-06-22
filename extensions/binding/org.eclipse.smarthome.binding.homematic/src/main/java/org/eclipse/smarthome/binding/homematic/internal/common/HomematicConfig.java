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
package org.eclipse.smarthome.binding.homematic.internal.common;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.eclipse.smarthome.binding.homematic.internal.model.HmChannel;
import org.eclipse.smarthome.binding.homematic.internal.model.HmGatewayInfo;
import org.eclipse.smarthome.binding.homematic.internal.model.HmInterface;

/**
 * The main gateway config class.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomematicConfig {
    private static final String ISO_ENCODING = "ISO-8859-1";
    private static final String UTF_ENCODING = "UTF-8";

    private static final String GATEWAY_TYPE_AUTO = "AUTO";
    private static final String GATEWAY_TYPE_CCU = "CCU";
    private static final String GATEWAY_TYPE_NOCCU = "NOCCU";

    private static final int DEFAULT_PORT_RF = 2001;
    private static final int DEFAULT_PORT_WIRED = 2000;
    private static final int DEFAULT_PORT_HMIP = 2010;
    private static final int DEFAULT_PORT_CUXD = 8701;
    private static final int DEFAULT_PORT_GROUP = 9292;
    public static final int DEFAULT_INSTALL_MODE_DURATION = 60;

    private String gatewayAddress;
    private String gatewayType = GATEWAY_TYPE_AUTO;

    private int rfPort;
    private int wiredPort;
    private int hmIpPort;
    private int cuxdPort;
    private int groupPort;

    private String callbackHost;
    private int xmlCallbackPort;
    private int binCallbackPort;

    private int socketMaxAlive = 900;
    private int timeout = 15;
    private int installModeDuration = DEFAULT_INSTALL_MODE_DURATION;
    private long discoveryTimeToLive = -1;
    private boolean unpairOnDeletion = false;

    private HmGatewayInfo gatewayInfo;

    /**
     * Returns the Homematic gateway address.
     */
    public String getGatewayAddress() {
        return gatewayAddress;
    }

    /**
     * Sets the Homematic gateway address.
     */
    public void setGatewayAddress(String gatewayAddress) {
        this.gatewayAddress = gatewayAddress;
    }

    /**
     * Returns the callback host address.
     */
    public String getCallbackHost() {
        return callbackHost;
    }

    /**
     * Sets the callback host address.
     */
    public void setCallbackHost(String callbackHost) {
        this.callbackHost = callbackHost;
    }

    /**
     * Sets the callback host port.
     *
     * @deprecated use setBinCallbackPort
     */
    @Deprecated
    public void setCallbackPort(int callbackPort) {
        this.binCallbackPort = callbackPort;
    }

    /**
     * Returns the XML-RPC callback host port.
     */
    public int getXmlCallbackPort() {
        return xmlCallbackPort;
    }

    /**
     * Sets the XML-RPC callback host port.
     */
    public void setXmlCallbackPort(int xmlCallbackPort) {
        this.xmlCallbackPort = xmlCallbackPort;
    }

    /**
     * Returns the BIN-RPC callback host port.
     */
    public int getBinCallbackPort() {
        return binCallbackPort;
    }

    /**
     * Sets the BIN-RPC callback host port.
     */
    public void setBinCallbackPort(int binCallbackPort) {
        this.binCallbackPort = binCallbackPort;
    }

    /**
     * Returns the HmGatewayInfo.
     */
    public HmGatewayInfo getGatewayInfo() {
        return gatewayInfo;
    }

    /**
     * Sets the HmGatewayInfo.
     */
    public void setGatewayInfo(HmGatewayInfo gatewayInfo) {
        this.gatewayInfo = gatewayInfo;
    }

    /**
     * Returns the max alive time of a socket connection to a Homematic gateway in seconds.
     */
    public int getSocketMaxAlive() {
        return socketMaxAlive;
    }

    /**
     * Sets the max alive time of a socket connection to a Homematic gateway in seconds.
     */
    public void setSocketMaxAlive(int socketMaxAlive) {
        this.socketMaxAlive = socketMaxAlive;
    }

    /**
     * Returns the timeout for the communication to a Homematic gateway in seconds.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout for the communication to a Homematic gateway in seconds.
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the time to live for discovery results of a Homematic gateway in seconds.
     */
    public long getDiscoveryTimeToLive() {
        return discoveryTimeToLive;
    }

    /**
     * Sets the time to live for discovery results of a Homematic gateway in seconds.
     */
    public void setDiscoveryTimeToLive(long discoveryTimeToLive) {
        this.discoveryTimeToLive = discoveryTimeToLive;
    }

    /**
     * Returns the HmGatewayType.
     */
    public String getGatewayType() {
        return gatewayType;
    }

    /**
     * Sets the HmGatewayType.
     */
    public void setGatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
    }
    
    /**
     * Returns time in seconds that the controller will be in install mode when
     * a device discovery is initiated
     * 
     * @return time in seconds that the controller remains in install mode
     */
    public int getInstallModeDuration() {
        return installModeDuration;
    }
    
    /**
     * Sets installModeDuration
     * 
     * @param installModeDuration time in seconds that the controller remains in
     *        install mode
     */
    public void setInstallModeDuration(int installModeDuration) {
        this.installModeDuration = installModeDuration;
    }

    /**
     * Returns if devices are unpaired from the gateway when their corresponding things are removed
     * 
     * @return <i>true</i> if devices are unpaired from the gateway when their corresponding things are removed
     */
    public boolean isUnpairOnDeletion() {
        return unpairOnDeletion;
    }

    /**
     * Sets unpairOnDeletion
     * 
     * @param unpairOnDeletion if set to <i>true</i>, devices are unpaired from the gateway when their corresponding
     *            things are removed
     */
    public void setUnpairOnDeletion(boolean unpairOnDeletion) {
        this.unpairOnDeletion = unpairOnDeletion;
    }

    /**
     * Returns the TclRegaScript url.
     */
    public String getTclRegaUrl() {
        return "http://" + gatewayAddress + ":8181/tclrega.exe";
    }

    /**
     * Returns the Homematic gateway port of the channel.
     */
    public int getRpcPort(HmChannel channel) {
        return getRpcPort(channel.getDevice().getHmInterface());
    }

    /**
     * Returns the Homematic gateway port of the interfaces.
     */
    public int getRpcPort(HmInterface hmInterface) {
        if (HmInterface.WIRED.equals(hmInterface)) {
            return getWiredPort();
        } else if (HmInterface.HMIP.equals(hmInterface)) {
            return getHmIpPort();
        } else if (HmInterface.CUXD.equals(hmInterface)) {
            return getCuxdPort();
        } else if (HmInterface.GROUP.equals(hmInterface)) {
            return getGroupPort();
        } else {
            return getRfPort();
        }
    }

    /**
     * Returns the port of the RF daemon.
     */
    private int getRfPort() {
        return rfPort == 0 ? DEFAULT_PORT_RF : rfPort;
    }

    /**
     * Returns the port of the wired daemon.
     */
    private int getWiredPort() {
        return wiredPort == 0 ? DEFAULT_PORT_WIRED : wiredPort;
    }

    /**
     * Returns the port of the HmIp daemon.
     */
    private int getHmIpPort() {
        return hmIpPort == 0 ? DEFAULT_PORT_HMIP : hmIpPort;
    }

    /**
     * Returns the port of the CUxD daemon.
     */
    private int getCuxdPort() {
        return cuxdPort == 0 ? DEFAULT_PORT_CUXD : cuxdPort;
    }

    /**
     * Returns the port of the group daemon.
     */
    public int getGroupPort() {
        return groupPort == 0 ? DEFAULT_PORT_GROUP : groupPort;
    }

    /**
     * Returns true, if a wired port is configured.
     */
    public boolean hasWiredPort() {
        return wiredPort != 0;
    }

    /**
     * Returns true, if a hmIp port is configured.
     */
    public boolean hasHmIpPort() {
        return hmIpPort != 0;
    }

    /**
     * Returns true, if a cuxd port is configured.
     */
    public boolean hasCuxdPort() {
        return cuxdPort != 0;
    }

    /**
     * Returns true, if a group port is configured.
     */
    public boolean hasGroupPort() {
        return groupPort != 0;
    }

    /**
     * Returns the encoding of a Homematic gateway.
     */
    public String getEncoding() {
        if (gatewayInfo != null && gatewayInfo.isHomegear()) {
            return UTF_ENCODING;
        } else {
            return ISO_ENCODING;
        }
    }

    /**
     * Returns true, if the configured gatewayType is CCU.
     */
    public boolean isCCUType() {
        return gatewayType.equalsIgnoreCase(HomematicConfig.GATEWAY_TYPE_CCU);
    }

    /**
     * Returns true, if the configured gatewayType is NoCCU.
     */
    public boolean isNoCCUType() {
        return gatewayType.equalsIgnoreCase(HomematicConfig.GATEWAY_TYPE_NOCCU);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        tsb.append("gatewayAddress", gatewayAddress).append("callbackHost", callbackHost)
                .append("xmlCallbackPort", xmlCallbackPort).append("binCallbackPort", binCallbackPort)
                .append("gatewayType", gatewayType).append("rfPort", getRfPort()).append("wiredPort", getWiredPort())
                .append("hmIpPort", getHmIpPort()).append("cuxdPort", getCuxdPort()).append("groupPort", getGroupPort())
                .append("timeout", timeout).append("discoveryTimeToLive", discoveryTimeToLive)
                .append("installModeDuration", installModeDuration).append("socketMaxAlive", socketMaxAlive);
        return tsb.toString();
    }
}

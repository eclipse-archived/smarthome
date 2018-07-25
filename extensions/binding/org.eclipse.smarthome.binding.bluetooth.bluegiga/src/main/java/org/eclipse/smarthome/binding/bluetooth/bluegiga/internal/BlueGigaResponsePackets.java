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
package org.eclipse.smarthome.binding.bluetooth.bluegiga.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaAttributeValueEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaAttributeWriteResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaExecuteWriteResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaFindByTypeValueResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaFindInformationFoundEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaFindInformationResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaGroupFoundEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaIndicateConfirmResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaIndicatedEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaPrepareWriteResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaProcedureCompletedEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadByGroupTypeResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadByHandleResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadByTypeResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadLongResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadMultipleResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadMultipleResponseEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaWriteCommandResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributedb.BlueGigaAttributeStatusEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributedb.BlueGigaReadResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributedb.BlueGigaReadTypeResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributedb.BlueGigaSendAttributesResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributedb.BlueGigaUserReadRequestEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributedb.BlueGigaUserReadResponseResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributedb.BlueGigaUserWriteResponseResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributedb.BlueGigaValueEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.attributedb.BlueGigaWriteResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaChannelMapGetResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaConnectionStatusEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaDisconnectResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaDisconnectedEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaFeatureIndEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaGetRssiResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaGetStatusResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaUpdateResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaVersionIndEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaConnectDirectResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaConnectSelectiveResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaDiscoverResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaEndProcedureResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaScanResponseEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaSetAdvDataResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaSetAdvParametersResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaSetModeResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaSetScanParametersResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.security.BlueGigaBondStatusEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.security.BlueGigaBondingFailEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.security.BlueGigaDeleteBondingResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.security.BlueGigaEncryptStartResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.security.BlueGigaGetBondsResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.security.BlueGigaPassKeyResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.security.BlueGigaPasskeyDisplayEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.security.BlueGigaPasskeyRequestEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.security.BlueGigaSetBondableModeResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.security.BlueGigaSetParametersResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.security.BlueGigaWhitelistBondsResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.system.BlueGigaAddressGetResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.system.BlueGigaBootEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.system.BlueGigaEndpointWatermarkRxEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.system.BlueGigaEndpointWatermarkTxEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.system.BlueGigaGetConnectionsResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.system.BlueGigaGetCountersResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.system.BlueGigaGetInfoResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.system.BlueGigaHelloResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.system.BlueGigaNoLicenseKeyEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.system.BlueGigaProtocolErrorEvent;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.system.BlueGigaResetResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.system.BlueGigaWhitelistAppendResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.system.BlueGigaWhitelistClearResponse;
import org.eclipse.smarthome.binding.bluetooth.bluegiga.internal.command.system.BlueGigaWhitelistRemoveResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to create BlueGiga BLE Response and Event packets (i.e. packets that we will receive).
 *
 * @author Chris Jackson - Initial contribution and API
 */
class BlueGigaResponsePackets {

    private static Logger logger = LoggerFactory.getLogger(BlueGigaResponsePackets.class);

    final private static Map<Integer, Class<?>> packetMap = new HashMap<Integer, Class<?>>();

    static {
        packetMap.put(Objects.hash(0x00, 0x06, true), BlueGigaProtocolErrorEvent.class);
        packetMap.put(Objects.hash(0x00, 0x02, true), BlueGigaEndpointWatermarkRxEvent.class);
        packetMap.put(Objects.hash(0x00, 0x03, true), BlueGigaEndpointWatermarkTxEvent.class);
        packetMap.put(Objects.hash(0x00, 0x05, true), BlueGigaNoLicenseKeyEvent.class);
        packetMap.put(Objects.hash(0x04, 0x05, false), BlueGigaAttributeWriteResponse.class);
        packetMap.put(Objects.hash(0x04, 0x0A, false), BlueGigaExecuteWriteResponse.class);
        packetMap.put(Objects.hash(0x04, 0x00, false), BlueGigaFindByTypeValueResponse.class);
        packetMap.put(Objects.hash(0x04, 0x03, false), BlueGigaFindInformationResponse.class);
        packetMap.put(Objects.hash(0x04, 0x07, false), BlueGigaIndicateConfirmResponse.class);
        packetMap.put(Objects.hash(0x04, 0x09, false), BlueGigaPrepareWriteResponse.class);
        packetMap.put(Objects.hash(0x04, 0x01, false), BlueGigaReadByGroupTypeResponse.class);
        packetMap.put(Objects.hash(0x04, 0x04, false), BlueGigaReadByHandleResponse.class);
        packetMap.put(Objects.hash(0x04, 0x02, false), BlueGigaReadByTypeResponse.class);
        packetMap.put(Objects.hash(0x04, 0x08, false), BlueGigaReadLongResponse.class);
        packetMap.put(Objects.hash(0x04, 0x0B, false), BlueGigaReadMultipleResponse.class);
        packetMap.put(Objects.hash(0x04, 0x06, false), BlueGigaWriteCommandResponse.class);
        packetMap.put(Objects.hash(0x04, 0x01, true), BlueGigaProcedureCompletedEvent.class);
        packetMap.put(Objects.hash(0x04, 0x05, true), BlueGigaAttributeValueEvent.class);
        packetMap.put(Objects.hash(0x04, 0x04, true), BlueGigaFindInformationFoundEvent.class);
        packetMap.put(Objects.hash(0x04, 0x02, true), BlueGigaGroupFoundEvent.class);
        packetMap.put(Objects.hash(0x04, 0x00, true), BlueGigaIndicatedEvent.class);
        packetMap.put(Objects.hash(0x04, 0x00, true), BlueGigaReadMultipleResponseEvent.class);
        packetMap.put(Objects.hash(0x02, 0x01, false), BlueGigaReadResponse.class);
        packetMap.put(Objects.hash(0x02, 0x02, false), BlueGigaReadTypeResponse.class);
        packetMap.put(Objects.hash(0x02, 0x02, false), BlueGigaSendAttributesResponse.class);
        packetMap.put(Objects.hash(0x02, 0x03, false), BlueGigaUserReadResponseResponse.class);
        packetMap.put(Objects.hash(0x02, 0x04, false), BlueGigaUserWriteResponseResponse.class);
        packetMap.put(Objects.hash(0x02, 0x00, false), BlueGigaWriteResponse.class);
        packetMap.put(Objects.hash(0x02, 0x02, true), BlueGigaAttributeStatusEvent.class);
        packetMap.put(Objects.hash(0x02, 0x01, true), BlueGigaUserReadRequestEvent.class);
        packetMap.put(Objects.hash(0x02, 0x00, true), BlueGigaValueEvent.class);
        packetMap.put(Objects.hash(0x03, 0x04, false), BlueGigaChannelMapGetResponse.class);
        packetMap.put(Objects.hash(0x03, 0x00, false), BlueGigaDisconnectResponse.class);
        packetMap.put(Objects.hash(0x03, 0x01, false), BlueGigaGetRssiResponse.class);
        packetMap.put(Objects.hash(0x03, 0x07, false), BlueGigaGetStatusResponse.class);
        packetMap.put(Objects.hash(0x03, 0x02, false), BlueGigaUpdateResponse.class);
        packetMap.put(Objects.hash(0x03, 0x04, true), BlueGigaDisconnectedEvent.class);
        packetMap.put(Objects.hash(0x03, 0x02, true), BlueGigaFeatureIndEvent.class);
        packetMap.put(Objects.hash(0x03, 0x00, true), BlueGigaConnectionStatusEvent.class);
        packetMap.put(Objects.hash(0x03, 0x01, true), BlueGigaVersionIndEvent.class);
        packetMap.put(Objects.hash(0x06, 0x07, false), BlueGigaSetScanParametersResponse.class);
        packetMap.put(Objects.hash(0x06, 0x03, false), BlueGigaConnectDirectResponse.class);
        packetMap.put(Objects.hash(0x06, 0x05, false), BlueGigaConnectSelectiveResponse.class);
        packetMap.put(Objects.hash(0x06, 0x02, false), BlueGigaDiscoverResponse.class);
        packetMap.put(Objects.hash(0x06, 0x08, false), BlueGigaSetAdvParametersResponse.class);
        packetMap.put(Objects.hash(0x06, 0x09, false), BlueGigaSetAdvDataResponse.class);
        packetMap.put(Objects.hash(0x06, 0x04, false), BlueGigaEndProcedureResponse.class);
        packetMap.put(Objects.hash(0x06, 0x01, false), BlueGigaSetModeResponse.class);
        packetMap.put(Objects.hash(0x06, 0x00, true), BlueGigaScanResponseEvent.class);
        packetMap.put(Objects.hash(0x05, 0x02, false), BlueGigaDeleteBondingResponse.class);
        packetMap.put(Objects.hash(0x05, 0x00, false), BlueGigaEncryptStartResponse.class);
        packetMap.put(Objects.hash(0x05, 0x05, false), BlueGigaGetBondsResponse.class);
        packetMap.put(Objects.hash(0x05, 0x04, false), BlueGigaPassKeyResponse.class);
        packetMap.put(Objects.hash(0x05, 0x01, false), BlueGigaSetBondableModeResponse.class);
        packetMap.put(Objects.hash(0x05, 0x03, false), BlueGigaSetParametersResponse.class);
        packetMap.put(Objects.hash(0x05, 0x07, false), BlueGigaWhitelistBondsResponse.class);
        packetMap.put(Objects.hash(0x00, 0x0A, false), BlueGigaWhitelistAppendResponse.class);
        packetMap.put(Objects.hash(0x00, 0x0B, false), BlueGigaWhitelistRemoveResponse.class);
        packetMap.put(Objects.hash(0x00, 0x0C, false), BlueGigaWhitelistClearResponse.class);
        packetMap.put(Objects.hash(0x05, 0x01, true), BlueGigaBondingFailEvent.class);
        packetMap.put(Objects.hash(0x05, 0x04, true), BlueGigaBondStatusEvent.class);
        packetMap.put(Objects.hash(0x05, 0x02, true), BlueGigaPasskeyDisplayEvent.class);
        packetMap.put(Objects.hash(0x05, 0x03, true), BlueGigaPasskeyRequestEvent.class);
        packetMap.put(Objects.hash(0x00, 0x02, false), BlueGigaAddressGetResponse.class);
        packetMap.put(Objects.hash(0x00, 0x01, false), BlueGigaHelloResponse.class);
        packetMap.put(Objects.hash(0x00, 0x00, false), BlueGigaResetResponse.class);
        packetMap.put(Objects.hash(0x00, 0x06, false), BlueGigaGetConnectionsResponse.class);
        packetMap.put(Objects.hash(0x00, 0x05, false), BlueGigaGetCountersResponse.class);
        packetMap.put(Objects.hash(0x00, 0x08, false), BlueGigaGetInfoResponse.class);
        packetMap.put(Objects.hash(0x00, 0x00, true), BlueGigaBootEvent.class);
    }

    public static BlueGigaResponse getPacket(int[] data) {
        int cmdClass = data[2];
        int cmdMethod = data[3];
        boolean isEvent = (data[0] & 0x80) != 0;

        Class<?> bleClass = packetMap.get(Objects.hash(cmdClass, cmdMethod, isEvent));

        if (bleClass == null) {
            return null;
        }

        Constructor<?> ctor;

        try {
            ctor = bleClass.getConstructor(int[].class);
            BlueGigaResponse bleFrame = (BlueGigaResponse) ctor.newInstance(data);
            return bleFrame;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            logger.error("Error instantiating BLE class", e);
        }

        return null;
    }
}

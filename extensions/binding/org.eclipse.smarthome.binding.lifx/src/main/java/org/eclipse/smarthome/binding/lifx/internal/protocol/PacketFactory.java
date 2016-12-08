/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.lifx.internal.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * A static factory for registering packet types that may be received and
 * dispatched to client cod * request types, like {@code PowerStateRequest}) or types received only via UDP
 * e. Packet handlers (used to construct actual packet
 * instances) may be retrieved via their packet type.
 *
 * @author Tim Buckley - Initial Contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 * @author Wouter Born - Support LIFX 2016 product line-up and infrared functionality
 */
public class PacketFactory {

    private static PacketFactory instance;

    public synchronized static PacketFactory getInstance() {
        if (instance == null) {
            instance = new PacketFactory();
        }

        return instance;
    }

    private final Map<Integer, PacketHandler<?>> handlers;

    private PacketFactory() {
        handlers = new HashMap<>();

        register(AcknowledgementResponse.class);
        register(EchoRequestResponse.class);
        register(GetEchoRequest.class);
        register(GetGroupRequest.class);
        register(GetHostFirmwareRequest.class);
        register(GetHostInfoRequest.class);
        register(GetInfoRequest.class);
        register(GetLabelRequest.class);
        register(GetLightInfraredRequest.class);
        register(GetLightPowerRequest.class);
        register(GetLocationRequest.class);
        register(GetMeshFirmwareRequest.class);
        register(GetPowerRequest.class);
        register(GetRequest.class);
        register(GetServiceRequest.class);
        register(GetTagLabelsRequest.class);
        register(GetTagsRequest.class);
        register(GetTagsRequest.class);
        register(GetVersionRequest.class);
        register(GetWifiFirmwareRequest.class);
        register(GetWifiInfoRequest.class);
        register(SetColorRequest.class);
        register(SetDimAbsoluteRequest.class);
        register(SetLabelRequest.class);
        register(SetLightInfraredRequest.class);
        register(SetLightPowerRequest.class);
        register(SetPowerRequest.class);
        register(SetTagsRequest.class);
        register(StateGroupResponse.class);
        register(StateHostFirmwareResponse.class);
        register(StateHostInfoResponse.class);
        register(StateInfoResponse.class);
        register(StateLabelResponse.class);
        register(StateLightInfraredResponse.class);
        register(StateLightPowerResponse.class);
        register(StateLocationResponse.class);
        register(StateMeshFirmwareResponse.class);
        register(StatePowerResponse.class);
        register(StateResponse.class);
        register(StateServiceResponse.class);
        register(StateVersionResponse.class);
        register(StateWifiFirmwareResponse.class);
        register(StateWifiInfoResponse.class);
        register(TagLabelsResponse.class);
        register(TagsResponse.class);
    }

    /**
     * Registers a packet handler for the given packet type.
     *
     * @param type the type to register
     * @param handler the packet handler to associate with the type
     */
    public final void register(int type, PacketHandler<?> handler) {
        handlers.put(type, handler);
    }

    /**
     * Registers a new generic packet handler for the given packet class. The
     * packet class must meet the criteria for {@link GenericHandler};
     * specifically, it must have an no-argument constructor and require no
     * parsing logic outside of an invocation of
     * {@link Packet#parse(java.nio.ByteBuffer)}.
     *
     * @param type the type of the packet to register
     * @param clazz the class of the packet to register
     */
    public final void register(int type, Class<? extends Packet> clazz) {
        handlers.put(type, new GenericHandler<>(clazz));
    }

    /**
     * Registers a generic packet type. All requirements of
     * {@link GenericHandler} must met; specifically, classes must have a
     * no-args constructor and require no additional parsing logic.
     * Additionally, a public static integer {@code TYPE} field must be defined.
     *
     * @param <T> the packet type to register
     * @param clazz the packet class to register
     */
    public final <T extends Packet> void register(Class<T> clazz) {
        GenericHandler<T> handler = new GenericHandler<>(clazz);

        if (!handler.isTypeFound()) {
            throw new IllegalArgumentException("Unable to register generic packet with no TYPE field.");
        }

        handlers.put(handler.getType(), handler);
    }

    /**
     * Gets a registered handler for the given packet type, if any exists. If
     * no matching handler can be found, {@code null} is returned.
     *
     * @param packetType the packet type of the handler to retrieve
     * @return a packet handler, or null
     */
    public PacketHandler<?> getHandler(int packetType) {
        return handlers.get(packetType);
    }

    public static PacketHandler<?> createHandler(int packetType) {
        return getInstance().getHandler(packetType);
    }

}

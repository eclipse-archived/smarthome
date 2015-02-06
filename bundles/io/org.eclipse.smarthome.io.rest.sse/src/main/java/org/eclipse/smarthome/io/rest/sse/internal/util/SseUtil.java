/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sse.internal.util;

import javax.servlet.ServletRequest;
import javax.ws.rs.core.MediaType;

import org.eclipse.smarthome.io.rest.sse.EventType;
import org.eclipse.smarthome.io.rest.sse.beans.EventBean;
import org.glassfish.jersey.media.sse.OutboundEvent;

/**
 * Utility class containing helper methods for the SSE implementation.
 *
 * @author Ivan Iliev - Initial Contribution and API
 *
 */
public class SseUtil {

    static {
        boolean servlet3 = false;
        try {
            servlet3 = ServletRequest.class.getMethod("startAsync") != null;
        } catch (Exception e) {
        } finally {
            SERVLET3_SUPPORT = servlet3;
        }
    }

    /**
     * True if the {@link ServletRequest} class has a "startAsync" method,
     * otherwise false.
     */
    public static final boolean SERVLET3_SUPPORT;

    /**
     * Creates a new {@link OutboundEvent} object containing an {@link EventBean} created for the given eventType,
     * objectIdentifier,
     * eventObject.
     *
     * @param eventType
     *            - the event type for the event
     * @param objectIdentifier
     *            - the identifier for the main event object
     * @param eventObject
     *            - the eventObject to be included
     * @return a new OutboundEvent.
     */
    public static OutboundEvent buildEvent(EventType eventType, String objectIdentifier, Object eventObject) {

        EventBean eventBean = new EventBean();
        eventBean.topic = eventType.getFullNameWithIdentifier(objectIdentifier);
        eventBean.object = eventObject;

        OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        OutboundEvent event = eventBuilder.name("message").mediaType(MediaType.APPLICATION_JSON_TYPE).data(eventBean)
                .build();

        return event;
    }

    /**
     * Used to mark our current thread(request processing) that SSE blocking
     * should be enabled.
     */
    private static ThreadLocal<Boolean> blockingSseEnabled = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    /**
     * Returns true if the current thread is processing an SSE request that
     * should block.
     *
     * @return
     */
    public static boolean shouldAsyncBlock() {
        return blockingSseEnabled.get().booleanValue();
    }

    /**
     * Marks the current thread as processing a blocking sse request.
     */
    public static void enableBlockingSse() {
        blockingSseEnabled.set(true);
    }
}

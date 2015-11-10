/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.io.rest.sse.internal.SseEventOutput;
import org.eclipse.smarthome.io.rest.sse.internal.util.SseUtil;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;

/**
 * SSE Resource for pushing events to currently listening clients.
 * 
 * @author Ivan Iliev - Initial Contribution and API
 * @author Yordan Zhelev - Added Swagger annotations
 * 
 */
@Path("events")
@Singleton
@Api
public class SseResource {

    private final SseBroadcaster broadcaster;

    private final ExecutorService executorService;

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpServletResponse response;

    @Context
    private HttpServletRequest request;

    public SseResource() {
        this.executorService = Executors.newSingleThreadExecutor();
        this.broadcaster = new SseBroadcaster();
    }

    /**
     * Subscribes the connecting client to the stream of events filtered by the
     * given eventFilter.
     * 
     * @param eventFilter
     * @return {@link EventOutput} object associated with the incoming
     *         connection.
     * @throws IOException
     * @throws InterruptedException
     */
    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    @ApiOperation(value = "Get all events.", response = EventOutput.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Topic is empty or contains invalid characters") })
    public Object getEvents(@QueryParam("topics") @ApiParam(value = "topics") String eventFilter) throws IOException,
            InterruptedException {

        if (!SseUtil.isValidTopicFilter(eventFilter)) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        // construct an EventOutput that will only write out events that match
        // the given filter
        final EventOutput eventOutput = new SseEventOutput(eventFilter);
        broadcaster.add(eventOutput);

        if (!SseUtil.SERVLET3_SUPPORT) {
            // if we don't have sevlet 3.0 async support, we want to make sure
            // that the response is not compressed and buffered so that the
            // client receives server sent events at the moment of sending them
            response.addHeader(HttpHeaders.CONTENT_ENCODING, "identity");

            // Response headers are written now, since the thread will be
            // blocked later on.
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(SseFeature.SERVER_SENT_EVENTS);
            response.flushBuffer();

            // enable blocking for this thread
            SseUtil.enableBlockingSse();
        }

        return eventOutput;
    }

    /**
     * Broadcasts an event described by the given parameter to all currently
     * listening clients.
     * 
     * @param sseEventType
     *            the SSE event type
     * @param event
     *            the event
     */
    public void broadcastEvent(final Event event) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                broadcaster.broadcast(SseUtil.buildEvent(event));
            }
        });
    }
}

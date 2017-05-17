/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

/**
 * Static helper methods to build up JSON-like Response objects and error handling.
 *
 * @author Joerg Plewe
 */
@Provider
public class JSONResponse {

    private final Logger logger = LoggerFactory.getLogger(JSONResponse.class);

    private static final String JSON_KEY_ERROR_MESSAGE = "message";
    public static final String JSON_KEY_ERROR = "error";
    public static final String JSON_KEY_HTTPCODE = "http-code";

    // also dump stacktrace?
    private final static boolean WITH_STACKTRACE = false;

    private static JSONResponse INSTANCE = new JSONResponse();

    private final Gson GSON = new GsonBuilder().setDateFormat(DateTimeType.DATE_PATTERN_WITH_TZ_AND_MS).create();

    /**
     * avoid instantiation apart from {@link #createResponse}.
     */
    private JSONResponse() {
    }

    /**
     * basic configuration of a ResponseBuilder
     *
     * @param status
     * @return ResponseBuilder configured for "Content-Type" MediaType.APPLICATION_JSON
     */
    private ResponseBuilder response(Response.Status status) {
        return Response.status(status).header("Content-Type", MediaType.APPLICATION_JSON);
    }

    /**
     * setup JSON depending on the content
     *
     * @param message a message (may be null)
     * @param status
     * @param entity
     * @param ex
     * @return
     */
    private JsonElement createErrorJson(String message, Response.Status status, Object entity, Exception ex) {
        JsonObject resultJson = new JsonObject();
        JsonObject errorJson = new JsonObject();
        resultJson.add(JSON_KEY_ERROR, errorJson);

        errorJson.addProperty(JSON_KEY_ERROR_MESSAGE, message);

        // in case we have a http status code, report it
        if (status != null) {
            errorJson.addProperty(JSON_KEY_HTTPCODE, status.getStatusCode());
        }

        // in case there is an entity...
        if (entity != null) {
            // return the existing object
            resultJson.add("entity", GSON.toJsonTree(entity));
        }

        // is there an exception?
        if (ex != null) {

            // JSONify the Exception
            JsonObject exceptionJson = new JsonObject();
            errorJson.add("exception", exceptionJson);
            {
                exceptionJson.addProperty("class", ex.getClass().getName());
                exceptionJson.addProperty("message", ex.getMessage());
                exceptionJson.addProperty("localized-message", ex.getLocalizedMessage());
                exceptionJson.addProperty("cause", null != ex.getCause() ? ex.getCause().getClass().getName() : null);

                if (WITH_STACKTRACE) {
                    exceptionJson.add("stacktrace", GSON.toJsonTree(ex.getStackTrace()));
                }
            }
        }

        return resultJson;
    }

    /**
     * in case of error (404 and such)
     *
     * @param status
     * @param errormessage
     * @return Response containing a status and the errormessage in JSON format
     */
    public static Response createErrorResponse(Response.Status status, String errormessage) {
        return createResponse(status, null, errormessage);
    }

    /**
     * Depending on the status, create a Response object containing either the entity alone or an error JSON
     * which might hold the entity as well.
     *
     * @param status
     * @param entity
     * @param errormessage an optional error message (may be null), ignored if the status family is successful
     * @return Response configure for error or success
     */
    public static Response createResponse(Response.Status status, Object entity, String errormessage) {
        ResponseBuilder rp = INSTANCE.response(status);
        if (status.getFamily() == Response.Status.Family.SUCCESSFUL) {
            PipedOutputStream out = new PipedOutputStream();

            try {
                PipedInputStream in = new PipedInputStream(out);
                rp.entity(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try (JsonWriter jsonWriter = INSTANCE.GSON
                            .newJsonWriter(new BufferedWriter(new OutputStreamWriter(out)))) {
                        if (entity != null) {
                            INSTANCE.GSON.toJson(entity, entity.getClass(), jsonWriter);
                        }
                    } catch (IOException e) {
                        INSTANCE.logger.error("Error streaming JSON through PipedInpuStream/PipedOutputStream.", e);
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        } else {
            JsonElement errorJson = INSTANCE.createErrorJson(errormessage, status, entity, null);
            rp.entity(errorJson);
        }

        return rp.build();
    }

    /**
     * trap exceptions
     *
     * @author Joerg Plewe
     */
    @Provider
    public static class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Exception> {

        private final Logger logger = LoggerFactory.getLogger(ExceptionMapper.class);

        /**
         * create JSON Response
         */

        @Override
        public Response toResponse(Exception e) {

            logger.debug("exception during REST Handling", e);

            Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;

            // in case the Exception is a WebApplicationException, it already carries a Status
            if (e instanceof WebApplicationException) {
                status = (Response.Status) ((WebApplicationException) e).getResponse().getStatusInfo();
            }

            JsonElement ret = INSTANCE.createErrorJson(e.getMessage(), status, null, e);
            return INSTANCE.response(status).entity(INSTANCE.GSON.toJson(ret)).build();
        }
    }
}

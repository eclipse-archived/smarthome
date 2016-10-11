/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest;

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

/**
 * Static helper methods to build up JSON-like Response objects and error handling.
 *
 * @author Joerg Plewe
 */
@Provider
public class JSONResponse {
    public static final String JSON_KEY_ERROR_MESSAGE = "message";
    public static final String JSON_KEY_ERROR = "error";
    public static final String JSON_KEY_HTTPCODE = "http-code";

    // also dump stacktrace?
    private final static boolean WITH_STACKTRACE = false;

    final static Gson GSON = new GsonBuilder().setDateFormat(DateTimeType.DATE_PATTERN_WITH_TZ_AND_MS)
            .setPrettyPrinting().create();

    /**
     * hide ctor a bit from public
     */
    JSONResponse() {
    }

    /**
     * basic configuration of a ResponseBuilder
     *
     * @param status
     * @return ResponseBuilder configured for "Content-Type" MediaType.APPLICATION_JSON
     */
    private static ResponseBuilder response(Response.Status status) {
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
    private static JsonElement createErrorJson(String message, Response.Status status, Object entity, Exception ex) {
        JsonObject ret = new JsonObject();
        JsonObject err = new JsonObject();
        ret.add(JSON_KEY_ERROR, err);

        err.addProperty(JSON_KEY_ERROR_MESSAGE, message);

        // in case we have a http status code, report it
        if (null != status) {
            err.addProperty(JSON_KEY_HTTPCODE, status.getStatusCode());
        }

        // in case there is an entity...
        if (null != entity) {
            // return the existing object
            ret.add("entity", GSON.toJsonTree(entity));
        }

        // is there an exception?
        if (null != ex) {

            // JSONify the Exception
            JsonObject exc = new JsonObject();
            err.add("exception", exc);
            {
                exc.addProperty("class", ex.getClass().getName());
                exc.addProperty("message", ex.getMessage());
                exc.addProperty("localized-message", ex.getLocalizedMessage());
                exc.addProperty("cause", null != ex.getCause() ? ex.getCause().getClass().getName() : null);

                if (WITH_STACKTRACE) {
                    exc.add("stacktrace", GSON.toJsonTree(ex.getStackTrace()));
                }
            }
        }

        return ret;
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
     * Depending in the status, create a Response object containing either the entity alone or an error JSON
     * which might hold the entity as well.
     *
     * @param status
     * @param entity
     * @param errormessage an optional error message (may be null), ignored if the status family is successful
     * @return Response configure for error or success
     */
    public static Response createResponse(Response.Status status, Object entity, String errormessage) {
        JsonElement ret;
        if (status.getFamily() == Response.Status.Family.SUCCESSFUL) {
            // create non-null JsonElement if null!=entity
            ret = null != entity ? GSON.toJsonTree(entity) : null;
        } else {
            ret = createErrorJson(errormessage, status, entity, null);
        }

        // configure response
        ResponseBuilder rp = response(status);
        if (null != ret) {
            rp = rp.entity(GSON.toJson(ret));
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

            JsonElement ret = createErrorJson(e.getMessage(), status, null, e);
            return response(status).entity(GSON.toJson(ret)).build();
        }
    }
}

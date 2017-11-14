/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.impl;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link JSONResponseHandler} checks an digitalSTROM-JSON response and can parse it to an {@link JsonObject}.
 *
 * @author Alexander Betker - Initial contribution
 * @author Alex Maier - Initial contribution
 * @author Michael Ochel - add Java-Doc, make methods static and change from SimpleJSON to GSON
 * @author Matthias Siegele - add Java-Doc, make methods static and change from SimpleJSON to GSON
 */
public class JSONResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(JSONResponseHandler.class);

    /**
     * Checks the digitalSTROM-JSON response and return true if it was successful, otherwise false.
     *
     * @param jsonResponse
     * @return true, if successful
     */
    public static boolean checkResponse(JsonObject jsonResponse) {
        if (jsonResponse == null) {
            return false;
        } else if (jsonResponse.get(JSONApiResponseKeysEnum.RESPONSE_OK.getKey()) != null) {
            return jsonResponse.get(JSONApiResponseKeysEnum.RESPONSE_OK.getKey()).toString()
                    .equals(JSONApiResponseKeysEnum.RESPONSE_SUCCESSFUL.getKey());
        } else {
            logger.error("JSONResponseHandler: error in json request. Error message : {}",
                    jsonResponse.get(JSONApiResponseKeysEnum.RESPONSE_MESSAGE.getKey()));
        }
        return false;
    }

    /**
     * Returns the {@link JsonObject} from the given digitalSTROM-JSON response {@link String} or null if the json
     * response was empty.
     *
     * @param jsonResponse
     * @return jsonObject
     */
    public static JsonObject toJsonObject(String jsonResponse) {
        if (jsonResponse != null && !jsonResponse.trim().equals("")) {
            try {
                return (JsonObject) new JsonParser().parse(jsonResponse);
            } catch (JsonParseException e) {
                logger.error("An JsonParseException occurred by parsing jsonRequest: {}", jsonResponse, e);
            }
        }
        return null;
    }

    /**
     * Returns the result {@link JsonObject} from the given digitalSTROM-JSON response {@link JsonObject}.
     *
     * @param jsonObject
     * @return json result object
     */
    public static JsonObject getResultJsonObject(JsonObject jsonObject) {
        if (jsonObject != null) {
            return (JsonObject) jsonObject.get(JSONApiResponseKeysEnum.RESULT.getKey());
        }
        return null;
    }

}
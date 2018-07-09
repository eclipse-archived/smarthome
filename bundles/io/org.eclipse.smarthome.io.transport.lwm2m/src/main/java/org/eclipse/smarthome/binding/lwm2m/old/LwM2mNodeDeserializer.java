/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.smarthome.binding.lwm2m.old;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.leshan.core.node.LwM2mMultipleResource;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mObject;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

public class LwM2mNodeDeserializer implements JsonDeserializer<LwM2mNode> {

    @Override
    public LwM2mNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        if (json == null) {
            return null;
        }

        LwM2mNode node = null;

        if (json.isJsonObject()) {
            JsonObject object = (JsonObject) json;

            if (!object.has("id")) {
                throw new JsonParseException("Missing id");
            }
            int id = object.get("id").getAsInt();

            if (object.has("instances")) {

                JsonArray array = object.get("instances").getAsJsonArray();
                LwM2mObjectInstance[] instances = new LwM2mObjectInstance[array.size()];

                for (int i = 0; i < array.size(); i++) {
                    instances[i] = context.deserialize(array.get(i), LwM2mNode.class);
                }
                node = new LwM2mObject(id, instances);

            } else if (object.has("resources")) {
                JsonArray array = object.get("resources").getAsJsonArray();
                LwM2mResource[] resources = new LwM2mResource[array.size()];

                for (int i = 0; i < array.size(); i++) {
                    resources[i] = context.deserialize(array.get(i), LwM2mNode.class);
                }
                node = new LwM2mObjectInstance(id, resources);

            } else if (object.has("value")) {
                // single value resource
                JsonPrimitive val = object.get("value").getAsJsonPrimitive();
                org.eclipse.leshan.core.model.ResourceModel.Type expectedType = getTypeFor(val);
                node = LwM2mSingleResource.newResource(id, deserializeValue(val, expectedType), expectedType);
            } else if (object.has("values")) {
                // multi-instances resource
                Map<Integer, Object> values = new HashMap<Integer, Object>();
                // TODO handle id for multiple resource
                int i = 0;
                org.eclipse.leshan.core.model.ResourceModel.Type expectedType = null;
                for (JsonElement val : object.get("values").getAsJsonArray()) {
                    JsonPrimitive pval = val.getAsJsonPrimitive();
                    expectedType = getTypeFor(pval);
                    values.put(i, deserializeValue(pval, expectedType));
                    i++;
                }
                // use string by default;
                if (expectedType == null) {
                    expectedType = org.eclipse.leshan.core.model.ResourceModel.Type.STRING;
                }
                node = LwM2mMultipleResource.newResource(id, values, expectedType);
            } else {
                throw new JsonParseException("Invalid node element");
            }
        } else {
            throw new JsonParseException("Invalid node element");
        }

        return node;
    }

    private org.eclipse.leshan.core.model.ResourceModel.Type getTypeFor(JsonPrimitive val) {
        if (val.isBoolean()) {
            return org.eclipse.leshan.core.model.ResourceModel.Type.BOOLEAN;
        }
        if (val.isString()) {
            return org.eclipse.leshan.core.model.ResourceModel.Type.STRING;
        }
        if (val.isNumber()) {
            if (val.getAsDouble() == val.getAsLong()) {
                return org.eclipse.leshan.core.model.ResourceModel.Type.INTEGER;
            } else {
                return org.eclipse.leshan.core.model.ResourceModel.Type.FLOAT;
            }
        }
        // use string as default value
        return org.eclipse.leshan.core.model.ResourceModel.Type.STRING;
    }

    private Object deserializeValue(JsonPrimitive val, org.eclipse.leshan.core.model.ResourceModel.Type expectedType) {
        switch (expectedType) {
            case BOOLEAN:
                return val.getAsBoolean();
            case STRING:
                return val.getAsString();
            case INTEGER:
                return val.getAsLong();
            case FLOAT:
                return val.getAsDouble();
            case TIME:
            case OPAQUE:
            default:
                return val.getAsString();
        }
    }
}
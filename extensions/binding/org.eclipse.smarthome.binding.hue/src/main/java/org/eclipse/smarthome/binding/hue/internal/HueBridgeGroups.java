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
package org.eclipse.smarthome.binding.hue.internal;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.dto.Group;
import org.eclipse.smarthome.binding.hue.internal.dto.Light;
import org.eclipse.smarthome.binding.hue.internal.dto.SuccessResponse;
import org.eclipse.smarthome.binding.hue.internal.dto.updates.GroupUpdateOrCreate;
import org.eclipse.smarthome.binding.hue.internal.dto.updates.LightStateUpdate;
import org.eclipse.smarthome.binding.hue.internal.exceptions.ApiException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.EntityNotAvailableException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.GroupTableFullException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.UnauthorizedException;
import org.eclipse.smarthome.binding.hue.internal.utils.AsyncHttpClient;
import org.eclipse.smarthome.binding.hue.internal.utils.Util;

/**
 * Group functionality. This is not used by the binding right now.
 *
 * @author David Graeff - Initial contribution, factored out of {@link HueBridge}.
 */
public class HueBridgeGroups {
    private HueBridge bridge;

    HueBridgeGroups(HueBridge bridge) {
        this.bridge = bridge;
    }

    /**
     * Creates a new group and returns it.
     * Due to API limitations, the name of the returned object
     * will simply be the same as the name parameter. The bridge will
     * append a number to the name if it's a duplicate. To get the final
     * name, call getGroup with the returned object.
     *
     * @param name new group name
     * @param lights lights in group
     * @return object representing new group
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws GroupTableFullException thrown if the group limit has been reached
     */
    public Group createGroup(@Nullable String name, List<Light> lights) throws IOException, ApiException {
        String body = bridge.gson.toJson(new GroupUpdateOrCreate(name, lights));
        AsyncHttpClient.Result result = bridge.http.post(bridge.getAbsoluteURL("groups"), body);

        bridge.handleErrors(result);

        List<SuccessResponse> entries = bridge.gson.fromJson(result.getBody(), SuccessResponse.GSON_TYPE);
        SuccessResponse response = entries.get(0);

        Matcher m = Pattern.compile("^/groups/([0-9]+)$").matcher((String) response.success.values().toArray()[0]);
        m.find();

        return new Group(name).withId(m.group(1));
    }

    /**
     * Changes the name of the group and returns the new name.
     * A number will be appended to duplicate names, which may result in a new name exceeding 32 characters.
     *
     * @param group group
     * @param name new name [0..32]
     * @return new name
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified group no longer exists
     */
    public String setGroupName(Group group, String name) throws IOException, ApiException {
        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        String body = bridge.gson.toJson(new GroupUpdateOrCreate(name));
        AsyncHttpClient.Result result = bridge.http.put(bridge.getAbsoluteURL("groups/" + Util.enc(group.id)), body);

        bridge.handleErrors(result);

        List<SuccessResponse> entries = bridge.gson.fromJson(result.getBody(), SuccessResponse.GSON_TYPE);
        SuccessResponse response = entries.get(0);

        return (String) response.success.get("/groups/" + Util.enc(group.id) + "/name");
    }

    /**
     * Changes the lights in the group.
     *
     * @param group group
     * @param lights new lights [1..16]
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified group no longer exists
     */
    public void setGroupLights(Group group, List<Light> lights) throws IOException, ApiException {
        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        String body = bridge.gson.toJson(new GroupUpdateOrCreate(null, lights));
        AsyncHttpClient.Result result = bridge.http.put(bridge.getAbsoluteURL("groups/" + Util.enc(group.id)), body);

        bridge.handleErrors(result);
    }

    /**
     * Changes the name and the lights of a group and returns the new name.
     *
     * @param group group
     * @param name new name [0..32]
     * @param lights [1..16]
     * @return new name
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified group no longer exists
     */
    public String setGroupAttributes(Group group, String name, List<Light> lights) throws IOException, ApiException {
        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        String body = bridge.gson.toJson(new GroupUpdateOrCreate(name, lights));
        AsyncHttpClient.Result result = bridge.http.put(bridge.getAbsoluteURL("groups/" + Util.enc(group.id)), body);

        bridge.handleErrors(result);

        List<SuccessResponse> entries = bridge.gson.fromJson(result.getBody(), SuccessResponse.GSON_TYPE);
        SuccessResponse response = entries.get(0);

        return (String) response.success.get("/groups/" + Util.enc(group.id) + "/name");
    }

    /**
     * Changes the state of a group.
     *
     * @param group group
     * @param update changes to the state
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified group no longer exists
     */
    public void setGroupState(Group group, LightStateUpdate update) throws IOException, ApiException {
        AsyncHttpClient.Result result = bridge.http
                .put(bridge.getAbsoluteURL("groups/" + Util.enc(group.id) + "/action"), bridge.gson.toJson(update));
        bridge.handleErrors(result);
    }

    /**
     * Delete a group.
     *
     * @param group group
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified group no longer exists
     */
    public void deleteGroup(Group group) throws IOException, ApiException {
        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        AsyncHttpClient.Result result = bridge.http.delete(bridge.getAbsoluteURL("groups/" + Util.enc(group.id)));
        bridge.handleErrors(result);
    }
}

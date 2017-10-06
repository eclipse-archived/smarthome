/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.binding.hue.internal.HttpClient.Result;
import org.eclipse.smarthome.binding.hue.internal.exceptions.ApiException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.DeviceOffException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.EntityNotAvailableException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.GroupTableFullException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.InvalidCommandException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.LinkButtonException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.UnauthorizedException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Representation of a connection with a Hue bridge.
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Andre Fuechsel - search for lights with given serial number added
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding, minor code cleanup
 */
public class HueBridge {
    private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private String ip;
    private String username;

    private Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
    private HttpClient http = new HttpClient();

    /**
     * Connect with a bridge as a new user.
     *
     * @param ip ip address of bridge
     */
    public HueBridge(String ip) {
        this.ip = ip;
    }

    /**
     * Connect with a bridge as an existing user.
     *
     * The username is verified by requesting the list of lights.
     * Use the ip only constructor and authenticate() function if
     * you don't want to connect right now.
     *
     * @param ip ip address of bridge
     * @param username username to authenticate with
     */
    public HueBridge(String ip, String username) throws IOException, ApiException {
        this.ip = ip;
        authenticate(username);
    }

    /**
     * Set the connect and read timeout for HTTP requests.
     *
     * @param timeout timeout in milliseconds or 0 for indefinitely
     */
    public void setTimeout(int timeout) {
        http.setTimeout(timeout);
    }

    /**
     * Returns the IP address of the bridge.
     *
     * @return ip address of bridge
     */
    public String getIPAddress() {
        return ip;
    }

    /**
     * Returns the username currently authenticated with or null if there isn't one.
     *
     * @return username or null
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns if authentication was successful on the bridge.
     *
     * @return true if authenticated on the bridge, false otherwise
     */
    public boolean isAuthenticated() {
        return getUsername() != null;
    }

    /**
     * Returns a list of lights known to the bridge.
     *
     * @return list of known lights
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public List<Light> getLights() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("lights"));

        handleErrors(result);

        Map<String, Light> lightMap = safeFromJson(result.getBody(), Light.gsonType);

        ArrayList<Light> lightList = new ArrayList<>();

        for (String id : lightMap.keySet()) {
            Light light = lightMap.get(id);
            light.setId(id);
            lightList.add(light);
        }

        return lightList;
    }

    /**
     * Returns the last time a search for new lights was started.
     * If a search is currently running, the current time will be
     * returned or null if a search has never been started.
     *
     * @return last search time
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public Date getLastSearch() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("lights/new"));

        handleErrors(result);

        String lastScan = safeFromJson(result.getBody(), NewLightsResponse.class).lastscan;

        switch (lastScan) {
            case "none":
                return null;
            case "active":
                return new Date();
            default:
                try {
                    return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(lastScan);
                } catch (ParseException e) {
                    return null;
                }
        }
    }

    /**
     * Start searching for new lights for 1 minute.
     * A maximum amount of 15 new lights will be added.
     *
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public void startSearch() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.post(getRelativeURL("lights"), "");

        handleErrors(result);
    }

    /**
     * Start searching for new lights with given serial numbers for 1 minute.
     * A maximum amount of 15 new lights will be added.
     *
     * @param serialNumbers list of serial numbers
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public void startSearch(List<String> serialNumbers) throws IOException, ApiException {
        requireAuthentication();

        String body = gson.toJson(new SearchForLightsRequest(serialNumbers));
        Result result = http.post(getRelativeURL("lights"), body);

        handleErrors(result);
    }

    /**
     * Returns detailed information for the given light.
     *
     * @param light light
     * @return detailed light information
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if a light with the given id doesn't exist
     */
    public FullLight getLight(Light light) throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("lights/" + enc(light.getId())));

        handleErrors(result);

        FullLight fullLight = safeFromJson(result.getBody(), FullLight.class);
        fullLight.setId(light.getId());
        return fullLight;
    }

    /**
     * Changes the name of the light and returns the new name.
     * A number will be appended to duplicate names, which may result in a new name exceeding 32 characters.
     *
     * @param light light
     * @param name new name [0..32]
     * @return new name
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified light no longer exists
     */
    public String setLightName(Light light, String name) throws IOException, ApiException {
        requireAuthentication();

        String body = gson.toJson(new SetAttributesRequest(name));
        Result result = http.put(getRelativeURL("lights/" + enc(light.getId())), body);

        handleErrors(result);

        List<SuccessResponse> entries = safeFromJson(result.getBody(), SuccessResponse.gsonType);
        SuccessResponse response = entries.get(0);

        return (String) response.success.get("/lights/" + enc(light.getId()) + "/name");
    }

    /**
     * Changes the state of a light.
     *
     * @param light light
     * @param update changes to the state
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified light no longer exists
     * @throws DeviceOffException thrown if the specified light is turned off
     * @throws IOException if the bridge cannot be reached
     */
    public void setLightState(Light light, StateUpdate update) throws IOException, ApiException {
        requireAuthentication();

        String body = update.toJson();
        Result result = http.put(getRelativeURL("lights/" + enc(light.getId()) + "/state"), body);
        handleErrors(result);
    }

    /**
     * Returns a group object representing all lights.
     *
     * @return all lights pseudo group
     */
    public Group getAllGroup() {
        requireAuthentication();

        return new Group();
    }

    /**
     * Returns the list of groups, including the unmodifiable all lights group.
     *
     * @return list of groups
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public List<Group> getGroups() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("groups"));

        handleErrors(result);

        Map<String, Group> groupMap = safeFromJson(result.getBody(), Group.gsonType);
        ArrayList<Group> groupList = new ArrayList<>();

        groupList.add(new Group());

        for (String id : groupMap.keySet()) {
            Group group = groupMap.get(id);
            group.setId(id);
            groupList.add(group);
        }

        return groupList;
    }

    /**
     * Creates a new group and returns it.
     * Due to API limitations, the name of the returned object
     * will simply be "Group". The bridge will append a number to this
     * name if it's a duplicate. To get the final name, call getGroup
     * with the returned object.
     *
     * @param lights lights in group
     * @return object representing new group
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws GroupTableFullException thrown if the group limit has been reached
     */
    public Group createGroup(List<Light> lights) throws IOException, ApiException {
        requireAuthentication();

        String body = gson.toJson(new SetAttributesRequest(lights));
        Result result = http.post(getRelativeURL("groups"), body);

        handleErrors(result);

        List<SuccessResponse> entries = safeFromJson(result.getBody(), SuccessResponse.gsonType);
        SuccessResponse response = entries.get(0);

        Group group = new Group();
        group.setName("Group");
        group.setId(Util.quickMatch("^/groups/([0-9]+)$", (String) response.success.values().toArray()[0]));
        return group;
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
    public Group createGroup(String name, List<Light> lights) throws IOException, ApiException {
        requireAuthentication();

        String body = gson.toJson(new SetAttributesRequest(name, lights));
        Result result = http.post(getRelativeURL("groups"), body);

        handleErrors(result);

        List<SuccessResponse> entries = safeFromJson(result.getBody(), SuccessResponse.gsonType);
        SuccessResponse response = entries.get(0);

        Group group = new Group();
        group.setName(name);
        group.setId(Util.quickMatch("^/groups/([0-9]+)$", (String) response.success.values().toArray()[0]));
        return group;
    }

    /**
     * Returns detailed information for the given group.
     *
     * @param group group
     * @return detailed group information
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if a group with the given id doesn't exist
     */
    public FullGroup getGroup(Group group) throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("groups/" + enc(group.getId())));

        handleErrors(result);

        FullGroup fullGroup = safeFromJson(result.getBody(), FullGroup.class);
        fullGroup.setId(group.getId());
        return fullGroup;
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
        requireAuthentication();

        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        String body = gson.toJson(new SetAttributesRequest(name));
        Result result = http.put(getRelativeURL("groups/" + enc(group.getId())), body);

        handleErrors(result);

        List<SuccessResponse> entries = safeFromJson(result.getBody(), SuccessResponse.gsonType);
        SuccessResponse response = entries.get(0);

        return (String) response.success.get("/groups/" + enc(group.getId()) + "/name");
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
        requireAuthentication();

        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        String body = gson.toJson(new SetAttributesRequest(lights));
        Result result = http.put(getRelativeURL("groups/" + enc(group.getId())), body);

        handleErrors(result);
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
        requireAuthentication();

        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        String body = gson.toJson(new SetAttributesRequest(name, lights));
        Result result = http.put(getRelativeURL("groups/" + enc(group.getId())), body);

        handleErrors(result);

        List<SuccessResponse> entries = safeFromJson(result.getBody(), SuccessResponse.gsonType);
        SuccessResponse response = entries.get(0);

        return (String) response.success.get("/groups/" + enc(group.getId()) + "/name");
    }

    /**
     * Changes the state of a group.
     *
     * @param group group
     * @param update changes to the state
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified group no longer exists
     */
    public void setGroupState(Group group, StateUpdate update) throws IOException, ApiException {
        requireAuthentication();

        String body = update.toJson();
        Result result = http.put(getRelativeURL("groups/" + enc(group.getId()) + "/action"), body);

        handleErrors(result);
    }

    /**
     * Delete a group.
     *
     * @param group group
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified group no longer exists
     */
    public void deleteGroup(Group group) throws IOException, ApiException {
        requireAuthentication();

        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        Result result = http.delete(getRelativeURL("groups/" + enc(group.getId())));

        handleErrors(result);
    }

    /**
     * Returns a list of schedules on the bridge.
     *
     * @return schedules
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public List<Schedule> getSchedules() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("schedules"));

        handleErrors(result);

        Map<String, Schedule> scheduleMap = safeFromJson(result.getBody(), Schedule.gsonType);

        ArrayList<Schedule> scheduleList = new ArrayList<>();

        for (String id : scheduleMap.keySet()) {
            Schedule schedule = scheduleMap.get(id);
            schedule.setId(id);
            scheduleList.add(schedule);
        }

        return scheduleList;
    }

    /**
     * Schedules a new command to be run at the specified time.
     * To select the command for the new schedule, simply run it
     * as you normally would in the callback. Instead of it running
     * immediately, it will be scheduled to run at the specified time.
     * It will automatically fail with an IOException, because there
     * will be no response. Note that GET methods cannot be scheduled,
     * so those will still run and return results immediately.
     *
     * @param time time to run command
     * @param callback callback in which the command is specified
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws InvalidCommandException thrown if the scheduled command is larger than 90 bytes or otherwise invalid
     */
    public void createSchedule(Date time, ScheduleCallback callback) throws IOException, ApiException {
        createSchedule(null, null, time, callback);
    }

    /**
     * Schedules a new command to be run at the specified time.
     * To select the command for the new schedule, simply run it
     * as you normally would in the callback. Instead of it running
     * immediately, it will be scheduled to run at the specified time.
     * It will automatically fail with an IOException, because there
     * will be no response. Note that GET methods cannot be scheduled,
     * so those will still run and return results immediately.
     *
     * @param name name [0..32]
     * @param time time to run command
     * @param callback callback in which the command is specified
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws InvalidCommandException thrown if the scheduled command is larger than 90 bytes or otherwise invalid
     */
    public void createSchedule(String name, Date time, ScheduleCallback callback) throws IOException, ApiException {
        createSchedule(name, null, time, callback);
    }

    /**
     * Schedules a new command to be run at the specified time.
     * To select the command for the new schedule, simply run it
     * as you normally would in the callback. Instead of it running
     * immediately, it will be scheduled to run at the specified time.
     * It will automatically fail with an IOException, because there
     * will be no response. Note that GET methods cannot be scheduled,
     * so those will still run and return results immediately.
     *
     * @param name name [0..32]
     * @param description description [0..64]
     * @param time time to run command
     * @param callback callback in which the command is specified
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws InvalidCommandException thrown if the scheduled command is larger than 90 bytes or otherwise invalid
     */
    public void createSchedule(String name, String description, Date time, ScheduleCallback callback)
            throws IOException, ApiException {
        requireAuthentication();

        handleCommandCallback(callback);

        String body = gson.toJson(new CreateScheduleRequest(name, description, scheduleCommand, time));
        Result result = http.post(getRelativeURL("schedules"), body);

        handleErrors(result);
    }

    /**
     * Returns detailed information for the given schedule.
     *
     * @param schedule schedule
     * @return detailed schedule information
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified schedule no longer exists
     */
    public FullSchedule getSchedule(Schedule schedule) throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("schedules/" + enc(schedule.getId())));

        handleErrors(result);

        FullSchedule fullSchedule = safeFromJson(result.getBody(), FullSchedule.class);
        fullSchedule.setId(schedule.getId());
        return fullSchedule;
    }

    /**
     * Changes a schedule.
     *
     * @param schedule schedule
     * @param update changes
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified schedule no longer exists
     */
    public void setSchedule(Schedule schedule, ScheduleUpdate update) throws IOException, ApiException {
        requireAuthentication();

        String body = update.toJson();
        Result result = http.put(getRelativeURL("schedules/" + enc(schedule.getId())), body);

        handleErrors(result);
    }

    /**
     * Changes the command of a schedule.
     *
     * @param schedule schedule
     * @param callback callback for new command
     * @see #createSchedule(String, String, Date, ScheduleCallback)
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws InvalidCommandException thrown if the scheduled command is larger than 90 bytes or otherwise invalid
     */
    public void setScheduleCommand(Schedule schedule, ScheduleCallback callback) throws IOException, ApiException {
        requireAuthentication();

        handleCommandCallback(callback);

        String body = gson.toJson(new CreateScheduleRequest(null, null, scheduleCommand, null));
        Result result = http.put(getRelativeURL("schedules/" + enc(schedule.getId())), body);

        handleErrors(result);
    }

    /**
     * Callback to specify a schedule command.
     */
    public interface ScheduleCallback {
        /**
         * Run the command you want to schedule as if you're executing
         * it normally. The request will automatically fail to produce
         * a result by throwing an IOException. Requests that only
         * get data (e.g. getGroups) will still execute immediately,
         * because those cannot be scheduled.
         *
         * @param bridge this bridge for convenience
         * @throws IOException always thrown right after executing a command
         */
        public void onScheduleCommand(HueBridge bridge) throws IOException, ApiException;
    }

    private ScheduleCommand scheduleCommand = null;

    private ScheduleCommand handleCommandCallback(ScheduleCallback callback) throws ApiException {
        // Temporarily reroute requests to a fake HTTP client
        HttpClient realClient = http;
        http = new HttpClient() {
            @Override
            protected Result doNetwork(String address, String requestMethod, String body) throws IOException {
                // GET requests cannot be scheduled, so will continue working normally for convenience
                if (requestMethod.equals("GET")) {
                    return super.doNetwork(address, requestMethod, body);
                } else {
                    String extractedAddress = Util.quickMatch("^http://[^/]+(.+)$", address);
                    JsonElement commandBody = new JsonParser().parse(body);
                    scheduleCommand = new ScheduleCommand(extractedAddress, requestMethod, commandBody);

                    // Return a fake result that will cause an exception and the callback to end
                    return new Result(null, 405);
                }
            }
        };

        // Run command
        try {
            scheduleCommand = null;
            callback.onScheduleCommand(this);
        } catch (IOException e) {
            // Command will automatically fail to return a result because of deferred execution
        } finally {
            if (scheduleCommand != null && Util.stringSize(scheduleCommand.getBody()) > 90) {
                throw new InvalidCommandException("Commmand body is larger than 90 bytes");
            }
        }

        // Restore HTTP client
        http = realClient;

        return scheduleCommand;
    }

    /**
     * Delete a schedule.
     *
     * @param schedule schedule
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the schedule no longer exists
     */
    public void deleteSchedule(Schedule schedule) throws IOException, ApiException {
        requireAuthentication();

        Result result = http.delete(getRelativeURL("schedules/" + enc(schedule.getId())));

        handleErrors(result);
    }

    /**
     * Authenticate on the bridge as the specified user.
     * This function verifies that the specified username is valid and will use
     * it for subsequent requests if it is, otherwise an UnauthorizedException
     * is thrown and the internal username is not changed.
     *
     * @param username username to authenticate
     * @throws UnauthorizedException thrown if authentication failed
     */
    public void authenticate(String username) throws IOException, ApiException {
        try {
            this.username = username;
            getLights();
        } catch (Exception e) {
            this.username = null;
            throw new UnauthorizedException(e.toString());
        }
    }

    /**
     * Link with bridge using the specified username and device type.
     *
     * @param username username for new user [10..40]
     * @param devicetype identifier of application [0..40]
     * @throws LinkButtonException thrown if the bridge button has not been pressed
     */
    public void link(String username, String devicetype) throws IOException, ApiException {
        this.username = link(new CreateUserRequest(username, devicetype));
    }

    /**
     * Link with bridge using the specified device type. A random valid username will be generated by the bridge and
     * returned.
     *
     * @return new random username generated by bridge
     * @param devicetype identifier of application [0..40]
     * @throws LinkButtonException thrown if the bridge button has not been pressed
     */
    public String link(String devicetype) throws IOException, ApiException {
        return (this.username = link(new CreateUserRequest(devicetype)));
    }

    private String link(CreateUserRequest request) throws IOException, ApiException {
        if (this.username != null) {
            throw new IllegalStateException("already linked");
        }

        String body = gson.toJson(request);
        Result result = http.post(getRelativeURL(""), body);

        handleErrors(result);

        List<SuccessResponse> entries = safeFromJson(result.getBody(), SuccessResponse.gsonType);
        SuccessResponse response = entries.get(0);

        return (String) response.success.get("username");
    }

    /**
     * Returns bridge configuration.
     *
     * @see Config
     * @return bridge configuration
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public Config getConfig() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("config"));

        handleErrors(result);

        return safeFromJson(result.getBody(), Config.class);
    }

    /**
     * Change the configuration of the bridge.
     *
     * @param update changes to the configuration
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public void setConfig(ConfigUpdate update) throws IOException, ApiException {
        requireAuthentication();

        String body = update.toJson();
        Result result = http.put(getRelativeURL("config"), body);

        handleErrors(result);
    }

    /**
     * Unlink the current user from the bridge.
     *
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public void unlink() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.delete(getRelativeURL("config/whitelist/" + enc(username)));

        handleErrors(result);
    }

    /**
     * Returns the entire bridge configuration.
     * This request is rather resource intensive for the bridge,
     * don't use it more often than necessary. Prefer using requests for
     * specific information your app needs.
     *
     * @return full bridge configuration
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public FullConfig getFullConfig() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL(""));

        handleErrors(result);

        return gson.fromJson(result.getBody(), FullConfig.class);
    }

    // Used as assert in requests that require authentication
    private void requireAuthentication() {
        if (this.username == null) {
            throw new IllegalStateException("linking is required before interacting with the bridge");
        }
    }

    // Methods that convert gson exceptions into ApiExceptions
    private <T> T safeFromJson(String json, Type typeOfT) throws ApiException {
        try {
            return gson.fromJson(json, typeOfT);
        } catch (JsonParseException e) {
            throw new ApiException("API returned unexpected result: " + e.getMessage());
        }
    }

    private <T> T safeFromJson(String json, Class<T> classOfT) throws ApiException {
        try {
            return gson.fromJson(json, classOfT);
        } catch (JsonParseException e) {
            throw new ApiException("API returned unexpected result: " + e.getMessage());
        }
    }

    // Used as assert in all requests to elegantly catch common errors
    private void handleErrors(Result result) throws IOException, ApiException {
        if (result.getResponseCode() != 200) {
            throw new IOException();
        } else {
            try {
                List<ErrorResponse> errors = gson.fromJson(result.getBody(), ErrorResponse.gsonType);
                if (errors == null) {
                    return;
                }

                for (ErrorResponse error : errors) {
                    switch (error.getType()) {
                        case 1:
                            username = null;
                            throw new UnauthorizedException(error.getDescription());
                        case 3:
                            throw new EntityNotAvailableException(error.getDescription());
                        case 7:
                            throw new InvalidCommandException(error.getDescription());
                        case 101:
                            throw new LinkButtonException(error.getDescription());
                        case 201:
                            throw new DeviceOffException(error.getDescription());
                        case 301:
                            throw new GroupTableFullException(error.getDescription());
                        default:
                            throw new ApiException(error.getDescription());
                    }
                }
            } catch (JsonParseException e) {
                // Not an error
            } catch (NullPointerException e) {
                // Object that looks like error
            }
        }
    }

    // UTF-8 URL encode
    private String enc(String str) {
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // throw new EndOfTheWorldException()
            throw new UnsupportedOperationException("UTF-8 not supported");
        }
    }

    private String getRelativeURL(String path) {
        String relativeUrl = "http://" + ip + "/api";
        if (username != null) {
            relativeUrl += "/" + enc(username);
        }
        if (!path.isEmpty()) {
            relativeUrl += "/" + path;
        }
        return relativeUrl;
    }
}

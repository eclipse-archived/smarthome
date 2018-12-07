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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.hue.internal.dto.CreateUserRequest;
import org.eclipse.smarthome.binding.hue.internal.dto.Datastore;
import org.eclipse.smarthome.binding.hue.internal.dto.ErrorResponse;
import org.eclipse.smarthome.binding.hue.internal.dto.Group;
import org.eclipse.smarthome.binding.hue.internal.dto.HueConfig;
import org.eclipse.smarthome.binding.hue.internal.dto.Light;
import org.eclipse.smarthome.binding.hue.internal.dto.LightState;
import org.eclipse.smarthome.binding.hue.internal.dto.NewLights;
import org.eclipse.smarthome.binding.hue.internal.dto.Schedule;
import org.eclipse.smarthome.binding.hue.internal.dto.SearchForLightsRequest;
import org.eclipse.smarthome.binding.hue.internal.dto.SuccessResponse;
import org.eclipse.smarthome.binding.hue.internal.dto.updates.ConfigUpdate;
import org.eclipse.smarthome.binding.hue.internal.dto.updates.GroupUpdateOrCreate;
import org.eclipse.smarthome.binding.hue.internal.dto.updates.LightStateUpdate;
import org.eclipse.smarthome.binding.hue.internal.exceptions.ApiException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.DeviceOffException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.EntityNotAvailableException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.GroupTableFullException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.InvalidCommandException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.LinkButtonException;
import org.eclipse.smarthome.binding.hue.internal.exceptions.UnauthorizedException;
import org.eclipse.smarthome.binding.hue.internal.utils.AsyncHttpClient;
import org.eclipse.smarthome.binding.hue.internal.utils.AsyncHttpClient.Result;
import org.eclipse.smarthome.binding.hue.internal.utils.Util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * A connection to a Hue bridge. You can also register for light changes on this class.
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Andre Fuechsel - search for lights with given serial number added
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding, minor code cleanup
 * @author David Graeff - Rewritten
 */
@NonNullByDefault
public class HueBridge {
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    protected String hostAndPort = "";
    protected String username = "";
    protected Datastore ds = new Datastore();

    protected final Set<LightStatusListener> lightStatusListeners = new HashSet<>();
    protected final Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
    protected AsyncHttpClient http; // not final for test mock injection

    /**
     * Create a hue bridge object. You need to configure/initialize this object by
     * calling {@link #initialize(String, String)}.
     *
     * @param httpClient The http client for communication
     */
    public HueBridge(AsyncHttpClient httpClient) {
        this.http = httpClient;
    }

    /**
     * The username may change on a call to {@link #link(String)} or {@link #removeApiKey()}
     *
     * @param hostAndPort ip address of bridge
     * @param username username to authenticate with or null if none so far
     */
    public void initialize(String hostAndPort, @Nullable String username) {
        this.hostAndPort = hostAndPort;
        this.username = Util.enc(username);
    }

    /**
     * Returns the address of the bridge.
     *
     * @return address of bridge (host:port)
     */
    public String getAddress() {
        return hostAndPort;
    }

    /**
     * Returns the username that will be used for bridge authentication or an empty string if there isn't one.
     * If a REST API call results in a 405 Forbidden response, the username will be reset to null
     * automatically.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Return true if a hue bridge is located at the configured address.
     *
     * A REST access to http://hue-bridge/api/config is performed. This URL
     * does not require an authenticated user.
     *
     * @return True if the REST access was successful
     */
    public boolean isHueBridgeAndReachable() {
        try {
            if (http.get("http://" + hostAndPort + "/api/config").getResponseCode() != 200) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Returns the last time a search for new lights was started.
     * If a search is currently running, the current time will be
     * returned or null if a search has never been started.
     *
     * @return last search time
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public @Nullable Date getLastSearch() throws IOException, ApiException {
        AsyncHttpClient.Result result = http.get(getAbsoluteURL("lights/new"));
        handleErrors(result);
        return gson.fromJson(result.getBody(), NewLights.class).getLastScan();
    }

    /**
     * Start searching for new lights for 1 minute.
     * A maximum amount of 15 new lights will be added.
     *
     * @param serialNumbers Optional list of serial numbers
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public void startSearch(@Nullable List<String> serialNumbers) throws IOException, ApiException {
        String body = serialNumbers == null ? "" : gson.toJson(new SearchForLightsRequest(serialNumbers));
        AsyncHttpClient.Result result = http.post(getAbsoluteURL("lights"), body);
        handleErrors(result);
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
        String body = gson.toJson(new GroupUpdateOrCreate(name));
        AsyncHttpClient.Result result = http.put(getAbsoluteURL("lights/" + Util.enc(light.id)), body);

        handleErrors(result);

        List<SuccessResponse> entries = gson.fromJson(result.getBody(), SuccessResponse.GSON_TYPE);
        SuccessResponse response = entries.get(0);

        return (String) response.success.get("/lights/" + Util.enc(light.id) + "/name");
    }

    /**
     * Changes the state of a light.
     *
     * @param light light
     * @param update changes to the state
     * @return Return a future that completes either with null on success, with an {@link UnauthorizedException} if no
     *         apiKey is setup yet, with an {@link IOException} on any IO error or with an {@link ApiException} on
     *         errors reported by the bridge.
     */
    public CompletableFuture<@Nullable LightStateUpdate> setLightState(Light light, @Nullable LightStateUpdate update) {
        if (update == null) {
            return CompletableFuture.completedFuture(null);
        }
        return wrap(http.put(getAbsoluteURLnoExcept("lights/" + light.id + "/state"), gson.toJson(update), 2000));
    }

    /**
     * Returns a list of lights known to the bridge.
     *
     * @return list of known lights
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public List<Light> updateLights() throws IOException, ApiException {
        AsyncHttpClient.Result result = http.get(getAbsoluteURL("lights"));
        handleErrors(result);
        Map<String, Light> copy = new HashMap<>(ds.lights);
        ds.lights = gson.fromJson(result.getBody(), Light.GSON_TYPE);
        notifyLightStatusListeners(copy);
        return ds.getLights();
    }

    /**
     * Returns the list of groups, including the unmodifiable all lights group.
     *
     * @return list of groups
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public List<Group> updateGroups() throws IOException, ApiException {
        AsyncHttpClient.Result result = http.get(getAbsoluteURL("groups"));
        handleErrors(result);
        ds.groups = gson.fromJson(result.getBody(), Group.GSON_TYPE);
        return ds.getGroups();
    }

    /**
     * Returns a list of schedules on the bridge.
     *
     * @return schedules
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public List<Schedule> updateSchedules() throws IOException, ApiException {
        AsyncHttpClient.Result result = http.get(getAbsoluteURL("schedules"));
        handleErrors(result);
        ds.schedules = gson.fromJson(result.getBody(), Schedule.GSON_TYPE);
        return ds.getSchedules();
    }

    /**
     * Updates and returns bridge configuration.
     *
     * @see HueConfig
     * @return bridge configuration
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public HueConfig updateConfig() throws IOException, ApiException {
        AsyncHttpClient.Result result = http.get(getAbsoluteURL("config"));
        handleErrors(result);
        ds.config = gson.fromJson(result.getBody(), HueConfig.class);
        return ds.config;
    }

    /**
     * Updates and returns the entire bridge datastore.
     *
     * <p>
     * To conserve bandwidth, prefer using the more specific API endpoint methods
     * like {@link #updateLights()}, {@link #updateGroups()} and so on.
     * </p>
     *
     * @return full bridge configuration
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public Datastore updateDataStore() throws IOException, ApiException {
        AsyncHttpClient.Result result = http.get(getAbsoluteURL(""));
        handleErrors(result);
        Map<String, Light> copy = new HashMap<>(ds.lights);
        ds = gson.fromJson(result.getBody(), Datastore.class);
        notifyLightStatusListeners(copy);
        return ds;
    }

    /**
     * Change the configuration of the bridge.
     *
     * @param update changes to the configuration
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public void setConfig(ConfigUpdate update) throws IOException, ApiException {
        AsyncHttpClient.Result result = http.put(getAbsoluteURL("config"), gson.toJson(update));
        handleErrors(result);
    }

    /**
     * Resets the REST API key. Call this whenever a method throws {@link UnauthorizedException}
     * which usually means that the known API key is not whitelisted anymore.
     */
    public void resetAuthentification() {
        this.username = "";
    }

    /**
     * Link with bridge using the specified username and device type.
     *
     * @param username username for new user [10..40]. Can be null for a new random username generated by bridge.
     * @param devicetype identifier of application [0..40]
     * @throws LinkButtonException thrown if the bridge button has not been pressed
     */
    public String createApiKey(@Nullable String username, String label) throws IOException, ApiException {
        CreateUserRequest request = new CreateUserRequest(username != null && !username.isEmpty() ? username : null,
                label);

        String body = gson.toJson(request);
        AsyncHttpClient.Result result = http.post("http://" + hostAndPort + "/api", body);

        handleErrors(result);

        List<SuccessResponse> entries = gson.fromJson(result.getBody(), SuccessResponse.GSON_TYPE);
        SuccessResponse response = entries.get(0);

        String newUsername = (String) response.success.get("username");
        this.username = newUsername;
        return newUsername;
    }

    /**
     * Unlink the current user from the bridge.
     *
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public void removeApiKey() throws IOException, ApiException {
        AsyncHttpClient.Result result = http.delete(getAbsoluteURL("config/whitelist/" + Util.enc(username)));
        handleErrors(result);
    }

    /**
     * Converts the result into an {@link ApiException} if there is an error.
     * A huge bridge may report multiple errors, only the first one is considered.
     *
     * @param result The HTTP result
     * @throws ApiException An exception if there was any error
     */
    public void handleErrors(AsyncHttpClient.Result result) throws ApiException {
        if (result.getBody().isEmpty()) {
            if (result.getResponseCode() == 403) {
                username = "";
                throw new UnauthorizedException();
            } else if (result.getResponseCode() == 404) {
                throw new EntityNotAvailableException("HTTP Error 404: " + result.getRequestURL());
            }
            if (result.getResponseCode() != 200) {
                throw new ApiException();
            }
        } else {
            try {
                List<ErrorResponse> errors = gson.fromJson(result.getBody(), ErrorResponse.GSON_TYPE);
                if (errors == null) {
                    return;
                }

                for (ErrorResponse error : errors) {
                    if (error.getType() == null) {
                        continue;
                    }

                    switch (error.getType()) {
                        case 1:
                            username = "";
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
            }
        }
    }

    /**
     * Construct a hue REST-API URL, including the hue API username.
     *
     * @param path Relative path, can be empty for just the root endpoint at http://hue-address/api/[user-name].
     * @return A hue REST API endpoint
     * @throws UnauthorizedException If no username is set yet, throws this exception.
     */
    String getAbsoluteURL(String path) throws UnauthorizedException {
        if (username.isEmpty()) {
            throw new UnauthorizedException("linking is required before interacting with the bridge");
        }
        return "http://" + hostAndPort + "/api/" + username + (path.isEmpty() ? "" : "/" + path);
    }

    private String getAbsoluteURLnoExcept(String path) {
        return "http://" + hostAndPort + "/api/" + username + (path.isEmpty() ? "" : "/" + path);
    }

    public @Nullable Light getLightById(String lightId) {
        return ds.lights.get(lightId);
    }

    public boolean registerLightStatusListener(LightStatusListener lightStatusListener) {
        boolean result = lightStatusListeners.add(lightStatusListener);
        if (result) {
            ds.lights.values().forEach(light -> lightStatusListener.onLightAdded(light));
        }
        return result;
    }

    public boolean unregisterLightStatusListener(LightStatusListener lightStatusListener) {
        return lightStatusListeners.remove(lightStatusListener);
    }

    /**
     * Computes the added, modified and removed lights and notify all listeners.
     * This method need to be called whenever the datastore or the datastore.lights field is updated.
     *
     * @param lastLightStateCopy A copy of the hue datastore lights, e.g. new HashMap<>(ds.lights)
     */
    private void notifyLightStatusListeners(Map<String, Light> lastLightStateCopy) {
        for (final Light fullLight : ds.lights.values()) {
            final String lightId = fullLight.id;
            if (lastLightStateCopy.containsKey(lightId)) {
                final Light lastFullLight = lastLightStateCopy.remove(lightId);
                final LightState lastFullLightState = lastFullLight.state;

                if (!lastFullLightState.equals(fullLight.state)) {
                    lightStatusListeners.forEach(l -> l.onLightStateChanged(fullLight));
                }
            } else {
                lightStatusListeners.forEach(l -> l.onLightAdded(fullLight));
            }
        }
        // Notify about removed lights
        for (Entry<String, Light> fullLightEntry : lastLightStateCopy.entrySet()) {
            lightStatusListeners.forEach(l -> l.onLightRemoved(fullLightEntry.getValue()));
        }
    }

    /**
     * Wraps a CompletableFuture network result. Any bridge error (analyzed via {@link #handleErrors(Result)})
     * leads to an exceptional complete on the returned future.
     *
     * @param o The original future
     * @return The future that will fail exceptionally if the bridge reports an error.
     */
    private CompletableFuture<@Nullable LightStateUpdate> wrap(CompletableFuture<Result> o) {
        return o.thenApply(r -> {
            try {
                handleErrors(r);
            } catch (ApiException e) {
                throw new CompletionException(e);
            }
            return null;
        });
    }
}

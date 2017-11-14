/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.DsAPI;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.HttpTransport;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONApiResponseKeysEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.serverConnection.constants.JSONRequestConstants;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.Apartment;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.CachedMeteringValue;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DSID;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceConfig;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceParameterClassEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceSceneSpec;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.JSONCachedMeteringValueImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.JSONDeviceConfigImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.JSONDeviceSceneSpecImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.MeteringTypeEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.MeteringUnitsEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.SensorEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.SensorIndexEnum;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.impl.JSONDeviceImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.impl.JSONApartmentImpl;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.Scene;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.SceneEnum;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link DsAPIImpl} is the implementation of the {@link DsAPI}.
 *
 * @author Alexander Betker
 * @author Alex Maier
 * @author Michael Ochel - implements new methods, updates and change from SimpleJSON to GSON
 * @author Matthias Siegele - implements new methods, updates and change from SimpleJSON to GSON
 */
public class DsAPIImpl implements DsAPI {

    private HttpTransport transport = null;

    public DsAPIImpl(HttpTransport transport) {
        this.transport = transport;
    }

    public DsAPIImpl(String uri, int connectTimeout, int readTimeout) {
        this.transport = new HttpTransportImpl(uri, connectTimeout, readTimeout);
    }

    public DsAPIImpl(String uri, int connectTimeout, int readTimeout, boolean aceptAllCerts) {
        this.transport = new HttpTransportImpl(uri, connectTimeout, readTimeout, aceptAllCerts);
    }

    private boolean withParameterGroupId(int groupID) {
        return (groupID > -1);
    }

    @Override
    public boolean callApartmentScene(String token, int groupID, String groupName, Scene sceneNumber, boolean force) {
        if (sceneNumber != null && isValidApartmentSceneNumber(sceneNumber.getSceneNumber())) {
            String response = null;

            if (groupName != null) {
                if (withParameterGroupId(groupID)) {
                    if (force) {
                        response = transport.execute(JSONRequestConstants.JSON_APARTMENT_CALLSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID
                                + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                    } else {
                        response = transport.execute(JSONRequestConstants.JSON_APARTMENT_CALLSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID
                                + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
                    }
                } else {
                    if (force) {
                        response = transport.execute(JSONRequestConstants.JSON_APARTMENT_CALLSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                    } else {
                        response = transport.execute(JSONRequestConstants.JSON_APARTMENT_CALLSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
                    }
                }
            } else if (withParameterGroupId(groupID)) {
                if (force) {
                    response = transport.execute(
                            JSONRequestConstants.JSON_APARTMENT_CALLSCENE + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                } else {
                    response = transport.execute(
                            JSONRequestConstants.JSON_APARTMENT_CALLSCENE + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
                }
            } else {
                if (force) {
                    response = transport.execute(
                            JSONRequestConstants.JSON_APARTMENT_CALLSCENE + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                } else {
                    response = transport.execute(
                            JSONRequestConstants.JSON_APARTMENT_CALLSCENE + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
                }
            }
            if (JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean undoApartmentScene(String token, int groupID, String groupName, Scene sceneNumber) {
        if (sceneNumber != null && isValidApartmentSceneNumber(sceneNumber.getSceneNumber())) {
            String response = null;

            if (groupName != null) {
                if (withParameterGroupId(groupID)) {
                    response = transport.execute(
                            JSONRequestConstants.JSON_APARTMENT_UNDOSCENE + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
                } else {
                    response = transport.execute(
                            JSONRequestConstants.JSON_APARTMENT_UNDOSCENE + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
                }
            } else if (withParameterGroupId(groupID)) {
                response = transport.execute(JSONRequestConstants.JSON_APARTMENT_UNDOSCENE
                        + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID
                        + groupID + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
            } else {
                response = transport.execute(
                        JSONRequestConstants.JSON_APARTMENT_UNDOSCENE + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
            }
            if (JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response))) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidApartmentSceneNumber(int sceneNumber) {
        return (sceneNumber > -1 && sceneNumber < 256);
    }

    @Override
    public Apartment getApartmentStructure(String token) {
        String response = null;

        response = transport.execute(
                JSONRequestConstants.JSON_APARTMENT_GET_STRUCTURE + JSONRequestConstants.PARAMETER_TOKEN + token);

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject apartObj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (apartObj != null && apartObj.get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE.getKey()) != null) {
                return new JSONApartmentImpl(
                        (JsonObject) apartObj.get(JSONApiResponseKeysEnum.APARTMENT_GET_STRUCTURE.getKey()));
            }
        }
        return null;
    }

    @Override
    public List<Device> getApartmentDevices(String token, boolean unassigned) {
        String response = null;

        if (unassigned) {
            response = transport
                    .execute(JSONRequestConstants.JSON_APARTMENT_GET_DEVICES + JSONRequestConstants.PARAMETER_TOKEN
                            + token + JSONRequestConstants.INFIX_PARAMETER_UNASSIGNED_TRUE);
        } else {
            response = transport.execute(
                    JSONRequestConstants.JSON_APARTMENT_GET_DEVICES + JSONRequestConstants.PARAMETER_TOKEN + token);
        }

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)
                && responseObj.get(JSONApiResponseKeysEnum.APARTMENT_GET_DEVICES.getKey()) instanceof JsonArray) {
            JsonArray array = (JsonArray) responseObj.get(JSONApiResponseKeysEnum.APARTMENT_GET_DEVICES.getKey());

            List<Device> deviceList = new LinkedList<Device>();
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) instanceof JsonObject) {
                    deviceList.add(new JSONDeviceImpl((JsonObject) array.get(i)));
                }
            }
            return deviceList;
        }
        return new LinkedList<Device>();
    }

    private boolean withParameterZoneId(int id) {
        return (id > -1);
    }

    @Override
    public boolean callZoneScene(String token, int id, String name, int groupID, String groupName,
            SceneEnum sceneNumber, boolean force) {
        if (sceneNumber != null && (withParameterZoneId(id) || name != null)) {
            String response = null;
            if (withParameterZoneId(id)) {
                if (name != null) {
                    if (withParameterGroupId(groupID)) {
                        if (groupName != null) {
                            if (force) {
                                response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                        + JSONRequestConstants.PARAMETER_TOKEN + token
                                        + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                        + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                        + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER
                                        + sceneNumber.getSceneNumber() + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID
                                        + groupID + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName
                                        + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                            } else {
                                response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                        + JSONRequestConstants.PARAMETER_TOKEN + token
                                        + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                        + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                        + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER
                                        + sceneNumber.getSceneNumber() + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID
                                        + groupID + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName);
                            }
                        } else {
                            if (force) {
                                response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                        + JSONRequestConstants.PARAMETER_TOKEN + token
                                        + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                        + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                        + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER
                                        + sceneNumber.getSceneNumber() + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID
                                        + groupID + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                            } else {
                                response = transport.execute(
                                        JSONRequestConstants.JSON_ZONE_CALLSCENE + JSONRequestConstants.PARAMETER_TOKEN
                                                + token + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                                + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER
                                                + sceneNumber.getSceneNumber()
                                                + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID);
                            }
                        }
                    } else if (groupName != null) {
                        if (force) {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                    + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName
                                    + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                        } else {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                    + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName);
                        }
                    } else {
                        if (force) {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                    + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                        } else {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                    + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
                        }
                    }
                } else {
                    if (withParameterGroupId(groupID)) {
                        if (groupName != null) {
                            if (force) {
                                response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                        + JSONRequestConstants.PARAMETER_TOKEN + token
                                        + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                        + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER
                                        + sceneNumber.getSceneNumber() + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID
                                        + groupID + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName
                                        + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                            } else {
                                response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                        + JSONRequestConstants.PARAMETER_TOKEN + token
                                        + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                        + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER
                                        + sceneNumber.getSceneNumber() + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID
                                        + groupID + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName);
                            }
                        } else {
                            if (force) {
                                response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                        + JSONRequestConstants.PARAMETER_TOKEN + token
                                        + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                        + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER
                                        + sceneNumber.getSceneNumber() + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID
                                        + groupID + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                            } else {
                                response = transport.execute(
                                        JSONRequestConstants.JSON_ZONE_CALLSCENE + JSONRequestConstants.PARAMETER_TOKEN
                                                + token + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER
                                                + sceneNumber.getSceneNumber()
                                                + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID);
                            }
                        }
                    } else if (groupName != null) {
                        if (force) {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName
                                    + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                        } else {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName);
                        }
                    } else {
                        if (force) {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                        } else {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_ID + id
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
                        }
                    }
                }
            } else if (name != null) {
                if (withParameterGroupId(groupID)) {
                    if (groupName != null) {
                        if (force) {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName
                                    + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                        } else {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName);
                        }
                    } else {
                        if (force) {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID
                                    + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                        } else {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID);
                        }
                    }
                } else if (groupName != null) {
                    if (force) {
                        response = transport
                                .execute(JSONRequestConstants.JSON_ZONE_CALLSCENE + JSONRequestConstants.PARAMETER_TOKEN
                                        + token + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                        + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER
                                        + sceneNumber.getSceneNumber() + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME
                                        + groupName + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                    } else {
                        response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName);
                    }
                } else {
                    if (force) {
                        response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                    } else {
                        response = transport.execute(JSONRequestConstants.JSON_ZONE_CALLSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
                    }
                }
            }
            if (JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean undoZoneScene(String token, int zoneID, String zoneName, int groupID, String groupName,
            SceneEnum sceneNumber) {
        if (sceneNumber != null && (withParameterZoneId(zoneID) || zoneName != null)) {
            String response = null;
            if (withParameterZoneId(zoneID)) {
                if (zoneName != null) {
                    if (withParameterGroupId(groupID)) {
                        if (groupName != null) {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_UNDOSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_ID + zoneID
                                    + JSONRequestConstants.INFIX_PARAMETER_NAME + zoneName
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName);
                        } else {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_UNDOSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_ID + zoneID
                                    + JSONRequestConstants.INFIX_PARAMETER_NAME + zoneName
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID);
                        }
                    } else if (groupName != null) {
                        response = transport.execute(JSONRequestConstants.JSON_ZONE_UNDOSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_ID
                                + zoneID + JSONRequestConstants.INFIX_PARAMETER_NAME + zoneName
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName);
                    } else {
                        response = transport.execute(JSONRequestConstants.JSON_ZONE_UNDOSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_ID
                                + zoneID + JSONRequestConstants.INFIX_PARAMETER_NAME + zoneName
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
                    }
                } else {
                    if (withParameterGroupId(groupID)) {
                        if (groupName != null) {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_UNDOSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_ID + zoneID
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName);
                        } else {
                            response = transport.execute(JSONRequestConstants.JSON_ZONE_UNDOSCENE
                                    + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_ID + zoneID
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID);
                        }
                    } else if (groupName != null) {
                        response = transport.execute(JSONRequestConstants.JSON_ZONE_UNDOSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_ID
                                + zoneID + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER
                                + sceneNumber.getSceneNumber() + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME
                                + groupName);
                    } else {
                        response = transport.execute(JSONRequestConstants.JSON_ZONE_UNDOSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_ID
                                + zoneID + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER
                                + sceneNumber.getSceneNumber());
                    }
                }
            } else if (zoneName != null) {
                if (withParameterGroupId(groupID)) {
                    if (groupName != null) {
                        response = transport
                                .execute(JSONRequestConstants.JSON_ZONE_UNDOSCENE + JSONRequestConstants.PARAMETER_TOKEN
                                        + token + JSONRequestConstants.INFIX_PARAMETER_NAME + zoneName
                                        + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER
                                        + sceneNumber.getSceneNumber() + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID
                                        + groupID + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName);
                    } else {
                        response = transport.execute(JSONRequestConstants.JSON_ZONE_UNDOSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_NAME + zoneName
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                + JSONRequestConstants.INFIX_PARAMETER_GROUP_ID + groupID);
                    }
                } else if (groupName != null) {
                    response = transport
                            .execute(JSONRequestConstants.JSON_ZONE_UNDOSCENE + JSONRequestConstants.PARAMETER_TOKEN
                                    + token + JSONRequestConstants.INFIX_PARAMETER_NAME + zoneName
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                    + JSONRequestConstants.INFIX_PARAMETER_GROUP_NAME + groupName);
                } else {
                    response = transport
                            .execute(JSONRequestConstants.JSON_ZONE_UNDOSCENE + JSONRequestConstants.PARAMETER_TOKEN
                                    + token + JSONRequestConstants.INFIX_PARAMETER_NAME + zoneName
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
                }
            }
            if (JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean turnDeviceOn(String token, DSID dsid, String name) {
        if (((dsid != null && dsid.getValue() != null) || name != null)) {
            String response = null;
            if (dsid != null && dsid.getValue() != null) {
                if (name != null) {
                    response = transport.execute(JSONRequestConstants.JSON_DEVICE_TURN_ON
                            + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_DSID
                            + dsid.getValue() + JSONRequestConstants.INFIX_PARAMETER_NAME + name);
                } else {
                    response = transport
                            .execute(JSONRequestConstants.JSON_DEVICE_TURN_ON + JSONRequestConstants.PARAMETER_TOKEN
                                    + token + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue());
                }
            } else if (name != null) {
                response = transport
                        .execute(JSONRequestConstants.JSON_DEVICE_TURN_ON + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_NAME + name);
            }

            if (JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean turnDeviceOff(String token, DSID dsid, String name) {
        if (((dsid != null && dsid.getValue() != null) || name != null)) {
            String response = null;
            if (dsid != null && dsid.getValue() != null) {
                if (name != null) {
                    response = transport.execute(JSONRequestConstants.JSON_DEVICE_TURN_OFF
                            + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_DSID
                            + dsid.getValue() + JSONRequestConstants.INFIX_PARAMETER_NAME + name);
                } else {
                    response = transport
                            .execute(JSONRequestConstants.JSON_DEVICE_TURN_OFF + JSONRequestConstants.PARAMETER_TOKEN
                                    + token + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue());
                }
            } else if (name != null) {
                response = transport
                        .execute(JSONRequestConstants.JSON_DEVICE_TURN_OFF + JSONRequestConstants.PARAMETER_TOKEN
                                + token + JSONRequestConstants.INFIX_PARAMETER_NAME + name);
            }

            if (JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DeviceConfig getDeviceConfig(String token, DSID dsid, String name, DeviceParameterClassEnum class_,
            int index) {
        if (((dsid != null && dsid.getValue() != null) || name != null) && class_ != null
                && withParameterIndex(index)) {
            String response = null;
            if (dsid != null && dsid.getValue() != null) {
                if (name != null) {
                    response = transport.execute(
                            JSONRequestConstants.JSON_DEVICE_GET_CONFIG + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue()
                                    + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                    + JSONRequestConstants.INFIX_PARAMETER_CLASS + class_.getClassIndex()
                                    + JSONRequestConstants.INFIX_PARAMETER_INDEX + index,
                            transport.getSensordataConnectionTimeout(), transport.getSensordataReadTimeout());
                } else {
                    response = transport.execute(
                            JSONRequestConstants.JSON_DEVICE_GET_CONFIG + JSONRequestConstants.PARAMETER_TOKEN + token
                                    + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue()
                                    + JSONRequestConstants.INFIX_PARAMETER_CLASS + class_.getClassIndex()
                                    + JSONRequestConstants.INFIX_PARAMETER_INDEX + index,
                            transport.getSensordataConnectionTimeout(), transport.getSensordataReadTimeout());
                }
            } else if (name != null) {
                response = transport.execute(
                        JSONRequestConstants.JSON_DEVICE_GET_CONFIG + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                + JSONRequestConstants.INFIX_PARAMETER_CLASS + class_.getClassIndex()
                                + JSONRequestConstants.INFIX_PARAMETER_INDEX + index,
                        transport.getSensordataConnectionTimeout(), transport.getSensordataReadTimeout());
            }
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject configObject = JSONResponseHandler.getResultJsonObject(responseObj);

                if (configObject != null) {
                    return new JSONDeviceConfigImpl(configObject);
                }
            }
        }
        return null;
    }

    private boolean withParameterIndex(int index) {
        return (index > -1);
    }

    @Override
    public int getDeviceOutputValue(String token, DSID dsid, String name, int offset) {
        if (((dsid != null && dsid.getValue() != null) || name != null) && withParameterOffset(offset)) {
            String response = null;
            if (dsid != null && dsid.getValue() != null) {
                if (name != null) {
                    response = transport.execute(
                            JSONRequestConstants.JSON_DEVICE_GET_OUTPUT_VALUE + JSONRequestConstants.PARAMETER_TOKEN
                                    + token + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue()
                                    + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                    + JSONRequestConstants.INFIX_PARAMETER_OFFSET + offset,
                            transport.getSensordataConnectionTimeout(), transport.getSensordataReadTimeout());
                } else {
                    response = transport.execute(
                            JSONRequestConstants.JSON_DEVICE_GET_OUTPUT_VALUE + JSONRequestConstants.PARAMETER_TOKEN
                                    + token + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue()
                                    + JSONRequestConstants.INFIX_PARAMETER_OFFSET + offset,
                            transport.getSensordataConnectionTimeout(), transport.getSensordataReadTimeout());
                }
            } else if (name != null) {
                response = transport.execute(
                        JSONRequestConstants.JSON_DEVICE_GET_OUTPUT_VALUE + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                + JSONRequestConstants.INFIX_PARAMETER_OFFSET + offset,
                        transport.getSensordataConnectionTimeout(), transport.getSensordataReadTimeout());
            }

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject valueObject = JSONResponseHandler.getResultJsonObject(responseObj);

                if (valueObject != null
                        && valueObject.get(JSONApiResponseKeysEnum.DEVICE_GET_OUTPUT_VALUE.getKey()) != null) {
                    int value = -1;
                    value = valueObject.get(JSONApiResponseKeysEnum.DEVICE_GET_OUTPUT_VALUE.getKey()).getAsInt();
                    return value;
                }
            }
        }
        return -1;
    }

    private boolean withParameterOffset(int offset) {
        return (offset > -1);
    }

    private boolean withParameterValue(int value) {
        return (value > -1);
    }

    @Override
    public boolean setDeviceOutputValue(String token, DSID dsid, String name, int offset, int value) {
        if (((dsid != null && dsid.getValue() != null) || name != null) && offset > -1 && withParameterValue(value)) {
            String response = null;

            if (dsid != null && dsid.getValue() != null) {
                if (name != null) {
                    response = transport.execute(JSONRequestConstants.JSON_DEVICE_SET_OUTPUT_VALUE
                            + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_DSID
                            + dsid.getValue() + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                            + JSONRequestConstants.INFIX_PARAMETER_OFFSET + offset
                            + JSONRequestConstants.INFIX_PARAMETER_VALUE + value);
                } else {
                    response = transport.execute(JSONRequestConstants.JSON_DEVICE_SET_OUTPUT_VALUE
                            + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_DSID
                            + dsid.getValue() + JSONRequestConstants.INFIX_PARAMETER_OFFSET + offset
                            + JSONRequestConstants.INFIX_PARAMETER_VALUE + value);
                }
            } else if (name != null) {
                response = transport.execute(JSONRequestConstants.JSON_DEVICE_SET_OUTPUT_VALUE
                        + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_NAME
                        + name + JSONRequestConstants.INFIX_PARAMETER_OFFSET + offset
                        + JSONRequestConstants.INFIX_PARAMETER_VALUE + value);
            }
            if (JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DeviceSceneSpec getDeviceSceneMode(String token, DSID dsid, String name, short sceneID) {
        if (((dsid != null && dsid.getValue() != null) || name != null) && sceneID > -1) {
            String response = null;

            if (dsid != null && dsid.getValue() != null) {
                if (name != null) {
                    response = transport.execute(
                            JSONRequestConstants.JSON_DEVICE_GET_SCENE_MODE + JSONRequestConstants.PARAMETER_TOKEN
                                    + token + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue()
                                    + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_ID + sceneID,
                            transport.getSensordataConnectionTimeout(), transport.getSensordataReadTimeout());
                } else {
                    response = transport.execute(
                            JSONRequestConstants.JSON_DEVICE_GET_SCENE_MODE + JSONRequestConstants.PARAMETER_TOKEN
                                    + token + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue()
                                    + JSONRequestConstants.INFIX_PARAMETER_SCENE_ID + sceneID,
                            transport.getSensordataConnectionTimeout(), transport.getSensordataReadTimeout());
                }
            } else if (name != null) {
                response = transport.execute(
                        JSONRequestConstants.JSON_DEVICE_GET_SCENE_MODE + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_ID + sceneID,
                        transport.getSensordataConnectionTimeout(), transport.getSensordataReadTimeout());
            }

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject sceneSpec = JSONResponseHandler.getResultJsonObject(responseObj);

                if (sceneSpec != null) {
                    return new JSONDeviceSceneSpecImpl(sceneSpec);
                }
            }
        }
        return null;
    }

    @Override
    public short getDeviceSensorValue(String token, DSID dsid, String name, SensorEnum sensorType) {
        if (((dsid != null && dsid.getValue() != null) || name != null) && sensorType != null) {
            switch (sensorType) {
                case ACTIVE_POWER:
                    return getDeviceSensorValue(token, dsid, name, SensorIndexEnum.ACTIVE_POWER);
                case ELECTRIC_METER:
                    return getDeviceSensorValue(token, dsid, name, SensorIndexEnum.ELECTRIC_METER);
                case OUTPUT_CURRENT:
                    return getDeviceSensorValue(token, dsid, name, SensorIndexEnum.OUTPUT_CURRENT);
                default:
                    return -1;
            }
        }
        return -1;
    }

    @Override
    public short getDeviceSensorValue(String token, DSID dsid, String name, SensorIndexEnum sensorIndex) {
        if (((dsid != null && dsid.getValue() != null) || name != null) && sensorIndex != null) {
            String response = null;

            if (dsid != null && dsid.getValue() != null) {
                if (name != null) {
                    response = transport.execute(
                            JSONRequestConstants.JSON_DEVICE_GET_SENSOR_VALUE + JSONRequestConstants.PARAMETER_TOKEN
                                    + token + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue()
                                    + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                    + JSONRequestConstants.INFIX_PARAMETER_SENSOR_INDEX + sensorIndex.getIndex(),
                            transport.getSensordataConnectionTimeout(), transport.getSensordataReadTimeout());
                } else {
                    response = transport.execute(
                            JSONRequestConstants.JSON_DEVICE_GET_SENSOR_VALUE + JSONRequestConstants.PARAMETER_TOKEN
                                    + token + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue()
                                    + JSONRequestConstants.INFIX_PARAMETER_SENSOR_INDEX + sensorIndex.getIndex(),
                            transport.getSensordataConnectionTimeout(), transport.getSensordataReadTimeout());
                }
            } else if (name != null) {
                response = transport.execute(
                        JSONRequestConstants.JSON_DEVICE_GET_SENSOR_VALUE + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                + JSONRequestConstants.INFIX_PARAMETER_SENSOR_INDEX + sensorIndex.getIndex(),
                        transport.getSensordataConnectionTimeout(), transport.getSensordataReadTimeout());
            }

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject valueObject = JSONResponseHandler.getResultJsonObject(responseObj);

                if (valueObject != null && valueObject
                        .get(JSONApiResponseKeysEnum.DEVICE_GET_SENSOR_VALUE_SENSOR_VALUE.getKey()) != null) {
                    short value = -1;
                    value = valueObject.get(JSONApiResponseKeysEnum.DEVICE_GET_SENSOR_VALUE_SENSOR_VALUE.getKey())
                            .getAsShort();
                    return value;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean callDeviceScene(String token, DSID dsid, String name, Scene sceneNumber, boolean force) {
        if (((dsid != null && dsid.getValue() != null) || name != null) && sceneNumber != null) {
            String response = null;

            if (dsid != null && dsid.getValue() != null) {
                if (name != null) {
                    if (force) {
                        response = transport.execute(JSONRequestConstants.JSON_DEVICE_CALLSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue()
                                + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                    } else {
                        response = transport.execute(JSONRequestConstants.JSON_DEVICE_CALLSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue()
                                + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
                    }
                } else {
                    if (force) {
                        response = transport.execute(JSONRequestConstants.JSON_DEVICE_CALLSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue()
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                                + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                    } else {
                        response = transport.execute(JSONRequestConstants.JSON_DEVICE_CALLSCENE
                                + JSONRequestConstants.PARAMETER_TOKEN + token
                                + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue()
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
                    }
                }
            } else if (name != null) {
                if (force) {
                    response = transport.execute(JSONRequestConstants.JSON_DEVICE_CALLSCENE
                            + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_NAME
                            + name + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber()
                            + JSONRequestConstants.INFIX_PARAMETER_FORCE_TRUE);
                } else {
                    response = transport.execute(JSONRequestConstants.JSON_DEVICE_CALLSCENE
                            + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_NAME
                            + name + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
                }
            }
            if (JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean undoDeviceScene(String token, DSID dsid, Scene sceneNumber) {
        if (((dsid != null && dsid.getValue() != null)) && sceneNumber != null) {
            String response = null;

            if (dsid != null && dsid.getValue() != null) {
                response = transport
                        .execute(JSONRequestConstants.JSON_DEVICE_UNDOSCENE + JSONRequestConstants.PARAMETER_TOKEN
                                + token + JSONRequestConstants.INFIX_PARAMETER_DSID + dsid.getValue()
                                + JSONRequestConstants.INFIX_PARAMETER_SCENE_NUMBER + sceneNumber.getSceneNumber());
            }
            if (JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean subscribeEvent(String token, String name, int subscriptionID, int connectionTimeout,
            int readTimeout) {
        if (name != null && !name.trim().equals("") && withParameterSubscriptionID(subscriptionID)) {
            String response = null;

            response = transport.execute(
                    JSONRequestConstants.JSON_EVENT_SUBSCRIBE + JSONRequestConstants.PARAMETER_TOKEN + token
                            + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                            + JSONRequestConstants.INFIX_PARAMETER_SUBSCRIPTION_ID + subscriptionID,
                    connectionTimeout, readTimeout);
            if (JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean unsubscribeEvent(String token, String name, int subscriptionID, int connectionTimeout,
            int readTimeout) {
        if (name != null && !name.trim().equals("") && withParameterSubscriptionID(subscriptionID)) {
            String response = null;

            response = transport.execute(
                    JSONRequestConstants.JSON_EVENT_UNSUBSCRIBE + JSONRequestConstants.PARAMETER_TOKEN + token
                            + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                            + JSONRequestConstants.INFIX_PARAMETER_SUBSCRIPTION_ID + subscriptionID,
                    connectionTimeout, readTimeout);
            if (JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response))) {
                return true;
            }
        }
        return false;
    }

    private boolean withParameterSubscriptionID(int subscriptionID) {
        return (subscriptionID > -1);
    }

    @Override
    public String getEvent(String token, int subscriptionID, int timeout) {
        if (withParameterSubscriptionID(subscriptionID) && withParameterTimeout(timeout)) {
            return transport.execute(JSONRequestConstants.JSON_EVENT_GET + JSONRequestConstants.PARAMETER_TOKEN + token
                    + JSONRequestConstants.INFIX_PARAMETER_SUBSCRIPTION_ID + subscriptionID
                    + JSONRequestConstants.INFIX_PARAMETER_TIMEOUT + timeout);
        }
        return null;
    }

    private boolean withParameterTimeout(int timeout) {
        return (timeout > -1);
    }

    @Override
    public int getTime(String token) {
        String response = null;

        response = transport
                .execute(JSONRequestConstants.JSON_SYSTEM_TIME + JSONRequestConstants.PARAMETER_TOKEN + token);

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);

            if (obj != null && obj.get(JSONApiResponseKeysEnum.SYSTEM_GET_TIME.getKey()) != null) {
                int time = -1;
                time = obj.get(JSONApiResponseKeysEnum.SYSTEM_GET_TIME.getKey()).getAsInt();

                return time;
            }
        }
        return -1;
    }

    private boolean valueInRange(int value) {
        return (value > -1 && value < 256);
    }

    @Override
    public List<Integer> getResolutions(String token) {
        String response = null;

        response = transport.execute(
                JSONRequestConstants.JSON_METERING_GET_RESOLUTIONS + JSONRequestConstants.PARAMETER_TOKEN + token);

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject resObj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (resObj != null
                    && resObj.get(JSONApiResponseKeysEnum.METERING_GET_RESOLUTIONS.getKey()) instanceof JsonArray) {
                JsonArray array = (JsonArray) resObj.get(JSONApiResponseKeysEnum.METERING_GET_RESOLUTIONS.getKey());

                List<Integer> resolutionList = new LinkedList<Integer>();
                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i) instanceof JsonObject) {
                        JsonObject jObject = (JsonObject) array.get(i);

                        if (jObject.get(JSONApiResponseKeysEnum.METERING_GET_RESOLUTION.getKey()) != null) {
                            int val = -1;
                            val = jObject.get(JSONApiResponseKeysEnum.METERING_GET_RESOLUTION.getKey()).getAsInt();
                            if (val != -1) {
                                resolutionList.add(val);
                            }
                        }
                    }
                }
                return resolutionList;
            }
        }
        return null;
    }

    @Override
    public List<CachedMeteringValue> getLatest(String token, MeteringTypeEnum type, List<String> meterDSIDs,
            MeteringUnitsEnum unit) {
        if (type != null && meterDSIDs != null) {
            String jsonMeterList = ".meters(";
            for (int i = 0; i < meterDSIDs.size(); i++) {
                if (!meterDSIDs.get(i).isEmpty()) {
                    jsonMeterList += meterDSIDs.get(i);
                    if (i < meterDSIDs.size() - 1 && !meterDSIDs.get(i + 1).isEmpty()) {
                        jsonMeterList += ",";
                    } else {
                        break;
                    }
                }
            }
            jsonMeterList += ")";
            String response = null;

            if (unit != null && type != MeteringTypeEnum.consumption) {
                response = transport.execute(JSONRequestConstants.JSON_METERING_GET_LATEST
                        + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_TYPE
                        + type.name() + JSONRequestConstants.INFIX_PARAMETER_FROM + jsonMeterList
                        + JSONRequestConstants.INFIX_PARAMETER_UNIT + unit.name());
            } else {
                response = transport.execute(JSONRequestConstants.JSON_METERING_GET_LATEST
                        + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_TYPE
                        + type.name() + JSONRequestConstants.INFIX_PARAMETER_FROM + jsonMeterList);
            }

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject latestObj = JSONResponseHandler.getResultJsonObject(responseObj);

                if (latestObj != null
                        && latestObj.get(JSONApiResponseKeysEnum.METERING_GET_LATEST.getKey()) instanceof JsonArray) {
                    JsonArray array = (JsonArray) latestObj.get(JSONApiResponseKeysEnum.METERING_GET_LATEST.getKey());
                    List<CachedMeteringValue> list = new LinkedList<CachedMeteringValue>();

                    for (int i = 0; i < array.size(); i++) {
                        if (array.get(i) instanceof JsonObject) {
                            list.add(new JSONCachedMeteringValueImpl((JsonObject) array.get(i)));
                        }
                    }
                    return list;
                }
            }
        }
        return null;
    }

    @Override
    public List<CachedMeteringValue> getLatest(String token, MeteringTypeEnum type, String meterDSIDs,
            MeteringUnitsEnum unit) {
        if (type != null && meterDSIDs != null) {

            String response = null;
            if (unit != null && type != MeteringTypeEnum.consumption) {
                response = transport.execute(JSONRequestConstants.JSON_METERING_GET_LATEST
                        + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_TYPE
                        + type.name() + JSONRequestConstants.INFIX_PARAMETER_FROM + meterDSIDs
                        + JSONRequestConstants.INFIX_PARAMETER_UNIT + unit.name());
            } else {
                response = transport.execute(JSONRequestConstants.JSON_METERING_GET_LATEST
                        + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_TYPE
                        + type.name() + JSONRequestConstants.INFIX_PARAMETER_FROM + meterDSIDs);
            }

            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject latestObj = JSONResponseHandler.getResultJsonObject(responseObj);
                if (latestObj != null
                        && latestObj.get(JSONApiResponseKeysEnum.METERING_GET_LATEST.getKey()) instanceof JsonArray) {
                    JsonArray array = (JsonArray) latestObj.get(JSONApiResponseKeysEnum.METERING_GET_LATEST.getKey());

                    List<CachedMeteringValue> list = new LinkedList<CachedMeteringValue>();
                    for (int i = 0; i < array.size(); i++) {
                        if (array.get(i) instanceof JsonObject) {
                            list.add(new JSONCachedMeteringValueImpl((JsonObject) array.get(i)));
                        }
                    }
                    return list;
                }
            }
        }
        return null;
    }

    @Override
    public boolean setDeviceValue(String token, DSID dsid, String name, int value) {
        if (((dsid != null && dsid.getValue() != null) || name != null) && valueInRange(value)) {
            String response = null;

            if (dsid != null && dsid.getValue() != null) {
                if (name != null) {
                    response = transport.execute(JSONRequestConstants.JSON_DEVICE_SET_VALUE
                            + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_DSID
                            + dsid.getValue() + JSONRequestConstants.INFIX_PARAMETER_NAME + name
                            + JSONRequestConstants.INFIX_PARAMETER_VALUE + value);
                } else {
                    response = transport.execute(JSONRequestConstants.JSON_DEVICE_SET_VALUE
                            + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_DSID
                            + dsid.getValue() + JSONRequestConstants.INFIX_PARAMETER_VALUE + value);
                }
            } else if (name != null) {
                response = transport.execute(JSONRequestConstants.JSON_DEVICE_SET_VALUE
                        + JSONRequestConstants.PARAMETER_TOKEN + token + JSONRequestConstants.INFIX_PARAMETER_NAME
                        + name + JSONRequestConstants.INFIX_PARAMETER_VALUE + value);
            }
            if (JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getMeterList(String token) {
        List<String> meterList = new LinkedList<String>();

        String response = transport
                .execute(JSONRequestConstants.JSON_PROPERTY_QUERY + JSONRequestConstants.PARAMETER_TOKEN + token
                        + JSONRequestConstants.INFIX_PARAMETER_QUERY + JSONRequestConstants.QUERY_GET_METERLIST);

        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);

            if (obj != null && obj.get(JSONApiResponseKeysEnum.DS_METER_QUERY.getKey()) instanceof JsonArray) {
                JsonArray array = (JsonArray) obj.get(JSONApiResponseKeysEnum.DS_METER_QUERY.getKey());

                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i) instanceof JsonObject) {
                        meterList.add(array.get(i).getAsJsonObject().get("dSID").getAsString());
                    }
                }
            }
        }
        return meterList;
    }

    @Override
    public String loginApplication(String loginToken) {
        if (StringUtils.isNotBlank(loginToken)) {
            String response = null;

            response = transport.execute(JSONRequestConstants.JSON_SYSTEM_LOGIN_APPLICATION + loginToken);
            JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

            if (JSONResponseHandler.checkResponse(responseObj)) {
                JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
                String tokenStr = null;

                if (obj != null && obj.get(JSONApiResponseKeysEnum.SYSTEM_LOGIN.getKey()) != null) {
                    tokenStr = obj.get(JSONApiResponseKeysEnum.SYSTEM_LOGIN.getKey()).getAsString();
                }
                if (tokenStr != null) {
                    return tokenStr;
                }
            }
        }
        return null;
    }

    @Override
    public String login(String user, String password) {
        String response = null;
        response = transport.execute(JSONRequestConstants.JSON_SYSTEM_LOGIN + JSONRequestConstants.PARAMETER_USER + user
                + JSONRequestConstants.INFIX_PARAMETER_PASSWORD + password);
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            String tokenStr = null;

            if (obj != null && obj.get(JSONApiResponseKeysEnum.SYSTEM_LOGIN.getKey()) != null) {
                tokenStr = obj.get(JSONApiResponseKeysEnum.SYSTEM_LOGIN.getKey()).getAsString();
            }
            if (tokenStr != null) {
                return tokenStr;
            }
        }
        return null;
    }

    @Override
    public boolean logout() {
        String response = transport.execute(JSONRequestConstants.JSON_SYSTEM_LOGOUT);

        if (JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response))) {
            return true;
        }
        return false;
    }

    @Override
    public String getDSID(String token) {
        String response = transport.execute(JSONRequestConstants.JSON_SYSTEM_GET_DSID + token);
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj != null) {
                String dsID = obj.get(JSONApiResponseKeysEnum.SYSTEM_DSID.getKey()).getAsString();
                if (dsID != null) {
                    return dsID;
                }
            }
        }
        return null;
    }

    @Override
    public boolean enableApplicationToken(String applicationToken, String sessionToken) {
        String response = null;
        response = transport.execute(
                "/json/system/enableToken?applicationToken=" + applicationToken + "&token=" + sessionToken, 60000,
                60000);
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        return JSONResponseHandler.checkResponse(responseObj);
    }

    @Override
    public String requestAppplicationToken(String applicationName) {
        String response = transport.execute("/json/system/requestApplicationToken?applicationName=" + applicationName);
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj != null) {
                String aplicationToken = obj.get(JSONApiResponseKeysEnum.SYSTEM_APPLICATION_TOKEN.getKey())
                        .getAsString();
                if (aplicationToken != null) {
                    return aplicationToken;
                }
            }
        }
        return null;
    }

    @Override
    public boolean revokeToken(String applicationToken, String sessionToken) {
        String response = null;
        response = transport
                .execute("/json/system/revokeToken?applicationToken=" + applicationToken + "&token=" + sessionToken);
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);
        return JSONResponseHandler.checkResponse(responseObj);
    }

    @Override
    public int checkConnection(String token) {
        return transport.checkConnection("/json/apartment/getName?token=" + token);
    }

    @Override
    public int[] getSceneValue(String token, DSID dsid, short sceneId) {
        String response = null;
        int[] value = { -1, -1 };
        response = transport.execute(
                "/json/device/getSceneValue?dsid=" + dsid.toString() + "&sceneID=" + sceneId + "&token=" + token, 4000,
                20000);
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj != null && obj.get("value") != null) {
                value[0] = obj.get("value").getAsInt();
                if (obj.get("angle") != null) {
                    value[1] = obj.get("angle").getAsInt();
                }
                return value;
            }
        }
        return value;
    }

    @Override
    public boolean increaseValue(String sessionToken, DSID dsid) {
        String response = null;
        response = transport.execute("/json/device/increaseValue?dsid=" + dsid.toString() + "&token=" + sessionToken);
        return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
    }

    @Override
    public boolean decreaseValue(String sessionToken, DSID dsid) {
        String response = null;
        response = transport.execute("/json/device/decreaseValue?dsid=" + dsid.toString() + "&token=" + sessionToken);
        return JSONResponseHandler.checkResponse(JSONResponseHandler.toJsonObject(response));
    }

    @Override
    public String getInstallationName(String sessionToken) {
        String response = null;
        response = transport.execute(" /json/apartment/getName?token=" + sessionToken);
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj != null && obj.get("name") != null) {
                return obj.get("name").getAsString();
            }
        }
        return null;
    }

    @Override
    public String getZoneName(String sessionToken, int zoneID) {
        String response = null;
        response = transport.execute(" /json/zone/getName?id=" + zoneID + "&token=" + sessionToken);
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj != null && obj.get("name") != null) {
                return obj.get("name").getAsString();
            }
        }
        return null;
    }

    @Override
    public String getDeviceName(String sessionToken, DSID dSID) {
        String response = null;
        response = transport.execute(" /json/device/getName?dsid=" + dSID.toString() + "&token=" + sessionToken);
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj != null && obj.get("name") != null) {
                return obj.get("name").getAsString();
            }
        }
        return null;
    }

    @Override
    public String getCircuitName(String sessionToken, DSID dSID) {
        String response = null;
        response = transport.execute("/json/circuit/getName?id=" + dSID.toString() + "&token=" + sessionToken);
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj != null && obj.get("name") != null) {
                return obj.get("name").getAsString();
            }
        }
        return null;
    }

    @Override
    public String getSceneName(String sessionToken, int zoneID, int groupID, short sceneID) {
        String response = null;
        response = transport.execute("/json/zone/sceneGetName?id" + zoneID + "&groupID?" + groupID + "&sceneNumber="
                + sceneID + "&token=" + sessionToken);
        JsonObject responseObj = JSONResponseHandler.toJsonObject(response);

        if (JSONResponseHandler.checkResponse(responseObj)) {
            JsonObject obj = JSONResponseHandler.getResultJsonObject(responseObj);
            if (obj != null && obj.get("name") != null) {
                return obj.get("name").getAsString();
            }
        }
        return null;
    }

}
/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.constants.SceneTypes;

/**
 * The {@link InternalScene} represents a digitalSTROM-Scene for the internal model.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class InternalScene {

    private final Short SCENE_ID;
    private final Short GROUP_ID;
    private final Integer ZONE_ID;
    private String SceneName;
    private final String INTERNAL_SCENE_ID;
    private boolean active = false;
    private boolean deviceHasChanged = false;
    private String sceneType = SceneTypes.GROUP_SCENE;

    private List<Device> devices = Collections.synchronizedList(new LinkedList<Device>());
    private SceneStatusListener listener = null;

    /**
     * Creates a new {@link InternalScene} with the given parameters. Only the <i>sceneID</i> must not be null. If the
     * <i>sceneName</i> is null, the internal scene id will be set as name in format "[zoneID]-[groupID]-[sceneID]". If
     * the
     * <i>zoneID</i> and/or the <i> groupID</i> is null, the broadcast address 0 will be set.
     *
     * @param zoneID can be null
     * @param groupID can be null
     * @param sceneID must not be null
     * @param sceneName can be null
     */
    public InternalScene(Integer zoneID, Short groupID, Short sceneID, String sceneName) {
        if (sceneID == null) {
            throw new IllegalArgumentException("The parameter sceneID can't be null!");
        }
        this.SCENE_ID = sceneID;
        if (groupID == null) {
            this.GROUP_ID = 0;
        } else {
            this.GROUP_ID = groupID;
        }
        if (zoneID == null) {
            this.ZONE_ID = 0;
        } else {
            this.ZONE_ID = zoneID;
        }
        this.INTERNAL_SCENE_ID = this.ZONE_ID + "-" + this.GROUP_ID + "-" + this.SCENE_ID;
        if (StringUtils.isBlank(sceneName)) {
            this.SceneName = this.INTERNAL_SCENE_ID;
        } else {
            this.SceneName = sceneName;
        }
        if ((sceneName != this.INTERNAL_SCENE_ID) && !sceneName.contains("Apartment-Scene: ")
                && !sceneName.contains("Zone-Scene: Zone:")
                && !(sceneName.contains("Zone: ") && sceneName.contains("Group: ") && sceneName.contains("Scene: "))) {
            sceneType = SceneTypes.NAMED_SCENE;
        } else if (this.ZONE_ID == 0) {
            sceneType = SceneTypes.APARTMENT_SCENE;
        } else if (this.GROUP_ID == 0) {
            sceneType = SceneTypes.ZONE_SCENE;
        }
    }

    /**
     * Activates this Scene.
     */
    public void activateScene() {
        if (!active) {
            this.active = true;
            deviceHasChanged = false;
            informListener();
            if (this.devices != null) {
                for (Device device : this.devices) {
                    device.callInternalScene(this);
                }
            }
        }
    }

    /**
     * Deactivates this Scene.
     */
    public void deactivateScene() {
        if (active) {
            this.active = false;
            deviceHasChanged = false;
            informListener();
            if (this.devices != null) {
                for (Device device : this.devices) {
                    device.undoInternalScene(this);
                }
            }
        }
    }

    /**
     * Will be called by a device, if an undo call of an other scene activated this scene.
     */
    public void activateSceneByDevice() {
        if (!active && !deviceHasChanged) {
            this.active = true;
            deviceHasChanged = false;
            informListener();
        }
    }

    /**
     * Will be called by a device, if an call of an other scene deactivated this scene.
     */
    public void deactivateSceneByDevice() {
        if (active) {
            this.active = false;
            deviceHasChanged = false;
            informListener();
        }
    }

    /**
     * This method has a device to call, if this scene was activated and the device state has changed.
     *
     * @param sceneNumber
     */
    public void deviceSceneChanged(short sceneNumber) {
        if (this.SCENE_ID != sceneNumber) {
            if (active) {
                deviceHasChanged = true;
                active = false;
                informListener();
            }
        }
    }

    private void informListener() {
        if (this.listener != null) {
            listener.onSceneStateChanged(this.active);
        }
    }

    /**
     * Returns true, if this scene is active, otherwise false.
     *
     * @return Scene is active? (true = yes | false = no)
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Adds an affected {@link Device} to this {@link InternalScene} device list.
     *
     * @param device
     */
    public void addDevice(Device device) {
        if (!this.devices.contains(device)) {
            this.devices.add(device);
        }
        int prio = 0;
        if (this.listener != null) {
            prio = 1000;
        } else {
            prio = 2000;
        }
        device.checkSceneConfig(SCENE_ID, prio);
    }

    /**
     * Overrides the existing device list of this {@link InternalScene} with a new reference to a {@link List} of
     * affected {@link Device}'s.
     *
     * @param deviceList
     */
    public void addReferenceDevices(List<Device> deviceList) {
        this.devices = deviceList;
        checkDeviceSceneConfig();
    }

    /**
     * Proves, if the scene configuration is saved to all {@link Device}'s. If not, the device initials the reading out
     * of the missing configuration in the following priority steps:
     * <ul>
     * <li>low priority, if no listener is added.</li>
     * <li>medium priority, if a listener is added.</li>
     * <li>high priority, if this scene has been activated.</li>
     * </ul>
     */
    public void checkDeviceSceneConfig() {
        int prio = 0;
        if (this.listener != null) {
            prio = 1000;
        } else {
            prio = 2000;
        }
        if (devices != null) {
            for (Device device : devices) {
                device.checkSceneConfig(SCENE_ID, prio);
            }
        }
    }

    /**
     * Returns the list of the affected {@link Device}'s.
     *
     * @return device list
     */
    public List<Device> getDeviceList() {
        return this.devices;

    }

    /**
     * Adds a {@link List} of affected {@link Device}'s.
     *
     * @param deviceList
     */
    public void addDevices(List<Device> deviceList) {
        for (Device device : deviceList) {
            addDevice(device);
        }
    }

    /**
     * Removes a not anymore affected {@link Device} from the device list.
     *
     * @param device
     */
    public void removeDevice(Device device) {
        this.devices.remove(device);
    }

    /**
     * Updates the affected {@link Device}'s with the given deviceList.
     *
     * @param deviceList
     */
    public void updateDeviceList(List<Device> deviceList) {
        if (!this.devices.equals(deviceList)) {
            this.devices.clear();
            addDevices(deviceList);
        }
    }

    /**
     * Returns the Scene name.
     *
     * @return scene name
     */
    public String getSceneName() {
        return SceneName;
    }

    /**
     * Sets the scene name.
     *
     * @param sceneName
     */
    public void setSceneName(String sceneName) {
        SceneName = sceneName;
    }

    /**
     * Returns the Scene id of this scene call.
     *
     * @return scene id
     */
    public Short getSceneID() {
        return SCENE_ID;
    }

    /**
     * Returns the group id of this scene call.
     *
     * @return group id
     */
    public Short getGroupID() {
        return GROUP_ID;
    }

    /**
     * Returns the zone id of this scene call.
     *
     * @return zone id
     */
    public Integer getZoneID() {
        return ZONE_ID;
    }

    /**
     * Returns the id of this scene call.
     *
     * @return scene call id
     */
    public String getID() {
        return INTERNAL_SCENE_ID;
    }

    /**
     * Registers a {@link SceneStatusListener} to this {@link InternalScene}.
     *
     * @param listener
     */
    public synchronized void registerSceneListener(SceneStatusListener listener) {
        this.listener = listener;
        this.listener.onSceneAdded(this);
        checkDeviceSceneConfig();

    }

    /**
     * Unregisters the {@link SceneStatusListener} from this {@link InternalScene}.
     */
    public synchronized void unregisterSceneListener() {
        if (listener != null) {
            // this.listener.onSceneRemoved(this);
            this.listener = null;
        }
    }

    /**
     * Returns the scene type.
     * <br>
     * <b>Note:</b>
     * The valid Scene types can be found at {@link SceneTypes}.
     *
     * @return sceneType
     */
    public String getSceneType() {
        return this.sceneType;
    }

    @Override
    public String toString() {
        return "NamedScene [SceneName=" + SceneName + ", NAMED_SCENE_ID=" + INTERNAL_SCENE_ID + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((INTERNAL_SCENE_ID == null) ? 0 : INTERNAL_SCENE_ID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof InternalScene)) {
            return false;
        }
        InternalScene other = (InternalScene) obj;
        if (INTERNAL_SCENE_ID == null) {
            if (other.getID() != null) {
                return false;
            }
        } else if (!INTERNAL_SCENE_ID.equals(other.getID())) {
            return false;
        }
        return true;
    }
}

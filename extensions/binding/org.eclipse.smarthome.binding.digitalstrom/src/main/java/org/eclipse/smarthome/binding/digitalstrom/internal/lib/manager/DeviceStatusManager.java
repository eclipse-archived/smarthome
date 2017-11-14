/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.lib.manager;

import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.ConnectionListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.ManagerStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.SceneStatusListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.TotalPowerConsumptionListener;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.stateEnums.ManagerStates;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.listener.stateEnums.ManagerTypes;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.SceneReadingJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.SensorJobExecutor;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.sensorJobExecutor.sensorJob.SensorJob;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.devices.deviceParameters.DeviceStateUpdate;
import org.eclipse.smarthome.binding.digitalstrom.internal.lib.structure.scene.InternalScene;

/**
 * <p>
 * The {@link DeviceStatusManager} is responsible for the synchronization between the internal model of the
 * digitalSTROM-devices and the real existing digitalSTROM-devices. You can change the state of an device by sending a
 * direct command to the devices or by calling a scene. Furthermore the {@link DeviceStatusManager} get informed over
 * the {@link ScenesManager} by the {@link EventListener} if scenes are called by external sources. All
 * configurations of the physically device will be synchronized to the internally managed model and updated as required.
 * The {@link DeviceStatusManager} also initializes {@link SensorJob}'s with the {@link SensorJobExecutor} and
 * {@link SceneReadingJobExecutor} to update required sensor and scene data.
 * 
 * <p>
 * Therefore the {@link DeviceStatusManager} uses the {@link StructureManager} for the internal management of the
 * structure of the digitalSTROM-system, {@link ConnectionManager} to check the connectivity,
 * {@link ScenesManager} to identify externally called scenes and to update the affected devices of these
 * called scenes to the internal model. The most methods of the above-named managers will be directly called over the
 * {@link DeviceStatusManager}, because they are linked to the affected managers.
 * 
 * <p>
 * To get informed by all relevant information you can register some useful listeners. Here the list of all listeners
 * which can registered or unregistered:
 * <ul>
 * <li>{@link DeviceStatusListener} over {@link #registerDeviceListener(DeviceStatusListener)} respectively
 * {@link #unregisterDeviceListener(DeviceStatusListener)}</li>
 * <li>{@link SceneStatusListener} over {@link #registerSceneListener(SceneStatusListener)} respectively
 * {@link #unregisterSceneListener(SceneStatusListener)}</li>
 * <li>{@link TotalPowerConsumtionListener} over
 * {@link #registerTotalPowerConsumptionListener(TotalPowerConsumptionListener)} respectively
 * {@link #unregisterTotalPowerConsumptionListener(TotalPowerConsumptionListener)}</li>
 * <li>{@link ManagerStatusListener} over {@link #registerStatusListener(ManagerStatusListener)}
 * respectively {@link #unregisterStatusListener()}</li>
 * <li>{@link ConnectionListener} over {@link #registerDeviceListener(DeviceStatusListener)} respectively
 * {@link #unregisterDeviceListener(DeviceStatusListener)}</li>
 * </ul>
 * For what the listener can be used please have a look at the listener.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface DeviceStatusManager {

    /**
     * Starts the working process for device synchronization. It also starts the {@link SensorJobExecutor} and the
     * {@link SceneReadingJobExecutor} and the {@link SceneManager}.
     */
    public void start();

    /**
     * Stops the working process for device synchronization. It also stops the {@link SensorJobExecutor} and the
     * {@link SceneReadingJobExecutor} and the {@link SceneManager}.
     */
    public void stop();

    /**
     * This method sends a call scene command for the given {@link InternalScene}, if call_undo is true otherwise it
     * sends a undo command.
     * <br>
     * It also updates the scene state, if the command was send successful.
     *
     * @param scene to call
     * @param call_undo (true = call | false = undo)
     */
    public void sendSceneComandsToDSS(InternalScene scene, boolean call_undo);

    /**
     * This method sends a stop command for the given {@link Device}.
     * <br>
     * It also reads out the current output value of the device and updates it, if the command was send successful.
     *
     * @param device
     */
    public void sendStopComandsToDSS(Device device);

    /**
     * This method sends the command for the given {@link DeviceStateUpdate} for the given {@link Device}. Please have a
     * look at {@link DeviceStateUpdate} to see what types is there and how the value {@link DeviceStateUpdate} will be
     * evaluated.
     * <br>
     * It also updates the device if the command was send successful.
     *
     * @param device
     * @param deviceStateUpdate
     */
    public void sendComandsToDSS(Device device, DeviceStateUpdate deviceStateUpdate);

    /**
     * This method adds a {@link SensorJobs} with the appropriate priority to the {@link SensorJobExecuter}.
     *
     * @param sensorJob
     * @param priority
     */
    public void updateSensorData(SensorJob sensorJob, String priority);

    /**
     * This method adds a {@link SensorJobs} with the appropriate priority to the {@link SceneReadingJobExecuter}.
     *
     * @param device
     * @param deviceStateUpdate
     */
    public void updateSceneData(Device device, DeviceStateUpdate deviceStateUpdate);

    /**
     * Registers the given {@link DeviceStatusListener} to the {@link Device}, if it exists or registers it as a
     * device discovery, if the id of the {@link DeviceStatusListener} is {@link DeviceStatusListener#DEVICE_DISCOVERY}.
     *
     * @param deviceListener
     */
    public void registerDeviceListener(DeviceStatusListener deviceListener);

    /**
     * Unregisters the given {@link DeviceStatusListener} from the {@link Device}, if it exists or unregisters the
     * device discovery, if the id of the {@link DeviceStatusListener} is {@link DeviceStatusListener#DEVICE_DISCOVERY}.
     *
     * @param deviceListener
     */
    public void unregisterDeviceListener(DeviceStatusListener deviceListener);

    /**
     * Registers the given {@link TotalPowerConsumptionListener} to this {@link DeviceStatusManager}.
     *
     * @param totalPowerConsumptionListener
     */
    public void registerTotalPowerConsumptionListener(TotalPowerConsumptionListener totalPowerConsumptionListener);

    /**
     * Unregisters the {@link TotalPowerConsumptionListener} from this {@link DeviceStatusManager}.
     */
    public void unregisterTotalPowerConsumptionListener();

    /**
     * Registers the given {@link SceneStatusListener} to the {@link InternalScene}, if it exists or registers it as a
     * scene discovery to the {@link SceneManager}, if the id of the {@link SceneStatusListener} is
     * {@link SceneStatusListener#SCENE_DISCOVERY}.
     *
     * @param sceneListener
     */
    public void registerSceneListener(SceneStatusListener sceneListener);

    /**
     * Unregisters the given {@link SceneStatusListener} from the {@link InternalScene} if it exist or unregisters the
     * scene discovery from the {@link SceneManager}, if the id of the {@link SceneStatusListener} is
     * {@link SceneStatusListener#SCENE_DISCOVERY}.
     *
     * @param sceneListener
     */
    public void unregisterSceneListener(SceneStatusListener sceneListener);

    /**
     * Registers the given {@link ConnectionListener} to the {@link ConnectionManager}.
     *
     * @param connectionListener
     */
    public void registerConnectionListener(ConnectionListener connectionListener);

    /**
     * Unregisters the {@link ConnectionListener} from the {@link ConnectionManager}.
     */
    public void unregisterConnectionListener();

    /**
     * Removes the {@link Device} with the given dSID from the internal model, if it exists.
     *
     * @param dSID
     */
    public void removeDevice(String dSID);

    /**
     * Registers the given {@link ManagerStatusListener} to all available managers. What manager are available please
     * have a look at {@link ManagerTypes}.
     *
     * @param statusListener
     */
    public void registerStatusListener(ManagerStatusListener statusListener);

    /**
     * Unregisters the {@link ManagerStatusListener} from all available managers. What manager are available please have
     * a look at {@link ManagerTypes}.
     */
    public void unregisterStatusListener();

    /**
     * Returns the {@link ManagerTypes} of this class.
     *
     * @return these {@link ManagerTypes}
     */
    public ManagerTypes getManagerType();

    /**
     * Returns the current {@link ManagerStates}.
     *
     * @return current {@link ManagerStates}
     */
    public ManagerStates getManagerState();

    /**
     * Reads the current total power consumption out and returns it.
     *
     * @return the current total power consumption
     */
    public int getTotalPowerConsumption();

    /**
     * Reads the current total energy meter value out and returns it.
     *
     * @return the current total energy meter value
     */
    public int getTotalEnergyMeterValue();
}

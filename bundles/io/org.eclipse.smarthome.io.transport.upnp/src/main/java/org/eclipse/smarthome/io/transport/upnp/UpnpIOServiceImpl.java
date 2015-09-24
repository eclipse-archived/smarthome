/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.transport.upnp;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jupnp.UpnpService;
import org.jupnp.controlpoint.ActionCallback;
import org.jupnp.controlpoint.ControlPoint;
import org.jupnp.controlpoint.SubscriptionCallback;
import org.jupnp.model.action.ActionArgumentValue;
import org.jupnp.model.action.ActionException;
import org.jupnp.model.action.ActionInvocation;
import org.jupnp.model.gena.CancelReason;
import org.jupnp.model.gena.GENASubscription;
import org.jupnp.model.message.UpnpResponse;
import org.jupnp.model.meta.Action;
import org.jupnp.model.meta.Device;
import org.jupnp.model.meta.DeviceIdentity;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.Service;
import org.jupnp.model.state.StateVariableValue;
import org.jupnp.model.types.ServiceId;
import org.jupnp.model.types.UDAServiceId;
import org.jupnp.model.types.UDN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpnpIOServiceImpl} is the implementation of the UpnpIOService
 * interface
 *
 * @author Karel Goderis - Initial contribution; added simple polling mechanism
 * @author Kai Kreuzer - added descriptor url retrieval
 * @author Markus Rathgeb - added NP checks in subscription ended callback
 * @author Andre Fuechsel - added methods to remove subscriptions
 */
@SuppressWarnings("rawtypes")
public class UpnpIOServiceImpl implements UpnpIOService {

    private final Logger logger = LoggerFactory.getLogger(UpnpIOServiceImpl.class);

    private final int DEFAULT_POLLING_INTERVAL = 60;

    private UpnpService upnpService;

    private Map<UpnpIOParticipant, Device> participants = new ConcurrentHashMap<UpnpIOParticipant, Device>(32);
    private Map<UpnpIOParticipant, ScheduledFuture> pollingJobs = new ConcurrentHashMap<UpnpIOParticipant, ScheduledFuture>(
            32);
    private Map<UpnpIOParticipant, Boolean> currentStates = new ConcurrentHashMap<UpnpIOParticipant, Boolean>(32);
    private Map<Service, UpnpSubscriptionCallback> subscriptionCallbacks = new ConcurrentHashMap<Service, UpnpSubscriptionCallback>(
            32);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    public class UpnpSubscriptionCallback extends SubscriptionCallback {

        public UpnpSubscriptionCallback(Service service) {
            super(service);
        }

        public UpnpSubscriptionCallback(Service service, int requestedDurationSeconds) {
            super(service, requestedDurationSeconds);
        }

        @Override
        protected void ended(GENASubscription subscription, CancelReason reason, UpnpResponse response) {
            final Service service = subscription.getService();
            if (service != null) {
                final ServiceId serviceId = service.getServiceId();
                final Device device = service.getDevice();
                if (device != null) {
                    final Device deviceRoot = device.getRoot();
                    if (deviceRoot != null) {
                        final DeviceIdentity deviceRootIdentity = deviceRoot.getIdentity();
                        if (deviceRootIdentity != null) {
                            final UDN deviceRootUdn = deviceRootIdentity.getUdn();
                            logger.debug("A GENA subscription '{}' for device '{}' was ended", serviceId, deviceRootUdn);
                        }
                    }
                }

                if (upnpService != null) {
                    final ControlPoint cp = upnpService.getControlPoint();
                    if (cp != null) {
                        final UpnpSubscriptionCallback callback = new UpnpSubscriptionCallback(service,
                                subscription.getActualDurationSeconds());
                        cp.execute(callback);
                    }
                }
            }
        }

        @Override
        protected void established(GENASubscription subscription) {
            logger.trace("A GENA subscription '{}' for device '{}' is established", subscription.getService()
                    .getServiceId().getId(), subscription.getService().getDevice().getRoot().getIdentity().getUdn());
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void eventReceived(GENASubscription sub) {

            Map<String, StateVariableValue> values = sub.getCurrentValues();
            Device device = sub.getService().getDevice();

            logger.trace("Receiving a GENA subscription '{}' response for device '{}'", sub.getService().getServiceId()
                    .getId(), device.getRoot().getIdentity().getUdn());
            for (UpnpIOParticipant participant : participants.keySet()) {
                if (participants.get(participant).equals(device.getRoot())) {
                    for (String stateVariable : values.keySet()) {
                        StateVariableValue value = values.get(stateVariable);
                        if (value.getValue() != null) {
                            try {
                                participant.onValueReceived(stateVariable, value.getValue().toString(), sub
                                        .getService().getServiceId().getId());
                            } catch (Exception e) {
                                logger.error("Participant threw an exception onValueReceived", e);
                            }
                        }
                    }
                    break;
                }
            }

        }

        @Override
        protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
            logger.debug("A GENA subscription '{}' for device '{}' missed events", subscription.getService()
                    .getServiceId(), subscription.getService().getDevice().getRoot().getIdentity().getUdn());

        }

        @Override
        protected void failed(GENASubscription subscription, UpnpResponse response, Exception e, String defaultMsg) {
            logger.debug("A GENA subscription '{}' for device '{}' failed", subscription.getService().getServiceId(),
                    subscription.getService().getDevice().getRoot().getIdentity().getUdn());

        }

    }

    public void activate() {
        logger.debug("Starting UPnP IO service...");
    }

    public void deactivate() {
        logger.debug("Stopping UPnP IO service...");
    }

    protected void setUpnpService(UpnpService upnpService) {
        this.upnpService = upnpService;
    }

    protected void unsetUpnpService(UpnpService upnpService) {
        this.upnpService = null;
    }

    @Override
    public void addSubscription(UpnpIOParticipant participant, String serviceID, int duration) {

        if (participant != null && serviceID != null) {
            registerParticipant(participant);
            Device device = participants.get(participant);

            if (device != null) {
                Service subService = searchSubService(serviceID, device);
                if (subService != null) {
                    logger.trace("Setting up an UPNP service subscription '{}' for particpant '{}'", serviceID,
                            participant.getUDN());

                    UpnpSubscriptionCallback callback = new UpnpSubscriptionCallback(subService, duration);
                    subscriptionCallbacks.put(subService, callback);
                    upnpService.getControlPoint().execute(callback);
                } else {
                    logger.trace("Could not find service '{}' for device '{}'", serviceID, device.getIdentity()
                            .getUdn());
                }
            } else {
                logger.trace("Could not find an upnp device for participant '{}'", participant.getUDN());
            }
        }

    }

    private Service searchSubService(String serviceID, Device device) {
        Service subService = findService(device, serviceID);
        if (subService == null) {

            // service not on the root device, we search the embedded devices as well
            Device[] embedded = device.getEmbeddedDevices();
            if (embedded != null) {
                for (Device aDevice : embedded) {
                    subService = findService(aDevice, serviceID);
                    if (subService != null) {
                        break;
                    }
                }
            }
        }
        return subService;
    }

    @Override
    public void removeSubscription(UpnpIOParticipant participant, String serviceID) {
        if (participant != null && serviceID != null) {
            Device device = participants.get(participant);

            if (device != null) {
                Service subService = searchSubService(serviceID, device);
                if (subService != null) {
                    logger.trace("Removing an UPNP service subscription '{}' for particpant '{}'", serviceID,
                            participant.getUDN());

                    UpnpSubscriptionCallback callback = subscriptionCallbacks.get(subService);
                    if (callback != null) {
                        callback.end();
                    }
                    subscriptionCallbacks.remove(subService);
                } else {
                    logger.trace("Could not find service '{}' for device '{}'", serviceID,
                            device.getIdentity().getUdn());
                }
            } else {
                logger.trace("Could not find an upnp device for participant '{}'", participant.getUDN());
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> invokeAction(UpnpIOParticipant participant, String serviceID, String actionID,
            Map<String, String> inputs) {

        HashMap<String, String> resultMap = new HashMap<String, String>();

        if (serviceID != null && actionID != null && participant != null) {

            registerParticipant(participant);
            Device device = participants.get(participant);

            if (device != null) {

                Service service = findService(device, serviceID);
                if (service != null) {

                    Action action = service.getAction(actionID);
                    if (action != null) {

                        ActionInvocation invocation = new ActionInvocation(action);
                        if (invocation != null) {
                            if (inputs != null) {
                                for (String variable : inputs.keySet()) {
                                    invocation.setInput(variable, inputs.get(variable));
                                }
                            }

                            logger.debug("Invoking Action '{}' of service '{}' for participant '{}'", new Object[] {
                                    actionID, serviceID, participant.getUDN() });
                            new ActionCallback.Default(invocation, upnpService.getControlPoint()).run();

                            ActionException anException = invocation.getFailure();
                            if (anException != null && anException.getMessage() != null) {
                                logger.debug(anException.getMessage());
                            }

                            Map<String, ActionArgumentValue> result = invocation.getOutputMap();
                            if (result != null) {
                                for (String variable : result.keySet()) {
                                    ActionArgumentValue newArgument = null;
                                    try {
                                        newArgument = result.get(variable);
                                        if (newArgument.getValue() != null) {
                                            resultMap.put(variable, newArgument.getValue().toString());
                                        }
                                    } catch (Exception e) {
                                        logger.debug(
                                                "An exception '{}' occurred processing ActionArgumentValue '{}' with value '{}'",
                                                new Object[] { e.getMessage(), newArgument.getArgument().getName(),
                                                        newArgument.getValue() });
                                    }
                                }
                            }
                        }
                    } else {
                        logger.debug("Could not find action '{}' for participant '{}'", actionID, participant.getUDN());
                    }
                } else {
                    logger.debug("Could not find service '{}' for participant '{}'", serviceID, participant.getUDN());
                }
            } else {
                logger.debug("Could not find an upnp device for participant '{}'", participant.getUDN());
            }
        }

        return resultMap;
    }

    @Override
    public boolean isRegistered(UpnpIOParticipant participant) {
        if (upnpService.getRegistry().getDevice(new UDN(participant.getUDN()), true) != null) {
            return true;
        } else {
            return false;
        }
    }

    public void registerParticipant(UpnpIOParticipant participant) {
        if (participant != null) {
            Device device = participants.get(participant);

            if (device == null) {
                device = upnpService.getRegistry().getDevice(new UDN(participant.getUDN()), true);
                if (device != null) {
                    logger.debug("Registering device '{}' for participant '{}'", device.getIdentity(),
                            participant.getUDN());
                    participants.put(participant, device);
                }
            }
        }
    }

    @Override
    public void unregisterParticipant(UpnpIOParticipant participant) {
        if (participant != null) {
            stopPollingForParticipant(participant);
            pollingJobs.remove(participant);
            currentStates.remove(participant);
            participants.remove(participant);
        }
    }

    @Override
    public URL getDescriptorURL(UpnpIOParticipant participant) {
        RemoteDevice device = upnpService.getRegistry().getRemoteDevice(new UDN(participant.getUDN()), true);
        if (device != null) {
            return device.getIdentity().getDescriptorURL();
        } else {
            return null;
        }
    }

    private Service findService(Device device, String serviceID) {
        Service service = null;

        String namespace = device.getType().getNamespace();
        if (namespace.equals(UDAServiceId.DEFAULT_NAMESPACE) || namespace.equals(UDAServiceId.BROKEN_DEFAULT_NAMESPACE)) {
            service = device.findService(new UDAServiceId(serviceID));
        } else {
            service = device.findService(new ServiceId(namespace, serviceID));
        }

        return service;
    }

    private class UPNPPollingRunnable implements Runnable {

        private UpnpIOParticipant participant;
        private String serviceID;
        private String actionID;

        public UPNPPollingRunnable(UpnpIOParticipant participant, String serviceID, String actionID) {
            this.participant = participant;
            this.serviceID = serviceID;
            this.actionID = actionID;
        }

        @Override
        public void run() {
            // It is assumed that during addStatusListener() a check is made
            // whether the participant
            // is correctly registered
            try {
                Device device = participants.get(participant);

                if (device != null) {

                    Service service = findService(device, serviceID);
                    if (service != null) {

                        Action action = service.getAction(actionID);
                        if (action != null) {

                            @SuppressWarnings("unchecked")
                            ActionInvocation invocation = new ActionInvocation(action);
                            if (invocation != null) {

                                logger.debug("Polling participant '{}' through Action '{}' of Service '{}' ",
                                        new Object[] { participant.getUDN(), actionID, serviceID });
                                new ActionCallback.Default(invocation, upnpService.getControlPoint()).run();

                                ActionException anException = invocation.getFailure();
                                if (anException != null
                                        && anException.getMessage()
                                                .contains("Connection error or no response received")) {
                                    // The UDN is not reachable anymore
                                    if (currentStates.get(participant)) {
                                        currentStates.put(participant, false);
                                        logger.debug("Signalling that '{}' is not responding", participant.getUDN());
                                        participant.onStatusChanged(false);
                                    }
                                } else {
                                    // The UDN functions correctly
                                    if (!currentStates.get(participant)) {
                                        currentStates.put(participant, true);
                                        logger.debug("Signalling that '{}' is again responding", participant.getUDN());
                                        participant.onStatusChanged(true);
                                    }
                                }
                            }
                        } else {
                            logger.debug("Could not find action '{}' for participant '{}'", actionID,
                                    participant.getUDN());
                        }
                    } else {
                        logger.debug("Could not find service '{}' for participant '{}'", serviceID,
                                participant.getUDN());
                    }
                }

            } catch (Exception e) {
                logger.error("An exception occurred while polling an UPNP device: '{}'", e.getStackTrace().toString());
            }
        }
    }

    @Override
    public void addStatusListener(UpnpIOParticipant participant, String serviceID, String actionID, int interval) {

        if (participant != null) {

            registerParticipant(participant);

            int pollingInterval = interval == 0 ? DEFAULT_POLLING_INTERVAL : interval;

            // remove the previous polling job, if any
            stopPollingForParticipant(participant);

            currentStates.put(participant, true);

            Runnable pollingRunnable = new UPNPPollingRunnable(participant, serviceID, actionID);
            pollingJobs.put(participant,
                    scheduler.scheduleAtFixedRate(pollingRunnable, 0, pollingInterval, TimeUnit.SECONDS));

        }
    }

    private void stopPollingForParticipant(UpnpIOParticipant participant) {
        if (pollingJobs.containsKey(participant)) {
            ScheduledFuture<?> pollingJob = pollingJobs.get(participant);
            if (pollingJob != null) {
                pollingJob.cancel(true);
            }
        }
    }

    @Override
    public void removeStatusListener(UpnpIOParticipant participant) {
        if (participant != null) {
            unregisterParticipant(participant);
        }
    }
}

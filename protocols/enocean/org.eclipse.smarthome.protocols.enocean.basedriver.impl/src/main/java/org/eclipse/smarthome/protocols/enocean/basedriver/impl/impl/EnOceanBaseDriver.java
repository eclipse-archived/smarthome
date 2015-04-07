/*******************************************************************************
 * Copyright (c) 2013, 2015 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Victor PERRON, Antonin CHAZALET, Andre BOTTARO.
 *******************************************************************************/

package org.eclipse.smarthome.protocols.enocean.basedriver.impl.impl;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.smarthome.protocols.enocean.basedriver.impl.esp.EspPacket;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.radio.Message;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.radio.Message1BS;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.radio.Message4BS;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.radio.MessageRPS;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.radio.MessageUTE;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.EnOceanHostImplException;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Logger;
import org.eclipse.smarthome.protocols.enocean.basedriver.impl.utils.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.enocean.EnOceanDevice;
import org.osgi.service.enocean.EnOceanEvent;
import org.osgi.service.enocean.EnOceanHost;
import org.osgi.service.enocean.EnOceanMessage;
import org.osgi.service.enocean.descriptions.EnOceanChannelDescriptionSet;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * EnOceanBaseDriver.
 */
public class EnOceanBaseDriver implements EnOceanPacketListener, ServiceTrackerCustomizer, EventHandler {

    /** TAG */
    public static final String TAG = EnOceanBaseDriver.class.getName();

    private BundleContext bc;
    private Hashtable eoDevices;
    private Hashtable eoHostRefs;

    private ServiceTracker eoDevicesTracker;
    private ServiceTracker eventAdminTracker;
    private ServiceRegistration eventHandlerRegistration;
    private EnOceanHostImpl host;

    /**
     * CONFIG_EXPORTED_PID_TABLE
     */
    public static final String CONFIG_EXPORTED_PID_TABLE = "org.enocean.ExportedDeviceTable";

    /**
     * The {@link EnOceanBaseDriver} constructor initiates the connection
     * towards an {@link EnOceanHostImpl} device. Then it registers itself as a
     * service listener for any {@link EnOceanDevice}, {@link EnOceanMessage}, {@link EnOceanChannelDescriptionSet} that
     * would be registered in the
     * framework.
     * 
     * @param bundleContext
     */
    public EnOceanBaseDriver(BundleContext bundleContext) {
        /* Init driver internal state */
        this.bc = bundleContext;
        eoDevices = new Hashtable(10);
        eoHostRefs = new Hashtable(10);

        /* Register initial EnOceanHost */
        String hostPath = System.getProperty("org.osgi.service.enocean.host.path", "/dev/ttyUSB0");
        Logger.i(TAG, "initial host path : " + hostPath);
        if (hostPath != null && hostPath != "") {
            if (hostPath.equals(":testcase:")) {
                host = new EnOceanHostTestImpl(hostPath, bc);
            } else {
                host = new EnOceanHostSerialImpl(hostPath, bc);
            }
            registerHost(hostPath, host);
            host.addPacketListener(this);
        }

        /* Look for an EventAdmin service */
        // ServiceReference ref = bc.getServiceReference(EventAdmin.class
        // .getName());
        // if (ref != null) {
        // eventAdmin = (EventAdmin) bc.getService(ref);
        // } else {
        // Logger.d(TAG,
        // "The execution environment provides NO EventAdmin service.");
        // }
        eventAdminTracker = new ServiceTracker(bundleContext, EventAdmin.class.getName(), null);
        eventAdminTracker.open();
        Logger.d(TAG,
                "A servicetracker for eventadmin service has been created, and opened. EventAdmin is required by the base driver.");

        /* Initializes self as EventHandler */
        Hashtable ht = new Hashtable();
        ht.put(org.osgi.service.event.EventConstants.EVENT_TOPIC, new String[] { EnOceanEvent.TOPIC_MSG_RECEIVED, });
        eventHandlerRegistration = bc.registerService(EventHandler.class.getName(), this, ht);
        Logger.d(TAG, "eventHandlerRegistration: " + eventHandlerRegistration);

        /* Track the EnOcean services */
        try {
            eoDevicesTracker = registerDeviceListener(bc, this);
            Logger.d(TAG, "eoDevicesTracker: " + eoDevicesTracker);
        } catch (InvalidSyntaxException e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    /**
     * This callback gets called every time a message has been correctly parsed
     * by one of the hosts.
     * 
     * @param data
     *            , i.e the data, and the optional data parts of an EnOcean
     *            packet/telegram (see, section "1.6.1 Packet description", page
     *            13 of EnOceanSerialProtocol3.pdf). E.g. for a (full) 1BS
     *            telegram 55000707017ad500008a92390001ffffffff3000eb, then data
     *            is d500008a92390001ffffffff3000
     */
    public void radioPacketReceived(byte[] data) {
        Logger.d(TAG,
                "radioPacketReceived(byte[] data - the data, and the optional data parts of an EnOcean packet/telegram: "
                        + data + ")");
        // The following line prints, for example for a 1BS telegram,
        // [DEBUG-EnOceanBaseDriver] data: d500008a92390001ffffffff3000
        Logger.d(TAG, "data: " + Utils.bytesToHexString(data));
        Message msg;
        // First, determine if teach-in and eventually create a device.
        Logger.d(TAG, "data[0]: " + data[0]);
        switch (data[0]) {
            case Message.MESSAGE_4BS:
                // TODO Here parameter data does NOT contain the full telegram as
                // expected... Fix this.
                msg = new Message4BS(data);
                Logger.d(TAG, "4BS msg received, payload: " + Utils.bytesToHexString(msg.getPayloadBytes()));
                if (msg.isTeachin()) {
                    EnOceanDevice dev = getAssociatedDevice(msg);
                    if (dev == null) {
                        if (msg.hasTeachInInfo()) {
                            Logger.d(TAG,
                                    "msg has TeachIn info - msg.teachInFunc(): " + msg.teachInFunc()
                                            + ", msg.teachInType(): " + msg.teachInType() + ", msg.teachInManuf(): "
                                            + msg.teachInManuf());
                            registerDeviceAndProfile(msg.getSenderId(), msg.getRorg(), msg.teachInFunc(),
                                    msg.teachInType(), msg.teachInManuf());
                        } else {
                            Logger.d(TAG,
                                    "msg has NO TeachIn info - msg.teachInFunc(): " + msg.teachInFunc()
                                            + ", msg.teachInType(): " + msg.teachInType() + ", msg.teachInManuf(): "
                                            + msg.teachInManuf());
                            new EnOceanDeviceImpl(bc, this, msg.getSenderId(), msg.getRorg(), -1, -1, -1);
                        }
                    } else {
                        Logger.d(TAG, "message was a teach-in, but device already exists.");
                    }
                    return; // No need to do more processing on the message
                } else {
                    Logger.d(TAG, "message is not a teach-in. msg: " + msg);
                }
                break;
            case Message.MESSAGE_RPS:
                // TODO Here parameter data does NOT contain the full telegram as
                // expected... Fix this.
                msg = new MessageRPS(data);
                Logger.d(TAG, "RPS msg received, payload: " + Utils.bytesToHexString(msg.getPayloadBytes()));
                EnOceanDevice dev = getAssociatedDevice(msg);
                if (dev == null) {
                    dev = new EnOceanDeviceImpl(bc, this, msg.getSenderId(), msg.getRorg(), -1, -1, -1);
                }
                break;
            case Message.MESSAGE_1BS:
                // TODO Here parameter data does NOT contain the full telegram as
                // expected... Fix this.
                msg = new Message1BS(data);
                Logger.d(TAG, "1BS msg received, data and optional data: " + Utils.bytesToHexString(msg.getBytes()));

                Logger.d(TAG, "DEBUG: msg: " + msg);
                Logger.d(TAG, "DEBUG: msg.getDbm: " + msg.getDbm());
                Logger.d(TAG, "DEBUG: msg.getDestinationId: " + msg.getDestinationId());
                Logger.d(TAG, "DEBUG: msg.getFunc: " + msg.getFunc());
                Logger.d(TAG, "DEBUG: msg.getRorg: " + msg.getRorg());
                Logger.d(TAG, "DEBUG: msg.getSecurityLevelFormat: " + msg.getSecurityLevelFormat());
                Logger.d(TAG, "DEBUG: msg.getSenderId (as an int): " + msg.getSenderId());
                Logger.d(TAG, "DEBUG: msg.getStatus: " + msg.getStatus());
                Logger.d(TAG, "DEBUG: msg.getSubTelNum: " + msg.getSubTelNum());
                Logger.d(TAG, "DEBUG: msg.getType: " + msg.getType());
                Logger.d(TAG, "DEBUG: msg.getBytes: " + msg.getBytes());
                Logger.d(TAG, "DEBUG: msg.getClass: " + msg.getClass());
                Logger.d(TAG, "DEBUG: msg.getPayloadBytes: " + msg.getPayloadBytes());
                Logger.d(TAG, "DEBUG: msg.getTelegrams: " + msg.getTelegrams());
                Logger.d(TAG, "DEBUG: msg.isTeachin: " + msg.isTeachin());

                if (msg.isTeachin()) {
                    EnOceanDevice enOceanDevice = getAssociatedDevice(msg);
                    if (enOceanDevice == null) {
                        // if (msg.hasTeachInInfo()) {
                        // registerDeviceAndProfile(msg.getSenderId(),
                        // msg.getRorg(), msg.teachInFunc(),
                        // msg.teachInType(), msg.teachInManuf());
                        // } else {
                        new EnOceanDeviceImpl(bc, this, msg.getSenderId(), msg.getRorg(), -1, -1, -1);
                        // }
                    } else {
                        Logger.d(TAG, "message was a teach-in, but device already exists.");
                    }
                    return; // No need to do more processing on the message
                } else {
                    Logger.d(TAG, "message is not a teach-in. msg: " + msg);
                }
                break;
            case Message.MESSAGE_UTE:
                // TODO Here parameter data does NOT contain the full telegram as
                // expected... Fix this.
                msg = new MessageUTE(data);
                Logger.d(TAG, "UTE msg received, data and optional data: " + Utils.bytesToHexString(msg.getBytes()));

                Logger.d(TAG, "DEBUG: msg: " + msg);
                Logger.d(TAG, "DEBUG: msg.getDbm: " + msg.getDbm());
                Logger.d(TAG, "DEBUG: msg.getDestinationId: " + msg.getDestinationId());
                Logger.d(TAG, "DEBUG: msg.getFunc: " + msg.getFunc());
                Logger.d(TAG, "DEBUG: msg.getRorg: " + msg.getRorg());
                Logger.d(TAG, "DEBUG: msg.getSecurityLevelFormat: " + msg.getSecurityLevelFormat());
                Logger.d(TAG, "DEBUG: msg.getSenderId (as an int): " + msg.getSenderId());
                Logger.d(TAG, "DEBUG: msg.getStatus: " + msg.getStatus());
                Logger.d(TAG, "DEBUG: msg.getSubTelNum: " + msg.getSubTelNum());
                Logger.d(TAG, "DEBUG: msg.getType: " + msg.getType());
                Logger.d(TAG, "DEBUG: msg.getBytes: " + msg.getBytes());
                Logger.d(TAG, "DEBUG: msg.getClass: " + msg.getClass());
                Logger.d(TAG, "DEBUG: msg.getPayloadBytes: " + msg.getPayloadBytes());
                Logger.d(TAG, "DEBUG: msg.getTelegrams: " + msg.getTelegrams());
                Logger.d(TAG, "DEBUG: msg.isTeachin: " + msg.isTeachin());

                if (msg.isTeachin()) {
                    EnOceanDevice enOceanDevice = getAssociatedDevice(msg);
                    if (enOceanDevice == null) {
                        if (msg.hasTeachInInfo()) {
                            // TODO AAA: Use msg.teachinFunc(), teachinType().
                            // instead of msg.getFunc(), getType() if relevant.
                            registerDeviceAndProfile(msg.getSenderId(), msg.getRorg(), msg.getFunc(), msg.getType(),
                                    msg.teachInManuf());
                        } else {
                            // TODO AAA: Handle that case properly.
                            Logger.d(TAG, "message was a teach-in, but has no teachin info.");
                        }
                    } else {
                        Logger.d(TAG, "message was a teach-in, but device already exists.");
                    }
                    return; // No need to do more processing on the message
                } else {
                    Logger.d(TAG, "message is not a teach-in. msg: " + msg);
                }
                break;
            // case Message.MESSAGE_SYS_EX:
            // return;
            // case Message.MESSAGE_VLD:
            // return;
            default:
                // TODO Here parameter data does NOT contain the full telegram as
                // expected... Fix this.
                msg = new Message(data) {
                    public int teachInType() {
                        return -1;
                    }

                    public int teachInManuf() {
                        return -1;
                    }

                    public int teachInFunc() {
                        return -1;
                    }

                    public boolean isTeachin() {
                        return false;
                    }

                    public boolean hasTeachInInfo() {
                        return false;
                    }
                };
                Logger.d(TAG, "The given data: " + data
                        + " may contain a message (?), nevertheless this message is not handle by the basedriver.");

                Logger.d(TAG, "DEBUG: msg: " + msg);
                Logger.d(TAG, "DEBUG: msg.getDbm: " + msg.getDbm());
                Logger.d(TAG, "DEBUG: msg.getDestinationId: " + msg.getDestinationId());
                Logger.d(TAG, "DEBUG: msg.getFunc: " + msg.getFunc());
                Logger.d(TAG, "DEBUG: msg.getRorg: " + msg.getRorg());
                Logger.d(TAG, "DEBUG: msg.getSecurityLevelFormat: " + msg.getSecurityLevelFormat());
                Logger.d(TAG, "DEBUG: msg.getSenderId (as an int): " + msg.getSenderId());
                Logger.d(TAG, "DEBUG: msg.getStatus: " + msg.getStatus());
                Logger.d(TAG, "DEBUG: msg.getSubTelNum: " + msg.getSubTelNum());
                Logger.d(TAG, "DEBUG: msg.getType: " + msg.getType());
                Logger.d(TAG, "DEBUG: msg.getBytes: " + msg.getBytes());
                Logger.d(TAG, "DEBUG: msg.getClass: " + msg.getClass());
                Logger.d(TAG, "DEBUG: msg.getPayloadBytes: " + msg.getPayloadBytes());
                Logger.d(TAG, "DEBUG: msg.getTelegrams: " + msg.getTelegrams());
                break;
        }

        // Try to associate the message with a device and send to EventAdmin.
        Logger.d(TAG, "Try to associate the message with a device and send to EventAdmin.");
        EnOceanDevice dev = getAssociatedDevice(msg);
        if (dev == null) {
            Logger.e(
                    TAG,
                    "The system can NOT find any EnOceanDevice that could be associated to the message. The message can not then be broadcasted to event admin.");
        } else {
            if (dev instanceof EnOceanDeviceImpl) {
                Logger.d(TAG, "A device has been found; dev: " + dev);
                EnOceanDeviceImpl implDev = (EnOceanDeviceImpl) dev;
                int rorg = implDev.getRorg();
                int func = implDev.getFunc();
                int type = implDev.getType();
                Logger.d(TAG, "dev's rorg (e.g. int 165 means hex a5): " + rorg + ", func: " + func + ", type: " + type);
                msg.setFunc(func);
                msg.setType(type);
                implDev.setLastMessage(msg);
                broadcastToEventAdmin(msg);
            } else {
                Logger.d(TAG, "This device dev is NOT an instance of EnOceanDeviceImpl. dev: " + dev);
            }
        }
    }

    public Object addingService(ServiceReference ref) {
        Object service = this.bc.getService(ref);
        if (service == null) {
            return null;
        } else {
            if (service instanceof EnOceanDevice) {
                String servicePid = (String) ref.getProperty(Constants.SERVICE_PID);
                String chipId = (String) ref.getProperty(EnOceanDevice.CHIP_ID);
                Object hasExport = ref.getProperty(EnOceanDevice.ENOCEAN_EXPORT);
                if (servicePid == null) {
                    if (chipId == null) {
                        throw new IllegalStateException();
                    }
                    servicePid = chipId;
                }

                try {
                    if (chipId == null && hasExport != null) {
                        host.allocChipID(servicePid);
                    }
                } catch (Exception e) {
                    Logger.d(TAG, "exception: " + e.getMessage());
                }
                eoDevices.put(servicePid, bc.getService(ref));
                Logger.d(TAG, "servicetracker: EnOceanDevice service registered, PID: " + servicePid);
            }
            return service;
        }
    }

    public void modifiedService(ServiceReference arg0, Object service) {
        Logger.d(TAG, "IN: EnOceanBaseDriver.modifiedService(ServiceReference: " + arg0 + ", Object: " + service + ")");
    }

    public void removedService(ServiceReference arg0, Object service) {
        Logger.d(TAG, "IN: EnOceanBaseDriver.removedService(ServiceReference: " + arg0 + ", Object: " + service + ")");
    }

    public void handleEvent(Event event) {
        Logger.d(TAG, "IN: EnOceanBaseDriver.handleEvent(event: " + event + ")");
        if (event.getTopic().equals(EnOceanEvent.TOPIC_MSG_RECEIVED)) {
            Logger.d(TAG, "event.getPropertyNames(): " + event.getPropertyNames());
            String[] propertyNames = event.getPropertyNames();
            int i = 0;
            while (i < propertyNames.length) {
                Logger.d(TAG, "propertyNames[" + i + "]: " + propertyNames[i] + ", event.getProperty(propertyNames["
                        + i + "]): " + event.getProperty(propertyNames[i]));
                i = i + 1;
            }
            if (event.getProperty(EnOceanEvent.PROPERTY_EXPORTED) != null) {
                EnOceanMessage msg = (EnOceanMessage) event.getProperty(EnOceanEvent.PROPERTY_MESSAGE);
                Logger.d(TAG, "msg: " + msg);
                if (msg != null) {
                    EspPacket pkt = new EspPacket(msg);
                    Logger.d(TAG, "Writing packet : " + Utils.bytesToHexString(pkt.serialize()));
                    host.send(pkt.serialize());
                } else {
                    Logger.d(TAG, "msg is null");
                }
            } else {
                Logger.d(TAG, "event.getProperty(EnOceanEvent.PROPERTY_EXPORTED) is null");
            }
        } else {
            Logger.d(TAG, "event.getTopic(): " + event.getTopic());
        }
        Logger.d(TAG, "OUT: EnOceanBaseDriver.handleEvent(event: " + event + ")");
    }

    /**
	 * 
	 */
    public void start() {
        try {
            host.startup();
        } catch (EnOceanHostImplException e) {
            e.printStackTrace();
        }
    }

    /**
	 * 
	 */
    public void stop() {
        unregisterDevices();
    }

    /**
     * @param hostPath
     * @param enOceanHost
     * @return the corresponding serviceRegistration
     */
    public ServiceRegistration registerHost(String hostPath, EnOceanHost enOceanHost) {
        ServiceRegistration sr = null;
        try {
            Properties props = new Properties();
            props.put(EnOceanHost.HOST_ID, hostPath);
            sr = bc.registerService(EnOceanHost.class.getName(), enOceanHost, props);
            eoHostRefs.put(hostPath, sr);
        } catch (Exception e) {
            Logger.e(TAG, "exception when registering host : " + e.getMessage());
        }
        return sr;
    }

    /**
     * @param data
     */
    public void send(byte[] data) {
        host.send(data);
    }

    private void unregisterDevices() {
        Iterator it = eoDevices.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if (entry.getValue() instanceof EnOceanDeviceImpl) {
                EnOceanDeviceImpl dev = (EnOceanDeviceImpl) entry.getValue();
                Logger.d(TAG, "unregistering device : " + Utils.bytesToHexString(Utils.intTo4Bytes(dev.getChipId())));
                dev.remove();
            }
            it.remove();
        }
    }

    private void broadcastToEventAdmin(EnOceanMessage eoMsg) {
        EventAdmin eventAdmin = (EventAdmin) eventAdminTracker.getService();
        if (eventAdmin == null) {
            Logger.e(TAG,
                    "The system can NOT find any implementation of EventAdmin. The EnOceanMessage can NOT be broadcasted.");
        } else {
            Map properties = new Hashtable();
            properties.put(EnOceanDevice.CHIP_ID, String.valueOf(eoMsg.getSenderId()));
            properties.put(EnOceanDevice.RORG, String.valueOf(eoMsg.getRorg()));
            properties.put(EnOceanDevice.FUNC, String.valueOf(eoMsg.getFunc()));
            properties.put(EnOceanDevice.TYPE, String.valueOf(eoMsg.getType()));
            properties.put(EnOceanEvent.PROPERTY_MESSAGE, eoMsg);

            Event event = new Event(EnOceanEvent.TOPIC_MSG_RECEIVED, properties);
            eventAdmin.sendEvent(event);
            Logger.d(TAG, "The following event has been sent via EventAdmin; event: " + event);
        }
    }

    /**
     * Internal method to retrieve the service reference associated to a
     * message's SENDER_ID.
     * 
     * @param msg
     * @return the EnOceanDevice service, or null.
     */
    private EnOceanDevice getAssociatedDevice(Message msg) {
        String strSenderId = String.valueOf(msg.getSenderId());
        String filter = "(&(objectClass=" + EnOceanDevice.class.getName() + ")(" + EnOceanDevice.CHIP_ID + "="
                + strSenderId + "))";
        ServiceReference[] ref = null;
        try {
            ref = bc.getServiceReferences(null, filter);
        } catch (InvalidSyntaxException e) {
            Logger.e(TAG, "Invalid syntax in device search : " + e.getMessage());
        }
        if (ref != null && ref.length == 1) {
            return (EnOceanDevice) bc.getService(ref[0]);
        }
        return null;
    }

    private void registerDeviceAndProfile(int senderId, int rorg, int func, int type, int manuf) {
        EnOceanDeviceImpl device = new EnOceanDeviceImpl(bc, this, senderId, rorg, func, type, manuf);
        device.registerProfile(func, type, manuf);
    }

    /* The functions that come below are used to register the necessary services */
    private ServiceTracker registerServiceFrom(BundleContext bundleContext, Class objectClass,
            ServiceTrackerCustomizer listener) {
        String filter = "(objectClass=" + (objectClass).getName() + ')';
        Filter deviceAdminFilter;
        try {
            deviceAdminFilter = bundleContext.createFilter(filter);
        } catch (InvalidSyntaxException e) {
            Logger.e(TAG, e.getMessage());
            return null;
        }
        ServiceTracker serviceTracker = new ServiceTracker(bundleContext, deviceAdminFilter, listener);
        serviceTracker.open();
        return serviceTracker;
    }

    private ServiceTracker registerDeviceListener(BundleContext bundleContext, ServiceTrackerCustomizer listener)
            throws InvalidSyntaxException {
        return registerServiceFrom(bundleContext, org.osgi.service.enocean.EnOceanDevice.class, listener);
    }

}

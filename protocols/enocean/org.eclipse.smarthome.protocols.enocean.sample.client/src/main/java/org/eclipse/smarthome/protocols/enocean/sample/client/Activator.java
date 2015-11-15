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

package org.eclipse.smarthome.protocols.enocean.sample.client;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.smarthome.protocols.enocean.sample.client.shell.MiscCommand;
import org.eclipse.smarthome.protocols.enocean.sample.client.utils.Logger;
import org.eclipse.smarthome.protocols.enocean.sample.client.utils.Utils;
import org.osgi.service.device.Constants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Victor PERRON, Mailys ROBIN, Andre BOTTARO, Antonin CHAZALET.
 */
public class Activator implements BundleActivator, ServiceTrackerCustomizer, EventHandler {

    /** TAG */
    public static final String TAG = Activator.class.getName();

    private ServiceTracker deviceTracker;
    private ServiceRegistration deviceEventSubscription;
    private BundleContext bc;

    private boolean alarmIsActive = false;

    public void start(BundleContext bundleContext) throws InvalidSyntaxException {
        Logger.d(TAG,
                "IN: org.eclipse.smarthome.protocols.enocean.sample.client.Activator.start(bc: " + bundleContext + ")");

        this.bc = bundleContext;

        /* Track device creation */
        deviceTracker = new ServiceTracker(bc,
                bc.createFilter("(&(objectclass=" + EnOceanDevice.class.getName() + "))"), this);
        deviceTracker.open();

        /* Track device events */
        /* Initializes self as EventHandler */
        Hashtable ht = new Hashtable();
        ht.put(org.osgi.service.event.EventConstants.EVENT_TOPIC, new String[] { EnOceanEvent.TOPIC_MSG_RECEIVED, });
        deviceEventSubscription = bc.registerService(EventHandler.class.getName(), this, ht);

        // try {
        // Thread.sleep(3000);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }

        // Display the EnOceanDevice services.
        ServiceReference[] srs = bc.getAllServiceReferences(EnOceanDevice.class.getName(), null);
        Logger.d(TAG, "srs: " + srs);
        if (srs == null) {
            Logger.d(TAG,
                    "There is NO service registered with the following class name: " + EnOceanDevice.class.getName());
        } else {
            Logger.d(TAG, "srs.length: " + srs.length);

            int i = 0;
            while (i < srs.length) {
                ServiceReference sr = srs[i];
                Logger.d(TAG, "sr: " + sr);

                String[] pks = sr.getPropertyKeys();
                int j = 0;
                while (j < pks.length) {
                    Logger.d(TAG, "pks[" + j + "]: " + pks[j] + ", event.getProperty(" + pks[j] + "): "
                            + sr.getProperty(pks[j]));
                    j = j + 1;
                }

                EnOceanDevice eod = (EnOceanDevice) bc.getService(sr);
                Logger.d(TAG, "eod: " + eod);
                Logger.d(TAG, "eod.getChipId(): " + eod.getChipId());
                Logger.d(TAG, "eod.getFunc(): " + eod.getFunc());
                Logger.d(TAG, "eod.getManufacturer(): " + eod.getManufacturer());
                Logger.d(TAG, "eod.getRollingCode(): " + eod.getRollingCode());
                Logger.d(TAG, "eod.getRorg(): " + eod.getRorg());
                Logger.d(TAG, "eod.getSecurityLevelFormat(): " + eod.getSecurityLevelFormat());
                Logger.d(TAG, "eod.getType(): " + eod.getType());
                Logger.d(TAG, "eod.getClass(): " + eod.getClass());
                Logger.d(TAG, "eod.getEncryptionKey(): " + eod.getEncryptionKey());
                Logger.d(TAG, "eod.getLearnedDevices(): " + eod.getLearnedDevices());
                Logger.d(TAG, "eod.getRPCs(): " + eod.getRPCs());

                // // The following RPC is a copy of:
                // // org.osgi.test.cases.enocean.rpc.QueryFunction
                // EnOceanRPC rpc = new EnOceanRPC() {
                //
                // // sender='0x0180abb8'
                //
                // // propertyNames[0]: enocean.device.profile.func,
                // // event.getProperty(propertyNames[0]): -1
                //
                // // propertyNames[1]: enocean.device.profile.rorg,
                // // event.getProperty(propertyNames[1]): 165
                //
                // // propertyNames[2]: enocean.device.chip_id,
                // // event.getProperty(propertyNames[2]): 25209784
                //
                // // propertyNames[3]: enocean.device.profile.type,
                // // event.getProperty(propertyNames[3]): -1
                //
                // // propertyNames[4]: enocean.message,
                // // event.getProperty(propertyNames[4]): a5000074080180abb800
                //
                // // private int senderId = -1;
                // private int senderId = 0x0180abb8;
                //
                // public void setSenderId(int chipId) {
                // this.senderId = chipId;
                // }
                //
                // // public void setPayload(byte[] data) {
                // // // does nothing;
                // // Logger.d(TAG, "rpc.setPayLoad(data: " + data + ")");
                // // }
                //
                // public int getSenderId() {
                // return senderId;
                // }
                //
                // public byte[] getPayload() {
                // return null;
                // }
                //
                // public int getManufacturerId() {
                // return 0x07ff;
                // }
                //
                // public int getFunctionId() {
                // // return 0x0007;
                // return -1;
                // }
                //
                // public String getName() {
                // // TODO Auto-generated method stub
                // return null;
                // }
                // };
                //
                // EnOceanHandler handler = new EnOceanHandler() {
                // public void notifyResponse(EnOceanRPC enOceanRPC,
                // byte[] payload) {
                // Logger.d(TAG, "enOceanRPC: " + enOceanRPC
                // + ", payload: " + payload);
                // }
                // };
                //
                // Logger.d(TAG, "BEFORE invoking...");
                // eod.invoke(rpc, handler);
                // Logger.d(TAG, "AFTER invoking...");

                i = i + 1;

                // // Let's create an enoceanrpc in order to turn on the plug.
                // EnOceanRPC turnOnRpc = new EnOceanRPC() {
                // private int senderId = 0x0180abb8;
                //
                // public void setSenderId(int chipId) {
                // this.senderId = chipId;
                // }
                //
                // public int getSenderId() {
                // return senderId;
                // }
                //
                // public byte[] getPayload() {
                // return null;
                // }
                //
                // public int getManufacturerId() {
                // return -1;
                // }
                //
                // public int getFunctionId() {
                // return -1;
                // }
                //
                // public String getName() {
                // return "HARDCODED_TURN_ON";
                // }
                // };
                //
                // EnOceanHandler handlerTurnOnRpc = new EnOceanHandler() {
                // public void notifyResponse(EnOceanRPC enOceanRPC,
                // byte[] payload) {
                // Logger.d(TAG, "enOceanRPC: " + enOceanRPC
                // + ", payload: " + payload);
                // }
                // };
                //
                // Logger.d(TAG, "BEFORE invoking...");
                // eod.invoke(turnOnRpc, handlerTurnOnRpc);
                // Logger.d(TAG, "AFTER invoking...");

            }
        }

        // Add shell commands to test EnOcean developments.
        Hashtable props = new Hashtable();
        props.put("osgi.command.scope", "felix");
        // "aa", see method "aa" in MiscCommand, etc.
        // "ap" is for appair, "on" for turn on, and "of" for turn off.
        props.put("osgi.command.function", new String[] { "aa", "bb", "ds", "ap", "on", "of", "apb", "onb", "ofb" });
        bc.registerService(MiscCommand.class.getName(), new MiscCommand(bc), props);

        // eventAdmin
        ServiceReference eventAdminRef = bundleContext.getServiceReference(EventAdmin.class.getName());
        if (eventAdminRef != null) {
            eventAdmin = (EventAdmin) bundleContext.getService(eventAdminRef);
        } else {
            Logger.d(TAG, "No event admin service.");
        }

        Logger.d(TAG, "OUT: org.eclipse.smarthome.protocols.enocean.sample.client.Activator.start(bc: " + bc + ")");
    }

    public void stop(BundleContext bundleContext) {
        deviceTracker.close();
        deviceEventSubscription.unregister();
    }

    public Object addingService(ServiceReference reference) {
        Logger.d(TAG, "> addingService(reference: " + reference + ")");
        Object service = bc.getService(reference);
        if (service != null) {
            if (service instanceof EnOceanDevice) {
                EnOceanDevice device = (EnOceanDevice) service;
                Logger.d(TAG, "> Registered a new EnOceanDevice : " + Utils.printUid(device.getChipId()) + ", device: "
                        + device);
                return service;
            }
        }
        return null;
    }

    public void modifiedService(ServiceReference reference, Object service) {
        Logger.d(TAG, "> modifiedService(reference: " + reference + ", service: " + service + ")");
        if (service != null) {
            if (service instanceof EnOceanDevice) {
                EnOceanDevice device = (EnOceanDevice) service;
                Logger.d(TAG, "> modifiedService method. device.getChipId(): " + Utils.printUid(device.getChipId())
                        + ", device: " + device);
            }
        }
    }

    public void removedService(ServiceReference reference, Object service) {
        Logger.d(TAG, "> removedService(reference: " + reference + ", service: " + service + ")");
        if (service != null) {
            if (service instanceof EnOceanDevice) {
                EnOceanDevice device = (EnOceanDevice) service;
                Logger.d(TAG, "> removedService method. device.getChipId(): " + Utils.printUid(device.getChipId())
                        + ", device: " + device);
            }
        }
    }

    public void handleEvent(Event event) {
        Logger.d(TAG, "> handleEvent(event: " + event);

        Logger.d(TAG, "event.getTopic(): " + event.getTopic());
        // event.getTopic():
        // org/osgi/service/enocean/EnOceanEvent/MESSAGE_RECEIVED
        Logger.d(TAG, "event.getPropertyNames(): " + event.getPropertyNames());
        // event.getPropertyNames(): [Ljava.lang.String;@194737d

        String[] pns = event.getPropertyNames();
        int i = 0;
        while (i < pns.length) {
            Logger.d(TAG,
                    "pns[" + i + "]: " + pns[i] + ", event.getProperty(" + pns[i] + "): " + event.getProperty(pns[i]));
            i = i + 1;

            // pns[0]: enocean.device.profile.func,
            // event.getProperty(enocean.device.profile.func): 2
            // pns[1]: enocean.device.profile.rorg,
            // event.getProperty(enocean.device.profile.rorg): 165
            // pns[2]: enocean.device.chip_id,
            // event.getProperty(enocean.device.chip_id): 8969457
            // pns[3]: enocean.device.profile.type,
            // event.getProperty(enocean.device.profile.type): 5
            // pns[4]: enocean.message, event.getProperty(enocean.message):
            // a5000035080088dcf100
            // pns[5]: event.topics, event.getProperty(event.topics):
            // org/osgi/service/enocean/EnOceanEvent/MESSAGE_RECEIVED
        }

        String topic = event.getTopic();
        if (topic.equals(EnOceanEvent.TOPIC_MSG_RECEIVED)) {
            String chipId = (String) event.getProperty(EnOceanDevice.CHIP_ID);
            String rorg = (String) event.getProperty(EnOceanDevice.RORG);
            String func = (String) event.getProperty(EnOceanDevice.FUNC);
            String type = (String) event.getProperty(EnOceanDevice.TYPE);
            EnOceanMessage data = (EnOceanMessage) event.getProperty(EnOceanEvent.PROPERTY_MESSAGE);
            String displayId = Utils.printUid(Integer.parseInt(chipId));
            String profile = rorg + "/" + func + "/" + type;
            Logger.d(TAG, "> MSG_RECEIVED event : sender=" + displayId + ", profile = '" + profile + "'");
            Logger.d(TAG,
                    "Try to identify the device that has sent the just received event (e.g. is it an A5-02-05 device - a temperature sensor range 0°C to +40°C ?).");
            if ("165".equals(rorg)) {
                // hex 0xa5 == int 165.
                if ("2".equals(func)) {
                    if ("5".equals(type)) {
                        Logger.d(TAG, "This event has been sent by an A5-02-05 device.");
                        Logger.d(TAG,
                                "The end of page 12, and the beginning of page 13 of EnOcean_Equipment_Profiles_EEP_V2.61_public.pdf specifies how to get the temp°C value starting from an EnOcean telegram.");
                        // propertyNames[4]: enocean.message,
                        // event.getProperty(propertyNames[4]):
                        // a5000035080088dcf100
                        byte[] payload = data.getPayloadBytes();
                        Logger.d(TAG, "payload: " + payload + ", payload.length: " + payload.length);
                        int j = 0;
                        while (j < payload.length) {
                            Logger.d(TAG, "payload[" + j + "]: " + payload[j]);
                            j = j + 1;
                        }
                        byte rawTemperatureDB1InHexAsAByte = payload[2];
                        float rawTemperatureDB1InNumberAsADouble = rawTemperatureDB1InHexAsAByte;
                        Logger.d(TAG, "rawTemperatureDB1InNumberAsADouble: " + rawTemperatureDB1InNumberAsADouble);
                        if (rawTemperatureDB1InNumberAsADouble < 0) {
                            Logger.d(TAG,
                                    "rawTemperatureDB1InNumberAsADouble is negative, so let's convert rawTemperatureDB1InNumberAsADouble to unsigned 0..255 value instead of -127..128 one.");
                            rawTemperatureDB1InNumberAsADouble = rawTemperatureDB1InNumberAsADouble * -1
                                    + 2 * (128 + rawTemperatureDB1InNumberAsADouble);
                            Logger.d(TAG, "rawTemperatureDB1InNumberAsADouble: " + rawTemperatureDB1InNumberAsADouble);
                        } else {
                            Logger.d(TAG, "rawTemperatureDB1InNumberAsADouble is positive, everything is ok.");
                        }

                        // Now let's apply the formula:
                        // (rawTemperatureDB1InNumberAsADouble-255)*-40/255+0 =
                        // temp in celsius.
                        double tempInCelsius = (rawTemperatureDB1InNumberAsADouble - 255) * -40 / 255 + 0;
                        Logger.d(TAG, "tempInCelsius: " + tempInCelsius);
                    } else {
                        Logger.d(TAG, "This event has NOT been sent by an A5-02-05 device. TYPE is NOT equal to 5.");
                    }
                } else {
                    Logger.d(TAG, "This event has NOT been sent by an A5-02-05 device. FUNC is NOT equal to 2.");
                }
            } else if ("246".equals(rorg)) {
                // hex 0xf6 == int 246.
                Logger.d(TAG, "This event has been sent by an F6-wx-yz device.");
                Logger.d(TAG,
                        "FUNC, and TYPE are NOT sent by F6-wx-yz device. The system then assumes that the device is an F6-02-01.");
                Logger.d(TAG,
                        "In EnOcean_Equipment_Profiles_EEP_V2.61_public.pdf, pages 13-14, F6-02-01 -> RPS Telegram, Rocker Switch, 2 Rocker, Light and Blind Control - Application Style 1");

                // byte[] payload = data.getPayloadBytes(); using
                // getPayloadBytes() is NOT enough here.
                byte[] payload = data.getBytes();
                // e.g. f6500029219f3003ffffffff3100 when the button BI of an
                // F6-02-01 device is pressed.

                Logger.d(TAG, "payload: " + payload + ", payload.length: " + payload.length);
                int j = 0;
                while (j < payload.length) {
                    Logger.d(TAG, "payload[" + j + "] & 0xff (value is displayed as an unsigned int): "
                            + (payload[j] & 0xff));
                    j = j + 1;
                }

                byte dataDB0InHexAsAByte = payload[1];
                Logger.d(TAG, "dataDB0InHexAsAByte: " + dataDB0InHexAsAByte);
                int dataDB0InHexAsAnInt = dataDB0InHexAsAByte & 0xff;
                Logger.d(TAG, "dataDB0InHexAsAnInt: " + dataDB0InHexAsAnInt);

                byte statusInHexAsAByte = payload[6];
                Logger.d(TAG, "statusInHexAsAByte: " + statusInHexAsAByte);
                int statusInHexAsAsAnInt = statusInHexAsAByte & 0xff;
                Logger.d(TAG, "statusInHexAsAsAnInt: " + statusInHexAsAsAnInt);

                if ("'0x018c0874'".equals(displayId)) {
                    // Crapy hardcoded case in order to handle the Eltako FRW-WS
                    // smoke detector.
                    // Its constructor Eltako just confirmed that its smoke
                    // detector acts as a switch/button...
                    Logger.d(TAG, "This is the Eltako FRW-WS smoke detector with id 0x018c0874");
                    // cf.
                    // http://www.eltako.com/fileadmin/downloads/fr/katalog/eltako_funk_KapT_franz.pdf
                    // page 7,
                    // FRW
                    // ORG = 0x05
                    // Data_byte3 = 0x10 = alarme
                    // 0x00 = fin d'alarme
                    // 0x30 = tension de batterie < 7,2 V
                    if ((new Integer(0x00).byteValue() & 0xff) == dataDB0InHexAsAnInt) {
                        // No Alarm
                        Logger.d(TAG, "No ALARM");
                        reportFireNormalSituation();
                    } else if ((new Integer(0x10).byteValue() & 0xff) == dataDB0InHexAsAnInt) {
                        // Alarm
                        Logger.d(TAG, "ALARM");
                        reportFire(displayId);
                    } else if ((new Integer(0x70).byteValue() & 0xff) == dataDB0InHexAsAnInt) {
                        Logger.d(TAG, "LOW BATTERY < 7,2 Volts");
                    } else {
                        Logger.d(TAG, "The given Data DB_0 is UNKNOWN; its value is: " + dataDB0InHexAsAnInt);
                    }
                } else if (("'0x01819912'".equals(displayId)) || ("'0x01819457'".equals(displayId))) {
                    // Crapy hardcoded case in order to handle the AfrisoLab
                    // WaterSensor eco.
                    // This device uses a PTM330, and follow the EnOcean's
                    // F6-05-01 EEP profile.
                    Logger.d(TAG, "This is the AfrisoLab Watersensor eco with id: " + displayId);
                    if ((new Integer(0x11).byteValue() & 0xff) == dataDB0InHexAsAnInt) {
                        if ((new Integer(0x20).byteValue() & 0xff) == statusInHexAsAsAnInt) {
                            // No Alarm
                            Logger.d(TAG, "No ALARM");
                            reportWaterNormalSituation();
                        } else if ((new Integer(0x30).byteValue() & 0xff) == statusInHexAsAsAnInt) {
                            // Alarm
                            Logger.d(TAG, "ALARM");
                            reportWater(displayId);
                        } else {
                            Logger.d(TAG, "The given Data DB_6 is UNKNOWN; its value is: " + statusInHexAsAsAnInt);
                        }
                    } else {
                        Logger.d(TAG, "The given Data DB_0 is UNKNOWN; its value is: " + dataDB0InHexAsAnInt);
                    }
                } else {
                    if ((new Integer(0x30).byteValue() & 0xff) == statusInHexAsAsAnInt) {
                        Logger.d(TAG, "Here, a button has been pressed.");
                        if ((new Integer(0x30).byteValue() & 0xff) == dataDB0InHexAsAnInt) {
                            // Check if A0 button has been pressed --> 0x30
                            Logger.d(TAG, "A0");
                        } else if ((new Integer(0x10).byteValue() & 0xff) == dataDB0InHexAsAnInt) {
                            // Check if AI button has been pressed --> 0x10
                            Logger.d(TAG, "AI");
                        } else if ((new Integer(0x70).byteValue() & 0xff) == dataDB0InHexAsAnInt) {
                            // Check if A0 button has been pressed --> 0x70
                            Logger.d(TAG, "B0");
                        } else if ((new Integer(0x50).byteValue() & 0xff) == dataDB0InHexAsAnInt) {
                            // Check if A0 button has been pressed --> 0x50
                            Logger.d(TAG, "BI");
                            // The switch is used to simulate the smoke
                            // detector.
                            // Alarm
                            Logger.d(TAG, "ALARM");
                            reportFire(displayId);
                            alarmIsActive = true;
                        } else {
                            Logger.d(TAG, "The given Data DB_0 is UNKNOWN; its value is: " + dataDB0InHexAsAnInt);
                        }
                    } else if ((new Integer(0x20).byteValue() & 0xff) == statusInHexAsAsAnInt) {
                        Logger.d(TAG, "Here, a button has been released (normally, this button was the pressed one.)");
                        if (alarmIsActive) {
                            // The switch is used to simulate the smoke
                            // detector.
                            // No Alarm
                            Logger.d(TAG, "No ALARM");
                            reportFireNormalSituation();
                            alarmIsActive = false;
                        }
                    } else {
                        Logger.d(TAG,
                                "The given status field of this RPS telegram is UNKNOWN. This status was (as an int): "
                                        + statusInHexAsAsAnInt);
                    }
                }
            } else if ("213".equals(rorg)) {
                // hex 0xd5 == int 213.
                Logger.d(TAG, "This event has been sent by a D5-wx-yz device.");
                Logger.d(TAG,
                        "FUNC, and TYPE are NOT sent by D5-wx-yz device. The system then assumes that the device is an D5-00-01.");
                Logger.d(TAG,
                        "In EnOcean_Equipment_Profiles_EEP_V2.61_public.pdf, pages 24, D5-00-01 -> 1BS Telegram, Contacts and Switches, Single Input Contact.");

                // Logger.d(TAG, "data.getBytes(): " + data.getBytes());
                // Logger.d(TAG, "Utils.bytesToHexString(data.getBytes()): "
                // + Utils.bytesToHexString(data.getBytes()));
                //
                // Logger.d(TAG, "data.getDbm(): " + data.getDbm());
                // Logger.d(TAG, "data.getDestinationId(): "
                // + data.getDestinationId());
                // Logger.d(TAG, "data.getFunc(): " + data.getFunc());
                //
                // Logger.d(TAG, "data.getPayloadBytes(): "
                // + data.getPayloadBytes());
                // Logger.d(TAG,
                // "Utils.bytesToHexString(data.getPayloadBytes()): "
                // + Utils.bytesToHexString(data.getPayloadBytes()));
                //
                // Logger.d(TAG, "data.getRorg(): " + data.getRorg());
                // Logger.d(TAG, "data.getSecurityLevelFormat(): "
                // + data.getSecurityLevelFormat());
                // Logger.d(TAG, "data.getSenderId(): " + data.getSenderId());
                // Logger.d(TAG, "data.getStatus(): " + data.getStatus());
                // Logger.d(TAG, "data.getSubTelNum(): " + data.getSubTelNum());
                // Logger.d(TAG, "data.getType(): " + data.getType());

                if (8 == data.getPayloadBytes()[0]) {
                    Logger.d(TAG, "An opening has been detected.");
                } else if (9 == data.getPayloadBytes()[0]) {
                    Logger.d(TAG, "A closing has been detected.");
                } else if (0 == data.getPayloadBytes()[0]) {
                    Logger.d(TAG, "The LRN button has been pressed.");
                } else {
                    Logger.d(TAG, "The given 1BS's Data DB_0 value (data.getPayloadBytes()[0]: "
                            + data.getPayloadBytes()[0]
                            + " doesn't correspond to anything in EnOcean's specs. There is a pb. The system doesn't know how to handle this message.");
                }
            } else {
                Logger.d(TAG,
                        "This event has NOT been sent by an A5-02-05 device, nor by a F6-wx-yz device, nor by a D5-wx-yz device. "
                                + "RORG is NOT equal to a5, nor f6,nor d5 (0xa5 is equal to int 165; 0xf6 -> 246, 0xd5 -> 213).");
            }
        }
    }

    /**
     * Topic used to report fire situation and normal situation. This topic is
     * similar as the one used by ZigBee smoke detector to report alarm.
     */
    private static final String NOTIFICATION_TOPIC = "zcl/command/received/ZoneStatusChangeNotification";

    /**
     * Property Zone Status contains long value 4452 for fire alarm and long
     * value 4196 for normal situation.
     */
    private static final String ZONE_STATUS_PROPERTY = "Zone Status";

    /**
     * Topic used to report water situation and normal situation.
     */
    private static final String WATERALARM_NOTIFICATION_TOPIC = "wateralarm/notif";

    /**
     * Property Zone Status contains long value 4452 for fire alarm and long
     * value 4196 for normal situation.
     */
    private static final String WATERALARM_PROPERTY = "WATERALARM";

    /**
     * used to report fire and normal situation
     */
    private EventAdmin eventAdmin;

    /**
     * Report fire situation on event admin.
     *
     * @param deviceSerial
     *            , i.e. the device EnOcean Id: 0x........
     */
    private void reportFire(String deviceSerial) {
        Logger.d(TAG, "reportAlarmSituation !!!!!");
        Dictionary properties = new Hashtable();
        properties.put(ZONE_STATUS_PROPERTY, new Long(4452));
        properties.put(Constants.DEVICE_SERIAL, deviceSerial);
        Event event = new Event(NOTIFICATION_TOPIC, properties);
        eventAdmin.postEvent(event);
    }

    /**
     * Report fire normal situation on event admin
     */
    private void reportFireNormalSituation() {
        Logger.d(TAG, "reportFireNormalSituation !!!!!");
        Dictionary properties = new Hashtable();
        properties.put(ZONE_STATUS_PROPERTY, new Long(4196));
        Event event = new Event(NOTIFICATION_TOPIC, properties);
        eventAdmin.postEvent(event);
    }

    /**
     * Report water situation on event admin.
     *
     * @param deviceSerial
     *            , i.e. the device EnOcean Id: 0x........
     */
    private void reportWater(String deviceSerial) {
        Logger.d(TAG, "reportWaterAlarmSituation !!!!!");
        Dictionary properties = new Hashtable();
        properties.put(WATERALARM_PROPERTY, new Long(4452));
        properties.put(Constants.DEVICE_SERIAL, deviceSerial);
        Event event = new Event(WATERALARM_NOTIFICATION_TOPIC, properties);
        eventAdmin.postEvent(event);
    }

    /**
     * Report water normal situation on event admin
     */
    private void reportWaterNormalSituation() {
        Logger.d(TAG, "reportWaterNormalSituation !!!!!");
        Dictionary properties = new Hashtable();
        properties.put(WATERALARM_PROPERTY, new Long(4196));
        Event event = new Event(WATERALARM_NOTIFICATION_TOPIC, properties);
        eventAdmin.postEvent(event);
    }
}

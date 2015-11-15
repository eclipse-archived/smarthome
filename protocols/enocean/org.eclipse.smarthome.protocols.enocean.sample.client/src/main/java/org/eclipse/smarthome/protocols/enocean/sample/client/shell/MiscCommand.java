package org.eclipse.smarthome.protocols.enocean.sample.client.shell;

import java.io.IOException;

import org.eclipse.smarthome.protocols.enocean.sample.client.utils.Logger;

/**
 * Command shell for Apache Felix.
 */
public class MiscCommand {

    /** TAG */
    public static final String TAG = MiscCommand.class.getName();

    private final BundleContext bc;

    /**
     * @param bc
     */
    public MiscCommand(BundleContext bc) {
        this.bc = bc;
    }

    /**
     * 
     */
    public void aa() {
        String result = "aa command";
        Logger.d(TAG, result);
    }

    /**
     * @param param1
     * @return result
     * @throws IOException
     */
    public String bb(String param1) throws IOException {
        String result = "bb command with param1: " + param1;
        // Logger.d(TAG, result);
        return result;
    }

    /**
     * Display all the services.
     */
    public void ds() {
        Logger.d(TAG, "This ds command displays all the services.");
        // display all the services.
        try {
            ServiceReference[] allsrs = bc.getAllServiceReferences(null, null);
            Logger.d(TAG, "allsrs: " + allsrs);
            if (allsrs == null) {
                Logger.d(TAG, "There is NO service registered at all.");
            } else {
                Logger.d(TAG, "allsrs.length: " + allsrs.length);

                int i = 0;
                while (i < allsrs.length) {
                    ServiceReference sr = allsrs[i];
                    Logger.d(TAG, "-sr: " + sr);
                    String[] pks = sr.getPropertyKeys();
                    int j = 0;
                    while (j < pks.length) {
                        Logger.d(TAG, "--pks[" + j + "]: " + pks[j] + ", sr.getProperty(" + pks[j] + "): "
                                + sr.getProperty(pks[j]));
                        Logger.d(TAG, "--sr.getProperty(" + pks[j] + ").getClass().getName()"
                                + sr.getProperty(pks[j]).getClass().getName());
                        j = j + 1;
                    }
                    i = i + 1;
                }
            }
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a EnOcean packet in order to appair with the plug.
     */
    public void ap() {
        // 55 00 0D 07 01 FD D4 D2 01 08 00 3E FF 91 00 00 00 00 00 03 01 85 64
        // 14 FF 00 E9

        // Let's create an enoceanrpc in order to appair with the plug.
        EnOceanRPC appairRPC = new EnOceanRPC() {
            private int senderId = 0x00000000;

            public void setSenderId(int chipId) {
                this.senderId = chipId;
            }

            public int getSenderId() {
                return senderId;
            }

            public byte[] getPayload() {
                return null;
            }

            public int getManufacturerId() {
                return -1;
            }

            public int getFunctionId() {
                return -1;
            }

            public String getName() {
                return "HARDCODED_UTE_APPAIR";
            }
        };

        EnOceanHandler handlerTurnOnRpc = new EnOceanHandler() {
            public void notifyResponse(EnOceanRPC enOceanRPC, byte[] payload) {
                Logger.d(TAG, "enOceanRPC: " + enOceanRPC + ", payload: " + payload);
            }
        };

        EnOceanDevice eod = getAnEnOceanDevice();
        if (eod == null) {
            Logger.e(this.getClass().getName(), "There is no EnOceanDevice --> So no invocation is possible.");
        } else {
            Logger.e(this.getClass().getName(), "There is an EnOceanDevice --> So invocation is possible.");
            Logger.d(TAG, "BEFORE invoking...");
            eod.invoke(appairRPC, handlerTurnOnRpc);
            Logger.d(TAG, "AFTER invoking...");
        }
    }

    /**
     * Send a EnOcean packet in order to turn on the plug.
     */
    public void on() {
        // 55 00 09 07 01 56 D2 01 00 01 00 00 00 00 00 03 01 85 64 14 FF 00 64

        // Let's create an enoceanrpc in order to appair with the plug.
        EnOceanRPC appairRPC = new EnOceanRPC() {
            private int senderId = 0x00000000;

            public void setSenderId(int chipId) {
                this.senderId = chipId;
            }

            public int getSenderId() {
                return senderId;
            }

            public byte[] getPayload() {
                return null;
            }

            public int getManufacturerId() {
                return -1;
            }

            public int getFunctionId() {
                return -1;
            }

            public String getName() {
                return "HARDCODED_VLD_TURN_ON";
            }
        };

        EnOceanHandler handlerTurnOnRpc = new EnOceanHandler() {
            public void notifyResponse(EnOceanRPC enOceanRPC, byte[] payload) {
                Logger.d(TAG, "enOceanRPC: " + enOceanRPC + ", payload: " + payload);
            }
        };

        EnOceanDevice eod = getAnEnOceanDevice();
        if (eod == null) {
            Logger.e(this.getClass().getName(), "There is no EnOceanDevice --> So no invocation is possible.");
        } else {
            Logger.e(this.getClass().getName(), "There is an EnOceanDevice --> So invocation is possible.");
            Logger.d(TAG, "BEFORE invoking...");
            eod.invoke(appairRPC, handlerTurnOnRpc);
            Logger.d(TAG, "AFTER invoking...");
        }

    }

    /**
     * Send a EnOcean packet in order to turn off the plug.
     */
    public void of() {
        // 55 00 09 07 01 56 D2 01 00 00 00 00 00 00 00 03 01 85 64 14 FF 00 F0

        // Let's create an enoceanrpc in order to appair with the plug.
        EnOceanRPC appairRPC = new EnOceanRPC() {
            private int senderId = 0x00000000;

            public void setSenderId(int chipId) {
                this.senderId = chipId;
            }

            public int getSenderId() {
                return senderId;
            }

            public byte[] getPayload() {
                return null;
            }

            public int getManufacturerId() {
                return -1;
            }

            public int getFunctionId() {
                return -1;
            }

            public String getName() {
                return "HARDCODED_VLD_TURN_OFF";
            }
        };

        EnOceanHandler handlerTurnOnRpc = new EnOceanHandler() {
            public void notifyResponse(EnOceanRPC enOceanRPC, byte[] payload) {
                Logger.d(TAG, "enOceanRPC: " + enOceanRPC + ", payload: " + payload);
            }
        };

        EnOceanDevice eod = getAnEnOceanDevice();
        if (eod == null) {
            Logger.e(this.getClass().getName(), "There is no EnOceanDevice --> So no invocation is possible.");
        } else {
            Logger.e(this.getClass().getName(), "There is an EnOceanDevice --> So invocation is possible.");
            Logger.d(TAG, "BEFORE invoking...");
            eod.invoke(appairRPC, handlerTurnOnRpc);
            Logger.d(TAG, "AFTER invoking...");
        }

    }

    /**
     * Send a EnOcean packet as PTM210 in order to appair with the plug, or
     * WaterControl.
     */
    public void apb() {
        // 55 00 07 07 01 7A F6 50 00 29 21 9F 30 01 FF FF FF FF 31 00 37

        // Let's create an enoceanrpc in order to appair with the plug.
        EnOceanRPC appairRPC = new EnOceanRPC() {
            private int senderId = 0x00000000;

            public void setSenderId(int chipId) {
                this.senderId = chipId;
            }

            public int getSenderId() {
                return senderId;
            }

            public byte[] getPayload() {
                return null;
            }

            public int getManufacturerId() {
                return -1;
            }

            public int getFunctionId() {
                return -1;
            }

            public String getName() {
                return "HARDCODED_APPAIR_TURN_ON";
            }
        };

        EnOceanHandler handlerTurnOnRpc = new EnOceanHandler() {
            public void notifyResponse(EnOceanRPC enOceanRPC, byte[] payload) {
                Logger.d(TAG, "enOceanRPC: " + enOceanRPC + ", payload: " + payload);
            }
        };

        EnOceanDevice eod = getAnEnOceanDevice();
        if (eod == null) {
            Logger.e(this.getClass().getName(), "There is no EnOceanDevice --> So no invocation is possible.");
        } else {
            Logger.e(this.getClass().getName(), "There is an EnOceanDevice --> So invocation is possible.");
            Logger.d(TAG, "BEFORE invoking...");
            eod.invoke(appairRPC, handlerTurnOnRpc);
            Logger.d(TAG, "AFTER invoking...");
        }
    }

    /**
     * Send a EnOcean packet as PTM210 in order to turn on the plug, or
     * WaterControl.
     */
    public void onb() {
        apb();
    }

    /**
     * Send a EnOcean packet as PTM210 in order to turn off the plug, or
     * WaterControl.
     */
    public void ofb() {
        // 55 00 07 07 01 7A F6 70 00 29 21 9F 30 01 FF FF FF FF 2D 00 62

        // Let's create an enoceanrpc in order to appair with the plug.
        EnOceanRPC appairRPC = new EnOceanRPC() {
            private int senderId = 0x00000000;

            public void setSenderId(int chipId) {
                this.senderId = chipId;
            }

            public int getSenderId() {
                return senderId;
            }

            public byte[] getPayload() {
                return null;
            }

            public int getManufacturerId() {
                return -1;
            }

            public int getFunctionId() {
                return -1;
            }

            public String getName() {
                return "HARDCODED_TURN_OFF";
            }
        };

        EnOceanHandler handlerTurnOnRpc = new EnOceanHandler() {
            public void notifyResponse(EnOceanRPC enOceanRPC, byte[] payload) {
                Logger.d(TAG, "enOceanRPC: " + enOceanRPC + ", payload: " + payload);
            }
        };

        EnOceanDevice eod = getAnEnOceanDevice();
        if (eod == null) {
            Logger.e(this.getClass().getName(), "There is no EnOceanDevice --> So no invocation is possible.");
        } else {
            Logger.e(this.getClass().getName(), "There is an EnOceanDevice --> So invocation is possible.");
            Logger.d(TAG, "BEFORE invoking...");
            eod.invoke(appairRPC, handlerTurnOnRpc);
            Logger.d(TAG, "AFTER invoking...");
        }
    }

    /**
     * @return an EnOceanDevice if any, the system doesn't care which on it will
     *         be. Return null otherwise.
     */
    private EnOceanDevice getAnEnOceanDevice() {
        try {
            ServiceReference[] srs = bc.getAllServiceReferences(EnOceanDevice.class.getName(), null);
            Logger.d(TAG, "srs: " + srs);
            if (srs == null) {
                Logger.d(TAG, "There is NO service registered with the following class name: "
                        + EnOceanDevice.class.getName());
                return null;
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
                    return eod;
                }
                return null;
            }
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}

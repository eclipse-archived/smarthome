package org.eclipse.smarthome.binding.enocean.rpc;

import org.osgi.service.enocean.EnOceanRPC;

public class CustomEnoceanRPC implements EnOceanRPC {

    // let's define here all the frames supported by the base driver. from now they are hardcoded as is:

    public static String APPAIR_FRAME = "HARDCODED_UTE_APPAIR";
    // 55 00 0D 07 01 FD D4 D2 01 08 00 3E FF 91 00 00 00 00 00 03 01 85 64 14 FF 00 E9

    public static String ON_FRAME = "HARDCODED_VLD_TURN_ON";
    // 55 00 09 07 01 56 D2 01 00 01 00 00 00 00 00 03 01 85 64 14 FF 00 64

    public static String OFF_FRAME = "HARDCODED_VLD_TURN_OFF";
    // 55 00 09 07 01 56 D2 01 00 00 00 00 00 00 00 03 01 85 64 14 FF 00 F0

    public static String PTM210_APPAIR_FRAME = "HARDCODED_APPAIR_TURN_ON";
    // 55 00 07 07 01 7A F6 50 00 29 21 9F 30 01 FF FF FF FF 31 00 37
    // PTM210 represents a classical Enocean switch, the frame on is also considered as a pairing one.

    public static String PTM210_ON_FRAME = "HARDCODED_APPAIR_TURN_ON";
    // same frame as above.

    public static String PTM210_OFF_FRAME = "HARDCODED_TURN_OFF";
    // 55 00 07 07 01 7A F6 70 00 29 21 9F 30 01 FF FF FF FF 2D 00 62
    // the switch frame off
    private String frame;
    private int senderId = 0x00000000;

    public CustomEnoceanRPC(String frame) {
        this.frame = frame;
    }

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
        // TODO Auto-generated method stub
        return frame;
    }
}

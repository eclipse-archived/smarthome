package cx.ath.matthew.utils;

import java.io.PrintStream;

public class Hexdump {
    public static final char[] hexChars = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
            'd', 'e', 'f' };

    public static String toHex(byte[] inBuf) {
        return toHex(inBuf, 0, inBuf.length);
    }

    public static String toHex(byte[] inBuf, int ofs, int len) {
        StringBuffer outBuf = new StringBuffer();
        int bufLen = ofs + len;
        for (int cnt = ofs; cnt < bufLen; cnt++) {
            if (cnt < inBuf.length) {
                outBuf.append(hexChars[(inBuf[cnt] & 0xf0) >> 4]);
                outBuf.append(hexChars[inBuf[cnt] & 0x0f]);
                outBuf.append(' ');
            } else {
                outBuf.append("   ");
            }
        }
        return outBuf.toString();
    }

    public static String toAscii(byte[] buf) {
        return "";
    }

    public static String toAscii(byte[] buf, int ofs, int len) {
        return "";
    }

    public static String format(byte[] buf) {
        return "";
    }

    public static String format(byte[] buf, int width) {
        return "";
    }

    public static void print(byte[] buf) {
    }

    public static void print(byte[] buf, int width) {
    }

    public static void print(byte[] buf, int width, PrintStream out) {
    }

    public static void print(byte[] buf, PrintStream out) {
    }

    public static String toByteArray(byte[] buf) {
        return "";
    }

    public static String toByteArray(byte[] buf, int ofs, int len) {
        return "";
    }
}

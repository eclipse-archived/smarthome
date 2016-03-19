package cx.ath.matthew.debug;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Conversion class to provide slf4j logging methods for the DBUS-JAVA library
 */
public class Debug {
    /** Critical messages */
    public static final int CRIT = 1;
    /** Error messages */
    public static final int ERR = 2;
    /** Warnings */
    public static final int WARN = 3;
    /** Information */
    public static final int INFO = 4;
    /** Debug messages */
    public static final int DEBUG = 5;
    /** Verbose debug messages */
    public static final int VERBOSE = 6;
    /** Set this to false to disable compilation of Debug statements */
    public static final boolean debug = false;

    /** The current output stream (defaults to System.err) */
    // public static PrintStream debugout = System.err;

    // public static interface FilterCommand {

    // public void filter(PrintStream output, int level, String location, String extra, String message,
    // String[] lines);
    // }/

    // public static void setProperties(Properties prop) {
    // }

    public static void loadConfig(File f) throws IOException {
    }

    // public static boolean debugging(String s, int loglevel) {
    // return false;
    // }

    // public static void setOutput(PrintStream p) throws IOException {
    // }

    // public static void setOutput(String filename) throws IOException {
    // }

    // public static void setOutput() throws IOException {
    // }

    public static void print(Object d) {
    }

    public static void print(int loglevel, Throwable t) {
    }

    public static void print(int loglevel, byte[] b) {
    }

    public static void print(int loglevel, String s) {
    }

    // private static String[] getTraceElements() {
    // return null;
    // }

    public static void print(int loglevel, Object o) {
    }

    public static void printMap(Map m) {
    }

    public static void printMap(int loglevel, Map m) {
    }

    public static void setThrowableTraces(boolean ttrace) {
    }

    // public static void setTiming(boolean timing) {
    // }

    // public static void setLineNos(boolean lines) {
    // }

    public static void setHexDump(boolean hexdump) {
    }

    // public static void setByteArrayCount(int count) {
    // }

    // public static void setByteArrayWidth(int width) {
    // }

    // public static void addFilterCommand(Class c, FilterCommand f) {
    // }

    private static void _print(Class c, int level, String loc, String extra, String message, String[] lines) {
    }
}

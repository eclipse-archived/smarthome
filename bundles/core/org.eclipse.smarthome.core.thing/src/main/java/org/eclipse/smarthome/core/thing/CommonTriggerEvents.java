package org.eclipse.smarthome.core.thing;

/**
 * Contains often used trigger events.
 *
 * @author Moritz Kammerer - Initial contribution and API.
 */
public final class CommonTriggerEvents {
    /**
     * Static class - no instances allowed.
     */
    private CommonTriggerEvents() {
    }

    public static final String PRESSED = "PRESSED";
    public static final String RELEASED = "RELEASED";
    public static final String SHORT_PRESSED = "SHORT_PRESSED";
    public static final String DOUBLE_PRESSED = "DOUBLE_PRESSED";
    public static final String LONG_PRESSED = "LONG_PRESSED";

}

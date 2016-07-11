package org.eclipse.smarthome.tools.docgenerator.util;

/**
 * Utilities collection for working with Markdown, XML, etc.
 */
public final class BooleanUtils {

    private BooleanUtils() {/* do not allow instances */}

    /**
     * Returns "Yes" or "No" (Strings) for the given boolean expression.
     *
     * @param expr the boolean expression
     * @return "Yes" for true and "No" for false and null
     */
    public static String booleanToYesOrNo(Boolean expr) {
        return expr == null || !(expr) ? "No" : "Yes";
    }

    /**
     * Returns "true" or "false" (Strings) for the given boolean expression.
     *
     * @param expr the boolean expression
     * @return "true" for true and "false" for false and null
     */
    public static String booleanToTrueOrFalse(Boolean expr) {
        return expr == null || !(expr) ? "false" : "true";
    }
}

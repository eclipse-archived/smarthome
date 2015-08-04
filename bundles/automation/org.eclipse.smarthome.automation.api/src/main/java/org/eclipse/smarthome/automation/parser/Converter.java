package org.eclipse.smarthome.automation.parser;

import java.util.Dictionary;

public interface Converter {

    /**
     * Converts String representation of JsonObject
     * 
     * @param source the String representation
     * @return key:value pairs represented by Dictionary.
     */
    public Dictionary getAsDictionary(String source);

}

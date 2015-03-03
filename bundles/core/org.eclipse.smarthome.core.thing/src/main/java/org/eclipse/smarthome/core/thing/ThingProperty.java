package org.eclipse.smarthome.core.thing;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;

/**
 * The {@link ThingProperty} enumeration defines all base configuration properties for a {@link Thing}. They
 * will be injected into the {@link Configuration} of a thing during its instantiation (compare
 * {@link BaseThingHandlerFactory#createThing(ThingTypeUID, Configuration, ThingUID, ThingUID)}). For this purpose
 * the operation {@link BaseThingHandlerFactory#getThingProperties(Thing)} must be overwritten in the concrete thing
 * handler factory class.
 * 
 * @author Thomas Höfer - initial contribution
 */
public enum ThingProperty {

    VENDOR("vendor"), 
    MODEL("model"), 
    SERIAL_NUMBER("serialNumber"), 
    HARDWARE_VERSION("hardwareVersion"), 
    FIRMWARE_VERSION("firmwareVersion");

    public final String keyName;

    private ThingProperty(String keyName) {
        this.keyName = keyName;
    }
}
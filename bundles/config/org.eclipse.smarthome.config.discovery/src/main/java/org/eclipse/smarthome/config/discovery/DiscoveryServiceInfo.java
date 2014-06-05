package org.eclipse.smarthome.config.discovery;

import java.util.List;

import org.eclipse.smarthome.core.thing.ThingTypeUID;


/**
 * The {@link DiscoveryServiceInfo} is an immutable container class containing
 * meta-information about its according {@link DiscoveryService}.
 *
 * @author Michael Grammling - Initial Contribution.
 *
 * @see DiscoveryService
 */
public final class DiscoveryServiceInfo {

    private List<ThingTypeUID> supportedThingTypes;
    private int timeout;


    /**
     * Creates a new instance of this class with the specified parameters.
     *
     * @param supportedThingTypes the list of Thing types which are supported (must not be null)
     *
     * @param timeout the discovery timeout in seconds after which the discovery service
     *     automatically stops its forced discovery process (>= 0).
     *
     * @throws IllegalArgumentException if the list of Thing types is null, or the timeout < 0
     */
    public DiscoveryServiceInfo(List<ThingTypeUID> supportedThingTypes, int timeout)
            throws IllegalArgumentException {

        if (supportedThingTypes == null) {
            throw new IllegalArgumentException("The supported Thing types must not be null!");
        }

        if (timeout < 0) {
            throw new IllegalArgumentException("The timeout must be >= 0!");
        }

        this.supportedThingTypes = supportedThingTypes;
        this.timeout = timeout;
    }

    /**
     * Returns the list of {@code Thing} types which are supported by the {@link DiscoveryService}.
     *
     * @return the list of Thing types which are supported by the discovery service
     *     (not null, could be empty)
     */
    public List<ThingTypeUID> getSupportedThingTypes() {
        return this.supportedThingTypes;
    }

    /**
     * Returns the amount of time in seconds after which the discovery service automatically
     * stops its forced discovery process.
     *
     * @return the discovery timeout in seconds (>= 0).
     */
    public int getDiscoveryTimeout() {
        return this.timeout;
    }

    @Override
    public String toString() {
        return "DiscoveryServiceInfo [supportedThingTypes="
                + supportedThingTypes + ", timeout=" + timeout + "]";
    }

}

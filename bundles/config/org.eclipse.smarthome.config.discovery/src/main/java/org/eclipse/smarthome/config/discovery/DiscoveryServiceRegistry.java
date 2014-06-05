package org.eclipse.smarthome.config.discovery;

import org.eclipse.smarthome.core.thing.ThingTypeUID;


/**
 * The {@link DiscoveryServiceRegistry} is a service interface which provides
 * the following features.
 * <ul>
 * <li>Monitoring of {@link DiscoveryService}s</li>
 * <li>Direct accessing monitored {@link DiscoveryService}s</li>
 * <li>Forwarding all events received from the monitored {@link DiscoveryService}s.</li>
 * </ul>
 *
 * @author Michael Grammling - Initial Contribution.
 *
 * @see DiscoveryService
 * @see DiscoveryListener
 */
public interface DiscoveryServiceRegistry {

    /**
     * Returns the {@link DiscoveryServiceInfo} object (meta information) of a
     * {@link DiscoveryService} for the specified {@code Thing} type by searching
     * in the list of any monitored {@link DiscoveryService}s.
     * <p>
     * If the specified {@code Thing} type is {@code null} or empty, or no according
     * {@link DiscoveryService} could be found, {@code null} is returned.
     *
     * @param thingType the Thing type which points to the according discovery service
     *     (could be null or empty)
     *
     * @return the discovery service meta information or null, if no according discovery
     *     service could be found
     */
    DiscoveryServiceInfo getDiscoveryInfo(ThingTypeUID thingTypeUID);

    /**
     * Forces the associated {@link DiscoveryService} to start a discovery.
     * <p>
     * Returns {@code true}, if a {@link DiscoveryService} could be found and forced
     * to start a discovery, otherwise {@code false}. If the discovery process has
     * already been started before, {@code true} is returned.
     *
     * @param thingType the Thing type pointing to the discovery service to be forced
     *     to start a discovery
     *
     * @return true if a discovery service could be found and forced to start a discovery,
     *     otherwise false
     */
    boolean forceDiscovery(ThingTypeUID thingTypeUID);

    /**
     * Aborts a started discovery on a {@link DiscoveryService}.
     * <p>
     * Returns {@code true}, if a {@link DiscoveryService} could be found and whose
     * discovery could be aborted, otherwise {@code false}. If the discovery process
     * has not been started before, {@code true} is returned.
     *
     * @param thingType the Thing type pointing to the discovery service whose discovery
     *     should be aborted
     *
     * @return true if a discovery service could be found and whose discovery could be
     *     aborted, otherwise false
     */
    boolean abortForceDiscovery(ThingTypeUID thingTypeUID);

    /**
     * Adds a {@link DiscoveryListener} to the listeners' registry.
     * <p>
     * When a {@link DiscoveryResult} is created by any of the monitored {@link DiscoveryService}s,
     * (e.g. by forcing the startup of the discovery process or while enabling the auto discovery
     * mode), the specified listener is notified.
     * <p>
     * This method returns silently if the specified listener is {@code null}
     * or has already been registered before.
     *
     * @param listener the listener to be added (could be null)
     */
    void addDiscoveryListener(DiscoveryListener listener);

    /**
     * Removes a {@link DiscoveryListener} from the listeners' registry.
     * <p>
     * When this method returns, the specified listener is no longer notified about
     * {@link DiscoveryResult}s created by any of the monitored {@link DiscoveryService}s
     * (e.g. by forcing the startup of the discovery process or while enabling the auto
     * discovery mode).
     * <p>
     * This method returns silently if the specified listener is {@code null}
     * or has not been registered before.
     *
     * @param listener the listener to be removed (could be null)
     */
    void removeDiscoveryListener(DiscoveryListener listener);

}

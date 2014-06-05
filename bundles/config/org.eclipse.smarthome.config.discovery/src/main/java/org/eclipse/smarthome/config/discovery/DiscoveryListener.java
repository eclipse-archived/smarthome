package org.eclipse.smarthome.config.discovery;

import org.eclipse.smarthome.core.thing.ThingUID;


/**
 * The {@link DiscoveryListener} interface for receiving discovery events.
 * <p>
 * A class that is interested in processing discovery events fired synchronously
 * by a {@link DiscoveryService} has to implement this interface.
 *
 * @author Michael Grammling - Initial Contribution.
 *
 * @see DiscoveryService
 */
public interface DiscoveryListener {

    /**
     * Invoked synchronously when a {@link DiscoveryResult} has been created
     * by the according {@link DiscoveryService}.
     * <p>
     * <i>Hint:</i> This method could even be invoked for {@link DiscoveryResult}s,
     * whose existence has already been informed about.
     *
     * @param source the discovery service which is the source of this event (not null)
     * @param result the discovery result (not null)
     */
    void thingDiscovered(DiscoveryService source, DiscoveryResult result);

    /**
     * Invoked synchronously when an already existing {@code Thing} ID has been
     * marked to be deleted by the according {@link DiscoveryService}.
     * <p>
     * <i>Hint:</i> This method could even be invoked for {@link DiscoveryResult}s,
     * whose removal has already been informed about.
     *
     * @param source the discovery service which is the source of this event (not null)
     * @param thingUID the Thing UID to be removed (not null)
     */
    void thingRemoved(DiscoveryService source, ThingUID thingUID);

    /**
     * Invoked synchronously when the according discovery process has finished.
     * <p>
     * This method is only a result of an enforced discovery process. This signal
     * is sent latest when the amount of time for the discovery process has been
     * reached.
     *
     * @param source the discovery service which is the source of this event (not null)
     */
    void discoveryFinished(DiscoveryService source);

    /**
     * Invoked synchronously when the according discovery process has caused an error.
     * <p>
     * This method could only be a result of an enforced discovery process.
     *
     * @param source the discovery service which is the source of this event (not null)
     * @param exception the error which occurred (could be null)
     */
    void discoveryErrorOccurred(DiscoveryService source, Exception exception);

}

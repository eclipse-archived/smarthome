/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery;

import java.util.Collection;

import org.eclipse.smarthome.core.thing.ThingTypeUID;


/**
 * The {@link DiscoveryService} is a service interface which each binding can implement
 * to provide an auto discovery process for one or more {@code Thing}s.
 * <p>
 * As an example, a typical discovery mechanism could scan the network for <i>UPnP</i>
 * devices, if requested.
 * <p>
 * A {@link DiscoveryService} must be able to finish its discovery process without any
 * user interaction.
 * <p>
 * <b>There are two different kind of executions:</b>
 * <ul>
 * <li><b>Background discovery:</b> If this mode is enabled, the discovery process should 
 *   run in the background as long as this mode is not disabled again.</li>
 * <li><b>Active scan:</b> If an active scan is triggered, the the service should try to actively
 *   query for new devices and should report new results within the defined scan timeout. An active
 *   scan can be aborted.</li>
 * </ul>
 *
 * @author Michael Grammling - Initial Contribution.
 * @author Kai Kreuzer - Refactored API
 *
 * @see DiscoveryListener
 * @see DiscoveryServiceRegistry
 */
public interface DiscoveryService {

    /**
     * Returns the list of {@code Thing} types which are supported by the {@link DiscoveryService}.
     *
     * @return the list of Thing types which are supported by the discovery service
     *     (not null, could be empty)
     */
    public Collection<ThingTypeUID> getSupportedThingTypes();

    /**
     * Returns the amount of time in seconds after which an active scan ends.
     *
     * @return the scan timeout in seconds (>= 0).
     */
    public int getScanTimeout();

    /**
     * Enables the background discovery mode if {@code true} is set, otherwise it is disabled.
     * <p>
     * If enabled, any registered listener must be notified about {@link DiscoveryResult}s.
     *
     * @param enabled true if the background discovery mode should be enabled, otherwise false
     */
    void setBackgroundDiscoveryEnabled(boolean enabled);

    /**
     * Returns {@code true} if the background discovery mode is enabled, otherwise {@code false}.
     *
     * @return true if the background discovery mode is enabled, otherwise false
     */
    boolean isBackgroundDiscoveryEnabled();

    /**
     * Triggers this service to start an active scan for new devices.<br>
     * This method must not block any calls such as {@link #abortScan()} and
     * must return fast.
     * <p>
     * If started, any registered {@link DiscoveryListener} must be notified about {@link DiscoveryResult}s.
     * <p>
     * If there is already a scan running, it is aborted and a new scan is triggered.
     * 
     * @param listener a listener that is notified about errors or termination of the scan
     */
    void startScan(ScanListener listener);

    /**
     * Stops an active scan for devices.<br>
     * This method must not block any calls such as {@link #startScan()} and must
     * return fast.
     * <p>
     * After this method returns, no further notifications about {@link DiscoveryResult}s
     * are allowed to be sent to any registered listener, exceptional the background discovery
     * mode is active.
     * <p>
     * This method returns silently, if the scan has not been started before.
     */
    void abortScan();

    /**
     * Adds a {@link DiscoveryListener} to the listeners' registry.
     * <p>
     * When a {@link DiscoveryResult} could be created while the discovery process is active
     * (e.g. by forcing the startup of the discovery process or while enabling the auto
     * discovery mode), the specified listener is notified.
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
     * a created {@link DiscoveryResult} while the discovery process is active
     * (e.g. by forcing the startup of the discovery process or while enabling the
     * auto discovery mode)
     * <p>
     * This method returns silently if the specified listener is {@code null}
     * or has not been registered before.
     *
     * @param listener the listener to be removed (could be null)
     */
    void removeDiscoveryListener(DiscoveryListener listener);

}

/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.discovery;


/**
 * The {@link DiscoveryService} is a service interface which each binding can implement
 * to provide an auto discovery process for one or more {@code Thing}s.
 * <p>
 * As an example, a typical discovery mechanism could scan the network for <i>UPnP</i>
 * devices if requested.
 * <p>
 * A {@link DiscoveryService} must be able to finish its discovery process without any
 * user interaction.
 * <p>
 * <b>There are two different kind of executions:</b>
 * <ul>
 * <li><b>Auto-Discovery:</b> If this mode is enabled, the discovery process should automatically
 *   run in the background as long as this mode is disabled again. If the discovery process is
 *   enforced while the auto-discovery mode is disabled, the discovery process must still be
 *   active for a certain amount of time specified in its meta-information.</li>
 * <li><b>Forced-Discovery:</b> If the discovery process is enforced, the discovery process
 *   must be active for a certain amount of time specified in the meta-information. If the
 *   auto-discovery mode is disabled while the enforced discovery process is running, the
 *   certain amount of time must still be considered. An enforced discovery process can be
 *   aborted.</li>
 * </ul>
 *
 * @author Michael Grammling - Initial Contribution.
 *
 * @see DiscoveryListener
 * @see DiscoveryServiceRegistry
 */
public interface DiscoveryService {

    /**
     * Returns the meta-information about this service.
     *
     * @return the meta-information about this service (not null)
     */
    DiscoveryServiceInfo getInfo();

    /**
     * Enables the auto discovery mode if {@code true} is set, otherwise it is disabled.
     * <p>
     * If enabled, any registered listener must be notified about {@link DiscoveryResult}s.
     *
     * @param enabled true if the auto discovery mode should be enabled, otherwise false
     */
    void setAutoDiscoveryEnabled(boolean enabled);

    /**
     * Returns {@code true} if the auto discovery mode is enabled, otherwise {@code false}.
     *
     * @return true if the auto discovery mode is enabled, otherwise false
     */
    boolean isAutoDiscoveryEnabled();

    /**
     * Forces this service to start a discovery process over a specific amount of time
     * defined in the {@link DiscoveryServiceInfo}.<br>
     * This method must not block any calls such as {@link #abortForceDiscovery()} and
     * must return fast.
     * <p>
     * If started, any registered listener must be notified about {@link DiscoveryResult}s.
     * <p>
     * This method returns silently, if the discovery process has already been started before.
     */
    void forceDiscovery();

    /**
     * Aborts an already started discovery process.<br>
     * This method must not block any calls such as {@link #forceDiscovery()} and must
     * return fast.
     * <p>
     * After this method returns, no further notifications about {@link DiscoveryResult}s
     * are allowed to be sent to any registered listener, exceptional the auto discovery
     * mode is active.
     * <p>
     * This method returns silently, if the discovery process has not been started before.
     */
    void abortForceDiscovery();

    /**
     * Returns {@code true} if the discovery process has been forced to be started before,
     * while calling this method, otherwise {@code false}.
     *
     * @return true if the discovery process has been forced to be started, otherwise false
     */
    boolean isForced();

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

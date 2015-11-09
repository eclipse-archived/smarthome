/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.core.status;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import org.eclipse.smarthome.config.core.status.events.ConfigStatusInfoEvent;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.events.EventPublisher;

/**
 * The {@link ConfigStatusService} provides the {@link ConfigStatusInfo} for a specific entity. For this purpose
 * it loops over all registered {@link ConfigStatusProvider}s and returns the {@link ConfigStatusInfo} for the matching
 * {@link ConfigStatusProvider}.
 *
 * @author Thomas Höfer - Initial contribution
 */
public final class ConfigStatusService implements ConfigStatusCallback {

    private final List<ConfigStatusProvider> configStatusProviders = new CopyOnWriteArrayList<>();
    private EventPublisher eventPublisher;

    private final ExecutorService executorService = ThreadPoolManager
            .getPool(ConfigStatusService.class.getSimpleName());

    /**
     * Retrieves the {@link ConfigStatusInfo} of the entity by using the registered
     * {@link ConfigStatusProvider} that supports the given entity.
     *
     * @param entityId the id of the entity whose configuration status information is to be retrieved (must not
     *            be null or empty)
     * @param locale the locale to be used for the corresponding configuration status messages; if null then the
     *            default local will be used
     *
     * @return the {@link ConfigStatusInfo} or null if there is no {@link ConfigStatusProvider} registered that
     *         supports the given entity
     *
     * @throws IllegalArgumentException if given entityId is null or empty
     */
    public ConfigStatusInfo getConfigStatus(String entityId, Locale locale) {
        if (entityId == null || entityId.equals("")) {
            throw new IllegalArgumentException("EntityId must not be null or empty");
        }
        Locale loc = locale != null ? locale : Locale.getDefault();
        for (ConfigStatusProvider configStatusProvider : configStatusProviders) {
            if (configStatusProvider.supportsEntity(entityId)) {
                return configStatusProvider.getConfigStatus(loc);
            }
        }
        return null;
    }

    @Override
    public void configUpdated(final ConfigStatusSource configStatusSource) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                final ConfigStatusInfo info = getConfigStatus(configStatusSource.entityId, null);
                if (info != null) {
                    eventPublisher.post(new ConfigStatusInfoEvent(configStatusSource.getTopic(), info, null));
                }
            }
        });
    }

    protected void addConfigStatusProvider(ConfigStatusProvider configStatusProvider) {
        configStatusProvider.setConfigStatusCallback(this);
        configStatusProviders.add(configStatusProvider);
    }

    protected void removeConfigStatusProvider(ConfigStatusProvider configStatusProvider) {
        configStatusProvider.setConfigStatusCallback(null);
        configStatusProviders.remove(configStatusProvider);
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }
}

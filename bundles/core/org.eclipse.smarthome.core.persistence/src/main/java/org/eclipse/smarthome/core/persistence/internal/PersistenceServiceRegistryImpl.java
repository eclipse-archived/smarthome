/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.persistence.internal;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.persistence.PersistenceServiceRegistry;

/**
 * This is a central service for accessing {@link PersistenceService}s. It is registered through DS and also provides
 * config options for the UI.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class PersistenceServiceRegistryImpl implements ConfigOptionProvider, PersistenceServiceRegistry {

    private Map<String, PersistenceService> services = new HashMap<String, PersistenceService>();
    private String defaultServiceId = null;

    public PersistenceServiceRegistryImpl() {
    }

    public void addPersistenceService(PersistenceService service) {
        services.put(service.getId(), service);
    }

    public void removePersistenceService(PersistenceService service) {
        services.remove(service.getId());
    }

    protected void activate(Map<String, Object> config) {
        modified(config);
    }

    protected void modified(Map<String, Object> config) {
        if (config != null) {
            defaultServiceId = (String) config.get("default");
        }
    }

    @Override
    public PersistenceService getDefault() {
        return get(getDefaultId());
    }

    @Override
    public PersistenceService get(String serviceId) {
        if (serviceId != null) {
            return services.get(serviceId);
        } else {
            return null;
        }
    }

    @Override
    public String getDefaultId() {
        if (defaultServiceId != null) {
            return defaultServiceId;
        } else {
            // if there is exactly one service available in the system, we assume that this should be used, if no
            // default is specifically configured.
            if (services.size() == 1) {
                return services.keySet().iterator().next();
            } else {
                return null;
            }
        }
    }

    @Override
    public Set<PersistenceService> getAll() {
        return new HashSet<>(services.values());
    }

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        Set<ParameterOption> options = new HashSet<>();
        if (uri.toString().equals("system:persistence") && param.equals("default")) {
            for (PersistenceService service : getAll()) {
                options.add(new ParameterOption(service.getId(), service.getLabel(locale)));
            }
        }
        return options;
    }

}

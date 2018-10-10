/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.net.security.internal;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import javax.net.ssl.SSLContext;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.registry.AbstractRegistry;
import org.eclipse.smarthome.io.net.security.internal.providerImpl.SSLContextConfigurableProvider;
import org.eclipse.smarthome.io.net.security.internal.providerImpl.SSLContextConfigurableProviderConfig;
import org.eclipse.smarthome.io.net.security.internal.providerImpl.SelfSignedSSLContextProviderConfig;
import org.eclipse.smarthome.io.net.security.internal.utils.ProviderWithCapabilities;
import org.eclipse.smarthome.io.security.api.RequestSecureSocketProvider;
import org.eclipse.smarthome.io.security.api.SecureSocketCapability;
import org.eclipse.smarthome.io.security.api.SecureSocketRequest;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service is responsible for providing SSLContexts that can be used all over ESH
 * for server applications like webservlets, Mqtt servers or any extension that need to
 * provide for instance a REST interface.
 *
 * The SSLContext is configured to use either the user provided java keystore file
 * (`../etc/default.keystore`) or if none is provided to generate a self-signed certificate
 * and public/private key-pair.
 *
 * @author David Graeff - Initial contribution
 */
@Component(immediate = true, service = { SecureSocketProviderRegistry.class, RequestSecureSocketProvider.class })
@NonNullByDefault
public class SecureSocketProviderRegistry
        extends AbstractRegistry<ProviderWithCapabilities, Object, SSLContextConfigurableProvider>
        implements RequestSecureSocketProvider {
    protected SelfSignedSSLContextProviderConfig configuration = new SelfSignedSSLContextProviderConfig();
    protected SSLContextConfigurableProviderConfig defaultContext = new SSLContextConfigurableProviderConfig();
    protected Map<SecureSocketRequest, @Nullable ProviderWithCapabilities> consumers = new TreeMap<>();
    private final Logger logger = LoggerFactory.getLogger(SecureSocketProviderRegistry.class);

    public SecureSocketProviderRegistry() {
        super(SSLContextConfigurableProvider.class);
    }

    @Override
    protected void onAddElement(ProviderWithCapabilities element) throws IllegalArgumentException {
        updateConsumers(element);
    }

    @Override
    protected void onRemoveElement(ProviderWithCapabilities element) {
        updateConsumers(element);
    }

    @Override
    public void addProviderRequest(SecureSocketRequest consumer) {
        logger.trace("New consumer: {}", describeConsumer(consumer));
        consumers.put(consumer, null);
        updateConsumers(null);
    }

    @Override
    public void removeProviderRequest(SecureSocketRequest consumer) {
        logger.trace("Consumer removed: {}", describeConsumer(consumer));
        consumers.remove(consumer);
    }

    private String describeConsumer(SecureSocketRequest consumer) {
        StringBuffer b = new StringBuffer();
        b.append("Mandatory capabilities: ");
        consumer.mandatoryCapabilities().forEach(c -> b.append(c.name()).append(", "));
        b.append("Optional capabilities: ");
        consumer.optionalCapabilities().forEach(c -> b.append(c.name()).append(", "));
        return b.toString();
    }

    private void updateConsumers(@Nullable ProviderWithCapabilities service) {
        Iterator<Entry<SecureSocketRequest, @Nullable ProviderWithCapabilities>> iterator = consumers.entrySet()
                .iterator();
        while (iterator.hasNext()) {
            Entry<SecureSocketRequest, @Nullable ProviderWithCapabilities> entry = iterator.next();
            // If the backend provider service changed, let's lookup the secure socket provider again
            if (entry.getValue() == service || entry.getValue() == null) {
                entry.setValue(findProvider(entry.getKey()));
                notifyConsumer(entry.getKey(), entry.getValue());
            }
        }
    }

    // Notify consumer of changed secure socket provider
    private void notifyConsumer(SecureSocketRequest key, @Nullable ProviderWithCapabilities value) {
        if (value == null) {
            key.providerResponse(null);
        } else if (value.provider instanceof SSLContext) {
            key.providerResponse((SSLContext) value.provider);
        } else {
            key.providerResponse(value.provider);
        }
    }

    /** A comparator class for two ProviderWithCapabilities. Used for sorting based on optional capabilities. */
    private static class Comp implements Comparator<ProviderWithCapabilities> {
        final SecureSocketRequest key;

        Comp(SecureSocketRequest key) {
            this.key = key;
        }

        @Override
        public int compare(ProviderWithCapabilities a, ProviderWithCapabilities b) {
            HashSet<SecureSocketCapability> setA = new HashSet<SecureSocketCapability>(a.capabilities);
            setA.retainAll(key.optionalCapabilities());
            HashSet<SecureSocketCapability> setB = new HashSet<SecureSocketCapability>(b.capabilities);
            setA.retainAll(key.optionalCapabilities());
            return Integer.compare(setB.size(), setA.size()); /// Sort from big to small
        }

    }

    /**
     * Finds a provider that amongst all registered providers that fulfills all mandatory requested capabilities
     * and fulfills as many optional capabilities as possible.
     *
     * A call to this method is quite expensive. All providers need to be filtered and sorted. Sorting need a new
     * {@link Comp} instance for each comparison.
     *
     * @param key
     * @return
     */
    private @Nullable ProviderWithCapabilities findProvider(SecureSocketRequest key) {
        Optional<ProviderWithCapabilities> first = stream()
                .filter(p -> p.capabilities.containsAll(key.mandatoryCapabilities())).sorted(new Comp(key)).findFirst();
        return (first.isPresent()) ? first.get() : null;
    }
}

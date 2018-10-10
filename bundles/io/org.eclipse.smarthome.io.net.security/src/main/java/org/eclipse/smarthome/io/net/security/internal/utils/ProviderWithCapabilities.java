package org.eclipse.smarthome.io.net.security.internal.utils;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.common.registry.Identifiable;
import org.eclipse.smarthome.io.net.security.internal.providerImpl.SSLContextConfigurableProvider;
import org.eclipse.smarthome.io.security.api.SecureSocketCapability;

public class ProviderWithCapabilities implements Identifiable<@NonNull Object> {
    /** Usually a SSLContext but might be a Scandium DtlsConnector or anything else */
    public final Object provider;
    public final Set<SecureSocketCapability> capabilities;
    public final SSLContextConfigurableProvider service;

    public ProviderWithCapabilities(Object provider, Set<SecureSocketCapability> capabilities,
            SSLContextConfigurableProvider service) {
        this.provider = provider;
        this.capabilities = capabilities;
        this.service = service;
    }

    @Override
    public @NonNull Object getUID() {
        return provider;
    }
}
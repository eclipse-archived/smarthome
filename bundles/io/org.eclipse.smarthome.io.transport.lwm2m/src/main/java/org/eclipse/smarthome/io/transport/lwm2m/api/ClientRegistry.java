package org.eclipse.smarthome.io.transport.lwm2m.api;

import java.util.Collection;

public interface ClientRegistry {
    Collection<LwM2MClient> getClients();

    LwM2MClient getClient(String endpoint);
}

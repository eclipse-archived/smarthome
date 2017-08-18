package org.eclipse.smarthome.io.transport.lwm2m.api;

public interface ClientRegistryObserver {
    void clientRegistered(LwM2MClient client);

    void clientUnRegistered(LwM2MClient client);
}

package org.eclipse.smarthome.io.transport.lwm2m.api;

public interface ClientObserver {
    void objectAdded(int oID, LwM2MObject object);

    void objectRemoved(int oID, LwM2MObject object);

    void clientStatusChanged(ClientStatus status);
}

package org.eclipse.smarthome.io.transport.lwm2m.api;

public interface ObjectObserver {
    void instanceAdded(int iID, LwM2MObjectInstance instance);

    void instanceRemoved(int iID, LwM2MObjectInstance instance);
}

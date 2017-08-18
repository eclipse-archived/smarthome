package org.eclipse.smarthome.io.transport.lwm2m.api;

public interface ObjectInstanceObserver {
    void resourceAdded(int resID, LwM2MResource resource);

    void resourceRemoved(int resID, LwM2MResource resource);

    void resourceUpdated(int resID, LwM2MResource resource);
}

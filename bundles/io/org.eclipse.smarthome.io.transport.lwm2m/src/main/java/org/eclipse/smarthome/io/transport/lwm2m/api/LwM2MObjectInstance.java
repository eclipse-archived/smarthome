package org.eclipse.smarthome.io.transport.lwm2m.api;

public interface LwM2MObjectInstance {
    LwM2MObject getObject();

    int getObjectInstanceID();

    void registerObjectInstanceObserver(ObjectInstanceObserver observer);

    void unregisterObjectInstanceObserver(ObjectInstanceObserver observer);

    LwM2MResource getResource(int rID);
}

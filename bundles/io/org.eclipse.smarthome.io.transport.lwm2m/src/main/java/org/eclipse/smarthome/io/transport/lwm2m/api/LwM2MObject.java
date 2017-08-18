package org.eclipse.smarthome.io.transport.lwm2m.api;

import java.util.Collection;

public interface LwM2MObject {
    Collection<LwM2MObjectInstance> getObjectInstances();

    LwM2MObjectInstance getObjectInstance(int iID);

    void deleteObjectInstance(int iID, OperationCallback callback);

    void registerObjectObserver(ObjectObserver observer);

    void unregisterObjectObserver(ObjectObserver observer);
}

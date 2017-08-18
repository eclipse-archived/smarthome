package org.eclipse.smarthome.io.transport.lwm2m.api;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

public interface LwM2MResource {
    State getValue();

    void setValue(Command command, OperationCallback callback);
}

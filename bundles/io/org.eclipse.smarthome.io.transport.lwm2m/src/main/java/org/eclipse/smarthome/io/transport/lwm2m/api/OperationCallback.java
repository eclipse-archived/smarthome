package org.eclipse.smarthome.io.transport.lwm2m.api;

public interface OperationCallback {
    void success();

    void failure(Throwable error);
}

package org.eclipse.smarthome.binding.lwm2m.old;

import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.smarthome.core.thing.Thing;

public class ObjectInstance {
    private Registration client;
    private int object;
    private int instance = 0;
    private boolean hasInstances = false;

    public int getInstanceID() {
        return instance;
    }

    public ObjectInstance(Registration client, Thing thing) {
        this.setClient(client);
        this.object = Integer.valueOf(thing.getThingTypeUID().getId());
        this.instance = Integer.valueOf(thing.getUID().getId());
    }

    public ObjectInstance(Registration client, int object, int instance) {
        this.setClient(client);
        this.object = object;
        this.instance = instance;
    }

    boolean hasInstance() {
        return hasInstances;
    }

    public ObjectInstance(Registration client, String url) {
        this.setClient(client);
        String[] parts = url.split("/");
        object = Integer.valueOf(parts[0]);
        if (parts.length > 1) {
            instance = Integer.valueOf(parts[1]);
            hasInstances = true;
        }
    }

    public int getObjectID() {
        return object;
    }

    public Registration getClient() {
        return client;
    }

    public void setClient(Registration client) {
        this.client = client;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if ((obj instanceof ObjectInstance)) {
            ObjectInstance other = (ObjectInstance) obj;
            return other.getObjectID() == object && other.getInstanceID() == instance && other.getClient() == client;
        }
        if ((obj instanceof LwM2mPath)) {
            LwM2mPath other = (LwM2mPath) obj;
            return other.getObjectId() == object && other.getObjectInstanceId() == instance;
        }
        return false;
    }
}
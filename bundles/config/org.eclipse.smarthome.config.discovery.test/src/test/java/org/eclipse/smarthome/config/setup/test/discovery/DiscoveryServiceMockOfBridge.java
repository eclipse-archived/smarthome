package org.eclipse.smarthome.config.setup.test.discovery;

import java.util.Random;

import org.eclipse.smarthome.config.discovery.internal.DiscoveryResultImpl;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

public class DiscoveryServiceMockOfBridge extends DiscoveryServiceMock {

    ThingUID bridge;

    public DiscoveryServiceMockOfBridge(ThingTypeUID thingType, int timeout, ThingUID bridge) {
        super(thingType, timeout);
        this.bridge = bridge;
    }

    @Override
    public void startScan() {
        thingDiscovered(new DiscoveryResultImpl(new ThingUID(thingType, "test" + new Random().nextInt(999999999)),
                bridge, null, null, null, DEFAULT_TTL));
    }

    public ThingUID getBridge() {
        return bridge;
    }

}

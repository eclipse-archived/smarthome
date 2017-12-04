package org.eclipse.smarthome.config.setup.test.discovery

import org.eclipse.smarthome.config.discovery.internal.DiscoveryResultImpl
import org.eclipse.smarthome.core.thing.ThingUID

class DiscoveryServiceMockOfBridge extends DiscoveryServiceMock {

    ThingUID bridge

    public DiscoveryServiceMockOfBridge(Object thingType, Object timeout, ThingUID bridge) {
        super(thingType, timeout)
        this.bridge = bridge
    }

    @Override
    public void startScan() {
        if (faulty) {
            throw new Exception()
        }
        thingDiscovered(new DiscoveryResultImpl(new ThingUID(thingType, generator((('A'..'Z')+('0'..'9')).join(), 9)), bridge, null, null, null, DEFAULT_TTL))
    }
}

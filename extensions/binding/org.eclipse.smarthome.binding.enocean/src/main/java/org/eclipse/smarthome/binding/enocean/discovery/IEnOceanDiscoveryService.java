package org.eclipse.smarthome.binding.enocean.discovery;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.enocean.EnOceanDevice;

public interface IEnOceanDiscoveryService {

    public EnOceanDevice getEnOceanDevice(ThingUID thingUID);

}

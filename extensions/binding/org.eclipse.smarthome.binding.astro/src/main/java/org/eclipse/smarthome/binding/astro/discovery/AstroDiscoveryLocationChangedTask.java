package org.eclipse.smarthome.binding.astro.discovery;

import org.eclipse.smarthome.core.library.types.PointType;

public class AstroDiscoveryLocationChangedTask implements Runnable {

    private AstroDiscoveryService astroDiscoveryService;
    private PointType previousLocation;

    public AstroDiscoveryLocationChangedTask(AstroDiscoveryService astroDiscoveryService) {
        this.astroDiscoveryService = astroDiscoveryService;
    }

    @Override
    public void run() {
        PointType currentLocation = astroDiscoveryService.getLocationProvider().getLocation();
        if (currentLocation != previousLocation) {
            astroDiscoveryService.createResults(currentLocation);
            previousLocation = currentLocation;
        }
    }

}

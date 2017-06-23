package org.eclipse.smarthome.binding.astro.discovery;

import org.eclipse.smarthome.core.i18n.LocationProvider;
import org.eclipse.smarthome.core.library.types.PointType;

public class AstroDiscoveryLocationChangedTask implements Runnable {

    private AstroDiscoveryService astroDiscoveryService;
    private PointType previousLocation;
    private LocationProvider locationProvider;

    public AstroDiscoveryLocationChangedTask(AstroDiscoveryService astroDiscoveryService,
            LocationProvider locationProvider) {
        this.astroDiscoveryService = astroDiscoveryService;
        this.locationProvider = locationProvider;
    }

    @Override
    public void run() {
        PointType currentLocation = locationProvider.getLocation();
        if ((currentLocation != null) && !currentLocation.equals(previousLocation)) {
            astroDiscoveryService.createResults(currentLocation);
            previousLocation = currentLocation;
        }
    }

}

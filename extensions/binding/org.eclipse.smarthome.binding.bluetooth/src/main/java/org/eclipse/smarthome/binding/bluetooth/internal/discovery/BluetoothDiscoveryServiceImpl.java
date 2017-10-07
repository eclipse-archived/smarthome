package org.eclipse.smarthome.binding.bluetooth.internal.discovery;

import org.eclipse.smarthome.binding.bluetooth.BluetoothBindingConstants;
import org.eclipse.smarthome.binding.bluetooth.BluetoothDiscoveryService;
import org.eclipse.smarthome.binding.bluetooth.internal.BluetoothUtils;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.URL;
import org.sputnikdev.bluetooth.manager.*;

import java.util.HashSet;

/**
 * @author Vlad Kolotov
 */
@Component(immediate = true, service = DiscoveryService.class)
public class BluetoothDiscoveryServiceImpl extends AbstractDiscoveryService
        implements BluetoothDiscoveryService, DeviceDiscoveryListener, AdapterDiscoveryListener {

    private final static int DISCOVERY_RATE_SEC = 10;

    private final Logger logger = LoggerFactory.getLogger(BluetoothDiscoveryServiceImpl.class);
    private BluetoothManager bluetoothManager;

    public BluetoothDiscoveryServiceImpl() throws IllegalArgumentException {
        super(new HashSet<ThingTypeUID>() {{
            add(BluetoothBindingConstants.THING_TYPE_ADAPTER);
            add(BluetoothBindingConstants.THING_TYPE_GENERIC);
            add(BluetoothBindingConstants.THING_TYPE_BLE);
        }}, 0, true);
    }

    @Override
    public void discovered(DiscoveredDevice device) {
        ThingUID bridgeUID = BluetoothUtils.getAdapterUID(device.getURL());

        ThingUID thingUID = device.getBluetoothClass() == 0 ?
                BluetoothUtils.getBleDeviceUID(device.getURL()) : BluetoothUtils.getGenericDeviceUID(device.getURL());

        DiscoveryResultBuilder builder = DiscoveryResultBuilder
                .create(thingUID)
                .withLabel(device.getAlias() != null ? device.getAlias() : device.getName())
                .withTTL(DISCOVERY_RATE_SEC * 3)
                .withBridge(bridgeUID);

        thingDiscovered(builder.build());
    }

    @Override
    public void discovered(DiscoveredAdapter adapter) {
        thingDiscovered(DiscoveryResultBuilder
                .create(BluetoothUtils.getAdapterUID(adapter.getURL()))
                .withLabel(adapter.getAlias() != null ? adapter.getAlias() : adapter.getName())
                .withTTL(DISCOVERY_RATE_SEC * 3).build());
    }

    @Override
    public void adapterLost(URL url) {
        logger.info("Adapter lost: {}", url);
        thingRemoved(BluetoothUtils.getAdapterUID(url));
    }

    @Override
    public void deviceLost(URL url) {
        logger.info("Device lost: {}", url);
        thingRemoved(BluetoothUtils.getGenericDeviceUID(url));
        thingRemoved(BluetoothUtils.getBleDeviceUID(url));
    }

    @Override
    protected synchronized void startScan() {
    }

    @Override
    protected void startBackgroundDiscovery() {
        this.bluetoothManager.setDiscoveryRate(DISCOVERY_RATE_SEC);
        this.bluetoothManager.setRediscover(true);
        this.bluetoothManager.addAdapterDiscoveryListener(this);
        this.bluetoothManager.addDeviceDiscoveryListener(this);
        this.bluetoothManager.start(false);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        this.bluetoothManager.removeAdapterDiscoveryListener(this);
        this.bluetoothManager.removeDeviceDiscoveryListener(this);
        this.bluetoothManager.stop();
    }

    @Reference
    public void setBluetoothManager(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
        startBackgroundDiscovery();
    }

    protected void unsetBluetoothManager(BluetoothManager bluetoothManager) {
        if (this.bluetoothManager != null) {
            this.bluetoothManager.removeAdapterDiscoveryListener(this);
            this.bluetoothManager.removeDeviceDiscoveryListener(this);
            stopBackgroundDiscovery();
        }
        this.bluetoothManager = null;
    }

}

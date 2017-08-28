/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.bluetooth.internal;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.binding.bluetooth.BluetoothBindingConstants;
import org.eclipse.smarthome.binding.bluetooth.handler.AdapterHandler;
import org.eclipse.smarthome.binding.bluetooth.handler.BluetoothDeviceHandler;
import org.eclipse.smarthome.binding.bluetooth.handler.GenericBluetoothDeviceHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParser;
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParserFactory;
import org.sputnikdev.bluetooth.manager.BluetoothManager;
import org.sputnikdev.bluetooth.manager.impl.BluetoothManagerFactory;
import org.sputnikdev.bluetooth.manager.transport.BluetoothObjectFactory;
import org.sputnikdev.bluetooth.manager.impl.BluetoothObjectFactoryProvider;

import static org.eclipse.smarthome.binding.bluetooth.BluetoothBindingConstants.THING_TYPE_ADAPTER;
import static org.eclipse.smarthome.binding.bluetooth.BluetoothBindingConstants.THING_TYPE_BLE;
import static org.eclipse.smarthome.binding.bluetooth.BluetoothBindingConstants.THING_TYPE_GENERIC;


/**
 * The {@link BluetoothHandlerFactory} is responsible for creating things and thing
 * handlers.
 * 
 * @author Vlad Kolotov - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, name = "binding.bluetooth",
        configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class BluetoothHandlerFactory extends BaseThingHandlerFactory {
    
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS =
            Collections.unmodifiableSet(new HashSet<>(
                    Arrays.asList(THING_TYPE_ADAPTER, THING_TYPE_GENERIC, THING_TYPE_BLE)));

    private BluetoothManager bluetoothManager;
    private BluetoothGattParser gattParser;
    private ItemRegistry itemRegistry;

    private int initialOnlineTimeout = BluetoothBindingConstants.INITIAL_ONLINE_TIMEOUT;
    private boolean initialConnectionControl = BluetoothBindingConstants.INITIAL_CONNECTION_CONTROL;

    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        String extensionFolder =
                (String) properties.get(BluetoothBindingConstants.BINDING_CONFIG_EXTENSION_FOLDER);
        String refreshRate = (String) properties.get(BluetoothBindingConstants.BINDING_CONFIG_UPDATE_RATE);
        this.initialOnlineTimeout = NumberUtils.toInt(
                (String) properties.get(BluetoothBindingConstants.BINDING_CONFIG_INITIAL_ONLINE_TIMEOUT),
                BluetoothBindingConstants.INITIAL_ONLINE_TIMEOUT);
        this.initialConnectionControl = BooleanUtils.toBooleanDefaultIfNull((Boolean) properties.get(
                BluetoothBindingConstants.BINDING_CONFIG_INITIAL_CONNECTION_CONTROL),
                BluetoothBindingConstants.INITIAL_CONNECTION_CONTROL);
        if (StringUtils.isBlank(extensionFolder)) {
            extensionFolder = "/home/pi/.bluetooth_smart";
        }
        BundleContext bundleContext = componentContext.getBundleContext();
        bluetoothManager = BluetoothManagerFactory.getManager();
        if (refreshRate != null && NumberUtils.isNumber(refreshRate)) {
            bluetoothManager.setRefreshRate(NumberUtils.toInt(refreshRate));
        }
        gattParser = BluetoothGattParserFactory.getDefault();
        File extensionFolderFile = new File(extensionFolder);
        if (extensionFolderFile.exists() && extensionFolderFile.isDirectory()) {
            gattParser.loadExtensionsFromFolder(extensionFolder);
        }
        bundleContext.registerService(BluetoothManager.class.getName(), bluetoothManager, new Hashtable<>());
        bundleContext.registerService(BluetoothGattParser.class.getName(), gattParser, new Hashtable<>());
    }



    @Override
    protected void deactivate(ComponentContext componentContext) {
        BluetoothManagerFactory.dispose(bluetoothManager);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ADAPTER)) {
            return new AdapterHandler(thing, itemRegistry, bluetoothManager, gattParser);
        } else if (thingTypeUID.equals(THING_TYPE_GENERIC)) {
            GenericBluetoothDeviceHandler genericBluetoothDeviceHandler =
                    new GenericBluetoothDeviceHandler(thing, itemRegistry, bluetoothManager, gattParser);
            genericBluetoothDeviceHandler.setInitialOnlineTimeout(initialOnlineTimeout);
            return genericBluetoothDeviceHandler;
        } else if (thingTypeUID.equals(THING_TYPE_BLE)) {
            BluetoothDeviceHandler bluetoothDeviceHandler =
                    new BluetoothDeviceHandler(thing, itemRegistry, bluetoothManager, gattParser);
            bluetoothDeviceHandler.setInitialOnlineTimeout(initialOnlineTimeout);
            bluetoothDeviceHandler.setInitialConnectionControl(initialConnectionControl);
            return bluetoothDeviceHandler;
        }

        return null;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    @Reference(unbind = "unregisterBluetoothObjectFactory", cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC)
    public void registerBluetoothObjectFactory(BluetoothObjectFactory bluetoothObjectFactory) {
        BluetoothObjectFactoryProvider.registerFactory(bluetoothObjectFactory);
    }

    public void unregisterBluetoothObjectFactory(BluetoothObjectFactory bluetoothObjectFactory) {
        BluetoothObjectFactoryProvider.unregisterFactory(bluetoothObjectFactory);
    }
}


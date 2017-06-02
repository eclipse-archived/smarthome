# BLE Binding overview
 
The BLE binding is implemented to allow bundle fragments to extend the main BLE bundle to add new thing handlers, and new bridges. This architecture means that fragment bundles must follow specific naming to avoid conflicts when the fragments are merged, and must always utilise the binging name ```ble```.

A base class structure is defined in the org.eclipse.smarthome.binding.ble bundle. This includes the main classes required to implement BLE -:
 
* BleBaseBridgeHandler. This implements the main functionality required by a BLE bridge including management of devices that a bridge discovers.
* BleDevice. This implements a BLE device. It manages the notifications of device notifications, BLE service and characteristic management, and provides the main interface to communicate to a BLE device.
* BleService. Implements the BLE service. A service holds a number of characteristics.
* BleCharacteristic. Implements the BLE characteristic. This is the basic component for communicating data to and from a BLE device
* BleDescriptor. Implements the BLE descriptors for each characteristic.
 
## Implementing a new BleBridge bundle
 
A BLE bridge handler provides the link with a BLE master hardware (eg a dongle, or system Bluetooth API). The new bridge bundle needs to implement two main classes – the BridgeHandler which should implement BleBridgeApi, and a ThingFactory required to instantiate the handler.
 
The BleBridgeHandler must implement any functionality required to interface to the Bluetooth layer. It is responsible for managing the Bluetooth scanning, device discovery (ie the device interrogation to get the list of services and characteristics) and reading and writing of characteristics. The bridge needs to manage any interaction between the interface with any things it provides – this needs to account for any constraints that a interface may impose such that Things do not need to worry about any peculiarities imposed by a specific interface.

Classes such as BleCharacteristic or BleService may be extended to provide additional functionality to interface to a specific library if needed.

The BleBridgeHandler must register the BleDiscoveryService to allow BLE thing discovery.
 
## Implementing a BleThing bundle
 
A BLE thing handler provides the functionality required to interact with a specific BLE device. The new thing bundle needs to implement two main classes – the ThingHandler, and a ThingFactory required to instantiate the handler.
 
Two fundamental communications methods can be employed in BLE – beacons, and connected mode. A BLE thing handler can implement one or both of these communications. In practice, a connected mode Thing implementation would normally handle the beacons in order to provide as a minimum the RSSI data.

### Thing Filter

Things are defined in the XML files as per the ESH architecture. A filter has been defined in the Thing properties in the XML that is used to select the thing type. The filter allows selection of the thing type from data available in the beacon - the property name for this filter is ```bleFilter``` as per the example below.
 

```
        <properties>
            <property name="bleFilter">NAME=Yeelight Blue II</property>
        </properties>
```

The available filters are as follows -:

* MANUFACTURER: The manufacturer ID specified as a hexadecimal value
* NAME: Provides selection on the long or short name in the device beacon
* SVC:

Multiple filters can be separated with a comma. The system requires that all filters match before the thing type is selected.

### Thing Naming

To avoid naming conflicts with different bundle fragments a strict naming policy for things and thing xml files is proposed. This should use the bundle fragment name and the thing name, separated with an underscore - eg for the YeeLight binding Blue2 thing, the thing type is ```yeelight_blue2```.

 
### Connected Mode Implementation
 
The connected mode BleThingHandler needs to handle the following functionality
* Get the things bridge handler. This will implement the BleBridgeApi.
* Call the BleBridgeApi.getDevice() method to get the BleDevice class for the requested device. The getDevice() method will return a BleDevice class even if the device is not currently known.
* Implement the BleDeviceListener methods. These provide callbacks for various notifications regarding device updates – eg when the connection state of a device changes, when the device discovery is complete, when a read and write completes, and when beacon messages are received.
* Call the BleBridgeApi.connect() method to connect to the device. Once the device is connected, the BleDeviceListener.onConnectionStateChange() callback will be called.
* Call the BleBridgeApi.discoverServices() method to discover all the BleServices and BleCharacteristics implemented by the device. Once this is complete, the BleDeviceListener.onServicesDiscovered() callback will be called.
* Call the readCharacteristic or writeCharacteristic() methods to interact with the device. The BleDeviceListener.onCharacteristicReadComplete() and BleDeviceListener.onCharacteristicWriteComplete() methods will be called on completion.
* Implement the BleDeviceListener.onCharacteristicUpdate() method to process any read responses or unsolicited updates of a characteristic value.
 
### Beacon Mode Implementation
 
The beacon mode BleThingHandler needs to handle the following functionality

* Get the things bridge handler. This will implement the BleBridgeApi.
* Call the BleBridgeApi.getDevice() method to get the BleDevice class for the requested device. The getDevice() method will return a BleDevice class even if the device is not currently known.
* Implement the BleDeviceListener.onScanRecordReceived() method to process the beacons. The notification will provide the current receive signal strength (RSSI), the raw beacon data, and various elements of generally useful beacon data is provided separately.

---
layout: documentation
---

{% include base.html %}

# Implementing a Firmware Update Handler

Bindings can implement the `org.eclipse.smarthome.core.thing.firmware.FirmwareUpdateHandler` interface in order that the firmware for the physical devices of their things can be updated. If this interface is implemented by the corresponding `ThingHandler` implementation then the framework will take care of registering the `FirmwareUpdateHandler` automatically as an OSGi service.

The `FirmwareUpdateHandler` consists of the following operations:
* `getThing()`: Returns the thing that is handled by the firmware update handler. 
* `updateFirmware(org.eclipse.smarthome.core.thing.firmware.Firmware firmware, org.eclipse.smarthome.core.thing.firmware.ProgressCallback progressCallback)`: Updates the firmware for the physical device of the thing that is handled by the firmware update handler. The given progress callback can be used to notify the framework of a new progress step that is going to be executed.
* `isUpdateExecutable()`: Returns true if the firmware update handler is in a state in which the firmware update for the device can be executed,
otherwise false (e.g. if the thing is offline or if its status detail is already firmware updating).

The thing returned by the `getThing()` operation must have set the `org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION` property. If it is not set then the framework cannot determine the thing´s `org.eclipse.smarthome.core.thing.firmware.FirmwareStatus`.

## Implementing the updateFirmware Operation

Initially the implementation should change the thing status to `org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE` and the thing status detail to `org.eclipse.smarthome.core.thing.ThingStatusDetail.FIRMWARE_UPDATING`. Afterwards if the handler would like to notify the framework of update progress information then the sequence of the firmware update progress steps has to be defined using the `defineSequence` operation of the given callback interface. Then by using the `next` operation of the callback interface the handler can indicate that the next progress step is going to be executed.

```java 
    public void updateFirmware(Firmware firmware, ProgressCallback progressCallback) {
        // if thing is handled by a thing handler
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.FIRMWARE_UPDATING);
        
        // define the sequence of the firmware update so that external consumers can listen for the progress
        progressCallback.defineSequence(ProgressStep.DOWNLOADING, ProgressStep.TRANSFERRING, ProgressStep.UPDATING);
        
        // download / read firmware image
        progressCallback.next();
        ... read input stream from firmware.getContent();
        
        // transfer image to device
        progressCallback.next();
        ... transfer image to device
        
        // triggering the actual firmware update
        progressCallback.next();
        ... trigger update
        
        // here: send immediately the success information because it is not mandatory for this implementation to wait for the successful update of the device
        progressCallback.success();
        // or: if required to check whether the firmware update was successful or erroneous then wait or spawn a new thread
    }
```

Finally the handler has to acknowledge in either case if the firmware update was successful or not. A successful firmware update is notified by the operation `progressCallback.success()` and an erroneous firmware update is notified by the operation `progressCallback.failed(String errorMessageKey, Object... arguments)`. In case of an erroneous firmware update the handler has to provide the error message key of the error message and optional arguments that are to be injected into the error message. The framework will take care of localizing the corresponding error message. Furthermore the framework will notify a firmware update failure if the update firmware operation takes longer than 30 minutes or if an unexpected exception occurred.

## Using a Firmware Update Background Transfer Handler

There are devices which require that the firmware of the device is already transferred to the device before the actual firmware update can be executed. For this purpose there is the interface `org.eclipse.smarthome.core.thing.firmware.FirmwareUpdateBackgroundTransferHandler` which extends the `org.eclipse.smarthome.core.thing.firmware.FirmwareUpdateHandler`. For these handlers there is only the additional operation `transferFirmware(org.eclipse.smarthome.core.thing.firmware.Firmware firmware)` to be implemented. This operation is invoked by the framework if there is a newer firmware available and if the `isUpdateExecutable()` returned false (false because there is no firmware on the device available that can be updated). The implementation should transfer the firmware to the device and return true for `isUpdateExecutable()` as soon as the transfer is completed. Then the framework is able to invoke the `updateFirmware` operation in order that the firmware of the device can be updated. Because for these handlers the firmware is already downloaded and transferred the firmware update sequence should not contain these progress steps.

# Updating the Firmware

The sections above explained what binding developers have to do in order that the firmware of a thing can be updated. The following sections will show how the firmware update can be executed from a client perspective.

## The Firmware

A `org.eclipse.smarthome.core.thing.firmware.Firmware` relates always to exactly one `org.eclipse.smarthome.core.thing.type.ThingType`. By its `org.eclipse.smarthome.core.thing.firmware.FirmwareUID` it is ensured that there is only one firmware in a specific version for a thing type available. 

Firmwares are made available to the system by `org.eclipse.smarthome.core.thing.firmware.FirmwareProvider`s that are tracked by the `org.eclipse.smarthome.core.thing.firmware.FirmwareRegistry`. The registry can be used to get a dedicated firmware, to get the latest firmware or to get all available firmwares for a specific thing type. 

The firmware implements the `Comparable` interface in order to be able to sort firmwares based on their versions. Firmwares are sorted in a descending sequence, i.e. that the latest firmware will be the first element in a sorted result set. The `compareTo` implementation of the firmware splits the firmware version by the delimiters ".", "-" and "\_" and compares the different parts of the firmware version. As a result the firmware version 2-0-1 is newer then firmware version 2.0.0 which again is newer than firmware version 1-9\_9.9\_abc. Consequently 2.0-0, 2-0\_0 and 2\_0.0 represent the same firmware version. Furthermore firmware version xyz\_1 is newer than firmware version abc.2 which again is newer than firmware version 2-0-1.
 
## Executing the Firmware Update

The `org.eclipse.smarthome.core.thing.firmware.FirmwareUpdateService` is registered as an OSGi service and is among others responsible for tracking all available `org.eclipse.smarthome.core.thing.firmware.FirmwareUpdateHandler`s. It is the central instance to start a firmware update and provides two public operations:

* `getFirmwareStatusInfo(org.eclipse.smarthome.core.thing.ThingUID thingUID)`: Returns the `org.eclipse.smarthome.core.thing.firmware.FirmwareStatusInfo` for the thing having the given thing UID. The firmware status info provides besides the UID of the latest updatable firmware the actual `org.eclipse.smarthome.core.thing.firmware.FirmwareStatus` which can be one of the following types:
  ** UNKNOWN: The firmware status can not be determined and hence it is unknown. Either the `org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION` property is not set for the thing or there is no `org.eclipse.smarthome.core.thing.firmware.FirmwareProvider` that provides a firmware for the thing type of the thing.
  ** UP\_TO\_DATE:  The firmware of the thing is up to date.
  ** UPDATE\_AVAILABLE: There is a newer firmware for the thing available. However the thing or the device is not in a state where its firmware can be updated, i.e. the operation `isUpdateExecutable()` returned false.
  ** UPDATE\_EXECUTABLE: There is a newer firmware of the thing available and the firmware update for the thing can be executed.
* `updateFirmware(org.eclipse.smarthome.core.thing.ThingUID thingUID, org.eclipse.smarthome.core.thing.firmware.FirmwareUID firmwareUID, java.util.Locale locale)`: Updates the firmware of the thing having the given thing UID by invoking the operation `updateFirmware` operation of the thing´s firmware update handler. The locale is used in order to internationalize possible error messages.

## The File System Firmware Provider

The firmware extension bundle (`/extensions/firmware/org.eclipse.smarthome.firmware.filesystem`) contains a `FirmwareProvider` that is able to provide firmwares from the file system. This provider retrieves the firmwares from the `{userdir}/firmware` directory which must follow a given folder structure: a sub folder for each binding and within the binding folders a sub folder for each thing type. 

``` 
{userdir}
   +-firmware
          +-{binding-id1}
                  +-{thing-type-id1}
                  +-{thing-type-id2}
          +-{binding-id2}
                  +-{thing-type-id3}
                  +-{thing-type-id4}
``` 

For each firmware the binary image file must be placed in the corresponding directory and additionally at least one property file containing the following values: 

```  
# the firmware version
version = V1.2-b18
# the name of the corresponding image file
image = imageV12b18.bin
# (optional) the model name
model = Model A
# (optional) the vendor
vendor = Company Ltd.
# (optional) a description
description = This is a camera.
# (optional) the change log 
changelog = This is the change log
# (optional) an URL of the online change log
onlineChangelog = http://www.company.com
# (optional) the prerequisite version of the firmware
prerequisiteVersion = V1.2-b12
# (optional) the MD5 hash of the image file
md5Hash = 1421abc212f
``` 

Additional property files may be placed into this directory with localized values for description, changelog and onlineChangelog (e.g. *_de.properties). 
  


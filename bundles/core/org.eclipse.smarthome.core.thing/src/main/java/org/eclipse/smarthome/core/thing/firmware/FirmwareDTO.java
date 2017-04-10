package org.eclipse.smarthome.core.thing.firmware;

public class FirmwareDTO {
    public String firmwareUID;
    public String vendor;
    public String model;
    public String description;
    public String version;
    public String changelog;
    public String prerequisiteVersion;

    public FirmwareDTO() {

    }

    public FirmwareDTO(String firmwareUID, String vendor, String model, String description, String version,
            String prerequisiteVersion, String changelog) {
        this.firmwareUID = firmwareUID;
        this.vendor = vendor;
        this.model = model;
        this.description = description;
        this.version = version;
        this.prerequisiteVersion = prerequisiteVersion;
        this.changelog = changelog;
    }
}

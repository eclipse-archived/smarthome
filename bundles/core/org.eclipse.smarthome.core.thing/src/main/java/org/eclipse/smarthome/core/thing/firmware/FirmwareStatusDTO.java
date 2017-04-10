package org.eclipse.smarthome.core.thing.firmware;

public class FirmwareStatusDTO {
    public String status;
    public String updatableVersion;

    public FirmwareStatusDTO(String status, String updatableVersion) {
        this.status = status;
        this.updatableVersion = updatableVersion;
    }
}

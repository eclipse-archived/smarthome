package org.eclipse.smarthome.core.thing.profiles;

public class ProfileTypeMapper {

    public ProfileTypeDTO map(ProfileType profileType) {
        ProfileTypeDTO dto = new ProfileTypeDTO();
        dto.supportedItemTypes = profileType.getSupportedItemTypes();
        return dto;
    }
}

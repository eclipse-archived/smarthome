package org.eclipse.smarthome.tools.docgenerator.data;

import org.eclipse.smarthome.tools.docgenerator.models.ConfigDescription;

public class ConfigurationList extends ModelList {
    /**
     * @return Returns a new {@link ConfigDescription} object.
     */
    @Override
    public ConfigDescription getNewModel() {
        return new ConfigDescription();
    }
}

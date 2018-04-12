package org.eclipse.smarthome.automation;

import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.config.core.Configuration;

public interface Module {

    /**
     * This method is used for getting the id of the {@link Module}. It is unique
     * in scope of the {@link Rule}.
     *
     * @return module id
     */
    String getId();

    /**
     * This method is used for getting the reference to {@link ModuleType} of this
     * module. The {@link ModuleType} contains description, tags and meta info for
     * this module.
     *
     * @return unique id of the {@link ModuleType} of this {@link Module}.
     */
    String getTypeUID();

    /**
     * This method is used for getting the label of the Module. The label is a
     * short, user friendly name of the Module.
     *
     * @return the label of the module or null.
     */
    String getLabel();

    /**
     * This method is used for getting the description of the Module. The
     * description is a long, user friendly description of the Module.
     *
     * @return the description of the module or null.
     */
    String getDescription();

    /**
     * This method is used for getting configuration values of the {@link Module}.
     *
     * @return current configuration values or null.
     */
    Configuration getConfiguration();

}
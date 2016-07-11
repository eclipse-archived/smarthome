package org.eclipse.smarthome.tools.docgenerator.data;

import org.eclipse.smarthome.tools.docgenerator.models.Parameter;

public class ParameterList extends ModelList {
    /**
     * @return Returns a new {@link Parameter} object.
     */
    @Override
    public Parameter getNewModel() {
        return new Parameter();
    }
}

package org.eclipse.smarthome.tools.docgenerator.data;

import org.eclipse.smarthome.tools.docgenerator.models.Option;

public class OptionList extends ModelList {
    /**
     * @return Returns a new {@link Option} object.
     */
    @Override
    public Option getNewModel() {
        return new Option();
    }
}

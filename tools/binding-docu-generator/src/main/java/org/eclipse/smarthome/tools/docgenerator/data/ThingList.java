package org.eclipse.smarthome.tools.docgenerator.data;

import org.eclipse.smarthome.tools.docgenerator.models.Thing;

public class ThingList extends ModelList {
    /**
     * Returns a new {@link Thing} object.
     *
     * @return
     */
    @Override
    public Thing getNewModel() {
        return new Thing();
    }
}

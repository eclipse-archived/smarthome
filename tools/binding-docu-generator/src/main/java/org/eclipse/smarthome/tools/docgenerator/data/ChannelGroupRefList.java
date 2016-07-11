package org.eclipse.smarthome.tools.docgenerator.data;

import org.eclipse.smarthome.tools.docgenerator.models.ChannelGroupRef;

public class ChannelGroupRefList extends ModelList {
    /**
     * @return Returns a new {@link ChannelGroupRef} object.
     */
    @Override
    public ChannelGroupRef getNewModel() {
        return new ChannelGroupRef();
    }
}

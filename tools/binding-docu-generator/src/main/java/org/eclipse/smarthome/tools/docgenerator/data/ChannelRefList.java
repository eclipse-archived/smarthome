package org.eclipse.smarthome.tools.docgenerator.data;

import org.eclipse.smarthome.tools.docgenerator.models.ChannelRef;

public class ChannelRefList extends ModelList {
    /**
     * @return Returns a new {@link ChannelRef} object.
     */
    @Override
    public ChannelRef getNewModel() {
        return new ChannelRef();
    }
}

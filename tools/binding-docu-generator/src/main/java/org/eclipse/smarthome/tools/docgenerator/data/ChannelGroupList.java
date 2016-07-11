package org.eclipse.smarthome.tools.docgenerator.data;

import org.eclipse.smarthome.tools.docgenerator.models.ChannelGroup;

public class ChannelGroupList extends ModelList {
    /**
     * @return Returns a new {@link ChannelGroup} object.
     */
    @Override
    public ChannelGroup getNewModel() {
        return new ChannelGroup();
    }
}

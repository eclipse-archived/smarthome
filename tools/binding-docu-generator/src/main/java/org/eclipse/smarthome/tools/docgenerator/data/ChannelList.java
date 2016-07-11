package org.eclipse.smarthome.tools.docgenerator.data;

import org.eclipse.smarthome.tools.docgenerator.models.Channel;

public class ChannelList extends ModelList {
    /**
     * @return Returns a new {@link Channel} object.
     */
    @Override
    public Channel getNewModel() {
        return new Channel();
    }
}


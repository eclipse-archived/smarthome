package org.eclipse.smarthome.core.thing;

public class ChannelTypeUID extends UID {

    public ChannelTypeUID(String channelUid) {
        super(channelUid);
    }

    public ChannelTypeUID(String bindingId, String id) {
        super(bindingId, id);
    }

    @Override
    protected int getNumberOfSegments() {
        return 2;
    }

    public String getId() {
        return getSegment(1);
    }

}

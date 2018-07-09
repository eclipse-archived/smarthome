package org.eclipse.smarthome.binding.lwm2m.old;

import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

// Thing ID pattern: bindingID/thingTypeId/bridge0..n/thingID/channelID
// For lwm2m with a pattern of /endpoint/objectid/objectinstanceid/resourceid/resourceinstanceid
// * the endpoint is the last bridgeID,
// * the objectid is the thingTypeId (without the bindingId),
// * the objectinstanceid is the thingID,
// * the resourceid is the channelID if no channelGroupId otherwise the channelGroupId,
// * the resourceinstanceid is default 0. If there is a channelGroupID, the channelID is the instanceID.
public class Lwm2mUID {
    private static final String BINDING_ID = null;

    public static String getEndpoint(Thing thing) {
        return thing instanceof Bridge ? thing.getUID().getId() : thing.getBridgeUID().getId();
    }

    public static int getResourceID(ChannelUID channelUid) {
        if (channelUid.isInGroup()) {
            return Integer.valueOf(channelUid.getGroupId());
        } else {
            return Integer.valueOf(channelUid.getId());
        }
    }

    public static int getResourceIDinstance(ChannelUID channelUid) {
        if (channelUid.isInGroup()) {
            return Integer.valueOf(channelUid.getIdWithoutGroup());
        } else {
            return 0;
        }
    }

    public static String getChannelID(LwM2mResource value, int resourceInstance) {
        if (value.isMultiInstances() && resourceInstance >= 0) {
            return String.valueOf(resourceInstance) + "#" + String.valueOf(value.getId());
        }
        return String.valueOf(value.getId());
    }

    public static ThingTypeUID getThingTypeUID(int objectID) {
        return new ThingTypeUID(BINDING_ID, String.valueOf(objectID));
    }

    public static ChannelUID createChannelUID(ThingUID uid, String channelID) {
        return new ChannelUID(uid, channelID);
    }

}

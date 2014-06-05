package org.eclipse.smarthome.core.thing;



/**
 * {@link ThingTypeUID} represents a unique identifier for thing types.
 */
public class ThingTypeUID extends UID {

    public ThingTypeUID(String uid) {
        super(uid);
    }

    public ThingTypeUID(String bindingId, String thingTypeId) {
        super(bindingId, thingTypeId);
    }

    @Override
    protected int getNumberOfSegments() {
        return 2;
    }

    public String getId() {
        return getSegment(1);
    }
}

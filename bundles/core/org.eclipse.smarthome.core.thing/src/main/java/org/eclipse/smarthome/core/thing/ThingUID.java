package org.eclipse.smarthome.core.thing;



/**
 * {@link ThingUID} represents a unique identifier for things.
 */
public class ThingUID extends UID {

    /**
     * Instantiates a new thing UID.
     * 
     * @param thingType
     *            the thing type
     * @param id
     *            the id
     */
    public ThingUID(ThingTypeUID thingTypeUID, String id) {
        super(thingTypeUID.getBindingId(), thingTypeUID.getId(), id);
    }

    /**
     * Instantiates a new thing UID.
     * 
     * @param bindingId
     *            the binding id
     * @param thingTypeId
     *            the thing type id
     * @param id
     *            the id
     */
	public ThingUID(String bindingId, String thingTypeId, String id) {
        super(bindingId, thingTypeId, id);
	}

    /**
     * Instantiates a new thing UID.
     * 
     * @param thingUID
     *            the thing UID
     */
	public ThingUID(String thingUID) {
        super(thingUID);
	}

    /**
     * Returns the thing type id.
     * 
     * @return thing type id
     */
    public String getThingTypeId() {
        return getSegment(1);
	}

    /**
     * Returns the id.
     * 
     * @return id
     */
	public String getId() {
        return getSegment(2);
	}

    @Override
    protected int getNumberOfSegments() {
        return 3;
    }

}

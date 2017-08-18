package org.openhab.binding.lwm2mleshan.internal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingFactory;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.TypeResolver;
import org.openhab.binding.lwm2mleshan.lwm2mLeshanBindingConstants;

public class CreateThings {
    public static void createThing(ThingRegistry thingRegistry, int objectID, int objectInstanceID) {
        ThingTypeUID thingTypeUID = Lwm2mUID.getThingTypeUID(objectID);
        ThingUID thingUID = new ThingUID(thingTypeUID, String.valueOf(objectInstanceID));
        Thing newThing = thingRegistry.get(thingUID);
        if (newThing == null) {
            // Is the thing type known? (OMA Registered lwm2m object)
            ThingType thingType = TypeResolver.resolve(thingTypeUID);
            if (thingType == null) {
                thingType = new ThingType(lwm2mLeshanBindingConstants.BINDING_ID, String.valueOf(objectID),
                        "Custom object #" + String.valueOf(objectID));
            }
            newThing = ThingFactory.createThing(thingType, thingUID, new Configuration());
            thingRegistry.add(newThing);
        }
    }
}

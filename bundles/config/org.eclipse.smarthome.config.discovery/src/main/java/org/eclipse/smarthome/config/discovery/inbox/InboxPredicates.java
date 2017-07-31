package org.eclipse.smarthome.config.discovery.inbox;

import java.util.function.Predicate;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultFlag;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * Implements static factory methods for {@link Predicate}s to filter in streams of {@link DiscoveryResult}s.
 *
 * @author Andre Fuechsel - Initial Contribution
 */
public class InboxPredicates {

    public static Predicate<DiscoveryResult> forBinding(String bindingId) {
        return r -> bindingId != null && bindingId.equals(r.getBindingId());
    }

    public static Predicate<DiscoveryResult> forThingTypeUID(ThingTypeUID uid) {
        return r -> uid != null && uid.equals(r.getThingTypeUID());
    }

    public static Predicate<DiscoveryResult> forThingUID(ThingUID thingUID) {
        return r -> thingUID != null && thingUID.equals(r.getThingUID());
    }

    public static Predicate<DiscoveryResult> withFlag(DiscoveryResultFlag flag) {
        return r -> flag == r.getFlag();
    }

    public static Predicate<DiscoveryResult> withProperty(String propertyName, String propertyValue) {
        return r -> r.getProperties().containsKey(propertyName)
                && r.getProperties().get(propertyName).equals(propertyValue);
    }

    public static Predicate<DiscoveryResult> withRepresentationProperty(String propertyName) {
        return r -> propertyName != null && propertyName.equals(r.getRepresentationProperty());
    }

    public static Predicate<DiscoveryResult> withRepresentationPropertyValue(String propertyValue) {
        return r -> propertyValue != null && propertyValue.equals(r.getProperties().get(r.getRepresentationProperty()));
    }
}

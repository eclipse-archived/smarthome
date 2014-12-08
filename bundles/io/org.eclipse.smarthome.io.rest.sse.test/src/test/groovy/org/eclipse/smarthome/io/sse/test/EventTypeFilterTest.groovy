package org.eclipse.smarthome.io.sse.test;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.io.rest.sse.EventType
import org.junit.Test

class EventTypeFilterTest {

    private static final String THINGS_OBJECT = "things";

    private static final String ITEMS_OBJECT = "items";

    private static final String ADDED_EVENT = "added";

    private static final String SH_NAMESPACE = "smarthome";
    @Test
    public void wildcardTest() {
        testFilter("*/*/*", EventType.VALUES_LIST)
        testFilter("*/*", EventType.VALUES_LIST)
        testFilter("*", EventType.VALUES_LIST)
        testFilter("", EventType.VALUES_LIST)
    }

    @Test
    public void namespaceTest() {
        testFilter(SH_NAMESPACE, getAllEventsByNamespace(SH_NAMESPACE))
        testFilter(SH_NAMESPACE+"/", getAllEventsByNamespace(SH_NAMESPACE))
        testFilter(SH_NAMESPACE+"/*", getAllEventsByNamespace(SH_NAMESPACE))
        testFilter(SH_NAMESPACE+"/*/*", getAllEventsByNamespace(SH_NAMESPACE))
    }

    @Test
    public void invalidTest() {
        //if filter tokens are more or less than the expected number we return all events
        testFilter("///////", EventType.VALUES_LIST)
        testFilter("asd/fghj/sdh/sdhd/dfj/dfj/dj", EventType.VALUES_LIST)
        testFilter("*/*/"+ADDED_EVENT+"/*", EventType.VALUES_LIST)

        //if filter can be parsed to 1 argument "@@@@" should return no values
        testFilter("//////////////@@@@", [])
    }

    @Test
    public void eventObjectTest() {
        def thingEvents = getAllEventsByEventObject(THINGS_OBJECT);
        testFilter("*/"+THINGS_OBJECT+"/*", thingEvents)
        testFilter("*/"+THINGS_OBJECT+"/", thingEvents)
        testFilter("*/"+THINGS_OBJECT, thingEvents)

        def smarthomeThingEvents = getAllEventsByNamespace(SH_NAMESPACE).findAll { THINGS_OBJECT.equals(it.eventObject) }

        testFilter(SH_NAMESPACE+"/"+THINGS_OBJECT, smarthomeThingEvents)
        testFilter(SH_NAMESPACE+"/"+THINGS_OBJECT+"/", smarthomeThingEvents)
        testFilter(SH_NAMESPACE+"/"+THINGS_OBJECT+"/*", smarthomeThingEvents)
    }

    @Test
    public void eventTypeTest() {
        def addedEvents = getAllEventsByEventType(ADDED_EVENT);

        testFilter("*/*/"+ADDED_EVENT, addedEvents)
        testFilter("*/*/"+ADDED_EVENT+"/", addedEvents)

        def smarthomeAddedEvents = addedEvents.findAll() { SH_NAMESPACE.equals(it.eventNamespace) }

        testFilter(SH_NAMESPACE+"/*/"+ADDED_EVENT, smarthomeAddedEvents)
        testFilter(SH_NAMESPACE+"/*/"+ADDED_EVENT+"/", smarthomeAddedEvents)

        def smarthomeThingAddedEvents = smarthomeAddedEvents.findAll() { THINGS_OBJECT.equals(it.eventObject) }

        testFilter(SH_NAMESPACE+"/" + THINGS_OBJECT + "/"+ADDED_EVENT, smarthomeThingAddedEvents)
        testFilter(SH_NAMESPACE+"/" + THINGS_OBJECT + "/"+ADDED_EVENT+"/", smarthomeThingAddedEvents)
    }


    @Test
    public void fullNameTest() {
        assertThat((getFullName(EventType.THING_ADDED) + "/test").equals(EventType.THING_ADDED.getFullNameWithIdentifier("test")), is(true));
    }

    @Test
    public void multipleFilterTest() {
        def itemAndThingsEvents = getAllEventsByEventObject(THINGS_OBJECT)
        itemAndThingsEvents.addAll(getAllEventsByEventObject(ITEMS_OBJECT))

        testFilter("*/"+THINGS_OBJECT+",*/"+ITEMS_OBJECT, itemAndThingsEvents)
        testFilter("*/"+THINGS_OBJECT+",*/"+ITEMS_OBJECT+",*", EventType.VALUES_LIST)
    }

    private void testFilter(filter,expected)  {
        def setExpected = expected as Set
        def filtered = EventType.getEventTopicByFilter(filter) as Set

        assertThat(filtered.equals(setExpected), is(true))
    }

    private List getAllEventsByNamespace(namespace) {
        EventType.VALUES_LIST.findAll { namespace.equals(it.eventNamespace) }
    }

    private List getAllEventsByEventType(eventType) {
        EventType.VALUES_LIST.findAll { eventType.equals(it.eventType) }
    }

    private List getAllEventsByEventObject(eventObject) {
        EventType.VALUES_LIST.findAll { eventObject.equals(it.eventObject) }
    }

    private String getFullName(EventType event) {
        StringBuilder builder = new StringBuilder();
        builder.append(event.eventNamespace);
        builder.append(event.FILTER_SEPARATOR);
        builder.append(event.eventObject);
        if (!event.eventType.isEmpty()) {
            builder.append(event.FILTER_SEPARATOR);
            builder.append(event.eventType);
        }

        return builder.toString();
    }
}

/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.item.tests

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.items.events.ItemAddedEvent
import org.eclipse.smarthome.core.items.events.ItemRemovedEvent
import org.eclipse.smarthome.core.items.events.ItemUpdatedEvent
import org.eclipse.smarthome.model.core.ModelRepository
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test

import com.google.common.collect.Sets

/**
 *
 * @author Alex Tugarev - Initial Contribution
 * @author Andre Fuechsel
 * @author Michael Grammling
 * @author Simon Kaufmann
 * @author Stefan Triller - Added test for ItemAddedEvents with multiple model files
 *
 */
class GenericItemProviderTest extends OSGiTest {


    private final static String TESTMODEL_NAME = "testModel.items"
    private final static String TESTMODEL_NAME2 = "testModel2.items"

    ModelRepository modelRepository
    ItemRegistry itemRegistry

    @Before
    void setUp() {
        itemRegistry = getService ItemRegistry
        assertThat itemRegistry, is(notNullValue())
        modelRepository = getService ModelRepository
        assertThat modelRepository, is(notNullValue())
        modelRepository.removeModel(TESTMODEL_NAME)
        modelRepository.removeModel(TESTMODEL_NAME2)
    }

    @After
    void tearDown() {
        modelRepository.removeModel(TESTMODEL_NAME)
        modelRepository.removeModel(TESTMODEL_NAME2)
    }

    @Test
    void 'assert that items from test model were added to item registry'() {
        def items = itemRegistry.getAll()
        assertThat items.size(), is(0)

        String model =
                '''
            Group Weather [TAG1]
            Group Weather_Chart (Weather)
            Number Weather_Temperature      "Outside Temperature [%.1f °C]" <temperature> (Weather_Chart) [TAG1, TAG2] { channel="yahooweather:weather:berlin:temperature" }
            Number Weather_Temp_Max         "Todays Maximum [%.1f °C]"  <temperature> (Weather_Chart)
            Number Weather_Temp_Min         "Todays Minimum [%.1f °C]"  <temperature> (Weather_Chart)
            Number Weather_Chart_Period     "Chart Period"
            DateTime Weather_LastUpdate     "Last Update [%1$ta %1$tR]" <clock> [TAG1, TAG2, TAG3]
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        def actualItems = itemRegistry.getAll()

        assertThat actualItems.size(), is(7)
    }

    @Test
    void 'assert that items have tags if specified'() {
        def items = itemRegistry.getAll()
        assertThat items.size(), is(0)

        String model =
                '''
            DateTime Weather_LastUpdate     "Last Update [%1$ta %1$tR]" <clock> [TAG1, TAG2, TAG3, TAG4-WITH-DASHES, "TAG5 String Tag"]
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        def actualItems = itemRegistry.getAll()

        assertThat actualItems.size(), is(1)

        def lastItem = actualItems.last()
        assertThat lastItem.getTags().sort().join(", "), is(equalTo("TAG1, TAG2, TAG3, TAG4-WITH-DASHES, TAG5 String Tag"))
    }

    @Test
    void 'assert that a broken model is ignored'() {
        assertThat itemRegistry.getAll().size(), is(0)

        String model =
                '''
            String test "Test Item [%s]" { channel="test:test:test:test" }
            String {something is wrong} test "Test Item [%s]" { channel="test:test:test:test" }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))

        assertThat itemRegistry.getAll().size(), is(0)
    }

    @Test
    void 'assert that items are removed correctly if the model gets broken'() {
        assertThat itemRegistry.getAll().size(), is(0)

        String model =
                '''
            String test1 "Test Item [%s]" { channel="test:test:test:test" }
            String test2 "Test Item [%s]" { channel="test:test:test:test" }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))

        assertThat itemRegistry.getAll().size(), is(2)

        model =
                '''
            String test1 "Test Item [%s]" { channel="test:test:test:test" }
            String {something is wrong} test "Test Item [%s]" { channel="test:test:test:test" }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))

        waitForAssert {
            assertThat itemRegistry.getAll().size(), is(0)
        }

        model =
                '''
            String test1 "Test Item [%s]" { channel="test:test:test:test" }
            String test2 "Test Item [%s]" { channel="test:test:test:test" }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))

        assertThat itemRegistry.getAll().size(), is(2)
    }

    @Test
    void 'assert that item events are sent correctly'() {
        List<Event> receivedEvents = new ArrayList<>()
        def itemEventSubscriber = [
            receive: { event -> receivedEvents.add(event) },
            getSubscribedEventTypes: { Sets.newHashSet(ItemAddedEvent.TYPE, ItemUpdatedEvent.TYPE, ItemRemovedEvent.TYPE) },
            getEventFilter: {},
        ] as EventSubscriber
        registerService(itemEventSubscriber)

        assertThat itemRegistry.getAll().size(), is(0)

        receivedEvents.clear()
        String model =
                '''
            String test1 "Test Item [%s]" { channel="test:test:test:test" }
            String test2 "Test Item [%s]" { channel="test:test:test:test" }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))

        waitForAssert {
            assertThat itemRegistry.getAll().size(), is(2)
            assertThat receivedEvents.size(), is(2)
            assertThat receivedEvents.find {it.getItem().name.equals("test1")}, isA(ItemAddedEvent)
            assertThat receivedEvents.find {it.getItem().name.equals("test2")}, isA(ItemAddedEvent)
        }

        receivedEvents.clear()
        model =
                '''
            String test1 "Test Item Changed [%s]" { channel="test:test:test:test" }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))

        waitForAssert {
            assertThat itemRegistry.getAll().size(), is(1)
            assertThat receivedEvents.size(), is(2)
            assertThat receivedEvents.find {it.getItem().name.equals("test1")}, isA(ItemUpdatedEvent)
            assertThat receivedEvents.find {it.getItem().name.equals("test2")}, isA(ItemRemovedEvent)
        }

        receivedEvents.clear()
        model =
                '''
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))

        waitForAssert {
            assertThat itemRegistry.getAll().size(), is(0)
            assertThat receivedEvents.size(), is(1)
            assertThat receivedEvents.find {it.getItem().name.equals("test1")}, isA(ItemRemovedEvent)
        }
    }

    @Test
    void 'assert that item events are sent only once per item even with multiple item files'() {
        List<Event> receivedEvents = new ArrayList<>()
        def itemEventSubscriber = [
            receive: { event -> receivedEvents.add(event) },
            getSubscribedEventTypes: { Sets.newHashSet(ItemAddedEvent.TYPE, ItemUpdatedEvent.TYPE, ItemRemovedEvent.TYPE) },
            getEventFilter: {},
        ] as EventSubscriber
        registerService(itemEventSubscriber)

        assertThat itemRegistry.getAll().size(), is(0)

        receivedEvents.clear()
        String model =
                '''
            String test1 "Test Item [%s]" { channel="test:test:test:test" }
            String test2 "Test Item [%s]" { channel="test:test:test:test" }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))

        waitForAssert {
            assertThat itemRegistry.getAll().size(), is(2)
            assertThat receivedEvents.size(), is(2)
            assertThat receivedEvents.find {it.getItem().name.equals("test1")}, isA(ItemAddedEvent)
            assertThat receivedEvents.find {it.getItem().name.equals("test2")}, isA(ItemAddedEvent)
        }

        receivedEvents.clear()

        model =
                '''
            String test3 "Test Item [%s]" { channel="test:test:test:test" }
            String test4 "Test Item [%s]" { channel="test:test:test:test" }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME2, new ByteArrayInputStream(model.bytes))

        //only ItemAddedEvents for items test3 and test4 should be fired, NOT for test1 and test2 again
        waitForAssert {
            assertThat itemRegistry.getAll().size(), is(4)
            assertThat receivedEvents.size(), is(2)
            assertThat receivedEvents.find {it.getItem().name.equals("test3")}, isA(ItemAddedEvent)
            assertThat receivedEvents.find {it.getItem().name.equals("test4")}, isA(ItemAddedEvent)
        }
    }

    @Test
    void 'assert that the itemRegistry gets the same instance on item updates without changes'() {
        assertThat itemRegistry.getAll().size(), is(0)

        String model =
                '''
            String test1 "Test Item [%s]" { channel="test:test:test:test" }
            String test2 "Test Item [%s]" { channel="test:test:test:test" }
            String test3 "Test Item [%s]" { channel="test:test:test:test" }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        assertThat itemRegistry.getAll().size(), is(3)
        def unchangedItem = itemRegistry.getItem("test1")

        model =
                '''
            String test1 "Test Item [%s]" { channel="test:test:test:test" }
            String test2 "Test Item Changed [%s]" { channel="test:test:test:test" }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        assertThat itemRegistry.getAll().size(), is(2)
        assertThat itemRegistry.getItem("test1").is(unchangedItem), is(true)

        model =
                '''
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        assertThat itemRegistry.getAll().size(), is(0)

        model =
                '''
            String test1 "Test Item [%s]" { channel="test:test:test:test" }
            '''
        modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
        assertThat itemRegistry.getAll().size(), is(1)
        assertThat itemRegistry.getItem("test1").is(unchangedItem), is(false)
    }
}

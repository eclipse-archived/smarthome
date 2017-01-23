/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.item.tests

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.model.core.ModelRepository
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test


class GenericItemProviderTest extends OSGiTest {


    private final static String TESTMODEL_NAME = "testModel.items"

    ModelRepository modelRepository
    ItemRegistry itemRegistry

    @Before
    void setUp() {
        itemRegistry = getService ItemRegistry
        assertThat itemRegistry, is(notNullValue())
        modelRepository = getService ModelRepository
        assertThat modelRepository, is(notNullValue())
        modelRepository.removeModel(TESTMODEL_NAME)
    }

    @After
    void tearDown() {
        modelRepository.removeModel(TESTMODEL_NAME)
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
}

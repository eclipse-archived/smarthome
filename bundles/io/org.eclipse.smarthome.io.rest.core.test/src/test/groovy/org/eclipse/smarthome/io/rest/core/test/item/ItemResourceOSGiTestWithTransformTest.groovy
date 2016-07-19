/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.test.item

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import javax.ws.rs.core.UriInfo

import org.eclipse.smarthome.core.items.ItemProvider
import org.eclipse.smarthome.core.items.ManagedItemProvider
import org.eclipse.smarthome.core.library.items.NumberItem
import org.eclipse.smarthome.core.library.types.DecimalType
import org.eclipse.smarthome.core.types.StateDescription
import org.eclipse.smarthome.core.types.StateDescriptionProvider
import org.eclipse.smarthome.core.types.StateOption
import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTO
import org.eclipse.smarthome.io.rest.core.item.ItemResource
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.BundleContext


/**
 * This test checks whether REST resource supports items where transformation will be applied.
 * The bundle org.eclipse.smarthome.core.transform has an optional package dependency so that we have to make sure
 * that code is working with and without org.eclipse.smarthome.core.transform bundle.
 *
 * Note: run the test with transform bundle NOT included in launch configuration NOR in pom file.
 *
 * TODO add tests where a real transformation will be done by adding a transformation service
 * TODO add an automated test case for run with and without transform bundle
 *
 * @author Jochen Hiller - Initial contribution
 */
class ItemResourceOSGiTestWithTransformTest extends OSGiTest {

    def ItemResource itemResource
    def ManagedItemProvider managedItemProvider
    def List<StateDescriptionProvider> stateDescriptionProviders

    @Before
    void setUp() {
        registerVolatileStorageService()
        managedItemProvider = getService ManagedItemProvider
        itemResource = getService ItemResource
        itemResource.uriInfo = [
            getPath: { return "path" },
            getBaseUri: { return new URI("uri") }
        ] as UriInfo

        // setup a state description provider to be used for an item
        def stateDescriptionProvider = [
            getStateDescription: { itemName, locale ->
                return new StateDescription(0, 100, 10, "%d °C", true, [
                    new StateOption("SOUND", "My great sound.")
                ])
            }
        ] as StateDescriptionProvider
        stateDescriptionProviders = new ArrayList<StateDescriptionProvider>();
        stateDescriptionProviders.add(stateDescriptionProvider)
    }

    @After
    void tearDown() {
        managedItemProvider.getAll().each {
            managedItemProvider.remove(it.name)
        }
    }

    /**
     * Tests whether a rest call be done for an item where state and state description are given.
     * This will result in use of the TransformatioHelper class.
     * This has to work with transform bundle included in runtime and with missing transform bundle.
     */
    @Test
    void 'assert getItems with transform bundle works'() {
        def NumberItem item1 = new NumberItem("Item1")
        item1.setState(new DecimalType("12.34"))

        def itemProvider = [
            getAll: { return [item1]},
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
        ] as ItemProvider
        registerService itemProvider

        // set after adding to ItemProvider. Why?
        item1.setStateDescriptionProviders(stateDescriptionProviders)

        def enrichedDTOResult = itemResource.getItems(null, null, null, false).getEntity()
        def EnrichedItemDTO enrichedDTO = enrichedDTOResult.find() { itemBean ->
            itemBean.name == "Item1"
        }
        assertThat enrichedDTO, is(notNullValue())
        assertThat enrichedDTO.name, is("Item1")
        assertThat enrichedDTO.state, is("12.34")
        assertThat enrichedDTO.stateDescription.minimum, is(BigDecimal.valueOf(0))
        assertThat enrichedDTO.stateDescription.maximum, is(BigDecimal.valueOf(100))
        assertThat enrichedDTO.stateDescription.step, is(BigDecimal.valueOf(10))
        assertThat enrichedDTO.stateDescription.pattern, is("%d °C")
        assertThat enrichedDTO.stateDescription.options[0].value, is("SOUND")
        assertThat enrichedDTO.stateDescription.options[0].label, is("My great sound.")
    }
}

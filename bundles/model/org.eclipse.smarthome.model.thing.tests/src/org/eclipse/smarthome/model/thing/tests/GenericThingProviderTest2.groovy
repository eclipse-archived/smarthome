package org.eclipse.smarthome.model.thing.tests;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.thing.test.hue.TestHueThingHandlerFactoryX
import org.eclipse.smarthome.test.OSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.component.ComponentContext;

class GenericThingProviderTest2 extends OSGiTest{
	
	private final static String TESTMODEL_NAME = "testModelX.things"

	ModelRepository modelRepository;
	ThingRegistry thingRegistry;
	
	@Before
	public void setUp() {
		thingRegistry = getService ThingRegistry
		assertThat thingRegistry, is(notNullValue())
		modelRepository = getService ModelRepository
		assertThat modelRepository, is(notNullValue())
		modelRepository.removeModel(TESTMODEL_NAME)
	}

	@After
	public void tearDown() {
		modelRepository.removeModel(TESTMODEL_NAME);
	}
	
	@Test
	public void 'assert that things are created and not removed on adding_removing triggerHandlerFactories'() {
		def componentContextMock = [
			getBundleContext: {getBundleContext()}
			] as ComponentContext
		
		def hueThingHandlerFactory = new TestHueThingHandlerFactoryX(componentContextMock)
		
		def things = thingRegistry.getAll()
		assertThat things.size(), is(0)

		String model =
			'''
			Bridge Xhue:Xbridge:myBridge [ XipAddress = "1.2.3.4", XuserName = "123" ] {
				XLCT001 bulb1 [ XlightId = "1" ] { Switch : notification }
				Bridge Xbridge myBridge2 [ ] {
					XLCT001 bulb2 [ ]
				}
			}
            Xhue:XTEST:bulb4 [ XlightId = "5"]{
                Switch : notification [ duration = "5" ]
            }
	
			Xhue:XLCT001:bulb3 [ XlightId = "4" ] {
				Switch : notification [ duration = "5" ]
			}
			'''
		modelRepository.addOrRefreshModel(TESTMODEL_NAME, new ByteArrayInputStream(model.bytes))
		def actualThings = thingRegistry.getAll()

		assertThat actualThings.size(), is(0)
		
		registerService(hueThingHandlerFactory, ThingHandlerFactory.class.name)
		
		actualThings = thingRegistry.getAll()
		assertThat thingRegistry.getAll().size(), is(6)
		
		unregisterService(hueThingHandlerFactory)
		
		assertThat  thingRegistry.getAll().size(), is(6)
		
		registerService(hueThingHandlerFactory, ThingHandlerFactory.class.name)
		
		assertThat thingRegistry.getAll().size(), is(6)
		
		def thingHF2 = new TestHueThingHandlerFactoryX(componentContextMock)
		
		registerService(thingHF2, ThingHandlerFactory.class.name)
		
		assertThat thingRegistry.getAll().size(), is(6)
	}

}

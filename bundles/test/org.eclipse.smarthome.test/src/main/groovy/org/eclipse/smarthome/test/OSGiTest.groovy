package org.eclipse.smarthome.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.junit.After
import org.junit.Before
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceRegistration;

abstract class OSGiTest {

    static BundleContext bundleContext
    def Map<String, ServiceRegistration<?>>registeredServices = [:]

    protected abstract BundleContext getBundleContext()

    @Before
    void bindBundleContext() {
        bundleContext = getBundleContext()
        assertThat bundleContext, is(notNullValue())
    }

    def <T> T getService(Class<T> clazz){
        def serviceReference = bundleContext.getServiceReference(clazz.name)
        assertThat serviceReference, is(notNullValue())
        return bundleContext.getService(serviceReference)
    }

    def registerMock(def mock, Hashtable properties = [:]) {
        def interfaceName = getInterfaceName(mock)
        assertThat interfaceName, is(notNullValue())
        registeredServices.put(interfaceName, bundleContext.registerService(interfaceName, mock, properties))
    }

    def unregisterMock(def mock) {
		def interfaceName = getInterfaceName(mock)
        registeredServices.get(interfaceName).unregister()
        registeredServices.remove(interfaceName)
    }
	
	def getInterfaceName(def mock) {
		mock.class.interfaces?.find({it})?.name
	}

    @After
    void unregisterMocks(){
        registeredServices.each() { interfaceName, service ->
            service.unregister()
        }
        registeredServices.clear()
    }
}

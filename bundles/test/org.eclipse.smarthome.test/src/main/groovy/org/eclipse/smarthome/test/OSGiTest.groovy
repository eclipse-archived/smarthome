package org.eclipse.smarthome.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.junit.After
import org.junit.Before
import org.osgi.framework.BundleContext
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * {@link OSGiTest} is an abstract base class for OSGi based tests. It provides 
 * convenience methods to register and unregister mocks as OSGi services. 
 * 
 * @author Dennis Nobel - Initial contribution
 */
abstract class OSGiTest {

    static BundleContext bundleContext
    def Map<String, ServiceRegistration<?>>registeredServices = [:]

    @Before
    public void bindBundleContext() {
        bundleContext = getBundleContext()
        assertThat bundleContext, is(notNullValue())
    }
	
	protected BundleContext getBundleContext() {
		return FrameworkUtil.getBundle(this.getClass()).getBundleContext()
	}

    protected <T> T getService(Class<T> clazz){
        def serviceReference = bundleContext.getServiceReference(clazz.name)
        return bundleContext.getService(serviceReference)
    }

    /**
     * Registers the given service as OSGi service. The first interface is used as OSGi service 
     * interface name.
     * @param service service to be registered
     * @return service registration object
     */
    protected registerService(def service, Hashtable properties = [:]) {
        def interfaceName = getInterfaceName(service)
        assertThat interfaceName, is(notNullValue())
        registeredServices.put(interfaceName, bundleContext.registerService(interfaceName, service, properties))
    }
	
	protected registerService(def service, String interfaceName, Hashtable properties = [:]) {
		assertThat interfaceName, is(notNullValue())
		registeredServices.put(interfaceName, bundleContext.registerService(interfaceName, service, properties))
	}

    protected unregisterService(def service) {
		def interfaceName = service instanceof String ?: getInterfaceName(service)
        registeredServices.get(interfaceName).unregister()
        registeredServices.remove(interfaceName)
    }
	
	protected waitFor(Closure<?> condition, int timeout = 1000, int sleepTime = 50) {
		def waitingTime = 0
		while(!condition() && waitingTime < timeout) {
			waitingTime += sleepTime
			sleep sleepTime
		}
	}

	protected waitForAssert(Closure<?> assertion, int timeout = 1000, int sleepTime = 50) {
		def waitingTime = 0
		while(waitingTime < timeout) {
			try {
				assertion()
				return
			} catch(Error error) {
				waitingTime += sleepTime
				sleep sleepTime
			}
		}
		assertion()
	}
	
	protected getInterfaceName(def service) {
		service.class.interfaces?.find({it})?.name
	}

    @After
    public void unregisterMocks() {
        registeredServices.each() { interfaceName, service ->
            service.unregister()
        }
        registeredServices.clear()
    }
}

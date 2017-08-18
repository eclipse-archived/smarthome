/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import java.util.concurrent.TimeUnit

import org.eclipse.smarthome.core.autoupdate.AutoUpdateBindingConfigProvider
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.osgi.framework.BundleContext
import org.osgi.framework.FrameworkUtil
import org.osgi.framework.ServiceReference
import org.osgi.framework.ServiceRegistration

/**
 * {@link OSGiTest} is an abstract base class for OSGi based tests. It provides
 * convenience methods to register and unregister mocks as OSGi services. All services, which
 * are registered through the {@link OSGiTest#registerService} methods, are unregistered
 * automatically in the tear down of the test.
 *
 * @author Dennis Nobel - Initial contribution
 */
abstract class OSGiTest {

    BundleContext bundleContext
    Map<String, ServiceRegistration> registeredServices = [:]

    @Before
    public void bindBundleContext() {
        bundleContext = getBundleContext()
        assertThat bundleContext, is(notNullValue())
    }

    /**
     * Returns the {@link BundleContext}, which is used for registration and unregistration of OSGi
     * services. By default it uses the bundle context of the test class itself. This method can be overridden
     * by concrete implementations to provide another bundle context.
     *
     * @return bundle context
     */
    protected BundleContext getBundleContext() {
        def bundle = FrameworkUtil.getBundle(this.getClass())
        return bundle?.getBundleContext()
    }

    /**
     * Returns an OSGi service for the given class.
     *
     * @param clazz class under which the OSGi service is registered
     * @return OSGi service or null if no service can be found for the given class
     */
    protected <T> T getService(Class<T> clazz){
        def serviceReference = bundleContext.getServiceReference(clazz.name)
        return serviceReference ? bundleContext.getService(serviceReference) : null
    }

    /**
     * Returns an OSGi service for the given class and the given filter.
     *
     * @param clazz class under which the OSGi service is registered
     * @param filter
     * @return OSGi service or null if no service can be found for the given class
     */
    protected <T> T getService(Class<T> clazz, Closure<Boolean> filter){
        def serviceReferences = bundleContext.getServiceReferences(clazz.name, null)
        def filteredServiceReferences = serviceReferences.findAll(filter)

        if (filteredServiceReferences.size() > 1) {
            Assert.fail("More than 1 service matching the filter is registered.")
        }
        if (filteredServiceReferences.empty) {
            null
        } else {
            bundleContext.getService(filteredServiceReferences.first())
        }
    }

    /**
     * Returns an OSGi service for the given class and the given filter.
     *
     * @param clazz class under which the OSGi service is registered
     * @param filter
     * @return OSGi service or null if no service can be found for the given class
     */
    protected <T,I extends T> I getService(Class<T> clazz, Class<I> implementationClass){
        (I) getService(clazz, { ServiceReference<?> serviceReference ->
            implementationClass.isAssignableFrom(bundleContext.getService(serviceReference).getClass())
        })
    }

    /**
     * Registers the given object as OSGi service. The first interface is used as OSGi service
     * interface name.
     *
     * @param service service to be registered
     * @param properties OSGi service properties
     * @return service registration object
     */
    protected registerService(def service, Hashtable properties = [:]) {
        def interfaceName = getInterfaceName(service)
        assertThat interfaceName, is(notNullValue())
        registeredServices.put(interfaceName, bundleContext.registerService(interfaceName, service, properties))
    }

    /**
     * Registers the given object as OSGi service. The given interface name as String is used as OSGi service
     * interface name.
     *
     * @param service service to be registered
     * @param interfaceName interface name of the OSGi service
     * @param properties OSGi service properties
     * @return service registration object
     */
    protected registerService(def service, String interfaceName, Hashtable properties = [:]) {
        assertThat interfaceName, is(notNullValue())
        registeredServices.put(interfaceName, bundleContext.registerService(interfaceName, service, properties))
    }

    protected registerService(def service, String[] interfaceNames, Hashtable properties = [:]) {
        assertThat interfaceNames, is(notNullValue())
        def ServiceRegistration ref = bundleContext.registerService(interfaceNames, service, properties)
        for (String i : interfaceNames) {
            registeredServices.put(i, ref)
        }
    }

    /**
     * Unregisters an OSGi service by the given object, that was registered before. If the object is
     * a String the service is unregistered by the interface name. If the given service parameter is
     * the service itself the interface name is taken from the first interface of the service object.
     *
     * @param service service or service interface name
     * @return the service registration that was unregistered or null if no service could be found
     */
    protected unregisterService(def service) {
        def interfaceName = service instanceof String ? service : getInterfaceName(service)
        ServiceRegistration reg = registeredServices.remove(interfaceName)
        if (reg != null) {
            reg.unregister()
        }
        Iterator<ServiceRegistration> regs = registeredServices.values().iterator()
        for (ServiceRegistration otherReg : regs) {
            if (otherReg == reg) {
                regs.remove()
            }
        }
    }

    /**
     * When this method is called it waits until the condition is fulfilled or the timeout is reached.
     * The condition is specified by a closure, that must return a boolean object. When the condition is
     * not fulfilled Thread.sleep is called at the current Thread for a specified time. After this time
     * the condition is checked again. By a default the specified sleep time is 50 ms. The default timeout
     * is 10000 ms.
     *
     * @param condition closure that must not have an argument and must return a boolean value
     * @param timeout timeout, default is 10000ms
     * @param sleepTime interval for checking the condition, default is 50ms
     */
    protected void waitFor(Closure<?> condition, int timeout = 10000, int sleepTime = 50) {
        def waitingTime = 0
        while(!condition() && waitingTime < timeout) {
            waitingTime += sleepTime
            sleep sleepTime
        }
    }

    /**
     * When this method is called it waits until the assertion is fulfilled or the timeout is reached.
     * The assertion is specified by a closure, that must throw an Exception, if the assertion is not fulfilled.
     * When the assertion is not fulfilled Thread.sleep is called at the current Thread for a specified time.
     * After this time the condition is checked again. By a default the specified sleep time is 50 ms.
     * The default timeout is 10000 ms.
     *
     * @param assertion closure that must not have an argument
     * @param timeout timeout, default is 10000ms
     * @param sleepTime interval for checking the condition, default is 50ms
     */
    protected void waitForAssert(Closure<?> assertion, int timeout = 10000, int sleepTime = 50) {
        waitForAssert(assertion, null, timeout, sleepTime)
    }

    /**
     * When this method is called it waits until the assertion is fulfilled or the timeout is reached.
     * The assertion is specified by a closure, that must throw an Exception, if the assertion is not fulfilled.
     * When the assertion is not fulfilled Thread.sleep is called at the current Thread for a specified time.
     * After this time the condition is checked again. By a default the specified sleep time is 50 ms.
     * The default timeout is 10000 ms.
     *
     * @param assertion closure that must not have an argument
     * @param beforeLastCall close that must not have an arugment and should be executed in front of the last call to ${code assertion}.
     * @param timeout timeout, default is 10000ms
     * @param sleepTime interval for checking the condition, default is 50ms
     */
    protected void waitForAssert(Closure<?> assertion, Closure<?> beforeLastCall, long timeout = 10000, int sleepTime = 50) {
        final long timeoutNs = TimeUnit.MILLISECONDS.toNanos(timeout);
        final long startingTime = System.nanoTime();
        while((System.nanoTime() - startingTime) < timeoutNs) {
            try {
                assertion()
                return
            } catch(Error | NullPointerException error) {
                sleep sleepTime
            }
        }
        if (beforeLastCall != null) {
            beforeLastCall()
        }
        assertion()
    }

    /**
     * Returns the interface name for a given service object by choosing the first interface.
     *
     * @param service service object
     * @return name of the first interface or null if the object has no interfaces
     */
    protected getInterfaceName(def service) {
        service.class.interfaces?.find({ it })?.name
    }

    /**
     * Registers a volatile storage service.
     */
    protected void registerVolatileStorageService() {
        registerService(new VolatileStorageService());
    }

    @After
    public void unregisterMocks() {
        registeredServices.each() { interfaceName, service ->
            service.unregister()
        }
        registeredServices.clear()
    }

    protected void disableItemAutoUpdate(){
        def autoupdateConfig = [
            autoUpdate: { String itemName -> return false }

        ] as AutoUpdateBindingConfigProvider
        registerService(autoupdateConfig)
    }

    protected void setDefaultLocale(Locale locale) {
        assertThat locale, is(notNullValue())

        def configAdmin = getService(Class.forName("org.osgi.service.cm.ConfigurationAdmin"))
        assertThat configAdmin, is(notNullValue())

        def localeProvider = getService(Class.forName("org.eclipse.smarthome.core.i18n.LocaleProvider"))
        assertThat localeProvider, is(notNullValue())

        def config = configAdmin.getConfiguration("org.eclipse.smarthome.core.i18nprovider", null)
        assertThat config, is(notNullValue())

        def properties = config.getProperties()
        if (properties == null) {
            properties = new Hashtable()
        }

        properties.put("language", locale.getLanguage())
        properties.put("script", locale.getScript())
        properties.put("region", locale.getCountry())
        properties.put("variant", locale.getVariant())

        config.update(properties)

        waitForAssert {
            assertThat localeProvider.getLocale(), is(locale)
        }
    }
}

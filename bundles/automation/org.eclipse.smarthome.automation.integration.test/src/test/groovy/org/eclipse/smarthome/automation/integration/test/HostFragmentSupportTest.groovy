/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.integration.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.automation.type.ModuleTypeRegistry
import org.eclipse.smarthome.core.autoupdate.AutoUpdateBindingConfigProvider
import org.eclipse.smarthome.core.storage.StorageService
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.FrameworkEvent
import org.osgi.framework.FrameworkListener
import org.osgi.service.packageadmin.PackageAdmin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This test class tests the Host - Fragment support for automation and localization resources
 * @author Ana Dimova - initial contribution
 */
@SuppressWarnings("deprecation")
class HostFragmentSupportTest extends OSGiTest{

    final Logger logger = LoggerFactory.getLogger(HostFragmentSupportTest.class)
    def ModuleTypeRegistry moduleTypeRegistry
    def PackageAdmin pkgAdmin

    def EXT = ".jar"
    def PATH = "src/test/resources/"
    def RESOURCES_TEST_BUNDLE_1 = "host-tb1"
    def RESOURCES_TEST_BUNDLE_2 = "host-tb2"
    def RESOURCES_TEST_BUNDLE_3 = "fragment-tb1"
    def RESOURCES_TEST_BUNDLE_4 = "fragment-tb2"

    def trigger1 = "Trigger1"
    def trigger2 = "Trigger2"
    def condition1 = "Condition1"
    def condition2 = "Condition2"
    def action1 = "Action1"

    def trigger1LabelBG = "Тригер 1 Етикет"
    def trigger1LabelDE = "Abzugshahn 1 Etikette"
    def trigger1LabelUpdatedBG = "Тригер 1 Обновен Етикет"
    def trigger1LabelUpdatedDE = "Abzugshahn 1 Aktualisiert Etikette"

    def trigger2LabelBG = "Тригер 2 Етикет"
    def trigger2LabelDE = "Abzugshahn 2 Etikette"
    def trigger2LabelUpdatedBG = "Тригер 2 Обновен Етикет"
    def trigger2LabelUpdatedDE = "Abzugshahn 2 Aktualisiert Etikette"

    def condition1LabelBG = "Условие 1 Етикет"
    def condition1LabelDE = "Bedingung 1 Etikette"
    def condition1LabelUpdatedBG = "Условие 1 Обновен Етикет"
    def condition1LabelUpdatedDE = "Bedingung 1 Aktualisiert Etikette"

    def condition2LabelBG = "Условие 2 Етикет"
    def condition2LabelDE = "Bedingung 2 Etikette"
    def condition2LabelUpdatedBG = "Условие 2 Обновен Етикет"
    def condition2LabelUpdatedDE = "Bedingung 2 Aktualisiert Etikette"

    def action1LabelBG = "Действие 1 Етикет"
    def action1LabelDE = "Aktion 1 Etikette"
    def action1LabelUpdatedBG = "Действие 1 Обновен Етикет"
    def action1LabelUpdatedDE = "Aktion 1 Aktualisiert Etikette"

    @Before
    void before() {
        logger.info('@Before.begin')

        Locale.setDefault(Locale.ENGLISH)
        registerVolatileStorageService()

        def autoupdateConfig = [
            autoUpdate: { String itemName ->
                println "AutoUpdate Item -> " + itemName
                return true
            }

        ] as AutoUpdateBindingConfigProvider
        registerService(autoupdateConfig)

        def StorageService storageService = getService(StorageService)
        moduleTypeRegistry = getService(ModuleTypeRegistry)
        pkgAdmin = getService(PackageAdmin)
        waitForAssert ({
            assertThat storageService, is(notNullValue())
            assertThat moduleTypeRegistry, is(notNullValue())
            assertThat pkgAdmin, is(notNullValue())
        }, 9000)

        logger.info('@Before.finish')
    }

    @After
    void after() {
        logger.info('@After');
    }
    
    protected void registerVolatileStorageService() {
        registerService(AutomationIntegrationJsonTest.VOLATILE_STORAGE_SERVICE)
    }
    
    @Test
    public void 'asserting that the installation of the host-fragment provides the resources correctly' () {
        logger.info('asserting that the installation of the host-fragment provides the resources correctly')

        // first install the host
        def Bundle host = bundleContext.installBundle(RESOURCES_TEST_BUNDLE_1, getClass().getClassLoader().getResourceAsStream(PATH + RESOURCES_TEST_BUNDLE_1 + EXT))
        assertThat host, is(notNullValue())
        host.start()

        // assert that the host resources are loaded
        waitForAssert({
            assertThat host.getState(), is(Bundle.ACTIVE)
            assertThat moduleTypeRegistry.get(trigger1), is(notNullValue())
            assertThat moduleTypeRegistry.get(trigger1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(trigger1).getLabel())
            assertThat moduleTypeRegistry.get(trigger1, new Locale("bg")).getLabel(), is(trigger1LabelBG)
            assertThat moduleTypeRegistry.get(trigger1, Locale.GERMANY).getLabel(), is(trigger1LabelDE)
            assertThat moduleTypeRegistry.get(condition1), is(notNullValue())
            assertThat moduleTypeRegistry.get(condition1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(condition1).getLabel())
            assertThat moduleTypeRegistry.get(condition1, new Locale("bg")).getLabel(), is(condition1LabelBG)
            assertThat moduleTypeRegistry.get(condition1, Locale.GERMANY).getLabel(), is(condition1LabelDE)
            assertThat moduleTypeRegistry.get(action1), is(notNullValue())
            assertThat moduleTypeRegistry.get(action1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(action1).getLabel())
            assertThat moduleTypeRegistry.get(action1, new Locale("bg")).getLabel(), is(action1LabelBG)
            assertThat moduleTypeRegistry.get(action1, Locale.GERMANY).getLabel(), is(action1LabelDE)
        }, 3000, 200)

        // then install the fragment
        def Bundle fragment = bundleContext.installBundle(RESOURCES_TEST_BUNDLE_3, getClass().getClassLoader().getResourceAsStream(PATH + RESOURCES_TEST_BUNDLE_3 + EXT))
        assertThat fragment, is(notNullValue())

        // assert that the host and fragment resources are loaded
        waitForAssert({
            assertThat fragment.getState(), is(Bundle.RESOLVED)
            assertThat moduleTypeRegistry.get(trigger1), is(notNullValue())
            assertThat moduleTypeRegistry.get(condition1), is(notNullValue())
            assertThat moduleTypeRegistry.get(action1), is(notNullValue())
            assertThat moduleTypeRegistry.get(trigger2), is(notNullValue())
            assertThat moduleTypeRegistry.get(trigger2, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(trigger2).getLabel())
            assertThat moduleTypeRegistry.get(trigger2, new Locale("bg")).getLabel(), is(trigger2LabelBG)
            assertThat moduleTypeRegistry.get(trigger2, Locale.GERMANY).getLabel(), is(trigger2LabelDE)
            assertThat moduleTypeRegistry.get(condition2), is(notNullValue())
            assertThat moduleTypeRegistry.get(condition2, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(condition2).getLabel())
            assertThat moduleTypeRegistry.get(condition2, new Locale("bg")).getLabel(), is(condition2LabelBG)
            assertThat moduleTypeRegistry.get(condition2, Locale.GERMANY).getLabel(), is(condition2LabelDE)
        }, 3000, 200)

        // first uninstall the host
        host.uninstall()

        // assert that the host and fragment resources are removed
        waitForAssert({
            assertThat host.getState(), is(Bundle.UNINSTALLED)
            assertThat moduleTypeRegistry.get(trigger1), is(nullValue())
            assertThat moduleTypeRegistry.get(condition1), is(nullValue())
            assertThat moduleTypeRegistry.get(action1), is(nullValue())
            assertThat moduleTypeRegistry.get(trigger2), is(nullValue())
            assertThat moduleTypeRegistry.get(condition2), is(nullValue())
        }, 3000, 200)

        // uninstall the fragment
        fragment.uninstall()
        assertThat fragment.getState(), is(Bundle.UNINSTALLED)
    }

    @Test
    public void 'asserting that the installation of the fragment-host provides the resources correctly' () {
        logger.info('asserting that the installation of the fragment-host provides the resources correctly')

        def waiting = true
        def FrameworkListener listener = new FrameworkListener() {
                    public void frameworkEvent(FrameworkEvent event) {
                        waiting = false
                    }
                }

        // first install the fragment
        def Bundle fragment = bundleContext.installBundle(RESOURCES_TEST_BUNDLE_3, getClass().getClassLoader().getResourceAsStream(PATH + RESOURCES_TEST_BUNDLE_3 + EXT))
        assertThat fragment, is(notNullValue())

        // assert that the host and fragment resources are not loaded
        waitForAssert({
            assertThat fragment.getState(), is(Bundle.INSTALLED)
            assertThat moduleTypeRegistry.get(trigger2), is(nullValue())
            assertThat moduleTypeRegistry.get(condition2), is(nullValue())
        }, 3000, 200)

        // then install the host
        def Bundle host = bundleContext.installBundle(RESOURCES_TEST_BUNDLE_1, getClass().getClassLoader().getResourceAsStream(PATH + RESOURCES_TEST_BUNDLE_1 + EXT))
        assertThat host, is(notNullValue())
        host.start()

        // assert that the host and fragment resources are loaded
        waitForAssert({
            assertThat host.getState(), is(Bundle.ACTIVE)
            assertThat fragment.getState(), is(Bundle.RESOLVED)
            assertThat moduleTypeRegistry.get(trigger1), is(notNullValue())
            assertThat moduleTypeRegistry.get(trigger1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(trigger1).getLabel())
            assertThat moduleTypeRegistry.get(trigger1, new Locale("bg")).getLabel(), is(trigger1LabelBG)
            assertThat moduleTypeRegistry.get(trigger1, Locale.GERMANY).getLabel(), is(trigger1LabelDE)
            assertThat moduleTypeRegistry.get(condition1), is(notNullValue())
            assertThat moduleTypeRegistry.get(condition1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(condition1).getLabel())
            assertThat moduleTypeRegistry.get(condition1, new Locale("bg")).getLabel(), is(condition1LabelBG)
            assertThat moduleTypeRegistry.get(condition1, Locale.GERMANY).getLabel(), is(condition1LabelDE)
            assertThat moduleTypeRegistry.get(action1), is(notNullValue())
            assertThat moduleTypeRegistry.get(action1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(action1).getLabel())
            assertThat moduleTypeRegistry.get(action1, new Locale("bg")).getLabel(), is(action1LabelBG)
            assertThat moduleTypeRegistry.get(action1, Locale.GERMANY).getLabel(), is(action1LabelDE)
            assertThat moduleTypeRegistry.get(trigger2), is(notNullValue())
            assertThat moduleTypeRegistry.get(trigger2, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(trigger2).getLabel())
            assertThat moduleTypeRegistry.get(trigger2, new Locale("bg")).getLabel(), is(trigger2LabelBG)
            assertThat moduleTypeRegistry.get(trigger2, Locale.GERMANY).getLabel(), is(trigger2LabelDE)
            assertThat moduleTypeRegistry.get(condition2), is(notNullValue())
            assertThat moduleTypeRegistry.get(condition2, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(condition2).getLabel())
            assertThat moduleTypeRegistry.get(condition2, new Locale("bg")).getLabel(), is(condition2LabelBG)
            assertThat moduleTypeRegistry.get(condition2, Locale.GERMANY).getLabel(), is(condition2LabelDE)
        }, 10000, 200)

        // first uninstall the fragment
        fragment.uninstall()
        assertThat fragment.getState(), is(Bundle.UNINSTALLED)

        waiting = true
        bundleContext.addFrameworkListener(listener);
        def Bundle[] bundles = [host, fragment] as Bundle[]
        pkgAdmin.refreshPackages(bundles)
        while ({
            sleep(3000)
            waiting == true
        }()) continue
            bundleContext.removeFrameworkListener(listener)

        waitForAssert({
            // assert that the host is updated and only its resources are available
            assertThat moduleTypeRegistry.get(trigger1), is(notNullValue())
            assertThat moduleTypeRegistry.get(trigger1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(trigger1).getLabel())
            assertThat moduleTypeRegistry.get(trigger1, new Locale("bg")).getLabel(), is(trigger1LabelBG)
            assertThat moduleTypeRegistry.get(trigger1, Locale.GERMANY).getLabel(), is(trigger1LabelDE)
            assertThat moduleTypeRegistry.get(condition1), is(notNullValue())
            assertThat moduleTypeRegistry.get(condition1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(condition1).getLabel())
            assertThat moduleTypeRegistry.get(condition1, new Locale("bg")).getLabel(), is(condition1LabelBG)
            assertThat moduleTypeRegistry.get(condition1, Locale.GERMANY).getLabel(), is(condition1LabelDE)
            assertThat moduleTypeRegistry.get(action1), is(notNullValue())
            assertThat moduleTypeRegistry.get(action1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(action1).getLabel())
            assertThat moduleTypeRegistry.get(action1, new Locale("bg")).getLabel(), is(action1LabelBG)
            assertThat moduleTypeRegistry.get(action1, Locale.GERMANY).getLabel(), is(action1LabelDE)
            assertThat moduleTypeRegistry.get(trigger2), is(nullValue())
            assertThat moduleTypeRegistry.get(condition2), is(nullValue())
        }, 3000, 200)

        // then uninstall the host
        host.uninstall()

        // assert that the host resources also are removed
        waitForAssert({
            assertThat host.getState(), is(Bundle.UNINSTALLED)
            assertThat moduleTypeRegistry.get(trigger1), is(nullValue())
            assertThat moduleTypeRegistry.get(condition1), is(nullValue())
            assertThat moduleTypeRegistry.get(action1), is(nullValue())
        }, 3000, 200)

    }

    @Test
    public void 'asserting that the update of the fragment-host provides the resources correctly' () {
        logger.info('asserting that the update of the fragment-host provides the resources correctly')

        def waiting = true
        def FrameworkListener listener = new FrameworkListener() {
                    public void frameworkEvent(FrameworkEvent event) {
                        waiting = false
                    }
                }

        // first install the fragment
        def Bundle fragment = bundleContext.installBundle(RESOURCES_TEST_BUNDLE_3, getClass().getClassLoader().getResourceAsStream(PATH + RESOURCES_TEST_BUNDLE_3 + EXT))
        assertThat fragment, is(notNullValue())

        // assert that the host and fragment resources are not loaded
        waitForAssert({
            assertThat fragment.getState(), is(Bundle.INSTALLED)
            assertThat moduleTypeRegistry.get(trigger2), is(nullValue())
            assertThat moduleTypeRegistry.get(condition2), is(nullValue())
        }, 3000, 200)

        // then install the host
        def Bundle host = bundleContext.installBundle(RESOURCES_TEST_BUNDLE_1, getClass().getClassLoader().getResourceAsStream(PATH + RESOURCES_TEST_BUNDLE_1 + EXT))
        assertThat host, is(notNullValue())
        host.start()

        // assert that the host and fragment resources are loaded
        waitForAssert({
            assertThat host.getState(), is(Bundle.ACTIVE)
            assertThat fragment.getState(), is(Bundle.RESOLVED)
            assertThat moduleTypeRegistry.get(trigger1), is(notNullValue())
            assertThat moduleTypeRegistry.get(trigger1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(trigger1).getLabel())
            assertThat moduleTypeRegistry.get(trigger1, new Locale("bg")).getLabel(), is(trigger1LabelBG)
            assertThat moduleTypeRegistry.get(trigger1, Locale.GERMANY).getLabel(), is(trigger1LabelDE)
            assertThat moduleTypeRegistry.get(condition1), is(notNullValue())
            assertThat moduleTypeRegistry.get(condition1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(condition1).getLabel())
            assertThat moduleTypeRegistry.get(condition1, new Locale("bg")).getLabel(), is(condition1LabelBG)
            assertThat moduleTypeRegistry.get(condition1, Locale.GERMANY).getLabel(), is(condition1LabelDE)
            assertThat moduleTypeRegistry.get(action1), is(notNullValue())
            assertThat moduleTypeRegistry.get(action1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(action1).getLabel())
            assertThat moduleTypeRegistry.get(action1, new Locale("bg")).getLabel(), is(action1LabelBG)
            assertThat moduleTypeRegistry.get(action1, Locale.GERMANY).getLabel(), is(action1LabelDE)
            assertThat moduleTypeRegistry.get(trigger2), is(notNullValue())
            assertThat moduleTypeRegistry.get(trigger2, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(trigger2).getLabel())
            assertThat moduleTypeRegistry.get(trigger2, new Locale("bg")).getLabel(), is(trigger2LabelBG)
            assertThat moduleTypeRegistry.get(trigger2, Locale.GERMANY).getLabel(), is(trigger2LabelDE)
            assertThat moduleTypeRegistry.get(condition2), is(notNullValue())
            assertThat moduleTypeRegistry.get(condition2, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(condition2).getLabel())
            assertThat moduleTypeRegistry.get(condition2, new Locale("bg")).getLabel(), is(condition2LabelBG)
            assertThat moduleTypeRegistry.get(condition2, Locale.GERMANY).getLabel(), is(condition2LabelDE)
        }, 3000, 200)

        // first update the fragment
        fragment.update(getClass().getClassLoader().getResourceAsStream(PATH + RESOURCES_TEST_BUNDLE_4 + EXT))

        waiting = true
        bundleContext.addFrameworkListener(listener);
        def Bundle[] bundles = [host, fragment] as Bundle[]
        pkgAdmin.refreshPackages(bundles)
        while ({
            sleep(3000)
            waiting == true
        }()) continue
            bundleContext.removeFrameworkListener(listener)

        waitForAssert({
            assertThat host.getState(), is(Bundle.ACTIVE)
            assertThat fragment.getState(), is(Bundle.RESOLVED)
            assertThat moduleTypeRegistry.get(trigger1), is(notNullValue())
            assertThat moduleTypeRegistry.get(trigger1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(trigger1).getLabel())
            assertThat moduleTypeRegistry.get(trigger1, new Locale("bg")).getLabel(), is(trigger1LabelBG)
            assertThat moduleTypeRegistry.get(trigger1, Locale.GERMANY).getLabel(), is(trigger1LabelDE)
            assertThat moduleTypeRegistry.get(condition1), is(notNullValue())
            assertThat moduleTypeRegistry.get(condition1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(condition1).getLabel())
            assertThat moduleTypeRegistry.get(condition1, new Locale("bg")).getLabel(), is(condition1LabelBG)
            assertThat moduleTypeRegistry.get(condition1, Locale.GERMANY).getLabel(), is(condition1LabelDE)
            assertThat moduleTypeRegistry.get(action1), is(notNullValue())
            assertThat moduleTypeRegistry.get(action1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(action1).getLabel())
            assertThat moduleTypeRegistry.get(action1, new Locale("bg")).getLabel(), is(action1LabelBG)
            assertThat moduleTypeRegistry.get(action1, Locale.GERMANY).getLabel(), is(action1LabelDE)
            assertThat moduleTypeRegistry.get(trigger2), is(notNullValue())
            assertThat moduleTypeRegistry.get(trigger2, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(trigger2).getLabel())
            assertThat moduleTypeRegistry.get(trigger2, new Locale("bg")).getLabel(), is(trigger2LabelUpdatedBG)
            assertThat moduleTypeRegistry.get(trigger2, Locale.GERMANY).getLabel(), is(trigger2LabelUpdatedDE)
            assertThat moduleTypeRegistry.get(condition2), is(notNullValue())
            assertThat moduleTypeRegistry.get(condition2, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(condition2).getLabel())
            assertThat moduleTypeRegistry.get(condition2, new Locale("bg")).getLabel(), is(condition2LabelUpdatedBG)
            assertThat moduleTypeRegistry.get(condition2, Locale.GERMANY).getLabel(), is(condition2LabelUpdatedDE)
        }, 3000, 200)

        // then update the host
        host.update(getClass().getClassLoader().getResourceAsStream(PATH + RESOURCES_TEST_BUNDLE_2 + EXT))

        waiting = true
        bundleContext.addFrameworkListener(listener);
        pkgAdmin.refreshPackages(bundles)
        while ({
            sleep(3000)
            waiting == true
        }()) continue
            bundleContext.removeFrameworkListener(listener)

        waitForAssert({
            assertThat host.getState(), is(Bundle.ACTIVE)
            assertThat fragment.getState(), is(Bundle.RESOLVED)
            assertThat moduleTypeRegistry.get(trigger1), is(notNullValue())
            assertThat moduleTypeRegistry.get(trigger1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(trigger1).getLabel())
            assertThat moduleTypeRegistry.get(trigger1, new Locale("bg")).getLabel(), is(trigger1LabelUpdatedBG)
            assertThat moduleTypeRegistry.get(trigger1, Locale.GERMANY).getLabel(), is(trigger1LabelUpdatedDE)
            assertThat moduleTypeRegistry.get(condition1), is(notNullValue())
            assertThat moduleTypeRegistry.get(condition1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(condition1).getLabel())
            assertThat moduleTypeRegistry.get(condition1, new Locale("bg")).getLabel(), is(condition1LabelUpdatedBG)
            assertThat moduleTypeRegistry.get(condition1, Locale.GERMANY).getLabel(), is(condition1LabelUpdatedDE)
            assertThat moduleTypeRegistry.get(action1), is(notNullValue())
            assertThat moduleTypeRegistry.get(action1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(action1).getLabel())
            assertThat moduleTypeRegistry.get(action1, new Locale("bg")).getLabel(), is(action1LabelUpdatedBG)
            assertThat moduleTypeRegistry.get(action1, Locale.GERMANY).getLabel(), is(action1LabelUpdatedDE)
            assertThat moduleTypeRegistry.get(trigger2), is(notNullValue())
            assertThat moduleTypeRegistry.get(trigger2, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(trigger2).getLabel())
            assertThat moduleTypeRegistry.get(trigger2, new Locale("bg")).getLabel(), is(trigger2LabelUpdatedBG)
            assertThat moduleTypeRegistry.get(trigger2, Locale.GERMANY).getLabel(), is(trigger2LabelUpdatedDE)
            assertThat moduleTypeRegistry.get(condition2), is(notNullValue())
            assertThat moduleTypeRegistry.get(condition2, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(condition2).getLabel())
            assertThat moduleTypeRegistry.get(condition2, new Locale("bg")).getLabel(), is(condition2LabelUpdatedBG)
            assertThat moduleTypeRegistry.get(condition2, Locale.GERMANY).getLabel(), is(condition2LabelUpdatedDE)
        }, 3000, 200)

        // first uninstall the fragment
        fragment.uninstall()
        assertThat fragment.getState(), is(Bundle.UNINSTALLED)

        waiting = true
        bundleContext.addFrameworkListener(listener);
        pkgAdmin.refreshPackages(bundles)
        while ({
            sleep(3000)
            waiting == true
        }()) continue
            bundleContext.removeFrameworkListener(listener)

        // assert that the host is updated and only its resources are available
        waitForAssert({
            assertThat moduleTypeRegistry.get(trigger1), is(notNullValue())
            assertThat moduleTypeRegistry.get(trigger1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(trigger1).getLabel())
            assertThat moduleTypeRegistry.get(trigger1, new Locale("bg")).getLabel(), is(trigger1LabelUpdatedBG)
            assertThat moduleTypeRegistry.get(trigger1, Locale.GERMANY).getLabel(), is(trigger1LabelUpdatedDE)
            assertThat moduleTypeRegistry.get(condition1), is(notNullValue())
            assertThat moduleTypeRegistry.get(condition1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(condition1).getLabel())
            assertThat moduleTypeRegistry.get(condition1, new Locale("bg")).getLabel(), is(condition1LabelUpdatedBG)
            assertThat moduleTypeRegistry.get(condition1, Locale.GERMANY).getLabel(), is(condition1LabelUpdatedDE)
            assertThat moduleTypeRegistry.get(action1), is(notNullValue())
            assertThat moduleTypeRegistry.get(action1, Locale.getDefault()).getLabel(), is(moduleTypeRegistry.get(action1).getLabel())
            assertThat moduleTypeRegistry.get(action1, new Locale("bg")).getLabel(), is(action1LabelUpdatedBG)
            assertThat moduleTypeRegistry.get(action1, Locale.GERMANY).getLabel(), is(action1LabelUpdatedDE)
            assertThat moduleTypeRegistry.get(trigger2), is(nullValue())
            assertThat moduleTypeRegistry.get(condition2), is(nullValue())
        }, 3000, 200)

        // then uninstall the host
        host.uninstall()
        assertThat host.getState(), is(Bundle.UNINSTALLED)

        // assert that the host resources also are removed
        waitForAssert({
            assertThat moduleTypeRegistry.get(trigger1), is(nullValue())
            assertThat moduleTypeRegistry.get(condition1), is(nullValue())
            assertThat moduleTypeRegistry.get(action1), is(nullValue())
        }, 3000, 200)
    }

}
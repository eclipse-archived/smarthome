/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml.test


import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.BundleProcessor.BundleProcessorListener
import org.eclipse.smarthome.config.xml.osgi.AbstractAsyncBundleProcessor
import org.eclipse.smarthome.test.SyntheticBundleInstaller
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle

/**
 * The AbstractAsyncBundleProcessorTest is a test for asynchronous loading of configuration description from XML documents.
 *
 * @author Simon Kaufmann - Initial contribution and API
 *
 */

class AbstractAsyncBundleProcessorTest {
    private def b2
    private def b1

    @Before
    void setUp() {
        b1 = [
            getSymbolicName: { -> "b1"},
            getBundleId: { -> 1L }
        ] as Bundle
        b2 = [
            getSymbolicName: { -> "b2"},
            getBundleId: { -> 2L }
        ] as Bundle
    }

    @Test
    void 'assert bundle configurations are loaded'() {
        def List<String> called = new ArrayList<String>()
        AbstractAsyncBundleProcessor acl = new AbstractAsyncBundleProcessor() {
                    @Override
                    protected void processBundle(Bundle bundle) {
                        called.add(bundle.getSymbolicName())
                    }
                };

        acl.addingBundle b1
        SyntheticBundleInstaller.waitUntilLoadingFinished(b1)

        acl.addingBundle b2
        SyntheticBundleInstaller.waitUntilLoadingFinished(b2)

        assertTrue called.contains("b1")
        assertTrue called.contains("b2")
    }

    @Test
    void 'assert only relevant bundle configurations are loaded'() {
        def List<String> called = new ArrayList<String>()
        AbstractAsyncBundleProcessor acl = new AbstractAsyncBundleProcessor() {
                    protected boolean isBundleRelevant(Bundle bundle) {
                        return bundle.getSymbolicName().equals("b2")
                    };

                    @Override
                    protected void processBundle(Bundle bundle) {
                        called.add(bundle.getSymbolicName())
                    }
                };

        acl.addingBundle b1
        SyntheticBundleInstaller.waitUntilLoadingFinished(b1)

        acl.addingBundle b2
        SyntheticBundleInstaller.waitUntilLoadingFinished(b2)

        assertFalse called.contains("b1")
        assertTrue called.contains("b2")
    }

    @Test
    void 'assert throwing exceptions does not break loading'() {
        def List<Long> called = new ArrayList<Long>()
        AbstractAsyncBundleProcessor acl = new AbstractAsyncBundleProcessor() {
                    @Override
                    protected void processBundle(Bundle bundle) {
                        called.add(bundle.getBundleId())
                        throw new RuntimeException()
                    }
                };

        acl.addingBundle b1
        SyntheticBundleInstaller.waitUntilLoadingFinished(b1)

        acl.addingBundle b2
        SyntheticBundleInstaller.waitUntilLoadingFinished(b2)

        assertTrue called.contains(1L)
        assertTrue called.contains(2L)
    }

    @Test
    void 'assert waiting for irrelevant and unkown bundles does not block'() {
        AbstractAsyncBundleProcessor acl = new AbstractAsyncBundleProcessor() {
                    protected boolean isBundleRelevant(Bundle arg0) {
                        false
                    };

                    @Override
                    protected void processBundle(Bundle bundle) {
                    }
                };

        acl.addingBundle b1

        SyntheticBundleInstaller.waitUntilLoadingFinished(b1)
        SyntheticBundleInstaller.waitUntilLoadingFinished(b2)
    }

    @Test
    void 'assert listeners informed when loading completed'() {
        def List<String> called = new ArrayList<String>()
        AbstractAsyncBundleProcessor acl = new AbstractAsyncBundleProcessor() {
                    @Override
                    protected void processBundle(Bundle bundle) {
                        Thread.sleep(1000)
                        called.add(bundle.getSymbolicName())
                    }
                };
        acl.registerListener([
            "bundleFinished": {context, Bundle bundle ->
                called.add(bundle.getSymbolicName())
            }
        ] as BundleProcessorListener)

        acl.addingBundle b1
        assertFalse called.contains("b1")
        SyntheticBundleInstaller.waitUntilLoadingFinished(b1)
        assertTrue called.contains("b1");
    }
}

/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.i18n;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.i18n.I18nProvider
import org.eclipse.smarthome.core.i18n.LocaleProvider
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle


/**
 * The {@link I18nProviderOSGiTest} checks if the {@link I18nProvider} service is working
 * properly. This test retrieves the {@link I18nProvider} service from the <i>OSGi</i>
 * service registry and calls any public methods on it in different scenarios.
 *
 * @author Michael Grammling - Initial Contribution
 * @author Thomas Höfer - Added tests for getText operation with arguments
 */
class I18nProviderOSGiTest extends OSGiTest {

    def RESOURCE = "resourceName"

    def KEY_HELLO = "HELLO"
    def KEY_HELLO_SINGLE_NAME = "HELLO_SINGLE_NAME"
    def KEY_HELLO_MULTIPLE_NAMES = "HELLO_MULTIPLE_NAMES"

    def KEY_BYE = "BYE"

    def HELLO_WORLD_DEFAULT = "Hallo Welt!"
    def HELLO_WORLD_EN = "Hello World!"
    def HELLO_WORLD_FR = "Bonjour le monde!"

    def NAMES = ["esh", "thing", "rule"].toArray()
    def DEFAULT_SINGLE_NAME_TEXT = "Hi {0}!"

    def HELLO_SINGLE_NAME_DEFAULT = "Hallo esh!"
    def HELLO_SINGLE_NAME_EN = "Hello esh!"
    def HELLO_SINGLE_NAME_FR = "Bonjour esh!"

    def HELLO_MULTIPLE_NAMES_DEFAULT = "Hallo esh, Hallo thing, Hallo rule!"
    def HELLO_MULTIPLE_NAMES_EN = "Hello esh, Hello thing, Hello rule!"
    def HELLO_MULTIPLE_NAMES_FR = "Bonjour esh, Bonjour thing, Bonjour rule!"

    def BYE_DEFAULT = "Tschuess!"

    I18nProvider i18nProvider

    Locale defaultLocale

    @Before
    void setUp() {
        i18nProvider = getService(I18nProvider)
        assertThat i18nProvider, is(notNullValue())

        LocaleProviderImpl localeProvider = getService(LocaleProvider,LocaleProviderImpl)
        Map<String,String> localeCfg = new HashMap<>();
        localeCfg.putAt("language", "de");
        localeCfg.putAt("region", "DE");
        localeProvider.modified(localeCfg);
    }

    @After
    void after() {
    }

    @Test
    void 'assert that getText without bundle is working properly'() {
        def text

        text = i18nProvider.getText(null, null, null, null)
        assertThat text, is(nullValue())

        text = i18nProvider.getText(null, null, "default", null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("default"))
    }

    @Test
    void 'assert that getText via bundle is working properly'() {
        def text

        Bundle bundle = getBundleContext().bundle

        text = i18nProvider.getText(bundle, null, null, null)
        assertThat text, is(nullValue())

        text = i18nProvider.getText(bundle, null, "default", null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("default"))

        text = i18nProvider.getText(bundle, "UNKNOWN_HELLO", "default", null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("default"))

        text = i18nProvider.getText(bundle, "UNKNOWN_HELLO", null, null)
        assertThat text, is(nullValue())

        text = i18nProvider.getText(bundle, KEY_HELLO, "default", null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_WORLD_DEFAULT))

        text = i18nProvider.getText(bundle, KEY_HELLO, "default", Locale.FRENCH)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_WORLD_FR))

        text = i18nProvider.getText(bundle, KEY_HELLO, "default", Locale.ENGLISH)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_WORLD_EN))

        text = i18nProvider.getText(bundle, KEY_BYE, "default", null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("default"))

        text = i18nProvider.getText(bundle, KEY_BYE, "default", Locale.ENGLISH)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(BYE_DEFAULT))
    }

    @Test
    void 'assert that getText with arguments delegates properly to getText without arguments'() {
        def text

        Bundle bundle = getBundleContext().bundle

        text = i18nProvider.getText(bundle, null, null, null, null)
        assertThat text, is(nullValue())

        text = i18nProvider.getText(bundle, null, "default", null, null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("default"))

        text = i18nProvider.getText(bundle, "UNKNOWN_HELLO", "default", null, null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("default"))

        text = i18nProvider.getText(bundle, "UNKNOWN_HELLO", null, null, null)
        assertThat text, is(nullValue())

        text = i18nProvider.getText(bundle, KEY_HELLO, "default", null, null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_WORLD_DEFAULT))

        text = i18nProvider.getText(bundle, KEY_HELLO, "default", Locale.FRENCH, null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_WORLD_FR))

        text = i18nProvider.getText(bundle, KEY_HELLO, "default", Locale.ENGLISH, null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_WORLD_EN))

        text = i18nProvider.getText(bundle, KEY_BYE, "default", null, null, null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("default"))

        text = i18nProvider.getText(bundle, KEY_BYE, "default", Locale.ENGLISH, null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(BYE_DEFAULT))
    }

    @Test
    void 'assert that getText with arguments via bundle is working properly'() {
        Bundle bundle = getBundleContext().bundle

        def text = i18nProvider.getText(bundle, KEY_HELLO_SINGLE_NAME, null, null, NAMES)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_SINGLE_NAME_DEFAULT))

        text = i18nProvider.getText(bundle, KEY_HELLO_SINGLE_NAME, DEFAULT_SINGLE_NAME_TEXT, null, NAMES)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_SINGLE_NAME_DEFAULT))

        text = i18nProvider.getText(bundle, null, DEFAULT_SINGLE_NAME_TEXT, null, NAMES)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("Hi esh!"))

        text = i18nProvider.getText(bundle, null, DEFAULT_SINGLE_NAME_TEXT, null, null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(DEFAULT_SINGLE_NAME_TEXT))

        text = i18nProvider.getText(bundle, null, DEFAULT_SINGLE_NAME_TEXT, null, [].toArray())
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(DEFAULT_SINGLE_NAME_TEXT))

        text = i18nProvider.getText(bundle, KEY_HELLO_SINGLE_NAME, null, Locale.ENGLISH, NAMES)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_SINGLE_NAME_EN))

        text = i18nProvider.getText(bundle, KEY_HELLO_SINGLE_NAME, null, Locale.FRENCH, NAMES)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_SINGLE_NAME_FR))

        text = i18nProvider.getText(bundle, KEY_HELLO_MULTIPLE_NAMES, null, null, NAMES)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_MULTIPLE_NAMES_DEFAULT))

        text = i18nProvider.getText(bundle, KEY_HELLO_MULTIPLE_NAMES, null, Locale.ENGLISH, NAMES)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_MULTIPLE_NAMES_EN))

        text = i18nProvider.getText(bundle, KEY_HELLO_MULTIPLE_NAMES, null, Locale.FRANCE, NAMES)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_MULTIPLE_NAMES_FR))


        text = i18nProvider.getText(bundle, KEY_HELLO_MULTIPLE_NAMES, null, null, [
            "esh",
            "thing",
            "rule",
            "config"
        ].toArray())
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_MULTIPLE_NAMES_DEFAULT))

        text = i18nProvider.getText(bundle, null, "Hallo {2}, Hallo {1}, Hallo {0}!", null, NAMES)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("Hallo rule, Hallo thing, Hallo esh!"))
    }
}

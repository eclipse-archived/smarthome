/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.i18n;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.PointType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;

/**
 * The {@link I18nProviderImplTest} tests the basic functionality of the {@link I18nProviderImpl} OSGi service.
 *
 * @author Stefan Triller - Initial contribution
 */
public class I18nProviderImplTest {

    private static final String CONFIG_LOCATION = "location";
    private static final String CONFIG_LANGUAGE = "language";
    private static final String CONFIG_SCRIPT = "script";
    private static final String CONFIG_REGION = "region";
    private static final String CONFIG_VARIANT = "variant";

    private static final String LOCATION_ZERO = "0,0";
    private static final String LOCATION_DARMSTADT = "49.876733,8.666809,1";
    private static final String LOCATION_HAMBURG = "53.588231,9.920082,5";

    private static final String LANGUAGE_DE = "de";
    private static final String LANGUAGE_RU = "ru";

    private static final String SCRIPT_DE = "Latn";
    private static final String SCRIPT_RU = "Cyrl";

    private static final String REGION_DE = "DE";
    private static final String REGION_RU = "RU";

    private static final String VARIANT_DE = "1996";
    private static final String VARIANT_RU = "";

    // LocationProvider translationProvider;
    I18nProviderImpl i18nProviderImpl;
    ConfigurationAdmin configAdmin;

    Dictionary<String, Object> initialConfig = new Hashtable<>();

    @Mock
    private ComponentContext componentContext;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private Bundle bundle;

    @Before
    public void setup() {

        initMocks(this);
        buildInitialConfig();
        when(componentContext.getProperties()).thenReturn(initialConfig);
        when(componentContext.getBundleContext()).thenReturn(bundleContext);
        when(bundleContext.getBundles()).thenReturn(new Bundle[] { bundle });

        i18nProviderImpl = new I18nProviderImpl();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void assertThatConfigurationWasSet() {
        i18nProviderImpl.modified((Map<String, Object>) initialConfig);

        PointType location = i18nProviderImpl.getLocation();
        Locale setLocale = i18nProviderImpl.getLocale();

        assertThat(location.toString(), is(LOCATION_ZERO));

        assertThat(setLocale.getLanguage(), is(initialConfig.get(CONFIG_LANGUAGE)));
        assertThat(setLocale.getScript(), is(initialConfig.get(CONFIG_SCRIPT)));
        assertThat(setLocale.getCountry(), is(initialConfig.get(CONFIG_REGION)));
        assertThat(setLocale.getVariant(), is(initialConfig.get(CONFIG_VARIANT)));
    }

    @Test
    public void assertThatDefaultLocaleWillBeUsed() {
        i18nProviderImpl.modified(new Hashtable<String, Object>());

        PointType location = i18nProviderImpl.getLocation();
        Locale setLocale = i18nProviderImpl.getLocale();

        assertNull(location);
        assertThat(setLocale, is(Locale.getDefault()));
    }

    @Test
    public void assertThatDefaultLocaleWillBeUsedAndLocationIsSet() {
        Hashtable<String, Object> conf = new Hashtable<String, Object>();
        conf.put(CONFIG_LOCATION, LOCATION_DARMSTADT);
        i18nProviderImpl.modified(conf);

        PointType location = i18nProviderImpl.getLocation();
        Locale setLocale = i18nProviderImpl.getLocale();

        assertThat(location.toString(), is(LOCATION_DARMSTADT));
        assertThat(setLocale, is(Locale.getDefault()));
    }

    @Test
    public void assertThatActivateSetsLocaleAndLocation() {
        i18nProviderImpl.activate(componentContext);

        PointType location = i18nProviderImpl.getLocation();
        Locale setLocale = i18nProviderImpl.getLocale();

        assertThat(location.toString(), is(LOCATION_ZERO));

        assertThat(setLocale.getLanguage(), is(initialConfig.get(CONFIG_LANGUAGE)));
        assertThat(setLocale.getScript(), is(initialConfig.get(CONFIG_SCRIPT)));
        assertThat(setLocale.getCountry(), is(initialConfig.get(CONFIG_REGION)));
        assertThat(setLocale.getVariant(), is(initialConfig.get(CONFIG_VARIANT)));
    }

    @Test
    public void assertThatConfigurationChangeWorks() {
        i18nProviderImpl.activate(componentContext);

        i18nProviderImpl.modified(buildRUConfig());

        PointType location = i18nProviderImpl.getLocation();
        Locale setLocale = i18nProviderImpl.getLocale();

        assertThat(location.toString(), is(LOCATION_HAMBURG));

        assertThat(setLocale.getLanguage(), is(LANGUAGE_RU));
        assertThat(setLocale.getScript(), is(SCRIPT_RU));
        assertThat(setLocale.getCountry(), is(REGION_RU));
        assertThat(setLocale.getVariant(), is(VARIANT_RU));
    }

    private void buildInitialConfig() {
        initialConfig.put(CONFIG_LOCATION, LOCATION_ZERO);
        initialConfig.put(CONFIG_LANGUAGE, LANGUAGE_DE);
        initialConfig.put(CONFIG_SCRIPT, SCRIPT_DE);
        initialConfig.put(CONFIG_REGION, REGION_DE);
        initialConfig.put(CONFIG_VARIANT, VARIANT_DE);
    }

    private Hashtable<String, Object> buildRUConfig() {
        Hashtable<String, Object> conf = new Hashtable<>();
        conf.put(CONFIG_LOCATION, LOCATION_HAMBURG);
        conf.put(CONFIG_LANGUAGE, LANGUAGE_RU);
        conf.put(CONFIG_SCRIPT, SCRIPT_RU);
        conf.put(CONFIG_REGION, REGION_RU);
        conf.put(CONFIG_VARIANT, VARIANT_RU);
        return conf;
    }

}

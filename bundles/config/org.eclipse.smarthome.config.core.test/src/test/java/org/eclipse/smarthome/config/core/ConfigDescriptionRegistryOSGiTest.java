/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.config.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.test.OSGiTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

/**
 *
 * @author Simon Kaufmann - converted to Java
 *
 */
public class ConfigDescriptionRegistryOSGiTest extends OSGiTest {

    private URI URI_DUMMY;
    private URI URI_DUMMY1;
    private URI URI_ALIASED;
    private ConfigDescriptionRegistry configDescriptionRegistry;
    private ConfigDescription configDescription;
    private @Mock ConfigDescriptionProvider configDescriptionProviderMock;
    private ConfigDescription configDescription1;
    private @Mock ConfigDescriptionProvider configDescriptionProviderMock1;
    private ConfigDescription configDescription2;
    private @Mock ConfigDescriptionProvider configDescriptionProviderMock2;
    private @Mock ConfigDescriptionAliasProvider aliasProvider;
    private @Mock ConfigOptionProvider configOptionsProviderMockAliased;
    private @Mock ConfigOptionProvider configOptionsProviderMock;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        URI_DUMMY = new URI("config:Dummy");
        URI_DUMMY1 = new URI("config:Dummy1");
        URI_ALIASED = new URI("config:Aliased");

        configDescriptionRegistry = getService(ConfigDescriptionRegistry.class);
        ConfigDescriptionParameter param1 = new ConfigDescriptionParameter("param1",
                ConfigDescriptionParameter.Type.INTEGER);
        List<ConfigDescriptionParameter> pList1 = new ArrayList<ConfigDescriptionParameter>();
        pList1.add(param1);

        configDescription = new ConfigDescription(URI_DUMMY, pList1);
        when(configDescriptionProviderMock.getConfigDescriptions(Matchers.any(Locale.class)))
                .thenReturn(Collections.singleton(configDescription));
        when(configDescriptionProviderMock.getConfigDescription(Matchers.eq(URI_DUMMY), Matchers.any(Locale.class)))
                .thenReturn(configDescription);

        configDescription1 = new ConfigDescription(URI_DUMMY1);
        when(configDescriptionProviderMock1.getConfigDescriptions(Matchers.any(Locale.class)))
                .thenReturn(Collections.singleton(configDescription1));
        when(configDescriptionProviderMock1.getConfigDescription(Matchers.eq(URI_DUMMY1), Matchers.any(Locale.class)))
                .thenReturn(configDescription1);

        ConfigDescriptionParameter param2 = new ConfigDescriptionParameter("param2",
                ConfigDescriptionParameter.Type.INTEGER);
        List<ConfigDescriptionParameter> pList2 = new ArrayList<ConfigDescriptionParameter>();
        pList2.add(param2);
        configDescription2 = new ConfigDescription(URI_DUMMY, pList2);
        when(configDescriptionProviderMock2.getConfigDescriptions(Matchers.any(Locale.class)))
                .thenReturn(Collections.singleton(configDescription2));
        when(configDescriptionProviderMock2.getConfigDescription(Matchers.eq(URI_DUMMY), Matchers.any(Locale.class)))
                .thenReturn(configDescription2);

        when(aliasProvider.getAlias(Matchers.eq(URI_ALIASED))).thenReturn(URI_DUMMY);

        when(configOptionsProviderMockAliased.getParameterOptions(Matchers.eq(URI_ALIASED), Matchers.anyString(),
                Matchers.anyString(), Matchers.any(Locale.class)))
                        .thenReturn(Collections.singletonList(new ParameterOption("Option", "Aliased")));
        when(configOptionsProviderMockAliased.getParameterOptions(Matchers.eq(URI_DUMMY), Matchers.anyString(),
                Matchers.anyString(), Matchers.any(Locale.class))).thenReturn(null);

        when(configOptionsProviderMock.getParameterOptions(Matchers.eq(URI_DUMMY), Matchers.anyString(),
                Matchers.anyString(), Matchers.any(Locale.class)))
                        .thenReturn(Collections.singletonList(new ParameterOption("Option", "Original")));
        when(configOptionsProviderMock.getParameterOptions(Matchers.eq(URI_ALIASED), Matchers.anyString(),
                Matchers.anyString(), Matchers.any(Locale.class))).thenReturn(null);
    }

    @Test
    public void testGetConfigDescription() throws Exception {
        registerService(configDescriptionProviderMock);

        ConfigDescription configDescription = configDescriptionRegistry.getConfigDescription(URI_DUMMY);
        assertThat(configDescription, is(notNullValue()));
        assertThat(configDescription.getUID(), is(equalTo(URI_DUMMY)));
    }

    @Test
    public void testGetConfigDescriptions() throws Exception {

        assertThat(configDescriptionRegistry.getConfigDescriptions().size(), is(0));

        configDescriptionRegistry.addConfigDescriptionProvider(configDescriptionProviderMock);
        assertThat(configDescriptionRegistry.getConfigDescriptions().size(), is(1));

        List<ConfigDescription> configDescriptions = new ArrayList<>(configDescriptionRegistry.getConfigDescriptions());
        assertThat(configDescriptions.get(0).getUID(), is(equalTo(URI_DUMMY)));
        assertThat(configDescriptions.get(0).toParametersMap().size(), is(1));
        assertThat(configDescriptions.get(0).toParametersMap().get("param1"), notNullValue());

        configDescriptionRegistry.addConfigDescriptionProvider(configDescriptionProviderMock1);
        assertThat(configDescriptionRegistry.getConfigDescriptions().size(), is(2));

        configDescriptionRegistry.removeConfigDescriptionProvider(configDescriptionProviderMock);
        assertThat(configDescriptionRegistry.getConfigDescriptions().size(), is(1));

        configDescriptionRegistry.removeConfigDescriptionProvider(configDescriptionProviderMock1);
        assertThat(configDescriptionRegistry.getConfigDescriptions().size(), is(0));
    }

    @Test
    public void testGetConfigDescriptions_options() throws Exception {

        assertThat(configDescriptionRegistry.getConfigDescriptions().size(), is(0));

        configDescriptionRegistry.addConfigDescriptionProvider(configDescriptionProviderMock);
        assertThat(configDescriptionRegistry.getConfigDescriptions().size(), is(1));

        configDescriptionRegistry.addConfigDescriptionProvider(configDescriptionProviderMock2);
        assertThat(configDescriptionRegistry.getConfigDescriptions().size(), is(1));

        List<ConfigDescription> configDescriptions = new ArrayList<>(configDescriptionRegistry.getConfigDescriptions());
        assertThat(configDescriptions.get(0).getUID(), is(equalTo(URI_DUMMY)));

        assertThat(configDescriptions.get(0).getParameters().size(), is(2));
        assertThat(configDescriptions.get(0).getParameters().get(0).getName(), is(equalTo("param1")));
        assertThat(configDescriptions.get(0).getParameters().get(1).getName(), is(equalTo("param2")));

        configDescriptionRegistry.removeConfigDescriptionProvider(configDescriptionProviderMock);
        assertThat(configDescriptionRegistry.getConfigDescriptions().size(), is(1));

        configDescriptionRegistry.removeConfigDescriptionProvider(configDescriptionProviderMock2);
        assertThat(configDescriptionRegistry.getConfigDescriptions().size(), is(0));
    }

    @Test
    public void testGetConfigDescriptions_aliasedOptions() throws Exception {
        assertThat(configDescriptionRegistry.getConfigDescriptions().size(), is(0));

        configDescriptionRegistry.addConfigDescriptionProvider(configDescriptionProviderMock);
        registerService(aliasProvider);
        registerService(configOptionsProviderMockAliased);

        ConfigDescription res = configDescriptionRegistry.getConfigDescription(URI_ALIASED);
        assertThat(res, is(notNullValue()));
        assertThat(res.getParameters().get(0).getOptions().size(), is(1));
        assertThat(res.getParameters().get(0).getOptions().get(0).getLabel(), is("Aliased"));
        assertThat(res.getUID(), is(URI_ALIASED));

        configDescriptionRegistry.removeConfigDescriptionProvider(configDescriptionProviderMock);
        assertThat(configDescriptionRegistry.getConfigDescriptions().size(), is(0));
    }

    @Test
    public void testGetConfigDescriptions_nonAliasOptions() throws Exception {
        assertThat(configDescriptionRegistry.getConfigDescriptions().size(), is(0));

        configDescriptionRegistry.addConfigDescriptionProvider(configDescriptionProviderMock);
        registerService(aliasProvider);
        registerService(configOptionsProviderMock);

        ConfigDescription res = configDescriptionRegistry.getConfigDescription(URI_ALIASED);
        assertThat(res, is(notNullValue()));
        assertThat(res.getParameters().get(0).getOptions().size(), is(1));
        assertThat(res.getParameters().get(0).getOptions().get(0).getLabel(), is("Original"));
        assertThat(res.getUID(), is(URI_ALIASED));

        configDescriptionRegistry.removeConfigDescriptionProvider(configDescriptionProviderMock);
        assertThat(configDescriptionRegistry.getConfigDescriptions().size(), is(0));
    }

}

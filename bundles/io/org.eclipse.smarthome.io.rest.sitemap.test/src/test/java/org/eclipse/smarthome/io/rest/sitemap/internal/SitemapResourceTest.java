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
package org.eclipse.smarthome.io.rest.sitemap.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.sitemap.ColorArray;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.eclipse.smarthome.model.sitemap.VisibilityRule;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Test aspects of the {@link SitemapResource}.
 *
 * @author Henning Treu - initial contribution
 *
 */
public class SitemapResourceTest {

    private static final int STATE_UPDATE_WAIT_TIME = 100;

    private static final String HTTP_HEADER_X_ATMOSPHERE_TRANSPORT = "X-Atmosphere-Transport";
    private static final String ITEM_NAME = "itemName";
    private static final String SITEMAP_PATH = "/sitemaps";
    private static final String SITEMAP_MODEL_NAME = "sitemapModel";
    private static final String SITEMAP_NAME = "defaultSitemap";
    private static final String VISIBILITY_RULE_ITEM_NAME = "visibilityRuleItem";
    private static final String LABEL_COLOR_ITEM_NAME = "labelColorItemName";
    private static final String VALUE_COLOR_ITEM_NAME = "valueColorItemName";

    private SitemapResource sitemapResource;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private SitemapProvider sitemapProvider;

    @Mock
    private Sitemap defaultSitemap;

    @Mock
    private ItemUIRegistry itemUIRegistry;

    @Mock
    private HttpHeaders headers;

    private GenericItem item;
    private GenericItem visibilityRuleItem;
    private GenericItem labelColorItem;
    private GenericItem valueColorItem;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        sitemapResource = new SitemapResource();

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromPath(SITEMAP_PATH));
        when(uriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromPath(SITEMAP_PATH));
        sitemapResource.uriInfo = uriInfo;

        item = new TestItem(ITEM_NAME);
        visibilityRuleItem = new TestItem(VISIBILITY_RULE_ITEM_NAME);
        labelColorItem = new TestItem(LABEL_COLOR_ITEM_NAME);
        valueColorItem = new TestItem(VALUE_COLOR_ITEM_NAME);

        configureSitemapProviderMock();
        configureSitemapMock();
        sitemapResource.addSitemapProvider(sitemapProvider);

        configureItemUIRegistry();
        sitemapResource.setItemUIRegistry(itemUIRegistry);

        // non-null is sufficient here.
        when(headers.getRequestHeader(HTTP_HEADER_X_ATMOSPHERE_TRANSPORT)).thenReturn(Collections.emptyList());
    }

    @Test
    public void whenNoSitemapProvidersAreSet_ShouldReturnEmptyList() {
        sitemapResource.removeSitemapProvider(sitemapProvider);
        Response sitemaps = sitemapResource.getSitemaps();

        assertThat(sitemaps.getEntity(), instanceOf(Collection.class));
        assertThat((Collection<?>) sitemaps.getEntity(), is(empty()));
    }

    @Test
    public void whenSitemapsAreProvided_ShouldReturnSitemapBeans() {
        Response sitemaps = sitemapResource.getSitemaps();

        assertThat((Collection<?>) sitemaps.getEntity(), hasSize(1));

        @SuppressWarnings("unchecked")
        SitemapDTO dto = ((Collection<SitemapDTO>) sitemaps.getEntity()).iterator().next();
        assertThat(dto.name, is(SITEMAP_MODEL_NAME));
        assertThat(dto.link, is(SITEMAP_PATH + "/" + SITEMAP_MODEL_NAME));
    }

    @Test
    public void whenLongPolling_ShouldObserveItems() {
        new Thread(() -> {
            try {
                Thread.sleep(STATE_UPDATE_WAIT_TIME); // wait for the #getPageData call and listeners to attach to the
                                                      // item
                item.setState(new DecimalType(BigDecimal.ONE));
            } catch (InterruptedException e) {
            }
        }).start();

        Response response = sitemapResource.getPageData(headers, null, SITEMAP_MODEL_NAME, SITEMAP_NAME, null);

        PageDTO pageDTO = (PageDTO) response.getEntity();
        assertThat(pageDTO.timeout, is(false)); // assert that the item state change did trigger the blocking method to
                                                // return
    }

    @Test
    public void whenLongPolling_ShouldObserveItemsFromVisibilityRules() {
        new Thread(() -> {
            try {
                Thread.sleep(STATE_UPDATE_WAIT_TIME); // wait for the #getPageData call and listeners to attach to the
                                                      // item
                visibilityRuleItem.setState(new DecimalType(BigDecimal.ONE));
            } catch (InterruptedException e) {
            }
        }).start();

        Response response = sitemapResource.getPageData(headers, null, SITEMAP_MODEL_NAME, SITEMAP_NAME, null);

        PageDTO pageDTO = (PageDTO) response.getEntity();
        assertThat(pageDTO.timeout, is(false)); // assert that the item state change did trigger the blocking method to
                                                // return
    }

    @Test
    public void whenLongPolling_ShouldObserveItemsFromLabelColorConditions() {
        new Thread(() -> {
            try {
                Thread.sleep(STATE_UPDATE_WAIT_TIME); // wait for the #getPageData call and listeners to attach to the
                                                      // item
                labelColorItem.setState(new DecimalType(BigDecimal.ONE));
            } catch (InterruptedException e) {
            }
        }).start();

        Response response = sitemapResource.getPageData(headers, null, SITEMAP_MODEL_NAME, SITEMAP_NAME, null);

        PageDTO pageDTO = (PageDTO) response.getEntity();
        assertThat(pageDTO.timeout, is(false)); // assert that the item state change did trigger the blocking method to
                                                // return
    }

    @Test
    public void whenLongPolling_ShouldObserveItemsFromValueColorConditions() {
        new Thread(() -> {
            try {
                Thread.sleep(STATE_UPDATE_WAIT_TIME); // wait for the #getPageData call and listeners to attach to the
                                                      // item
                valueColorItem.setState(new DecimalType(BigDecimal.ONE));
            } catch (InterruptedException e) {
            }
        }).start();

        Response response = sitemapResource.getPageData(headers, null, SITEMAP_MODEL_NAME, SITEMAP_NAME, null);

        PageDTO pageDTO = (PageDTO) response.getEntity();
        assertThat(pageDTO.timeout, is(false)); // assert that the item state change did trigger the blocking method to
                                                // return
    }

    private void configureItemUIRegistry() throws ItemNotFoundException {
        EList<Widget> widgets = initSitemapWidgets();
        when(itemUIRegistry.getChildren(defaultSitemap)).thenReturn(widgets);
        when(itemUIRegistry.getItem(ITEM_NAME)).thenReturn(item);
        when(itemUIRegistry.getItem(VISIBILITY_RULE_ITEM_NAME)).thenReturn(visibilityRuleItem);
        when(itemUIRegistry.getItem(LABEL_COLOR_ITEM_NAME)).thenReturn(labelColorItem);
        when(itemUIRegistry.getItem(VALUE_COLOR_ITEM_NAME)).thenReturn(valueColorItem);
    }

    private EList<Widget> initSitemapWidgets() {
        Widget w1 = mock(Widget.class);
        when(w1.getItem()).thenReturn(ITEM_NAME);

        // add visibility rules to the mock widget:
        VisibilityRule visibilityRule = mock(VisibilityRule.class);
        when(visibilityRule.getItem()).thenReturn(VISIBILITY_RULE_ITEM_NAME);
        BasicEList<VisibilityRule> visibilityRules = new BasicEList<>(1);
        visibilityRules.add(visibilityRule);
        when(w1.getVisibility()).thenReturn(visibilityRules);

        // add label color conditions to the item:
        ColorArray labelColor = mock(ColorArray.class);
        when(labelColor.getItem()).thenReturn(LABEL_COLOR_ITEM_NAME);
        EList<ColorArray> labelColors = new BasicEList<>();
        labelColors.add(labelColor);
        when(w1.getLabelColor()).thenReturn(labelColors);

        // add value color conditions to the item:
        ColorArray valueColor = mock(ColorArray.class);
        when(valueColor.getItem()).thenReturn(VALUE_COLOR_ITEM_NAME);
        EList<ColorArray> valueColors = new BasicEList<>();
        valueColors.add(valueColor);
        when(w1.getValueColor()).thenReturn(valueColors);

        BasicEList<Widget> widgets = new BasicEList<>(1);
        widgets.add(w1);
        return widgets;
    }

    private void configureSitemapMock() {
        when(defaultSitemap.getName()).thenReturn(SITEMAP_NAME);
    }

    private void configureSitemapProviderMock() {
        when(sitemapProvider.getSitemapNames()).thenReturn(Collections.singleton(SITEMAP_MODEL_NAME));
        when(sitemapProvider.getSitemap(SITEMAP_MODEL_NAME)).thenReturn(defaultSitemap);
    }

    private class TestItem extends GenericItem {

        public TestItem(String name) {
            super("Number", name);
        }

        @Override
        public @NonNull List<@NonNull Class<? extends State>> getAcceptedDataTypes() {
            return Collections.emptyList();
        }

        @Override
        public @NonNull List<@NonNull Class<? extends Command>> getAcceptedCommandTypes() {
            return Collections.emptyList();
        }
    }
}

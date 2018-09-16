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
package org.eclipse.smarthome.ui.internal.proxy;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.sitemap.Image;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.Switch;
import org.eclipse.smarthome.model.sitemap.Video;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link ProxyServletService} class.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class ProxyServletServiceTest {

    static private ProxyServletService service;

    @Before
    public void setUp() {
        service = new ProxyServletService();
    }

    @Test
    public void testMaybeAppendAuthHeaderWithFullCredentials() throws URISyntaxException {
        Request request = mock(Request.class);
        URI uri = new URI("http://testuser:testpassword@127.0.0.1:8080/content");
        service.maybeAppendAuthHeader(uri, request);
        verify(request).header(HttpHeader.AUTHORIZATION,
                "Basic " + B64Code.encode("testuser:testpassword", StringUtil.__ISO_8859_1));
    }

    @Test
    public void testMaybeAppendAuthHeaderWithoutPassword() throws URISyntaxException {
        Request request = mock(Request.class);
        URI uri = new URI("http://testuser@127.0.0.1:8080/content");
        service.maybeAppendAuthHeader(uri, request);
        verify(request).header(HttpHeader.AUTHORIZATION,
                "Basic " + B64Code.encode("testuser:", StringUtil.__ISO_8859_1));
    }

    @Test
    public void testMaybeAppendAuthHeaderWithoutCredentials() throws URISyntaxException {
        Request request = mock(Request.class);
        URI uri = new URI("http://127.0.0.1:8080/content");
        service.maybeAppendAuthHeader(uri, request);
        verify(request, never()).header(any(HttpHeader.class), anyString());
    }

    @Test
    public void testProxyUriUnexpectedWidgetType() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Switch widget = mock(Switch.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);

        URI uri = service.uriFromRequest(request);
        assertNull(uri);
    }

    @Test
    public void testProxyUriImageWithoutItemButValidUrl() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Image widget = mock(Image.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);
        when(widget.getUrl()).thenReturn("https://www.eclipse.org/smarthome/test.jpg");
        when(widget.getItem()).thenReturn(null);

        URI uri = service.uriFromRequest(request);
        assertNotNull(uri);
        assertEquals(uri.toString(), "https://www.eclipse.org/smarthome/test.jpg");
    }

    @Test
    public void testProxyUriImageWithoutItemAndInvalidUrl() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Image widget = mock(Image.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);
        when(widget.getUrl()).thenReturn("test");
        when(widget.getItem()).thenReturn(null);

        URI uri = service.uriFromRequest(request);
        assertNull(uri);
    }

    @Test
    public void testProxyUriImageWithItemButUndefState() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Image widget = mock(Image.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);
        when(widget.getUrl()).thenReturn("https://www.eclipse.org/smarthome/test.jpg");
        when(widget.getItem()).thenReturn("testItem");
        when(itemUIRegistry.getItemState(eq("testItem"))).thenReturn(UnDefType.UNDEF);

        URI uri = service.uriFromRequest(request);
        assertNotNull(uri);
        assertEquals(uri.toString(), "https://www.eclipse.org/smarthome/test.jpg");
    }

    @Test
    public void testProxyUriImageWithItemButNullState() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Image widget = mock(Image.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);
        when(widget.getUrl()).thenReturn("https://www.eclipse.org/smarthome/test.jpg");
        when(widget.getItem()).thenReturn("testItem");
        when(itemUIRegistry.getItemState(eq("testItem"))).thenReturn(UnDefType.NULL);

        URI uri = service.uriFromRequest(request);
        assertNotNull(uri);
        assertEquals(uri.toString(), "https://www.eclipse.org/smarthome/test.jpg");
    }

    @Test
    public void testProxyUriImageWithItemButUnexpectedState() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Image widget = mock(Image.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);
        when(widget.getUrl()).thenReturn("https://www.eclipse.org/smarthome/test.jpg");
        when(widget.getItem()).thenReturn("testItem");
        when(itemUIRegistry.getItemState(eq("testItem"))).thenReturn(OnOffType.ON);

        URI uri = service.uriFromRequest(request);
        assertNotNull(uri);
        assertEquals(uri.toString(), "https://www.eclipse.org/smarthome/test.jpg");
    }

    @Test
    public void testProxyUriImageWithItemButStateWithInvalidUrl() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Image widget = mock(Image.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);
        when(widget.getUrl()).thenReturn("https://www.eclipse.org/smarthome/test.jpg");
        when(widget.getItem()).thenReturn("testItem");
        when(itemUIRegistry.getItemState(eq("testItem"))).thenReturn(new StringType("test"));

        URI uri = service.uriFromRequest(request);
        assertNotNull(uri);
        assertEquals(uri.toString(), "https://www.eclipse.org/smarthome/test.jpg");
    }

    @Test
    public void testProxyUriImageWithItemAndStateWithValidUrl() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Image widget = mock(Image.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);
        when(widget.getUrl()).thenReturn("https://www.eclipse.org/smarthome/test.jpg");
        when(widget.getItem()).thenReturn("testItem");
        when(itemUIRegistry.getItemState(eq("testItem")))
                .thenReturn(new StringType("https://www.eclipse.org/smarthome/item.jpg"));

        URI uri = service.uriFromRequest(request);
        assertNotNull(uri);
        assertEquals(uri.toString(), "https://www.eclipse.org/smarthome/item.jpg");
    }

    @Test
    public void testProxyUriVideoWithoutItemButValidUrl() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Video widget = mock(Video.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);
        when(widget.getUrl()).thenReturn("https://www.eclipse.org/smarthome/test.mp4");
        when(widget.getItem()).thenReturn(null);

        URI uri = service.uriFromRequest(request);
        assertNotNull(uri);
        assertEquals(uri.toString(), "https://www.eclipse.org/smarthome/test.mp4");
    }

    @Test
    public void testProxyUriVideoWithoutItemAndInvalidUrl() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Video widget = mock(Video.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);
        when(widget.getUrl()).thenReturn("test");
        when(widget.getItem()).thenReturn(null);

        URI uri = service.uriFromRequest(request);
        assertNull(uri);
    }

    @Test
    public void testProxyUriVideoWithItemButUndefState() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Video widget = mock(Video.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);
        when(widget.getUrl()).thenReturn("https://www.eclipse.org/smarthome/test.mp4");
        when(widget.getItem()).thenReturn("testItem");
        when(itemUIRegistry.getItemState(eq("testItem"))).thenReturn(UnDefType.UNDEF);

        URI uri = service.uriFromRequest(request);
        assertNotNull(uri);
        assertEquals(uri.toString(), "https://www.eclipse.org/smarthome/test.mp4");
    }

    @Test
    public void testProxyUriVideoWithItemButNullState() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Video widget = mock(Video.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);
        when(widget.getUrl()).thenReturn("https://www.eclipse.org/smarthome/test.mp4");
        when(widget.getItem()).thenReturn("testItem");
        when(itemUIRegistry.getItemState(eq("testItem"))).thenReturn(UnDefType.NULL);

        URI uri = service.uriFromRequest(request);
        assertNotNull(uri);
        assertEquals(uri.toString(), "https://www.eclipse.org/smarthome/test.mp4");
    }

    @Test
    public void testProxyUriVideoWithItemButUnexpectedState() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Video widget = mock(Video.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);
        when(widget.getUrl()).thenReturn("https://www.eclipse.org/smarthome/test.mp4");
        when(widget.getItem()).thenReturn("testItem");
        when(itemUIRegistry.getItemState(eq("testItem"))).thenReturn(OnOffType.ON);

        URI uri = service.uriFromRequest(request);
        assertNotNull(uri);
        assertEquals(uri.toString(), "https://www.eclipse.org/smarthome/test.mp4");
    }

    @Test
    public void testProxyUriVideoWithItemButStateWithInvalidUrl() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Video widget = mock(Video.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);
        when(widget.getUrl()).thenReturn("https://www.eclipse.org/smarthome/test.mp4");
        when(widget.getItem()).thenReturn("testItem");
        when(itemUIRegistry.getItemState(eq("testItem"))).thenReturn(new StringType("test"));

        URI uri = service.uriFromRequest(request);
        assertNotNull(uri);
        assertEquals(uri.toString(), "https://www.eclipse.org/smarthome/test.mp4");
    }

    @Test
    public void testProxyUriVideoWithItemAndStateWithValidUrl() {
        ItemUIRegistry itemUIRegistry = mock(ItemUIRegistry.class);
        ModelRepository modelRepository = mock(ModelRepository.class);
        Sitemap sitemap = mock(Sitemap.class);
        Video widget = mock(Video.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        service.setModelRepository(modelRepository);
        service.setItemUIRegistry(itemUIRegistry);

        when(request.getParameter(eq("sitemap"))).thenReturn("testSitemap");
        when(request.getParameter(eq("widgetId"))).thenReturn("testWidget");
        when(modelRepository.getModel(eq("testSitemap"))).thenReturn(sitemap);
        when(itemUIRegistry.getWidget(eq(sitemap), eq("testWidget"))).thenReturn(widget);
        when(widget.getUrl()).thenReturn("https://www.eclipse.org/smarthome/test.mp4");
        when(widget.getItem()).thenReturn("testItem");
        when(itemUIRegistry.getItemState(eq("testItem")))
                .thenReturn(new StringType("https://www.eclipse.org/smarthome/item.mp4"));

        URI uri = service.uriFromRequest(request);
        assertNotNull(uri);
        assertEquals(uri.toString(), "https://www.eclipse.org/smarthome/item.mp4");
    }

}
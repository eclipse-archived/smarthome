/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.StateChangeListener;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.eclipse.smarthome.io.rest.core.item.EnrichedItemDTOMapper;
import org.eclipse.smarthome.model.sitemap.Chart;
import org.eclipse.smarthome.model.sitemap.Frame;
import org.eclipse.smarthome.model.sitemap.Image;
import org.eclipse.smarthome.model.sitemap.LinkableWidget;
import org.eclipse.smarthome.model.sitemap.List;
import org.eclipse.smarthome.model.sitemap.Mapping;
import org.eclipse.smarthome.model.sitemap.Mapview;
import org.eclipse.smarthome.model.sitemap.Selection;
import org.eclipse.smarthome.model.sitemap.Setpoint;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.SitemapProvider;
import org.eclipse.smarthome.model.sitemap.Slider;
import org.eclipse.smarthome.model.sitemap.Switch;
import org.eclipse.smarthome.model.sitemap.Video;
import org.eclipse.smarthome.model.sitemap.Webview;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * <p>
 * This class acts as a REST resource for sitemaps and provides different methods to interact with them, like retrieving
 * a list of all available sitemaps or just getting the widgets of a single page.
 * </p>
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Chris Jackson
 * @author Yordan Zhelev - Added Swagger annotations
 */
@Path(SitemapResource.PATH_SITEMAPS)
@Api
public class SitemapResource implements RESTResource {

    private final Logger logger = LoggerFactory.getLogger(SitemapResource.class);

    public static final String PATH_SITEMAPS = "sitemaps";

    private static final long TIMEOUT_IN_MS = 30000;

    @Context
    UriInfo uriInfo;

    private ItemUIRegistry itemUIRegistry;

    private java.util.List<SitemapProvider> sitemapProviders = new ArrayList<>();

    public void setItemUIRegistry(ItemUIRegistry itemUIRegistry) {
        this.itemUIRegistry = itemUIRegistry;
    }

    public void unsetItemUIRegistry(ItemUIRegistry itemUIRegistry) {
        this.itemUIRegistry = null;
    }

    public void addSitemapProvider(SitemapProvider provider) {
        sitemapProviders.add(provider);
    }

    public void removeSitemapProvider(SitemapProvider provider) {
        sitemapProviders.remove(provider);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get all available sitemaps.", response = SitemapDTO.class, responseContainer = "Collection")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getSitemaps() {
        logger.debug("Received HTTP GET request at '{}'", uriInfo.getPath());
        Object responseObject = getSitemapBeans(uriInfo.getAbsolutePathBuilder().build());
        return Response.ok(responseObject).build();
    }

    @GET
    @Path("/{sitemapname: [a-zA-Z_0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get sitemap by name.", response = SitemapDTO.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getSitemapData(@Context HttpHeaders headers,
            @PathParam("sitemapname") @ApiParam(value = "sitemap name") String sitemapname,
            @QueryParam("type") String type, @QueryParam("jsoncallback") @DefaultValue("callback") String callback) {
        logger.debug("Received HTTP GET request at '{}' for media type '{}'.",
                new Object[] { uriInfo.getPath(), type });
        Object responseObject = getSitemapBean(sitemapname, uriInfo.getBaseUriBuilder().build());
        return Response.ok(responseObject).build();
    }

    @GET
    @Path("/{sitemapname: [a-zA-Z_0-9]*}/{pageid: [a-zA-Z_0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Polls the data for a sitemap.", response = PageDTO.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Sitemap with requested name does not exist or page does not exist, or page refers to a non-linkable widget") })
    public Response getPageData(@Context HttpHeaders headers,
            @PathParam("sitemapname") @ApiParam(value = "sitemap name") String sitemapname,
            @PathParam("pageid") @ApiParam(value = "page id") String pageId) {
        logger.debug("Received HTTP GET request at '{}'", uriInfo.getPath());

        if (headers.getRequestHeader("X-Atmosphere-Transport") != null) {
            // Make the REST-API pseudo-compatible with openHAB 1.x
            // The client asks Atmosphere for server push functionality,
            // so we do a simply listening for changes on the appropriate items
            blockUnlessChangeOccurs(sitemapname, pageId);
        }
        Object responseObject = getPageBean(sitemapname, pageId, uriInfo.getBaseUriBuilder().build());
        return Response.ok(responseObject).build();
    }

    private PageDTO getPageBean(String sitemapName, String pageId, URI uri) {
        Sitemap sitemap = getSitemap(sitemapName);
        if (sitemap != null) {
            if (pageId.equals(sitemap.getName())) {
                return createPageBean(sitemapName, sitemap.getLabel(), sitemap.getIcon(), sitemap.getName(),
                        sitemap.getChildren(), false, isLeaf(sitemap.getChildren()), uri);
            } else {
                Widget pageWidget = itemUIRegistry.getWidget(sitemap, pageId);
                if (pageWidget instanceof LinkableWidget) {
                    EList<Widget> children = itemUIRegistry.getChildren((LinkableWidget) pageWidget);
                    PageDTO pageBean = createPageBean(sitemapName, itemUIRegistry.getLabel(pageWidget),
                            itemUIRegistry.getCategory(pageWidget), pageId, children, false, isLeaf(children), uri);
                    EObject parentPage = pageWidget.eContainer();
                    while (parentPage instanceof Frame) {
                        parentPage = parentPage.eContainer();
                    }
                    if (parentPage instanceof Widget) {
                        String parentId = itemUIRegistry.getWidgetId((Widget) parentPage);
                        pageBean.parent = getPageBean(sitemapName, parentId, uri);
                        pageBean.parent.widgets = null;
                        pageBean.parent.parent = null;
                    } else if (parentPage instanceof Sitemap) {
                        pageBean.parent = getPageBean(sitemapName, sitemap.getName(), uri);
                        pageBean.parent.widgets = null;
                    }
                    return pageBean;
                } else {
                    if (logger.isDebugEnabled()) {
                        if (pageWidget == null) {
                            logger.debug("Received HTTP GET request at '{}' for the unknown page id '{}'.", uri,
                                    pageId);
                        } else {
                            logger.debug(
                                    "Received HTTP GET request at '{}' for the page id '{}'. "
                                            + "This id refers to a non-linkable widget and is therefore no valid page id.",
                                    uri, pageId);
                        }
                    }
                    throw new WebApplicationException(404);
                }
            }
        } else {
            logger.info("Received HTTP GET request at '{}' for the unknown sitemap '{}'.", uri, sitemapName);
            throw new WebApplicationException(404);
        }
    }

    public Collection<SitemapDTO> getSitemapBeans(URI uri) {
        Collection<SitemapDTO> beans = new LinkedList<SitemapDTO>();
        logger.debug("Received HTTP GET request at '{}'.", UriBuilder.fromUri(uri).build().toASCIIString());
        for (SitemapProvider provider : sitemapProviders) {
            for (String modelName : provider.getSitemapNames()) {
                Sitemap sitemap = provider.getSitemap(modelName);
                if (sitemap != null) {
                    SitemapDTO bean = new SitemapDTO();
                    bean.name = modelName;
                    bean.icon = sitemap.getIcon();
                    bean.label = sitemap.getLabel();
                    bean.link = UriBuilder.fromUri(uri).path(bean.name).build().toASCIIString();
                    bean.homepage = new PageDTO();
                    bean.homepage.link = bean.link + "/" + sitemap.getName();
                    beans.add(bean);
                }
            }
        }
        return beans;
    }

    public SitemapDTO getSitemapBean(String sitemapname, URI uri) {
        Sitemap sitemap = getSitemap(sitemapname);
        if (sitemap != null) {
            return createSitemapBean(sitemapname, sitemap, uri);
        } else {
            logger.info("Received HTTP GET request at '{}' for the unknown sitemap '{}'.", uriInfo.getPath(),
                    sitemapname);
            throw new WebApplicationException(404);
        }
    }

    private SitemapDTO createSitemapBean(String sitemapName, Sitemap sitemap, URI uri) {
        SitemapDTO bean = new SitemapDTO();

        bean.name = sitemapName;
        bean.icon = sitemap.getIcon();
        bean.label = sitemap.getLabel();

        bean.link = UriBuilder.fromUri(uri).path(SitemapResource.PATH_SITEMAPS).path(bean.name).build().toASCIIString();
        bean.homepage = createPageBean(sitemap.getName(), sitemap.getLabel(), sitemap.getIcon(), sitemap.getName(),
                sitemap.getChildren(), true, false, uri);
        return bean;
    }

    private PageDTO createPageBean(String sitemapName, String title, String icon, String pageId, EList<Widget> children,
            boolean drillDown, boolean isLeaf, URI uri) {
        PageDTO bean = new PageDTO();
        bean.id = pageId;
        bean.title = title;
        bean.icon = icon;
        bean.leaf = isLeaf;
        bean.link = UriBuilder.fromUri(uri).path(PATH_SITEMAPS).path(sitemapName).path(pageId).build().toASCIIString();
        if (children != null) {
            int cntWidget = 0;
            for (Widget widget : children) {
                String widgetId = pageId + "_" + cntWidget;
                WidgetDTO subWidget = createWidgetBean(sitemapName, widget, drillDown, uri, widgetId);
                if (subWidget != null)
                    bean.widgets.add(subWidget);
                cntWidget++;
            }
        } else {
            bean.widgets = null;
        }
        return bean;
    }

    private WidgetDTO createWidgetBean(String sitemapName, Widget widget, boolean drillDown, URI uri, String widgetId) {
        // Test visibility
        if (itemUIRegistry.getVisiblity(widget) == false)
            return null;

        WidgetDTO bean = new WidgetDTO();
        if (widget.getItem() != null) {
            try {
                Item item = itemUIRegistry.getItem(widget.getItem());
                if (item != null) {
                    bean.item = EnrichedItemDTOMapper.map(item, false, UriBuilder.fromUri(uri).build());
                }
            } catch (ItemNotFoundException e) {
                logger.debug(e.getMessage());
            }
        }
        bean.widgetId = widgetId;
        bean.icon = itemUIRegistry.getCategory(widget);
        bean.labelcolor = itemUIRegistry.getLabelColor(widget);
        bean.valuecolor = itemUIRegistry.getValueColor(widget);
        bean.label = itemUIRegistry.getLabel(widget);
        bean.type = widget.eClass().getName();
        if (widget instanceof LinkableWidget) {
            LinkableWidget linkableWidget = (LinkableWidget) widget;
            EList<Widget> children = itemUIRegistry.getChildren(linkableWidget);
            if (widget instanceof Frame) {
                int cntWidget = 0;
                for (Widget child : children) {
                    widgetId += "_" + cntWidget;
                    WidgetDTO subWidget = createWidgetBean(sitemapName, child, drillDown, uri, widgetId);
                    if (subWidget != null) {
                        bean.widgets.add(subWidget);
                        cntWidget++;
                    }
                }
            } else if (children.size() > 0) {
                String pageName = itemUIRegistry.getWidgetId(linkableWidget);
                bean.linkedPage = createPageBean(sitemapName, itemUIRegistry.getLabel(widget),
                        itemUIRegistry.getCategory(widget), pageName, drillDown ? children : null, drillDown,
                        isLeaf(children), uri);
            }
        }
        if (widget instanceof Switch) {
            Switch switchWidget = (Switch) widget;
            for (Mapping mapping : switchWidget.getMappings()) {
                MappingDTO mappingBean = new MappingDTO();
                // Remove quotes - if they exist
                if (mapping.getCmd() != null) {
                    if (mapping.getCmd().startsWith("\"") && mapping.getCmd().endsWith("\"")) {
                        mappingBean.command = mapping.getCmd().substring(1, mapping.getCmd().length() - 1);
                    } else {
                        mappingBean.command = mapping.getCmd();
                    }
                }
                mappingBean.label = mapping.getLabel();
                bean.mappings.add(mappingBean);
            }
        }
        if (widget instanceof Selection) {
            Selection selectionWidget = (Selection) widget;
            for (Mapping mapping : selectionWidget.getMappings()) {
                MappingDTO mappingBean = new MappingDTO();
                // Remove quotes - if they exist
                if (mapping.getCmd() != null) {
                    if (mapping.getCmd().startsWith("\"") && mapping.getCmd().endsWith("\"")) {
                        mappingBean.command = mapping.getCmd().substring(1, mapping.getCmd().length() - 1);
                    } else {
                        mappingBean.command = mapping.getCmd();
                    }
                }
                mappingBean.label = mapping.getLabel();
                bean.mappings.add(mappingBean);
            }
        }
        if (widget instanceof Slider) {
            Slider sliderWidget = (Slider) widget;
            bean.sendFrequency = sliderWidget.getFrequency();
            bean.switchSupport = sliderWidget.isSwitchEnabled();
        }
        if (widget instanceof List) {
            List listWidget = (List) widget;
            bean.separator = listWidget.getSeparator();
        }
        if (widget instanceof Image) {
            Image imageWidget = (Image) widget;
            String wId = itemUIRegistry.getWidgetId(widget);
            if (uri.getPort() < 0 || uri.getPort() == 80) {
                bean.url = uri.getScheme() + "://" + uri.getHost() + "/proxy?sitemap=" + sitemapName
                        + ".sitemap&widgetId=" + wId;
            } else {
                bean.url = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + "/proxy?sitemap="
                        + sitemapName + ".sitemap&widgetId=" + wId;
            }
            if (imageWidget.getRefresh() > 0) {
                bean.refresh = imageWidget.getRefresh();
            }
        }
        if (widget instanceof Video) {
            String wId = itemUIRegistry.getWidgetId(widget);
            if (uri.getPort() < 0 || uri.getPort() == 80) {
                bean.url = uri.getScheme() + "://" + uri.getHost() + "/proxy?sitemap=" + sitemapName
                        + ".sitemap&widgetId=" + wId;
            } else {
                bean.url = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + "/proxy?sitemap="
                        + sitemapName + ".sitemap&widgetId=" + wId;
            }
        }
        if (widget instanceof Webview) {
            Webview webViewWidget = (Webview) widget;
            bean.url = webViewWidget.getUrl();
            bean.height = webViewWidget.getHeight();
        }
        if (widget instanceof Mapview) {
            Mapview mapViewWidget = (Mapview) widget;
            bean.height = mapViewWidget.getHeight();
        }
        if (widget instanceof Chart) {
            Chart chartWidget = (Chart) widget;
            bean.service = chartWidget.getService();
            bean.period = chartWidget.getPeriod();
            if (chartWidget.getRefresh() > 0) {
                bean.refresh = chartWidget.getRefresh();
            }
        }
        if (widget instanceof Setpoint) {
            Setpoint setpointWidget = (Setpoint) widget;
            bean.minValue = setpointWidget.getMinValue();
            bean.maxValue = setpointWidget.getMaxValue();
            bean.step = setpointWidget.getStep();
        }
        return bean;
    }

    private boolean isLeaf(EList<Widget> children) {
        for (Widget w : children) {
            if (w instanceof Frame) {
                if (isLeaf(((Frame) w).getChildren())) {
                    return false;
                }
            } else if (w instanceof LinkableWidget) {
                LinkableWidget linkableWidget = (LinkableWidget) w;
                if (itemUIRegistry.getChildren(linkableWidget).size() > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private Sitemap getSitemap(String sitemapname) {
        for (SitemapProvider provider : sitemapProviders) {
            Sitemap sitemap = provider.getSitemap(sitemapname);
            if (sitemap != null) {
                return sitemap;
            }
        }

        return null;
    }

    private void blockUnlessChangeOccurs(String sitemapname, String pageId) {
        Sitemap sitemap = getSitemap(sitemapname);
        if (sitemap != null) {
            if (pageId.equals(sitemap.getName())) {
                waitForChanges(sitemap.getChildren());
            } else {
                Widget pageWidget = itemUIRegistry.getWidget(sitemap, pageId);
                if (pageWidget instanceof LinkableWidget) {
                    EList<Widget> children = itemUIRegistry.getChildren((LinkableWidget) pageWidget);
                    waitForChanges(children);
                }
            }
        }
    }

    /**
     * This method only returns when a change has occurred to any item on the
     * page to display or if the timeout is reached
     *
     * @param widgets
     *            the widgets of the page to observe
     */
    private boolean waitForChanges(EList<Widget> widgets) {
        long startTime = (new Date()).getTime();
        boolean timeout = false;
        BlockingStateChangeListener listener = new BlockingStateChangeListener();
        // let's get all items for these widgets
        Set<GenericItem> items = getAllItems(widgets);
        for (GenericItem item : items) {
            item.addStateChangeListener(listener);
        }
        while (!listener.hasChangeOccurred() && !timeout) {
            timeout = (new Date()).getTime() - startTime > TIMEOUT_IN_MS;
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                timeout = true;
                break;
            }
        }
        for (GenericItem item : items) {
            item.removeStateChangeListener(listener);
        }
        return !timeout;
    }

    /**
     * Collects all items that are represented by a given list of widgets
     *
     * @param widgets
     *            the widget list to get the items for added to all bundles containing REST resources
     * @return all items that are represented by the list of widgets
     */
    private Set<GenericItem> getAllItems(EList<Widget> widgets) {
        Set<GenericItem> items = new HashSet<GenericItem>();
        if (itemUIRegistry != null) {
            for (Widget widget : widgets) {
                String itemName = widget.getItem();
                if (itemName != null) {
                    try {
                        Item item = itemUIRegistry.getItem(itemName);
                        if (item instanceof GenericItem) {
                            final GenericItem gItem = (GenericItem) item;
                            items.add(gItem);
                        }
                    } catch (ItemNotFoundException e) {
                        // ignore
                    }
                } else {
                    if (widget instanceof Frame) {
                        items.addAll(getAllItems(((Frame) widget).getChildren()));
                    }
                }
            }
        }
        return items;
    }

    /**
     * This is a state change listener, which is merely used to determine, if a
     * state change has occurred on one of a list of items.
     *
     * @author Kai Kreuzer - Initial contribution and API
     *
     */
    private static class BlockingStateChangeListener implements StateChangeListener {

        private boolean changed = false;

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(Item item, State oldState, State newState) {
            changed = true;
        }

        /**
         * determines, whether a state change has occurred since its creation
         *
         * @return true, if a state has changed
         */
        public boolean hasChangeOccurred() {
            return changed;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateUpdated(Item item, State state) {
            // ignore if the state did not change
        }
    }

}

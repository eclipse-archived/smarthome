/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschränkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap.internal;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;

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

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.io.rest.MediaTypeHelper;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.eclipse.smarthome.io.rest.item.ItemResource;
import org.eclipse.smarthome.io.rest.sitemap.internal.beans.MappingBean;
import org.eclipse.smarthome.io.rest.sitemap.internal.beans.PageBean;
import org.eclipse.smarthome.io.rest.sitemap.internal.beans.SitemapBean;
import org.eclipse.smarthome.io.rest.sitemap.internal.beans.SitemapListBean;
import org.eclipse.smarthome.io.rest.sitemap.internal.beans.WidgetBean;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.sitemap.Chart;
import org.eclipse.smarthome.model.sitemap.Frame;
import org.eclipse.smarthome.model.sitemap.Image;
import org.eclipse.smarthome.model.sitemap.LinkableWidget;
import org.eclipse.smarthome.model.sitemap.List;
import org.eclipse.smarthome.model.sitemap.Mapping;
import org.eclipse.smarthome.model.sitemap.Selection;
import org.eclipse.smarthome.model.sitemap.Setpoint;
import org.eclipse.smarthome.model.sitemap.Sitemap;
import org.eclipse.smarthome.model.sitemap.Slider;
import org.eclipse.smarthome.model.sitemap.Switch;
import org.eclipse.smarthome.model.sitemap.Video;
import org.eclipse.smarthome.model.sitemap.Webview;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.json.JSONWithPadding;

/**
 * <p>This class acts as a REST resource for sitemaps and provides different methods to interact with them,
 * like retrieving a list of all available sitemaps or just getting the widgets of a single page.</p>
 * 
 * <p>The typical content types are XML or JSON.</p>
 * 
 * <p>This resource is registered with the Jersey servlet.</p>
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Chris Jackson
 */
@Path(SitemapResource.PATH_SITEMAPS)
public class SitemapResource implements RESTResource {

	private static final Logger logger = LoggerFactory.getLogger(SitemapResource.class); 

    protected static final String SITEMAP_FILEEXT = ".sitemap";

	public static final String PATH_SITEMAPS = "sitemaps";
    
	@Context UriInfo uriInfo;

	static private ItemUIRegistry itemUIRegistry;

	static private ModelRepository modelRepository;
	
	public void setItemRegistry(ItemUIRegistry itemUIRegistry) {
		SitemapResource.itemUIRegistry = itemUIRegistry;
	}
	
	public void unsetItemRegistry(ItemUIRegistry itemUIRegistry) {
		SitemapResource.itemUIRegistry = null;
	}	
	
	static public ItemUIRegistry getItemUIRegistry() {
		return itemUIRegistry;
	}

	public void setModelRepository(ModelRepository modelRepository) {
		SitemapResource.modelRepository = modelRepository;
	}
	
	public void unsetModelRepository(ModelRepository modelRepository) {
		SitemapResource.modelRepository = null;
	}
	
	static public ModelRepository getModelRepository() {
		return modelRepository;
	}

	@GET
    @Produces( { MediaType.WILDCARD })
    public Response getSitemaps(
    		@Context HttpHeaders headers,
    		@QueryParam("type") String type, 
    		@QueryParam("jsoncallback") @DefaultValue("callback") String callback) {
		logger.debug("Received HTTP GET request at '{}' for media type '{}'.", new Object[] { uriInfo.getPath(), type });
		String responseType = MediaTypeHelper.getResponseMediaType(headers.getAcceptableMediaTypes(), type);
		if(responseType!=null) {
	    	Object responseObject = responseType.equals(MediaTypeHelper.APPLICATION_X_JAVASCRIPT) ?
	    			new JSONWithPadding(new SitemapListBean(getSitemapBeans(uriInfo.getAbsolutePathBuilder().build())), callback) : new SitemapListBean(getSitemapBeans(uriInfo.getAbsolutePathBuilder().build()));
	    	return Response.ok(responseObject, responseType).build();
		} else {
			return Response.notAcceptable(null).build();
		}
    }

	@GET @Path("/{sitemapname: [a-zA-Z_0-9]*}")
    @Produces( { MediaType.WILDCARD })
    public Response getSitemapData(
    		@Context HttpHeaders headers,
    		@PathParam("sitemapname") String sitemapname, 
    		@QueryParam("type") String type, 
    		@QueryParam("jsoncallback") @DefaultValue("callback") String callback) {
		logger.debug("Received HTTP GET request at '{}' for media type '{}'.", new Object[] { uriInfo.getPath(), type });
		String responseType = MediaTypeHelper.getResponseMediaType(headers.getAcceptableMediaTypes(), type);
		if(responseType!=null) {
	    	Object responseObject = responseType.equals(MediaTypeHelper.APPLICATION_X_JAVASCRIPT) ?
	    			new JSONWithPadding(getSitemapBean(sitemapname, uriInfo.getBaseUriBuilder().build()), callback) : getSitemapBean(sitemapname, uriInfo.getBaseUriBuilder().build());
	    	return Response.ok(responseObject, responseType).build();
		} else {
			return Response.notAcceptable(null).build();
		}
    }

    @GET @Path("/{sitemapname: [a-zA-Z_0-9]*}/{pageid: [a-zA-Z_0-9]*}")
	@Produces( { MediaType.WILDCARD })
    public Response getPageData(
    		@Context HttpHeaders headers,
    		@PathParam("sitemapname") String sitemapname,
    		@PathParam("pageid") String pageId,
    		@QueryParam("type") String type, 
    		@QueryParam("jsoncallback") @DefaultValue("callback") String callback) {
		logger.debug("Received HTTP GET request at '{}' for media type '{}'.", new Object[] { uriInfo.getPath(), type });

		String responseType = MediaTypeHelper.getResponseMediaType(headers.getAcceptableMediaTypes(), type);
		if(responseType!=null) {
	    	Object responseObject = responseType.equals(MediaTypeHelper.APPLICATION_X_JAVASCRIPT) ?
	    			new JSONWithPadding(getPageBean(sitemapname, pageId, uriInfo.getBaseUriBuilder().build()), callback) : getPageBean(sitemapname, pageId, uriInfo.getBaseUriBuilder().build());
	    	throw new WebApplicationException(Response.ok(responseObject, responseType).build());
		} else {
			throw new WebApplicationException(Response.notAcceptable(null).build());
		}
    }
	
    static public PageBean getPageBean(String sitemapName, String pageId, URI uri) {
		Sitemap sitemap = getSitemap(sitemapName);
		if(sitemap!=null) {
			if(pageId.equals(sitemap.getName())) {
				return createPageBean(sitemapName, sitemap.getLabel(), sitemap.getIcon(), sitemap.getName(), sitemap.getChildren(), false, isLeaf(sitemap.getChildren()), uri);
			} else {
				Widget pageWidget = itemUIRegistry.getWidget(sitemap, pageId);
				if(pageWidget instanceof LinkableWidget) {
					EList<Widget> children = itemUIRegistry.getChildren((LinkableWidget) pageWidget);
					PageBean pageBean = createPageBean(sitemapName, itemUIRegistry.getLabel(pageWidget), itemUIRegistry.getIcon(pageWidget), 
							pageId, children, false, isLeaf(children), uri);
					EObject parentPage = pageWidget.eContainer();
					while(parentPage instanceof Frame) {
						parentPage = parentPage.eContainer();
					}
					if(parentPage instanceof Widget) {
						String parentId = itemUIRegistry.getWidgetId((Widget) parentPage);
						pageBean.parent = getPageBean(sitemapName, parentId, uri);
						pageBean.parent.widgets = null;
						pageBean.parent.parent = null;
					} else if(parentPage instanceof Sitemap) {
						pageBean.parent = getPageBean(sitemapName, sitemap.getName(), uri);
						pageBean.parent.widgets = null;
					}
					return pageBean;
				} else {
					if(logger.isDebugEnabled()) {
						if(pageWidget==null) {
			    			logger.debug("Received HTTP GET request at '{}' for the unknown page id '{}'.", uri, pageId);
						} else {
			    			logger.debug("Received HTTP GET request at '{}' for the page id '{}'. " + 
			    					"This id refers to a non-linkable widget and is therefore no valid page id.", uri, pageId);
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

	public Collection<SitemapBean> getSitemapBeans(URI uri) {
		Collection<SitemapBean> beans = new LinkedList<SitemapBean>();
		logger.debug("Received HTTP GET request at '{}'.", UriBuilder.fromUri(uri).build().toASCIIString());
		for(String modelName : modelRepository.getAllModelNamesOfType("sitemap")) {
			Sitemap sitemap = (Sitemap) modelRepository.getModel(modelName);
			if(sitemap!=null) {
				SitemapBean bean = new SitemapBean();
				bean.name = StringUtils.removeEnd(modelName, SITEMAP_FILEEXT);
				bean.icon = sitemap.getIcon();
				bean.label = sitemap.getLabel();
				bean.link = UriBuilder.fromUri(uri).path(bean.name).build().toASCIIString();
				bean.homepage = new PageBean();
				bean.homepage.link = bean.link + "/" + sitemap.getName();
				beans.add(bean);
			}
		}
		return beans;
	}

	public SitemapBean getSitemapBean(String sitemapname, URI uri) {
		Sitemap sitemap = getSitemap(sitemapname);
		if(sitemap!=null) {
			return createSitemapBean(sitemapname, sitemap, uri);
		} else {
			logger.info("Received HTTP GET request at '{}' for the unknown sitemap '{}'.", uriInfo.getPath(), sitemapname);
			throw new WebApplicationException(404);
		}
	}

	private SitemapBean createSitemapBean(String sitemapName, Sitemap sitemap, URI uri) {
    	SitemapBean bean = new SitemapBean();
		
    	bean.name = sitemapName;
		bean.icon = sitemap.getIcon();
		bean.label = sitemap.getLabel();

    	bean.link = UriBuilder.fromUri(uri).path(SitemapResource.PATH_SITEMAPS).path(bean.name).build().toASCIIString();
    	bean.homepage = createPageBean(sitemap.getName(), sitemap.getLabel(), sitemap.getIcon(), sitemap.getName(), sitemap.getChildren(), true, false, uri);
    	return bean;
    }
    
    static private PageBean createPageBean(String sitemapName, String title, String icon, String pageId, EList<Widget> children, boolean drillDown, boolean isLeaf, URI uri) {
    	PageBean bean = new PageBean();
    	bean.id = pageId;
    	bean.title = title;
    	bean.icon = icon;
		bean.leaf = isLeaf;
    	bean.link = UriBuilder.fromUri(uri).path(PATH_SITEMAPS).path(sitemapName).path(pageId).build().toASCIIString();
    	if(children!=null) {
    		int cntWidget = 0;
	    	for(Widget widget : children) {
	    		String widgetId = pageId + "_" + cntWidget;
	    		WidgetBean subWidget = createWidgetBean(sitemapName, widget, drillDown, uri, widgetId);
				if(subWidget != null)
	    		bean.widgets.add(subWidget);
	    		cntWidget++;
	    	}
    	} else {
    		bean.widgets = null;
    	}
		return bean;
	}

	static private WidgetBean createWidgetBean(String sitemapName, Widget widget, boolean drillDown, URI uri, String widgetId) {
		// Test visibility
		if(itemUIRegistry.getVisiblity(widget) == false)
			return null;

    	WidgetBean bean = new WidgetBean();
    	if(widget.getItem()!=null) {
    		Item item = ItemResource.getItem(widget.getItem());
        	if(item!=null) {
        		bean.item = ItemResource.createItemBean(item, false, UriBuilder.fromUri(uri).build().toASCIIString());
        	}
    	}
    	bean.widgetId = widgetId;
    	bean.icon = itemUIRegistry.getIcon(widget);
		bean.labelcolor = itemUIRegistry.getLabelColor(widget);
		bean.valuecolor = itemUIRegistry.getValueColor(widget);
    	bean.label = itemUIRegistry.getLabel(widget);
    	bean.type = widget.eClass().getName();
    	if (widget instanceof LinkableWidget) {
			LinkableWidget linkableWidget = (LinkableWidget) widget;
			EList<Widget> children = itemUIRegistry.getChildren(linkableWidget);
    		if(widget instanceof Frame) {
    			int cntWidget=0;
    			for(Widget child : children) {
    				widgetId += "_" + cntWidget;
					WidgetBean subWidget = createWidgetBean(sitemapName, child, drillDown, uri, widgetId);
					if(subWidget != null) {
						bean.widgets.add(subWidget);
    	    		cntWidget++;
    			}
				}
    		} else if(children.size()>0)  {
				String pageName = itemUIRegistry.getWidgetId(linkableWidget);
				bean.linkedPage = createPageBean(sitemapName, itemUIRegistry.getLabel(widget), itemUIRegistry.getIcon(widget), pageName, 
						drillDown ? children : null, drillDown, isLeaf(children), uri);
    		}
		}
    	if(widget instanceof Switch) {
    		Switch switchWidget = (Switch) widget;
    		for(Mapping mapping : switchWidget.getMappings()) {
    			MappingBean mappingBean = new MappingBean();
				// Remove quotes - if they exist
				if(mapping.getCmd() != null) {
					if(mapping.getCmd().startsWith("\"") && mapping.getCmd().endsWith("\"")) {
						mappingBean.command = mapping.getCmd().substring(1, mapping.getCmd().length()-1);
					}
				}
				else {
					mappingBean.command = mapping.getCmd();
				}
				mappingBean.label = mapping.getLabel();
				bean.mappings.add(mappingBean);
			}
		}
		if (widget instanceof Selection) {
			Selection selectionWidget = (Selection) widget;
			for (Mapping mapping : selectionWidget.getMappings()) {
				MappingBean mappingBean = new MappingBean();
				// Remove quotes - if they exist
				if(mapping.getCmd() != null) {
					if(mapping.getCmd().startsWith("\"") && mapping.getCmd().endsWith("\"")) {
						mappingBean.command = mapping.getCmd().substring(1, mapping.getCmd().length()-1);
					}
				}
				else {
					mappingBean.command = mapping.getCmd();
				}
    			mappingBean.label = mapping.getLabel();
    			bean.mappings.add(mappingBean);
    		}
    	}
    	if(widget instanceof Slider) {
    		Slider sliderWidget = (Slider) widget;
    		bean.sendFrequency = sliderWidget.getFrequency();
    		bean.switchSupport = sliderWidget.isSwitchEnabled();
    	}
    	if(widget instanceof List) {
    		List listWidget = (List) widget;
    		bean.separator = listWidget.getSeparator();
    	}
    	if(widget instanceof Image) {
    		Image imageWidget = (Image) widget;
    		String wId = itemUIRegistry.getWidgetId(widget);
			if (uri.getPort() < 0 || uri.getPort() == 80) {
				bean.url = uri.getScheme() + "://" + uri.getHost() + "/proxy?sitemap=" + sitemapName + ".sitemap&widgetId=" + wId;
			} else {
				bean.url = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + "/proxy?sitemap=" + sitemapName + ".sitemap&widgetId=" + wId;
			}
    		if(imageWidget.getRefresh()>0) {
    			bean.refresh = imageWidget.getRefresh(); 
    		}
    	}
    	if(widget instanceof Video) {
    		String wId = itemUIRegistry.getWidgetId(widget);
			if (uri.getPort() < 0 || uri.getPort() == 80) {
				bean.url = uri.getScheme() + "://" + uri.getHost() + "/proxy?sitemap=" + sitemapName + ".sitemap&widgetId=" + wId;
			} else {
				bean.url = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + "/proxy?sitemap=" + sitemapName	+ ".sitemap&widgetId=" + wId;
			}
    	}
    	if(widget instanceof Webview) {
    		Webview webViewWidget = (Webview) widget;
    		bean.url = webViewWidget.getUrl();
    		bean.height = webViewWidget.getHeight();
    	}
    	if(widget instanceof Chart) {
    		Chart chartWidget = (Chart) widget;
    		bean.service = chartWidget.getService();
    		bean.period = chartWidget.getPeriod();
    		if(chartWidget.getRefresh()>0) {
    			bean.refresh = chartWidget.getRefresh(); 
    		}
    	}
    	if(widget instanceof Setpoint) {
    		Setpoint setpointWidget = (Setpoint) widget;
    		bean.minValue = setpointWidget.getMinValue();
    		bean.maxValue = setpointWidget.getMaxValue();
    		bean.step = setpointWidget.getStep();
    	}
		return bean;
	}

	private static boolean isLeaf(EList<Widget> children) {
		for(Widget w : children) {
			if(w instanceof Frame) {
				if(isLeaf(((Frame) w).getChildren())) {
					return false;
				}
			} else if(w instanceof LinkableWidget) {
				LinkableWidget linkableWidget = (LinkableWidget) w;
				if(itemUIRegistry.getChildren(linkableWidget).size() > 0) {
					return false;
				}
			}
		}
		return true;
	}

	static public Sitemap getSitemap(String sitemapname) {
        if(modelRepository!=null) {
			Sitemap sitemap = (Sitemap) modelRepository.getModel(sitemapname + SITEMAP_FILEEXT);
			return sitemap;
        }
        return null;
    }
}

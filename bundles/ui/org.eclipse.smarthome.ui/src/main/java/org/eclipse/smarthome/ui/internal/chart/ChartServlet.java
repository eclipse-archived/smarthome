/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.internal.chart;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.ui.chart.ChartProvider;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet generates time-series charts for a given set of items. It
 * accepts the following HTTP parameters:
 * <ul>
 * <li>w: width in pixels of image to generate</li>
 * <li>h: height in pixels of image to generate</li>
 * <li>period: the time span for the x-axis. Value can be h,4h,8h,12h,D,3D,W,2W,M,2M,4M,Y</li>
 * <li>items: A comma separated list of item names to display</li>
 * <li>groups: A comma separated list of group names, whose members should be displayed</li>
 * <li>service: The persistence service name. If not supplied the first service found will be used.</li>
 * <li>theme: The chart theme to use. If not supplied the chart provider uses a default theme.</li>
 * <li>dpi: The DPI (dots per inch) value. If not supplied, a default is used.</code></li>
 * <li>legend: Show the legend? If not supplied, the ChartProvider should make his own decision.</li>
 * </ul>
 *
 * @author Chris Jackson
 * @author Holger Reichert - Support for themes, DPI, legend hiding
 *
 */
public class ChartServlet extends HttpServlet {

    private static final long serialVersionUID = 7700873790924746422L;
    private static final int CHART_HEIGHT = 240;
    private static final int CHART_WIDTH = 480;
    private static final String DATE_FORMAT = "yyyyMMddHHmm";

    private final Logger logger = LoggerFactory.getLogger(ChartServlet.class);

    private String providerName = "default";
    private int defaultHeight = CHART_HEIGHT;
    private int defaultWidth = CHART_WIDTH;
    private double scale = 1.0;

    // The URI of this servlet
    public static final String SERVLET_NAME = "/chart";

    protected static final Map<String, Long> PERIODS = new HashMap<String, Long>();

    static {
        PERIODS.put("h", 3600000L);
        PERIODS.put("4h", 14400000L);
        PERIODS.put("8h", 28800000L);
        PERIODS.put("12h", 43200000L);
        PERIODS.put("D", 86400000L);
        PERIODS.put("2D", 172800000L);
        PERIODS.put("3D", 259200000L);
        PERIODS.put("W", 604800000L);
        PERIODS.put("2W", 1209600000L);
        PERIODS.put("M", 2592000000L);
        PERIODS.put("2M", 5184000000L);
        PERIODS.put("4M", 10368000000L);
        PERIODS.put("Y", 31536000000L);
    }

    protected HttpService httpService;
    protected ItemUIRegistry itemUIRegistry;
    static protected Map<String, ChartProvider> chartProviders = new HashMap<String, ChartProvider>();

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    public void setItemUIRegistry(ItemUIRegistry itemUIRegistry) {
        this.itemUIRegistry = itemUIRegistry;
    }

    public void unsetItemUIRegistry(ItemUIRegistry itemUIRegistry) {
        this.itemUIRegistry = null;
    }

    public void addChartProvider(ChartProvider provider) {
        chartProviders.put(provider.getName(), provider);
    }

    public void removeChartProvider(ChartProvider provider) {
        chartProviders.remove(provider.getName());
    }

    static public Map<String, ChartProvider> getChartProviders() {
        return chartProviders;
    }

    protected void activate(Map<String, Object> config) {
        try {
            logger.debug("Starting up chart servlet at " + SERVLET_NAME);

            Hashtable<String, String> props = new Hashtable<String, String>();
            httpService.registerServlet(SERVLET_NAME, this, props, createHttpContext());

        } catch (NamespaceException e) {
            logger.error("Error during chart servlet startup", e);
        } catch (ServletException e) {
            logger.error("Error during chart servlet startup", e);
        }

        applyConfig(config);
    }

    protected void deactivate() {
        httpService.unregister(SERVLET_NAME);
    }

    protected void modified(Map<String, Object> config) {
        applyConfig(config);
    }

    /**
     * Handle the initial or a changed configuration.
     *
     * @param config the configuration
     */
    private void applyConfig(Map<String, Object> config) {
        if (config == null) {
            return;
        }

        final String providerNameString = Objects.toString(config.get("provider"), null);
        if (providerNameString != null) {
            providerName = providerNameString;
        }

        final String defaultHeightString = Objects.toString(config.get("defaultHeight"), null);
        if (defaultHeightString != null) {
            defaultHeight = Integer.parseInt(defaultHeightString);
        }

        final String defaultWidthString = Objects.toString(config.get("defaultWidth"), null);
        if (defaultWidthString != null) {
            defaultWidth = Integer.parseInt(defaultWidthString);
        }

        final String scaleString = Objects.toString(config.get("scale"), null);
        if (scaleString != null) {
            scale = Double.parseDouble(scaleString);
            // Set scale to normal if the custom value is unrealistically low
            if (scale < 0.1) {
                scale = 1.0;
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        logger.debug("Received incoming chart request: {}", req);

        int width = defaultWidth;
        try {
            width = Integer.parseInt(req.getParameter("w"));
        } catch (Exception e) {
        }
        int height = defaultHeight;
        try {
            String h = req.getParameter("h");
            if (h != null) {
                Double d = Double.parseDouble(h) * scale;
                height = d.intValue();
            }
        } catch (Exception e) {
        }

        // To avoid ambiguity you are not allowed to specify period, begin and end time at the same time.
        if (req.getParameter("period") != null && req.getParameter("begin") != null
                && req.getParameter("end") != null) {
            throw new ServletException("Do not specify the three parameters period, begin and end at the same time.");
        }

        // Read out the parameter period, begin and end and save them.
        Date timeBegin = null;
        Date timeEnd = null;

        Long period = PERIODS.get(req.getParameter("period"));
        if (period == null) {
            // use a day as the default period
            period = PERIODS.get("D");
        }

        if (req.getParameter("begin") != null) {
            try {
                timeBegin = new SimpleDateFormat(DATE_FORMAT).parse(req.getParameter("begin"));
            } catch (ParseException e) {
                throw new ServletException("Begin and end must have this format: " + DATE_FORMAT + ".");
            }
        }

        if (req.getParameter("end") != null) {
            try {
                timeEnd = new SimpleDateFormat(DATE_FORMAT).parse(req.getParameter("end"));
            } catch (ParseException e) {
                throw new ServletException("Begin and end must have this format: " + DATE_FORMAT + ".");
            }
        }

        // Set begin and end time and check legality.
        if (timeBegin == null && timeEnd == null) {
            timeEnd = new Date();
            timeBegin = new Date(timeEnd.getTime() - period);
            logger.debug("No begin or end is specified, use now as end and now-period as begin.");
        } else if (timeEnd == null) {
            timeEnd = new Date(timeBegin.getTime() + period);
            logger.debug("No end is specified, use begin + period as end.");
        } else if (timeBegin == null) {
            timeBegin = new Date(timeEnd.getTime() - period);
            logger.debug("No begin is specified, use end-period as begin");
        } else if (timeEnd.before(timeBegin)) {
            throw new ServletException("The end is before the begin.");
        }

        // If a persistence service is specified, find the provider
        String serviceName = req.getParameter("service");

        ChartProvider provider = getChartProviders().get(providerName);
        if (provider == null) {
            throw new ServletException("Could not get chart provider.");
        }

        // Read out the parameter 'dpi'
        Integer dpi = null;
        if (req.getParameter("dpi") != null) {
            try {
                dpi = Integer.valueOf(req.getParameter("dpi"));
            } catch (NumberFormatException e) {
                throw new ServletException("dpi parameter is invalid");
            }
            if (dpi <= 0) {
                throw new ServletException("dpi parameter is <= 0");
            }
        }

        // Read out parameter 'legend'
        Boolean legend = null;
        if (req.getParameter("legend") != null) {
            legend = BooleanUtils.toBoolean(req.getParameter("legend"));
        }

        // Set the content type to that provided by the chart provider
        res.setContentType("image/" + provider.getChartType());
        try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(res.getOutputStream())) {
            BufferedImage chart = provider.createChart(serviceName, req.getParameter("theme"), timeBegin, timeEnd,
                    height, width, req.getParameter("items"), req.getParameter("groups"), dpi, legend);
            ImageIO.write(chart, provider.getChartType().toString(), imageOutputStream);
        } catch (ItemNotFoundException e) {
            logger.debug("{}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Illegal argument in chart: {}", e.getMessage());
        }
    }

    /**
     * Creates a {@link HttpContext}
     *
     * @return a {@link HttpContext}
     */
    protected HttpContext createHttpContext() {
        HttpContext defaultHttpContext = httpService.createDefaultHttpContext();
        return defaultHttpContext;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {
    }

}

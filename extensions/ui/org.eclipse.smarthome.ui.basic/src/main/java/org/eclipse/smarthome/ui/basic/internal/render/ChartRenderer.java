/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.basic.internal.render;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.model.sitemap.Chart;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.basic.internal.WebAppConfig;
import org.eclipse.smarthome.ui.basic.render.RenderException;
import org.eclipse.smarthome.ui.basic.render.WidgetRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of the {@link WidgetRenderer} interface, which
 * can produce HTML code for Chart widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Vlad Ivanov - BasicUI changes
 *
 */
public class ChartRenderer extends AbstractWidgetRenderer {

    private final Logger logger = LoggerFactory.getLogger(ChartRenderer.class);

    @Override
    public boolean canRender(Widget w) {
        return w instanceof Chart;
    }

    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Chart chart = (Chart) w;

        try {
            String itemParam = null;
            Item item = itemUIRegistry.getItem(chart.getItem());
            if (item instanceof GroupItem) {
                itemParam = "groups=" + chart.getItem();
            } else {
                itemParam = "items=" + chart.getItem();
            }

            String chartUrl = "/chart?" + itemParam + "&period=" + chart.getPeriod();
            if (chart.getService() != null) {
                chartUrl += "&service=" + chart.getService();
            }
            // if legend parameter is given, add corresponding GET parameter
            if (chart.getLegend() != null) {
                if (chart.getLegend()) {
                    chartUrl += "&legend=true";
                } else {
                    chartUrl += "&legend=false";
                }
            }
            // add theme GET parameter
            String chartTheme = null;
            switch (config.getTheme()) {
                case WebAppConfig.THEME_NAME_DEFAULT:
                    chartTheme = "bright";
                    break;
                case WebAppConfig.THEME_NAME_DARK:
                    chartTheme = "dark";
                    break;
            }
            if (chartTheme != null) {
                chartUrl += "&theme=" + chartTheme;
            }
            // add timestamp to circumvent browser cache
            String url = chartUrl + "&t=" + (new Date()).getTime();

            String snippet = getSnippet("chart");
            snippet = preprocessSnippet(snippet, w);

            if (chart.getRefresh() > 0) {
                snippet = StringUtils.replace(snippet, "%update_interval%", Integer.toString(chart.getRefresh()));
            } else {
                snippet = StringUtils.replace(snippet, "%update_interval%", "0");
            }

            snippet = StringUtils.replace(snippet, "%id%", itemUIRegistry.getWidgetId(w));
            snippet = StringUtils.replace(snippet, "%proxied_url%", chartUrl);
            snippet = StringUtils.replace(snippet, "%valid_url%", "true");
            snippet = StringUtils.replace(snippet, "%url%", url);

            sb.append(snippet);
        } catch (ItemNotFoundException e) {
            logger.warn("Chart cannot be rendered as item '{}' does not exist.", chart.getItem());
        }
        return null;
    }
}

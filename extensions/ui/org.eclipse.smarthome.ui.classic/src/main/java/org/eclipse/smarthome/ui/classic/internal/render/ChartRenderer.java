/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.classic.internal.render;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.model.sitemap.Chart;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.classic.render.RenderException;
import org.eclipse.smarthome.ui.classic.render.WidgetRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of the {@link WidgetRenderer} interface, which
 * can produce HTML code for Chart widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ChartRenderer extends AbstractWidgetRenderer {

    private final Logger logger = LoggerFactory.getLogger(ChartRenderer.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRender(Widget w) {
        return w instanceof Chart;
    }

    /**
     * {@inheritDoc}
     */
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

            String url = "/chart?" + itemParam + "&period=" + chart.getPeriod() + "&random=1";
            if (chart.getService() != null)
                url += "&service=" + chart.getService();

            String snippet = getSnippet("image");

            if (chart.getRefresh() > 0) {
                snippet = StringUtils.replace(snippet, "%setrefresh%",
                        "<script type=\"text/javascript\">imagesToRefreshOnPage=1</script>");
                snippet = StringUtils.replace(snippet, "%refresh%",
                        "id=\"%id%\" onload=\"setTimeout('reloadImage(\\'%url%\\', \\'%id%\\')', " + chart.getRefresh()
                                + ")\"");
            } else {
                snippet = StringUtils.replace(snippet, "%setrefresh%", "");
                snippet = StringUtils.replace(snippet, "%refresh%", "");
            }

            snippet = StringUtils.replace(snippet, "%id%", itemUIRegistry.getWidgetId(w));
            snippet = StringUtils.replace(snippet, "%url%", url);
            snippet = StringUtils.replace(snippet, "%refresh%", Integer.toString(chart.getRefresh()));

            sb.append(snippet);
        } catch (ItemNotFoundException e) {
            logger.warn("Chart cannot be rendered as item '{}' does not exist.", chart.getItem());
        }
        return null;
    }
}

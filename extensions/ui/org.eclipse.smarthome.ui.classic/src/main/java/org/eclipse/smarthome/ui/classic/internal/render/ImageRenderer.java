/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.classic.internal.render;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.model.sitemap.Image;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.classic.render.RenderException;
import org.eclipse.smarthome.ui.classic.render.WidgetRenderer;

/**
 * This is an implementation of the {@link WidgetRenderer} interface, which
 * can produce HTML code for Image widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ImageRenderer extends AbstractWidgetRenderer {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRender(Widget w) {
        return w instanceof Image;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Image image = (Image) w;
        String snippet = (image.getChildren().size() > 0) ? getSnippet("image_link") : getSnippet("image");

        if (image.getRefresh() > 0) {
            snippet = StringUtils.replace(snippet, "%refresh%", "id=\"%id%\" data-timeout=\"" + image.getRefresh()
                            + "\" onload=\"startReloadImage('%url%', '%id%')\"");
        } else {
            snippet = StringUtils.replace(snippet, "%refresh%", "");
        }

        String widgetId = itemUIRegistry.getWidgetId(w);
        snippet = StringUtils.replace(snippet, "%id%", widgetId);

        String sitemap = w.eResource().getURI().path();

        String url = "../proxy?sitemap=" + sitemap + "&widgetId=" + widgetId + "&t=" + (new Date()).getTime();
        snippet = StringUtils.replace(snippet, "%url%", url);

        sb.append(snippet);
        return null;
    }
}

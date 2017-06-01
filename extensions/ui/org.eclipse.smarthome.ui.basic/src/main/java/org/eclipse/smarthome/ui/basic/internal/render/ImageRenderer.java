/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.basic.internal.render;

import java.net.URI;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.sitemap.Image;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.basic.render.RenderException;
import org.eclipse.smarthome.ui.basic.render.WidgetRenderer;

/**
 * This is an implementation of the {@link WidgetRenderer} interface, which
 * can produce HTML code for Image widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Vlad Ivanov - BasicUI changes
 *
 */
public class ImageRenderer extends AbstractWidgetRenderer {

    @Override
    public boolean canRender(Widget w) {
        return w instanceof Image;
    }

    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Image image = (Image) w;
        String snippet = (image.getChildren().size() > 0) ? getSnippet("image_link") : getSnippet("image");

        if (image.getRefresh() > 0) {
            snippet = StringUtils.replace(snippet, "%update_interval%", Integer.toString(image.getRefresh()));
        } else {
            snippet = StringUtils.replace(snippet, "%update_interval%", "0");
        }

        String widgetId = itemUIRegistry.getWidgetId(w);
        snippet = StringUtils.replace(snippet, "%id%", widgetId);
        snippet = preprocessSnippet(snippet, w);

        String sitemap = null;
        if (w.eResource() != null) {
            sitemap = w.eResource().getURI().path();
        }
        boolean validUrl = false;
        if (image.getUrl() != null && !image.getUrl().isEmpty()) {
            try {
                URI.create(image.getUrl());
                validUrl = true;
            } catch (IllegalArgumentException ex) {
            }
        }
        String proxiedUrl = "../proxy?sitemap=" + sitemap + "&amp;widgetId=" + widgetId;
        State state = itemUIRegistry.getState(w);
        String url;
        if (state instanceof RawType) {
            url = state.toFullString();
        } else if ((sitemap != null) && ((state instanceof StringType) || validUrl)) {
            url = proxiedUrl + "&amp;t=" + (new Date()).getTime();
        } else {
            url = "images/none.png";
        }
        snippet = StringUtils.replace(snippet, "%valid_url%", validUrl ? "true" : "false");
        snippet = StringUtils.replace(snippet, "%proxied_url%", proxiedUrl);
        snippet = StringUtils.replace(snippet, "%url%", url);

        sb.append(snippet);
        return null;
    }
}

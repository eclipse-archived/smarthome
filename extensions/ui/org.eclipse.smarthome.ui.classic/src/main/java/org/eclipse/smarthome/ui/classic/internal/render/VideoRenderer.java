/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.classic.internal.render;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.sitemap.Video;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.classic.render.RenderException;
import org.eclipse.smarthome.ui.classic.render.WidgetRenderer;

/**
 * This is an implementation of the {@link WidgetRenderer} interface, which
 * can produce HTML code for Video widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class VideoRenderer extends AbstractWidgetRenderer {

    @Override
    public boolean canRender(Widget w) {
        return w instanceof Video;
    }

    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Video videoWidget = (Video) w;
        String snippet = null;

        String widgetId = itemUIRegistry.getWidgetId(w);
        String sitemap = w.eResource().getURI().path();

        if (videoWidget.getEncoding() != null && videoWidget.getEncoding().toLowerCase().contains("mjpeg")) {
            // we handle mjpeg streams as an html image as browser can usually handle this
            snippet = getSnippet("image");
            snippet = StringUtils.replace(snippet, "%setrefresh%", "");
            snippet = StringUtils.replace(snippet, "%refresh%", "");
        } else {
            snippet = getSnippet("video");
        }
        String url = "../proxy?sitemap=" + sitemap + "&widgetId=" + widgetId;
        String mediaType = "";
        if (videoWidget.getEncoding() != null && videoWidget.getEncoding().toLowerCase().contains("hls")) {
            // For HTTP Live Stream we don't proxy the URL and we set the appropriate media type
            State state = itemUIRegistry.getState(w);
            url = (state instanceof StringType) ? state.toString() : videoWidget.getUrl();
            mediaType = "type=\"application/vnd.apple.mpegurl\"";
        }
        snippet = StringUtils.replace(snippet, "%url%", url);
        snippet = StringUtils.replace(snippet, "%media_type%", mediaType);
        sb.append(snippet);
        return null;
    }
}

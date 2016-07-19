/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.classic.internal.render;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.model.sitemap.Slider;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.classic.internal.servlet.WebAppServlet;
import org.eclipse.smarthome.ui.classic.render.RenderException;
import org.eclipse.smarthome.ui.classic.render.WidgetRenderer;

/**
 * <p>
 * This is an implementation of the {@link WidgetRenderer} interface, which can produce HTML code for Slider widgets.
 * </p>
 *
 * <p>
 * Note: As the WebApp.Net framework cannot render real sliders in the UI, we instead show buttons to increase or
 * decrease the value.
 * </p>
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class SliderRenderer extends AbstractWidgetRenderer {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRender(Widget w) {
        return w instanceof Slider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Slider s = (Slider) w;

        String snippetName = "slider";

        String snippet = getSnippet(snippetName);

        // set the default send-update frequency to 200ms
        String frequency = s.getFrequency() == 0 ? "200" : Integer.toString(s.getFrequency());

        snippet = StringUtils.replace(snippet, "%id%", itemUIRegistry.getWidgetId(s));
        snippet = StringUtils.replace(snippet, "%category%", getCategory(s));
        snippet = StringUtils.replace(snippet, "%item%", w.getItem());
        snippet = StringUtils.replace(snippet, "%label%", getLabel(s));
        snippet = StringUtils.replace(snippet, "%state%", getStateAsNumber(s));
        snippet = StringUtils.replace(snippet, "%format%", getFormat());
        snippet = StringUtils.replace(snippet, "%frequency%", frequency);
        snippet = StringUtils.replace(snippet, "%switch%", s.isSwitchEnabled() ? "1" : "0");
        snippet = StringUtils.replace(snippet, "%servletname%", WebAppServlet.SERVLET_NAME);

        // Process the color tags
        snippet = processColor(w, snippet);

        sb.append(snippet);
        return null;
    }
}

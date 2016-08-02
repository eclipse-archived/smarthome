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
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.sitemap.Colorpicker;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.classic.internal.servlet.WebAppServlet;
import org.eclipse.smarthome.ui.classic.render.RenderException;
import org.eclipse.smarthome.ui.classic.render.WidgetRenderer;

/**
 * <p>
 * This is an implementation of the {@link WidgetRenderer} interface, which can produce HTML code for Colorpicker
 * widgets.
 * </p>
 *
 * <p>
 * Note: This renderer requires the files "jquery.miniColors.css" and "jquery.miniColors.js" in the web folder of this
 * bundle
 * </p>
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class ColorpickerRenderer extends AbstractWidgetRenderer {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRender(Widget w) {
        return w instanceof Colorpicker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Colorpicker cp = (Colorpicker) w;

        String snippetName = "colorpicker";

        String snippet = getSnippet(snippetName);

        // set the default send-update frequency to 200ms
        String frequency = cp.getFrequency() == 0 ? "200" : Integer.toString(cp.getFrequency());

        // get RGB hex value
        State state = itemUIRegistry.getState(cp);
        String hexValue = "#ffffff";
        if (state instanceof HSBType) {
            HSBType hsbState = (HSBType) state;
            hexValue = "#" + Integer.toHexString(hsbState.getRGB()).substring(2);
        }
        String label = getLabel(cp);
        String purelabel = itemUIRegistry.getLabel(w);
        purelabel = purelabel.replaceAll("\\\"", "\\\\'");

        snippet = StringUtils.replace(snippet, "%id%", itemUIRegistry.getWidgetId(cp));
        snippet = StringUtils.replace(snippet, "%category%", escapeURLPath(itemUIRegistry.getCategory(cp)));
        snippet = StringUtils.replace(snippet, "%item%", w.getItem());
        snippet = StringUtils.replace(snippet, "%label%", label);
        snippet = StringUtils.replace(snippet, "%format%", getFormat());
        snippet = StringUtils.replace(snippet, "%purelabel%", purelabel);
        snippet = StringUtils.replace(snippet, "%state%", hexValue);
        snippet = StringUtils.replace(snippet, "%frequency%", frequency);
        snippet = StringUtils.replace(snippet, "%servletname%", WebAppServlet.SERVLET_NAME);

        String style = "";
        String color = itemUIRegistry.getLabelColor(w);
        if (color != null) {
            style = "color:" + color;
        }
        snippet = StringUtils.replace(snippet, "%labelstyle%", style);

        style = "";
        color = itemUIRegistry.getValueColor(w);
        if (color != null) {
            style = "color:" + color;
        }
        snippet = StringUtils.replace(snippet, "%valuestyle%", style);

        sb.append(snippet);
        return null;
    }
}

/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.classic.internal.render;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.sitemap.Setpoint;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.classic.internal.servlet.WebAppServlet;
import org.eclipse.smarthome.ui.classic.render.RenderException;
import org.eclipse.smarthome.ui.classic.render.WidgetRenderer;

/**
 * This is an implementation of the {@link WidgetRenderer} interface, which
 * can produce HTML code for Setpoint widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class SetpointRenderer extends AbstractWidgetRenderer {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRender(Widget w) {
        return w instanceof Setpoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Setpoint sp = (Setpoint) w;

        State state = itemUIRegistry.getState(w);
        String newLowerState = state.toString();
        String newHigherState = state.toString();

        // set defaults for min, max and step
        BigDecimal step = sp.getStep();
        if (step == null) {
            step = BigDecimal.ONE;
        }
        BigDecimal minValue = sp.getMinValue();
        if (minValue == null) {
            minValue = BigDecimal.ZERO;
        }
        BigDecimal maxValue = sp.getMaxValue();
        if (maxValue == null) {
            maxValue = BigDecimal.valueOf(100);
        }

        // if the current state is a valid value, we calculate the up and down step values
        if (state instanceof DecimalType) {
            DecimalType actState = (DecimalType) state;
            BigDecimal newLower = actState.toBigDecimal().subtract(step);
            BigDecimal newHigher = actState.toBigDecimal().add(step);
            if (newLower.compareTo(minValue) < 0) {
                newLower = minValue;
            }
            if (newHigher.compareTo(maxValue) > 0) {
                newHigher = maxValue;
            }
            newLowerState = newLower.toString();
            newHigherState = newHigher.toString();
        }

        String snippetName = "setpoint";
        String snippet = getSnippet(snippetName);

        snippet = StringUtils.replace(snippet, "%id%", itemUIRegistry.getWidgetId(w));
        snippet = StringUtils.replace(snippet, "%category%", escapeURLPath(itemUIRegistry.getCategory(w)));
        snippet = StringUtils.replace(snippet, "%item%", w.getItem());
        snippet = StringUtils.replace(snippet, "%state%", state.toString());
        snippet = StringUtils.replace(snippet, "%newlowerstate%", newLowerState);
        snippet = StringUtils.replace(snippet, "%newhigherstate%", newHigherState);
        snippet = StringUtils.replace(snippet, "%label%", getLabel(w));
        snippet = StringUtils.replace(snippet, "%format%", getFormat());
        snippet = StringUtils.replace(snippet, "%servletname%", WebAppServlet.SERVLET_NAME);
        snippet = StringUtils.replace(snippet, "%minValue%", minValue.toString());
        snippet = StringUtils.replace(snippet, "%maxValue%", maxValue.toString());
        snippet = StringUtils.replace(snippet, "%step%", step.toString());

        // Process the color tags
        snippet = processColor(w, snippet);

        sb.append(snippet);
        return null;
    }
}

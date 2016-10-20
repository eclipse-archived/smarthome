/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.basic.internal.render;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.model.sitemap.Mapping;
import org.eclipse.smarthome.model.sitemap.Selection;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.basic.render.RenderException;
import org.eclipse.smarthome.ui.basic.render.WidgetRenderer;

import com.google.gson.JsonObject;

/**
 * This is an implementation of the {@link WidgetRenderer} interface, which
 * can produce HTML code for Selection widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Vlad Ivanov - BasicUI changes
 *
 */
public class SelectionRenderer extends AbstractWidgetRenderer {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRender(Widget w) {
        return w instanceof Selection;
    }

    /**
     * Get command-label map for a Selection widget
     *
     * @return String representing JSON object
     */
    private String getMappingsJSON(Selection w) {
        JsonObject resultObject = new JsonObject();
        for (Mapping mapping : w.getMappings()) {
            resultObject.addProperty(mapping.getCmd(), mapping.getLabel());
        }
        String result = resultObject.toString();
        result = StringEscapeUtils.escapeHtml(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        String snippet = getSnippet("selection");

        snippet = preprocessSnippet(snippet, w);
        snippet = StringUtils.replace(snippet, "%value_map%", getMappingsJSON((Selection) w));
        snippet = StringUtils.replace(snippet, "%label_header%", getLabel(w));

        String state = itemUIRegistry.getState(w).toString();
        Selection selection = (Selection) w;
        String mappingLabel = null;

        StringBuilder rowSB = new StringBuilder();
        for (Mapping mapping : selection.getMappings()) {
            String rowSnippet = getSnippet("selection_row");
            String command = mapping.getCmd() != null ? mapping.getCmd() : "";
            rowSnippet = StringUtils.replace(rowSnippet, "%item%", w.getItem() != null ? w.getItem() : "");
            rowSnippet = StringUtils.replace(rowSnippet, "%cmd%", escapeHtml(command));
            rowSnippet = StringUtils.replace(rowSnippet, "%label%",
                    mapping.getLabel() != null ? mapping.getLabel() : "");
            if (state.equals(mapping.getCmd())) {
                mappingLabel = mapping.getLabel();
                rowSnippet = StringUtils.replace(rowSnippet, "%checked%", "checked=\"true\"");
            } else {
                rowSnippet = StringUtils.replace(rowSnippet, "%checked%", "");
            }
            rowSB.append(rowSnippet);
        }
        snippet = StringUtils.replace(snippet, "%rows%", rowSB.toString());
        snippet = StringUtils.replace(snippet, "%value_header%", mappingLabel != null ? mappingLabel : "");

        // Process the color tags
        snippet = processColor(w, snippet);

        sb.append(snippet);
        return null;
    }
}

/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.ui.classic.internal.render;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.model.sitemap.Text;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.classic.render.RenderException;
import org.eclipse.smarthome.ui.classic.render.WidgetRenderer;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This is an implementation of the {@link WidgetRenderer} interface, which
 * can produce HTML code for Text widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@Component(service = WidgetRenderer.class)
public class TextRenderer extends AbstractWidgetRenderer {

    @Override
    public boolean canRender(Widget w) {
        return w instanceof Text;
    }

    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Text text = (Text) w;

        String snippet = (text.getChildren().size() > 0) ? getSnippet("text_link") : getSnippet("text");

        snippet = StringUtils.replace(snippet, "%id%", itemUIRegistry.getWidgetId(w));
        snippet = StringUtils.replace(snippet, "%category%", getCategory(w));
        snippet = StringUtils.replace(snippet, "%label%", getLabel(w));
        snippet = StringUtils.replace(snippet, "%format%", getFormat());
        snippet = StringUtils.replace(snippet, "%state%", getState(w));

        // Process the color tags
        snippet = processColor(w, snippet);

        sb.append(snippet);
        return null;
    }

    @Override
    @Reference
    protected void setItemUIRegistry(ItemUIRegistry ItemUIRegistry) {
        super.setItemUIRegistry(ItemUIRegistry);
    }

    @Override
    protected void unsetItemUIRegistry(ItemUIRegistry ItemUIRegistry) {
        super.unsetItemUIRegistry(ItemUIRegistry);
    }

}

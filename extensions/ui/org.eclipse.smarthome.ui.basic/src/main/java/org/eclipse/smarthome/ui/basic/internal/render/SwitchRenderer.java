/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.basic.internal.render;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.model.sitemap.Mapping;
import org.eclipse.smarthome.model.sitemap.Switch;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.basic.render.RenderException;
import org.eclipse.smarthome.ui.basic.render.WidgetRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of the {@link WidgetRenderer} interface, which
 * can produce HTML code for Switch widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Vlad Ivanov - BasicUI changes
 *
 */
public class SwitchRenderer extends AbstractWidgetRenderer {

    private final Logger logger = LoggerFactory.getLogger(SwitchRenderer.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRender(Widget w) {
        return w instanceof Switch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Switch s = (Switch) w;

        String snippetName = null;
        Item item;
        try {
            item = itemUIRegistry.getItem(w.getItem());
            if (s.getMappings().size() == 0) {
                if (item instanceof RollershutterItem) {
                    snippetName = "rollerblind";
                } else if (item instanceof GroupItem && ((GroupItem) item).getBaseItem() instanceof RollershutterItem) {
                    snippetName = "rollerblind";
                } else {
                    snippetName = "switch";
                }
            } else {
                snippetName = "buttons";
            }
        } catch (ItemNotFoundException e) {
            logger.warn("Cannot determine item type of '{}'", w.getItem(), e);
            snippetName = "switch";
        }

        String snippet = getSnippet(snippetName);
        State state = itemUIRegistry.getState(w);
        String value = getValue(w);

        snippet = preprocessSnippet(snippet, w);
        snippet = StringUtils.replace(snippet, "%value%", value);
        snippet = StringUtils.replace(snippet, "%has_value%", new Boolean(value != "").toString());
        snippet = StringUtils.replace(snippet, "%count%", Integer.toString(s.getMappings().size()));

        if (s.getMappings().size() == 0) {
            if (state instanceof PercentType) {
                state = ((PercentType) state).intValue() > 0 ? OnOffType.ON : OnOffType.OFF;
            }
            if (state.equals(OnOffType.ON)) {
                snippet = snippet.replaceAll("%checked%", "checked=true");
            } else {
                snippet = snippet.replaceAll("%checked%", "");
            }
        } else {
            StringBuilder buttons = new StringBuilder();
            for (Mapping mapping : s.getMappings()) {
                String button = getSnippet("button");
                button = StringUtils.replace(button, "%item%", w.getItem());
                button = StringUtils.replace(button, "%cmd%", mapping.getCmd());
                button = StringUtils.replace(button, "%label%", escapeHtml(mapping.getLabel()));
                if (s.getMappings().size() > 1 && state.toString().equals(mapping.getCmd())) {
                    button = StringUtils.replace(button, "%class%", "mdl-button--accent");
                } else {
                    button = StringUtils.replace(button, "%class%", "mdl-button");
                }
                buttons.append(button);
            }
            snippet = StringUtils.replace(snippet, "%buttons%", buttons.toString());
        }

        // Process the color tags
        snippet = processColor(w, snippet);

        sb.append(snippet);
        return null;
    }
}

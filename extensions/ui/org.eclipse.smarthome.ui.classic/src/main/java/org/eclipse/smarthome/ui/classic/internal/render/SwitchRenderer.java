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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.util.UnitUtils;
import org.eclipse.smarthome.model.sitemap.Mapping;
import org.eclipse.smarthome.model.sitemap.Switch;
import org.eclipse.smarthome.model.sitemap.Widget;
import org.eclipse.smarthome.ui.classic.internal.servlet.WebAppServlet;
import org.eclipse.smarthome.ui.classic.render.RenderException;
import org.eclipse.smarthome.ui.classic.render.WidgetRenderer;
import org.eclipse.smarthome.ui.items.ItemUIRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of the {@link WidgetRenderer} interface, which
 * can produce HTML code for Switch widgets.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@Component(service = WidgetRenderer.class)
public class SwitchRenderer extends AbstractWidgetRenderer {

    private final Logger logger = LoggerFactory.getLogger(SwitchRenderer.class);

    private static final int MAX_BUTTONS = 4;
    private static final int MAX_LABEL_SIZE = 12;
    private static final String ELLIPSIS = "\u2026";

    @Override
    public boolean canRender(Widget w) {
        return w instanceof Switch;
    }

    @Override
    public EList<Widget> renderWidget(Widget w, StringBuilder sb) throws RenderException {
        Switch s = (Switch) w;

        String snippetName = null;
        Item item = null;
        int nbButtons = 0;
        try {
            item = itemUIRegistry.getItem(w.getItem());
            if (s.getMappings().size() == 0) {
                if (item instanceof RollershutterItem) {
                    snippetName = "rollerblind";
                } else if (item instanceof GroupItem && ((GroupItem) item).getBaseItem() instanceof RollershutterItem) {
                    snippetName = "rollerblind";
                } else if (item.getStateDescription() != null && item.getStateDescription().getOptions().size() > 0
                        && item.getStateDescription().getOptions().size() <= MAX_BUTTONS) {
                    // Render with buttons only when a max of MAX_BUTTONS options are defined
                    snippetName = "buttons";
                    nbButtons = item.getStateDescription().getOptions().size();
                } else {
                    snippetName = "switch";
                }
            } else {
                snippetName = "buttons";
                nbButtons = s.getMappings().size();
            }
        } catch (ItemNotFoundException e) {
            logger.warn("Cannot determine item type of '{}'", w.getItem(), e);
            snippetName = "switch";
        }

        String snippet = getSnippet(snippetName);

        snippet = StringUtils.replace(snippet, "%id%", itemUIRegistry.getWidgetId(w));
        snippet = StringUtils.replace(snippet, "%category%", getCategory(w));
        snippet = StringUtils.replace(snippet, "%state%", getState(w));
        snippet = StringUtils.replace(snippet, "%format%", getFormat());
        snippet = StringUtils.replace(snippet, "%item%", w.getItem());
        snippet = StringUtils.replace(snippet, "%label%", getLabel(w));
        snippet = StringUtils.replace(snippet, "%servletname%", WebAppServlet.SERVLET_NAME);

        State state = itemUIRegistry.getState(w);

        if (nbButtons == 0) {
            if (state.equals(OnOffType.ON)) {
                snippet = snippet.replaceAll("%checked%", "checked=true");
            } else {
                snippet = snippet.replaceAll("%checked%", "");
            }
        } else {
            StringBuilder buttons = new StringBuilder();
            if (s.getMappings().size() > 0) {
                for (Mapping mapping : s.getMappings()) {
                    buildButton(s, mapping.getLabel(), mapping.getCmd(), -1, nbButtons > 1, item, state, buttons);
                }
            } else {
                for (StateOption option : item.getStateDescription().getOptions()) {
                    // Truncate the button label to MAX_LABEL_SIZE characters
                    buildButton(s, option.getLabel(), option.getValue(), MAX_LABEL_SIZE, nbButtons > 1, item, state,
                            buttons);
                }
            }
            snippet = StringUtils.replace(snippet, "%buttons%", buttons.toString());
        }

        // Process the color tags
        snippet = processColor(w, snippet);

        sb.append(snippet);
        return null;
    }

    private void buildButton(Switch w, String lab, String cmd, int maxLabelSize, boolean severalButtons, Item item,
            State state, StringBuilder buttons) throws RenderException {
        String button = getSnippet("button");

        String command = cmd;
        String label = lab;

        if (item instanceof NumberItem && ((NumberItem) item).getDimension() != null) {
            String unit = getUnitForWidget(w);
            command = StringUtils.replace(command, UnitUtils.UNIT_PLACEHOLDER, unit);
            label = StringUtils.replace(label, UnitUtils.UNIT_PLACEHOLDER, unit);
        }

        if (label != null && maxLabelSize >= 1 && label.length() > maxLabelSize) {
            label = label.substring(0, maxLabelSize - 1) + ELLIPSIS;
        }

        button = StringUtils.replace(button, "%item%", w.getItem());
        button = StringUtils.replace(button, "%cmd%", StringEscapeUtils.escapeHtml(command));
        button = StringUtils.replace(button, "%label%", label != null ? StringEscapeUtils.escapeHtml(label) : "");

        String buttonClass;
        State compareMappingState = state;
        if (state instanceof QuantityType) { // convert the item state to the command value for proper
                                             // comparison and buttonClass calculation
            compareMappingState = convertStateToLabelUnit((QuantityType<?>) state, command);
        }

        if (severalButtons && compareMappingState.toString().equals(command)) {
            buttonClass = "Warn"; // button with red color
        } else {
            buttonClass = "Action"; // button with blue color
        }
        button = StringUtils.replace(button, "%type%", buttonClass);

        buttons.append(button);
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

/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.classic.internal.servlet;

import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.TypeParser;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet receives events from the web app and sends these as
 * commands to the bus.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Stefan Bußweiler - Migration to new ESH event concept
 *
 */
public class CmdServlet extends BaseServlet {

    private final Logger logger = LoggerFactory.getLogger(CmdServlet.class);

    public static final String SERVLET_NAME = "CMD";

    private EventPublisher eventPublisher;

    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    protected void activate() {
        try {
            logger.debug("Starting up CMD servlet at " + WEBAPP_ALIAS + "/" + SERVLET_NAME);

            Hashtable<String, String> props = new Hashtable<String, String>();
            httpService.registerServlet(WEBAPP_ALIAS + "/" + SERVLET_NAME, this, props, createHttpContext());

        } catch (NamespaceException e) {
            logger.error("Error during servlet startup", e);
        } catch (ServletException e) {
            logger.error("Error during servlet startup", e);
        }
    }

    protected void deactivate() {
        httpService.unregister(WEBAPP_ALIAS + "/" + SERVLET_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");

        for (Object key : req.getParameterMap().keySet()) {
            String itemName = key.toString();

            if (!itemName.startsWith("__")) { // all additional webapp params start with "__" and should be ignored
                String commandName = req.getParameter(itemName);
                try {
                    Item item = itemRegistry.getItem(itemName);

                    // we need a special treatment for the "TOGGLE" command of switches;
                    // this is no command officially supported and must be translated
                    // into real commands by the webapp.
                    if ((item instanceof SwitchItem || item instanceof GroupItem) && commandName.equals("TOGGLE")) {
                        commandName = OnOffType.ON.equals(item.getStateAs(OnOffType.class)) ? "OFF" : "ON";
                    }

                    Command command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), commandName);
                    if (command != null) {
                        eventPublisher.post(ItemEventFactory.createCommandEvent(itemName, command));
                    } else {
                        logger.warn("Received unknown command '{}' for item '{}'", commandName, itemName);
                    }
                } catch (ItemNotFoundException e) {
                    logger.warn("Received command '{}' for item '{}', but the item does not exist in the registry",
                            commandName, itemName);
                }
            }
        }
    }

}

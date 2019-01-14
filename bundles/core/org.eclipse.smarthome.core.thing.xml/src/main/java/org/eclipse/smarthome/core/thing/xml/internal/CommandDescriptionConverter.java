/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
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
package org.eclipse.smarthome.core.thing.xml.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.xml.util.GenericUnmarshaller;
import org.eclipse.smarthome.config.xml.util.NodeIterator;
import org.eclipse.smarthome.config.xml.util.NodeList;
import org.eclipse.smarthome.config.xml.util.NodeValue;
import org.eclipse.smarthome.core.types.CommandDescription;
import org.eclipse.smarthome.core.types.CommandOption;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * The {@link CommandDescriptionConverter} is a concrete implementation of the {@code XStream} {@link Converter}
 * interface used to convert a command description within an XML document into a {@link CommandDescription} object.
 * <p>
 * This converter converts {@code command} XML tags.
 *
 * @author Henning Treu - Initial Contribution
 */
public class CommandDescriptionConverter extends GenericUnmarshaller<CommandDescription> {

    public CommandDescriptionConverter() {
        super(CommandDescription.class);
    }

    @Override
    public final CommandDescription unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        CommandDescriptionImpl commandDescription = null;

        NodeList nodes = (NodeList) context.convertAnother(context, NodeList.class);
        NodeIterator nodeIterator = new NodeIterator(nodes.getList());

        NodeList commandOptionsNode = (NodeList) nodeIterator.next();
        if (commandOptionsNode != null) {
            if ("options".equals(commandOptionsNode.getNodeName())) {

                commandDescription = new CommandDescriptionImpl();
                for (Object coNodeObject : commandOptionsNode.getList()) {
                    NodeValue optionsNode = (NodeValue) coNodeObject;

                    if ("option".equals(optionsNode.getNodeName())) {
                        String name = (String) optionsNode.getValue();
                        String command = optionsNode.getAttributes().get("value");

                        if (name != null && command != null) {
                            commandDescription.addCommandOption(new CommandOption(command, name));
                        }
                    } else {
                        throw new ConversionException("The 'options' node must only contain 'option' nodes!");
                    }
                }
            }
        }

        nodeIterator.assertEndOfType();

        return commandDescription;
    }

    /**
     * The {@link CommandDescriptionImpl} groups state command properties.
     *
     * @author Henning Treu - initial contribution
     *
     */
    @NonNullByDefault
    private class CommandDescriptionImpl implements CommandDescription {

        private final List<CommandOption> commandOptions;

        public CommandDescriptionImpl() {
            commandOptions = new ArrayList<>();
        }

        /**
         * Adds a {@link CommandOption} to this {@link CommandDescriptionImpl}.
         *
         * @param commandOption a commandOption to be added to this {@link CommandDescriptionImpl}.
         */
        public void addCommandOption(CommandOption commandOption) {
            commandOptions.add(commandOption);
        }

        @Override
        public List<CommandOption> getCommandOptions() {
            return Collections.unmodifiableList(commandOptions);
        }
    }

}

/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml.internal;

import java.util.List;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.xml.ConfigDescriptionConverter;
import org.eclipse.smarthome.config.xml.ConfigDescriptionParameterConverter;
import org.eclipse.smarthome.config.xml.util.NodeAttributes;
import org.eclipse.smarthome.config.xml.util.NodeAttributesConverter;
import org.eclipse.smarthome.config.xml.util.XmlDocumentReader;

import com.thoughtworks.xstream.XStream;


/**
 * The {@link ConfigDescriptionReader} reads XML documents, which contain the
 * {@code config-descriptions} XML tag, and converts them to {@link List}
 * objects consisting of {@link ConfigDescription} objects.
 * <p>
 * This reader uses {@code XStream} and {@code StAX} to parse and convert the XML document.
 * 
 * @author Michael Grammling - Initial Contribution
 */
public class ConfigDescriptionReader extends XmlDocumentReader<List<ConfigDescription>> {

    /**
     * The default constructor of this class.
     */
    public ConfigDescriptionReader() {
        super.setClassLoader(ConfigDescriptionReader.class.getClassLoader());
    }

    @Override
    public void registerConverters(XStream xstream) {
        xstream.registerConverter(new NodeAttributesConverter());
        xstream.registerConverter(new ConfigDescriptionConverter());
        xstream.registerConverter(new ConfigDescriptionParameterConverter());
    }

    @Override
    public void registerAliases(XStream xstream) {
        xstream.alias("config-descriptions", List.class);
        xstream.alias("config-description", ConfigDescription.class);
        xstream.alias("config-description-ref", NodeAttributes.class);
        xstream.alias("parameter", ConfigDescriptionParameter.class);
    }

}

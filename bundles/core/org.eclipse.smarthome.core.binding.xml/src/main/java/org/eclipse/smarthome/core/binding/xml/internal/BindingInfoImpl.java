/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.binding.xml.internal;

import static org.eclipse.smarthome.config.xml.util.ConverterAssertion.assertNeitherNullNorEmpty;

import java.net.URI;

import org.eclipse.smarthome.core.binding.BindingInfo;

import com.thoughtworks.xstream.converters.ConversionException;


/**
 * The {@link BindingInfoImpl} class is a concrete implementation of the {@link BindingInfo}
 * service interface.
 * <p>
 * <b>Hint:</b> This class is immutable.
 * 
 * @author Michael Grammling - Initial Contribution
 */
public class BindingInfoImpl implements BindingInfo {

    private String id;
    private String name;
    private String description;
    private String author;
    private URI configDescriptionURI;


    public BindingInfoImpl(String id, String name, String description, String author,
            URI configDescriptionURI) throws ConversionException {

        assertNeitherNullNorEmpty("ID", id);
        assertNeitherNullNorEmpty("name", name);
        assertNeitherNullNorEmpty("author", author);

        this.id = id;
        this.name = name;
        this.description = description;
        this.author = author;

        this.configDescriptionURI = configDescriptionURI;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getAuthor() {
        return this.author;
    }

    @Override
    public boolean hasConfigDescriptionURI() {
        return (this.configDescriptionURI != null);
    }

    @Override
    public URI getConfigDescriptionURI() {
        return this.configDescriptionURI;
    }

    @Override
    public String toString() {
        return "BindingInfoImpl [id=" + id + ", name=" + name
                + ", description=" + description + ", author=" + author
                + ", configDescriptionURI=" + configDescriptionURI + "]";
    }

}

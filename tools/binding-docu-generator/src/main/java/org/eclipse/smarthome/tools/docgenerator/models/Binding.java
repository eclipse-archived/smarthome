/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution.
 */

package org.eclipse.smarthome.tools.docgenerator.models;

public class Binding implements Model<org.eclipse.smarthome.tools.docgenerator.schemas.Binding> {
    /**
     * The attribute holding the original instance from the XML parser.
     */
    private org.eclipse.smarthome.tools.docgenerator.schemas.Binding delegate;

    /**
     * Default constructor.
     */
    public Binding() {

    }

    /**
     * Constructor.
     *
     * @param delegate The real binding object.
     */
    public Binding(org.eclipse.smarthome.tools.docgenerator.schemas.Binding delegate) {
        this.delegate = delegate;
    }

    /**
     * @return Returns the instance of the concrete implementation.
     */
    @Override
    public org.eclipse.smarthome.tools.docgenerator.schemas.Binding getRealImpl() {
        return delegate;
    }

    /**
     * @param binding Set the concrete implementation instance.
     */
    @Override
    public void setModel(org.eclipse.smarthome.tools.docgenerator.schemas.Binding binding) {
        this.delegate = binding;
    }

    /**
     * @return Id of the binding.
     */
    public String id() {
        return delegate.getId();
    }

    /**
     * @return Name of the binding.
     */
    public String name() {
        return delegate.getName();
    }

    /**
     * @return Author of the binding.
     */
    public String author() {
        return delegate.getAuthor();
    }

    /**
     * @return Description of the binding.
     */
    public String description() {
        return delegate.getDescription();
    }

    /**
     * @return {@link ConfigDescription} of the binding.
     */
    public ConfigDescription configDescription() {
        return new ConfigDescription(delegate.getConfigDescription());
    }

    /**
     * @return The URI for the referenced config description.
     */
    public String configDescriptionRef() {
        if (delegate.getConfigDescriptionRef() != null) {
            return delegate.getConfigDescriptionRef().getUri();
        } else {
            return null;
        }
    }
}

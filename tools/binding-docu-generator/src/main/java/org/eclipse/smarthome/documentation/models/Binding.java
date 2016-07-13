/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.documentation.models;

public class Binding implements Model<org.eclipse.smarthome.documentation.schemas.binding.v1_0.Binding> {
    /**
     * The attribute holding the original instance from the XML parser.
     */
    private org.eclipse.smarthome.documentation.schemas.binding.v1_0.Binding binding;

    /**
     * Default constructor.
     */
    public Binding() {

    }

    /**
     * Constructor.
     *
     * @param binding The real binding object.
     */
    public Binding(org.eclipse.smarthome.documentation.schemas.binding.v1_0.Binding binding) {
        setModel(binding);
    }

    /**
     * @return Returns the instance of the concrete implementation.
     */
    public org.eclipse.smarthome.documentation.schemas.binding.v1_0.Binding getRealImpl() {
        return binding;
    }

    /**
     * @param binding Set the concrete implementation instance.
     */
    public void setModel(org.eclipse.smarthome.documentation.schemas.binding.v1_0.Binding binding) {
        this.binding = binding;
    }

    /**
     * @return Id of the binding.
     */
    public String id() {
        return binding.getId();
    }

    /**
     * @return Name of the binding.
     */
    public String name() {
        return binding.getName();
    }

    /**
     * @return Author of the binding.
     */
    public String author() {
        return binding.getAuthor();
    }

    /**
     * @return Description of the binding.
     */
    public String description() {
        return binding.getDescription();
    }

    /**
     * @return {@link ConfigDescription} of the binding.
     */
    public ConfigDescription configDescription() {
        return new ConfigDescription(binding.getConfigDescription());
    }

    /**
     * @return The URI for the referenced config description.
     */
    public String configDescriptionRef() {
        if (binding.getConfigDescriptionRef() != null) {
            return binding.getConfigDescriptionRef().getUri();
        } else {
            return null;
        }
    }
}

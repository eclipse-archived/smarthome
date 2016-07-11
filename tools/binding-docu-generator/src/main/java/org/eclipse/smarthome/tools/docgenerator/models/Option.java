/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution.
 */

package org.eclipse.smarthome.tools.docgenerator.models;

public class Option implements Model<org.eclipse.smarthome.tools.docgenerator.schemas.Option> {
    /**
     * The original instance from the XML parser.
     */
    private org.eclipse.smarthome.tools.docgenerator.schemas.Option delegate;

    /**
     * Default constructor.
     */
    public Option() {
    }

    /**
     * Constructor.
     *
     * @param delegate The instance from the XML parser.
     */
    public Option(org.eclipse.smarthome.tools.docgenerator.schemas.Option delegate) {
        this.delegate = delegate;
    }

    /**
     * @return The original instance from the XML parser.
     */
    @Override
    public org.eclipse.smarthome.tools.docgenerator.schemas.Option getRealImpl() {
        return delegate;
    }

    /**
     * Set the model.
     *
     * @param option The instance from the XML parser.
     */
    @Override
    public void setModel(org.eclipse.smarthome.tools.docgenerator.schemas.Option option) {
        this.delegate = option;
    }

    /**
     * @return Value of the option.
     */
    public String value() {
        return delegate.getValue();
    }
}

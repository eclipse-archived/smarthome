/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution.
 */

package org.eclipse.smarthome.tools.docgenerator.models;

import org.eclipse.smarthome.tools.docgenerator.schemas.BridgeType;
import org.eclipse.smarthome.tools.docgenerator.schemas.BridgeTypeRef;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class to not fully depend on the existing models.
 */
public class Bridge extends Thing {
    /**
     * The object we get from the XML parser.
     */
    private BridgeType delegate;

    /**
     * Default constructor.
     */
    public Bridge() {
    }

    /**
     * @param delegate The object from the XML parser.
     */
    public Bridge(BridgeType delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    /**
     * @return Returns the {@link BridgeType} instance.
     */
    @Override
    public BridgeType getRealImpl() {
        return delegate;
    }

    /**
     * @return Returns the supported bridge type refs, a list of URIs.
     */
    public List<String> supportedBridgeTypeRefs() {
        List<String> refs = new ArrayList<String>();
        if (delegate.getSupportedBridgeTypeRefs() != null) {
            for (BridgeTypeRef ref : delegate.getSupportedBridgeTypeRefs().getBridgeTypeRef()) {
                refs.add(ref.getId());
            }
        }
        return refs;
    }

}

/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.UID;

/**
 * Identifier of a profile type.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class ProfileTypeUID extends UID {

    public static final String SYSTEM_SCOPE = "system";

    @Nullable
    private final String label;

    public ProfileTypeUID(String profileType) {
        super(profileType);
        this.label = null;
    }

    public ProfileTypeUID(String scope, String id, String label) {
        super(scope, id);
        this.label = label;
    }

    @Override
    protected int getMinimalNumberOfSegments() {
        return 2;
    }

    public String getScope() {
        return getSegment(0);
    }

    public String getId() {
        return getSegment(1);
    }

    @Nullable
    public String getLabel() {
        return label;
    }

}

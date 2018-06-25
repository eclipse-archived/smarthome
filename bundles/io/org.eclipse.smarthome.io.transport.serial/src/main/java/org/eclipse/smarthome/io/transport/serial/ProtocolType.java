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
package org.eclipse.smarthome.io.transport.serial;

import java.net.URI;

/**
 *
 * @author MatthiasS
 *
 */
public class ProtocolType {
    public enum PathType {
        NET,
        LOCAL;
        public static PathType fromURI(URI uri) {
            return uri.getSchemeSpecificPart().startsWith("//") ? NET : LOCAL;
        }
    }

    PathType pathType;
    String scheme;

    public ProtocolType(PathType pathType, String scheme) {
        this.pathType = pathType;
        this.scheme = scheme;
    }

    public PathType getPathType() {
        return pathType;
    }

    public void setPathType(PathType pathType) {
        this.pathType = pathType;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

}

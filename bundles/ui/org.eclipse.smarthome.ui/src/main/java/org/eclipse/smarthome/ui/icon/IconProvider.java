/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.icon;

import java.io.InputStream;

/**
 * An icon provider can provide {@link InputStream}s for icon names.
 * The source of the images can depend on the provider implementation.
 * So far, the byte stream should represent a PNG image. In future, this
 * interface could be enhanced to support different image formats.
 *
 * The iconName is of the format "<name>[_<status>]", e.g. "Light_ON"
 * and does not have any file extension.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public interface IconProvider {

    /**
     * determines whether this provider can deliver an icon for a given name
     * 
     * @param iconName the icon name
     * @return true, if this provider can deliver an icon
     */
    boolean hasIcon(String iconName);

    /**
     * retrieves the {@link InputStream} of an icon
     * 
     * @param iconName the icon name
     * @return a PNG byte stream of the icon
     */
    InputStream getIcon(String iconName);

}

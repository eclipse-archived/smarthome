/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.classic.render;

/**
 * An exception used by {@link WidgetRenderer}s, if an error occurs.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class RenderException extends Exception {

    private static final long serialVersionUID = -3801828613192343641L;

    public RenderException(String msg) {
        super(msg);
    }

}

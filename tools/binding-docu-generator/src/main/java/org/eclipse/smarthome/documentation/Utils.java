
/*
 * Copyright (c) Alexander Kammerer 2015.
 *
 * All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.smarthome.documentation;

/**
 * @author Alexander Kammerer <alexander.kammerer@online.de>
 */
public class Utils {
    public static String yesOrNo(Boolean expr) {
        return (expr) ? "Yes" : "No";
    }

    public static String trueOrFalse(Boolean expr) {
        if (expr == null) {
            expr = false;
        }
        return (expr) ? "true" : "false";
    }
}

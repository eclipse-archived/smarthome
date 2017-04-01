/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import com.google.gson.Gson;

/**
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
class Command {
    String key;
    Object value;

    public Command(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    String toJson() {
        return "\"" + key + "\":" + new Gson().toJson(value);
    }
}

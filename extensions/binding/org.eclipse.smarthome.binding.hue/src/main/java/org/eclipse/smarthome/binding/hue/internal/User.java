/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.internal;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 * A whitelisted user.
 *
 * @author Q42, standalone Jue library (https://github.com/Q42/Jue)
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 */
public class User {
    @SerializedName("last use date")
    private Date lastUseDate;

    @SerializedName("create date")
    private Date createDate;

    private String name;

    /**
     * Returns the last time a command was issued as this user.
     *
     * @return time of last command by this user
     */
    public Date getLastUseDate() {
        return lastUseDate;
    }

    /**
     * Returns the date this user was created.
     *
     * @return creation date of user
     */
    public Date getCreationDate() {
        return createDate;
    }

    /**
     * Returns the username of this user.
     *
     * @return username
     */
    public String getUsername() {
        return name;
    }
}

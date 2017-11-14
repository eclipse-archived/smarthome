/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.extensionservice.marketplace.internal.model;

import java.util.Set;

/**
 * A node represents an entry on the marketplace.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class Node {

    public String id;

    public String name;

    public Integer favorited;

    public Integer installsTotal;

    public Integer installsRecent;

    public Set<String> tags;

    public String shortdescription;

    public String body;

    public Long created;

    public Long changed;

    public String image;

    public String license;

    public String companyname;

    public String status;

    public String version;

    public String supporturl;

    public String packagetypes;

    public String packageformat;

    public String updateurl;
}

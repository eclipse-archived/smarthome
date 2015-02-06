/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap.internal.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a java bean that is used to serialize page content to JSON.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class PageBean {

    public String id;

    public String title;
    public String icon;
    public String link;
    public PageBean parent;
    public boolean leaf;

    public List<WidgetBean> widgets = new ArrayList<WidgetBean>();

    public PageBean() {
    }

}

/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap.internal.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.io.rest.core.item.beans.ItemBean;

/**
 * This is a java bean that is used with JAXB to serialize widgets to JSON.
 * 
 * @author Kai Kreuzer - Initial contribution and API
 * @author Chris Jackson
 *
 */
public class WidgetBean {

    public String widgetId;
    public String type;
    public String name;

    public String label;
    public String icon;
    public String labelcolor;
    public String valuecolor;

    // widget-specific attributes
    public List<MappingBean> mappings = new ArrayList<MappingBean>();
    public Boolean switchSupport;
    public Integer sendFrequency;
    public String separator;
    public Integer refresh;
    public Integer height;
    public BigDecimal minValue;
    public BigDecimal maxValue;
    public BigDecimal step;
    public String url;
    public String service;
    public String period;

    public ItemBean item;
    public PageBean linkedPage;

    // only for frames, other linkable widgets link to a page
    public final List<WidgetBean> widgets = new ArrayList<WidgetBean>();

    public WidgetBean() {
    }

}
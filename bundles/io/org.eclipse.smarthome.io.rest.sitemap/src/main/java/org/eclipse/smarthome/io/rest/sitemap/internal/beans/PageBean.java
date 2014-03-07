/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap.internal.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a java bean that is used with JAXB to serialize page content
 * to XML or JSON.
 *  
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@XmlRootElement(name="page")
public class PageBean {

	public String id;
	
	public String title;
	public String icon;
	public String link;
	public PageBean parent;
	public boolean leaf;
	
	@XmlElement(name="widget")
	public List<WidgetBean> widgets = new ArrayList<WidgetBean>();
	
	public PageBean() {}
		
}

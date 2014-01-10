/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschränkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.sitemap.internal.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a java bean that is used with JAXB to serialize sitemap lists to JSONP.
 *  
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
@XmlRootElement(name="sitemaps")
public class SitemapListBean {

	public SitemapListBean() {}
	
	public SitemapListBean(Collection<SitemapBean> list) {
		entries.addAll(list);
	}
	
	@XmlElement(name="sitemap")
	public final List<SitemapBean> entries = new ArrayList<SitemapBean>();
	
}

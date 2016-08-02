/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.lazygen;

import static org.eclipse.xtext.util.Strings.equal;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xpand2.XpandExecutionContext;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.ecore.EcoreSupportStandaloneSetup;
import org.eclipse.xtext.generator.CompositeGeneratorException;
import org.eclipse.xtext.generator.LanguageConfig;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;

/**
 *
 * @author Holger Schill, Simon Kaufmann - Initial contribution and API
 *
 */
public class LazyLanguageConfig extends LanguageConfig {
	
	private final static Logger LOG = Logger.getLogger(LazyLanguageConfig.class);
	String uri = null;
	boolean isUI = false;

	@Override
	public void setUri(String uri) {
		this.uri = uri;
	}

	private Grammar grammar;

	@Override
	public Grammar getGrammar() {
		setUriReally(uri);
		return grammar;
	}

	@Override
	public void initialize(boolean isUi) {
		this.isUI = isUi;
	}

	public void initializeReally() {
		super.initialize(isUI);
	}

	@Override
	protected void validateGrammar(Grammar grammar) {
	}

	@Override
	public void generate(Grammar grammar, XpandExecutionContext ctx) {
		initializeReally();
		super.generate(grammar, ctx);
	}

	@Override
	public void generate(LanguageConfig config, XpandExecutionContext ctx) throws CompositeGeneratorException {
		initializeReally();
		super.generate(config, ctx);
	}

	public void setUriReally(String uri) {
		ResourceSet rs = GlobalResourceSet.getINSTANCE();
		for (String loadedResource : getLoadedResources()) {
			URI loadedResourceUri = URI.createURI(loadedResource);
			if (equal(loadedResourceUri.fileExtension(), "ecore")) {
				IResourceServiceProvider resourceServiceProvider = IResourceServiceProvider.Registry.INSTANCE
						.getResourceServiceProvider(loadedResourceUri);
				if (resourceServiceProvider == null) {
					EcoreSupportStandaloneSetup.setup();
				}
			} else if (equal(loadedResourceUri.fileExtension(), "xcore")) {
				IResourceServiceProvider resourceServiceProvider = IResourceServiceProvider.Registry.INSTANCE
						.getResourceServiceProvider(loadedResourceUri);
				if (resourceServiceProvider == null) {
					try {
						Class<?> xcore = Class.forName("org.eclipse.emf.ecore.xcore.XcoreStandaloneSetup");
						xcore.getDeclaredMethod("doSetup", new Class[0]).invoke(null);
					} catch (ClassNotFoundException e) {
						LOG.error("Couldn't initialize Xcore support. Is it on the classpath?");
					} catch (Exception e) {
						LOG.error("Couldn't initialize Xcore support.", e);
					}
				}
			}
			Resource res = rs.getResource(loadedResourceUri, true);
			if (res == null || res.getContents().isEmpty())
				LOG.error("Error loading '" + loadedResource + "'");
			else if (!res.getErrors().isEmpty())
				LOG.error("Error loading '" + loadedResource + "': " + res.getErrors().toString());
		}
		EcoreUtil.resolveAll(rs);
		XtextResource resource = (XtextResource) rs.getResource(URI.createURI(uri), true);
		if (resource.getContents().isEmpty()) {
			throw new IllegalArgumentException("Couldn't load grammar for '" + uri + "'.");
		}
		if (!resource.getErrors().isEmpty()) {
			LOG.error(resource.getErrors());
			throw new IllegalStateException("Problem parsing '" + uri + "':" + resource.getErrors().toString());
		}

		final Grammar grammar = (Grammar) resource.getContents().get(0);
		validateGrammar(grammar);
		this.grammar = grammar;
	}
}

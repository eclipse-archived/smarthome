/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.lazygen;

import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.xtext.generator.Generator;

/**
 *
 * @author Holger Schill, Simon Kaufmann - Initial contribution and API
 *
 */
public class LazyGenerator extends Generator {
	
	LazyLanguageConfig langConfig = null;

	public void addLazyLanguage(LazyLanguageConfig langConfig) {
		this.langConfig = langConfig;
		super.addLanguage(langConfig);
	}

	@Override
	protected void invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		super.checkConfigurationInternal(issues);
		super.invokeInternal(ctx, monitor, issues);
	}

	@Override
	protected void checkConfigurationInternal(Issues issues) {

	}

}

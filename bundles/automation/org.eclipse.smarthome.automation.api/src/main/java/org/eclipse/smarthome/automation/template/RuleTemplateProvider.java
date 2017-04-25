/*******************************************************************************
 *
 * Copyright (c) 2016  Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 *******************************************************************************/
package org.eclipse.smarthome.automation.template;

import java.util.Locale;

import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;

/**
 * The {@link RuleTemplateProvider} provides basic functionality for managing {@link RuleTemplate}s.
 * It can be used for
 * <ul>
 * <li>Get the existing {@link RuleTemplate}s with the {@link Provider#getAll()},
 * {@link TemplateProvider#getTemplates(Locale)} and {@link #getTemplate(String, Locale)} methods.</li>
 * </ul>
 * Listers that are listening for adding removing or updating can be added with the
 * {@link #addProviderChangeListener(ProviderChangeListener)}
 * and removed with the {@link #removeProviderChangeListener(ProviderChangeListener)} methods.
 *
 * @author Ana Dimova
 */
public interface RuleTemplateProvider extends TemplateProvider<RuleTemplate> {

}

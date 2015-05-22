/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.parser.json;

import org.eclipse.smarthome.automation.template.Template;

/**
 * @author Yordan Mihaylov
 *
 */
public interface TemplateManager {

  /**
   * This method is used to get Template defined by its UID. The returned
   * Template is localized by default locale.
   * 
   * @param templateUID the an unique id in scope of all registered Templates
   * @return {@link Template} instance or null when Template with specified UID
   *         does not exists.
   */
  public Template getTemplate(String templateUID);

}

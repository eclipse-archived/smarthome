/*
 * Copyright (c) 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of ProSyst Software GmbH. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with ProSyst.
 */
package org.eclipse.smarthome.automation.parser.json;

import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateRegistry;

/**
 * @author Yordan Mihailov
 *
 */
public class TemplateManagerImpl implements TemplateManager {

  private TemplateRegistry templeteRegistry;

  /**
   * 
   */
  public TemplateManagerImpl(TemplateRegistry templeteRegistry) {
    this.templeteRegistry = templeteRegistry;
  }

  public Template getTemplate(String templateUID) {
    return templeteRegistry.get(templateUID, null);
  }

}

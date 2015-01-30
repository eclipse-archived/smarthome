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

package org.eclipse.smarthome.automation.template;

import java.util.Collection;
import java.util.Map;

import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.core.common.registry.Registry;

import org.eclipse.smarthome.automation.Input;
import org.eclipse.smarthome.automation.Output;
import org.eclipse.smarthome.automation.descriptor.Descriptor;

/**
 * This interface provides main functionality to manage {@link Template}s in the
 * Rule Engine. It can add {@link Template}s, get existing ones and remove them
 * from the Rule Engine. The {@link Template}s can be used by any creator of
 * Rules, but they can be modified only by its creator. The template
 * modification is done by updating its {@link Descriptor}.
 * 
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public interface TemplateRegistry extends Registry<RuleTemplate, String> {

  /**
   * This method is used to create a {@link Template} from descriptor
   * 
   * @param uid is an unique identifier of template in scope of RuleEngine
   * @param descriptor is a {@link Descriptor} which provides meta information
   *          describing {@link ConfigDescriptionParameter}s, {@link Input}s and
   *          {@link Output}s of of created template instance .
   * @return {@link Template} instance.
   */
  public <T extends Template> T createTemplate(String uid, Descriptor descriptor);

  /**
   * This method is used to update existing Template from RuleEngine.
   * 
   * @param template a template which has to be updated
   * @throws IllegalArgumentException when update can not be completed
   */
  public void update(Template template);

  /**
   * This method removes template specified by type.
   * 
   * @param uid an unique id of template which has to be removed.
   * @return true when the template is removed. False when the template of
   *         specified type does not exists.
   * @throws SecurityException when user, who is not owner of the template, is
   *           trying to remove it.
   */
  public boolean removeTemplate(String uid);

  /**
   * This method is used to get template of specified by type.
   * 
   * @param uid the an unique id in scope of registered templates
   * @return template instance or null.
   */
  public <T extends Template> T getTemplate(String uid);

  /**
   * This method is used for getting the templates filtered by tag.
   * 
   * @param tag specifies the filter for getting the templates, if it is
   *          <code>null</code> then returns all templates.
   * @return the templates, which correspond to the specified filter.
   */
  public <T extends Template> Collection<T> getTemplatesByTag(String tag);

  /**
   * This method is used for getting the templates, specified by type module,
   * i.e. {@link ActionTemplate}, {@link ConditionTemplate},
   * {@link TriggerTemplate} and etc.
   * 
   * @param moduleType the class of module which is looking for.
   * @return collection of templates, corresponding to specified type
   */
  public <T extends Template> Collection<T> getTemplates(Class<T> moduleType);

}

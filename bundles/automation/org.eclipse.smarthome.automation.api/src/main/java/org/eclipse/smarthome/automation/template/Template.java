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

import java.util.Set;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.descriptor.Descriptor;

/**
 * The templates define types of shared, ready to use components, which can be
 * instantiated and configured to produce automation objects instances (
 * {@link Module} and {@link Rule}). Each Template has a unique type and
 * {@link Descriptor}. The {@link Descriptor} defines meta info of building
 * parts of the automation objects.
 * <p>
 * The {@link Template}s can be used by any creator of Rules, but they can be
 * modified only by its creator. The template modification is done by updating
 * its {@link Descriptor}.
 * <p>
 * Templates can have
 * <li><code>tags</code> - non-hierarchical keywords or terms for describing
 * them.
 * 
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 *
 * @param <T> defines the type of {@link Descriptor}.
 */
public interface Template {

  /**
   * This method is used to get the descriptor of Template.
   * 
   * @return {@link Descriptor} of Template.
   */
  public Descriptor getDescriptor();

  /**
   * This method is used for getting the type of descriptor. It is unique in
   * scope of RuleEngine.
   * 
   * @return the type of descriptor
   */
  public String getUID();

  /**
   * Templates can have <li><code>tags</code> - non-hierarchical keywords or
   * terms for describing them. This method is used for getting the tags assign
   * to this Template. The tags are used to filter the templates.
   * 
   * @return a list of tags
   */
  public Set<String> getTags();

  /**
   * Templates can have <li><code>tags</code> - non-hierarchical keywords or
   * terms for describing them. This method is used for setting the tags to this
   * templates. The tags are used to filter the rules.
   * 
   * @param tags list of tags assign to this Rule.
   * @throws IllegalStateException IllegalStateException when the rule is in
   *           active state.
   */
  public void setTags(Set<String> tags);

  /**
   * This method is used for getting the label of the Template. The label is a
   * short, user friendly name of the Template defined by this descriptor.
   * 
   * @return the label of the Template.
   */
  public String getLabel();

  /**
   * This method is used for setting the label of the Template. The label is a
   * short, user friendly name of the Template defined by this descriptor.
   * 
   * @param label of the Template.
   */
  public void setLabel(String label);

  /**
   * This method is used for getting the description of the Template. The
   * description is a long, user friendly description of the Template defined by
   * this descriptor.
   * 
   * @return the description of the Template.
   */
  public String getDescription();

  /**
   * This method is used for setting the description of the Template. The
   * description is a long, user friendly description of the Template defined by
   * this descriptor.
   * 
   * @param description of the Template.
   */
  public void setDescription(String description);

}

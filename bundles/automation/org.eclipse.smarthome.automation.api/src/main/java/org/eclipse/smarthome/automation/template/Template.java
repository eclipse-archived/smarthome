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

import org.eclipse.smarthome.automation.Rule;

/**
 * The templates define types of shared, ready to use rule definitions, which
 * can be instantiated and configured to produce {@link Rule} instances . Each
 * Template has a unique id (UID).
 * <p>
 * The {@link Template}s can be used by any creator of Rules, but they can be modified only by its owner. The template
 * modification is done by updating the {@link Template} through {@link TemplateRegistry}
 * <p>
 * Templates can have <code>tags</code> - non-hierarchical keywords or terms for describing them.
 *
 * @author Yordan Mihaylov, Ana Dimova, Vasil Ilchev - Initial Contribution
 */
public interface Template {

    public enum Visibility {

        /**
         * The template is visible by everyone.
         */
        PUBLIC,

        /**
         * The template is visible only by its creator.
         */
        PRIVATE
    }

    /**
     * This method is used for getting the type of Template. It is unique in scope
     * of RuleEngine.
     *
     * @return the unique id of Template.
     */
    public String getUID();

    /**
     * Templates can have <li><code>tags</code> - non-hierarchical keywords or terms for describing them. The tags are
     * used to filter the templates. This method is used for getting the assign tags to this Template.
     *
     * @return tags of the template
     */
    public Set<String> getTags();

    /**
     * Templates can have <li><code>tags</code> - non-hierarchical keywords or terms for describing them. The tags are
     * used to filter the templates. This method is used for assigning tags to this Template.
     *
     * @param tags set of tags assign to the template.
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

    /**
     * This method is used to show visibility of the template
     *
     * @return visibility of template
     */
    public Visibility getVisibility();

}

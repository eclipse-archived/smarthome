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

package org.eclipse.smarthome.automation.type;

import java.util.Set;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * ModuleType defines meta info of {@link Module} instance which are building
 * blocks of the {@link Rule}. The meta info contains information about ( {@link ConfigDescriptionParameter}s,
 * {@link Input}s and {@link Output}s) and
 * defines an unique id which can be referred by the {@link Module} have {@link Input} elements.
 * <p>
 * The {@link ModuleType}s can be used by any creator of Rules, but they can be modified only by its owner.
 *
 * @author Yordan Mihaylov - Initial Contribution
 *
 */
public abstract class ModuleType {

    private String UID;
    private Visibility visibility = Visibility.PUBLIC;

    private Set<String> tags;
    private String label;
    private String description;

    protected Set<ConfigDescriptionParameter> configDescriptions;

    public enum Visibility {

        /**
         * The ModuleType is visible by everyone.
         */
        PUBLIC,

        /**
         * The ModuleType is visible only by its creator.
         */
        PRIVATE
    }

    /**
     * @param UID
     * @param configDescriptions
     * @param visability
     */
    public ModuleType(String UID, Set<ConfigDescriptionParameter> configDescriptions) {
        this.UID = UID;
        this.configDescriptions = configDescriptions;
    }

    /**
     * This method is used for getting the unique id (UID) of ModuleType. It is
     * unique in scope of RuleEngine. The UID can consists of segments separated
     * by column. The first segment contains system ModuleType, which corresponds
     * to the {@link ModuleHandler} of the same type, and the rest are optional and
     * contains UIDs of custom module type.
     *
     * @return the type of descriptor
     */
    public String getUID() {
        return UID;
    }

    /**
     * This method is used for getting the Set of {@link ConfigDescriptionParameter}s define by this module type.<br/>
     *
     * @return a {@link Set} of configuration descriptions.
     */
    public Set<ConfigDescriptionParameter> getConfigurationDescription() {
        return configDescriptions;
    }

    /**
     * ModuleTypes can have <li><code>tags</code> - non-hierarchical keywords or terms for describing them. The tags are
     * used to filter the ModuleTypes. This method is used for getting the tags assign to this ModuleType.
     *
     * @return tags assign to this ModuleType
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * ModuleTypes can have <li><code>tags</code> - non-hierarchical keywords or terms for describing them. The tags are
     * used to filter the ModuleTypes. This method is used for assigning tags to this ModuleType.
     *
     * @param tags set of tags assign to this Rule.
     */
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    /**
     * This method is used for getting the label of the ModuleType. The label is a
     * short, user friendly name of the ModuleType defined by this descriptor.
     *
     * @return the label of the ModuleType.
     */
    public String getLabel() {
        return label;
    }

    /**
     * This method is used for setting the label of the ModuleType. The label is a
     * short, user friendly name of the ModuleType defined by this descriptor.
     *
     * @param label of the ModuleType.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * This method is used for getting the description of the ModuleType. The
     * description is a long, user friendly description of the ModuleType defined
     * by this descriptor.
     *
     * @return the description of the ModuleType.
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method is used for setting the description of the ModuleType. The
     * description is a long, user friendly description of the ModuleType defined
     * by this descriptor.
     *
     * @param description of the ModuleType.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * This method is used to show visibility of the ModuleType. Default
     * visibility is {@link Visibility#PUBLIC}.
     *
     * @return visibility of ModuleType
     */
    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * This method is used to change visibility of module type. Default visibility
     * is {@link Visibility#PUBLIC}
     *
     * @param visibility new visibility value.
     */
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

}

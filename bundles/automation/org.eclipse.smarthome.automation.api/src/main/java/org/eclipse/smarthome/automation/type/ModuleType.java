/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.type;

import java.util.Set;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;

/**
 * Each {@link ModuleType} instance defines the meta-information needed for creation of {@link Module} instance which is
 * a building block for the {@link Rule}. The meta-information describes the {@link ConfigDescriptionParameter}s,
 * {@link Input}s and {@link Output}s of the {@link Module}s. Each {@link ModuleType} instance defines an unique id
 * which is used as reference from the {@link Module}s, to find their meta-information.
 * <p>
 * Whether the {@link ModuleType}s can be used by anyone, depends from their visibility, but they can be modified only
 * by their creator.
 * 
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * 
 */
public abstract class ModuleType {

    /**
     * This field is used as reference from the {@link Module}s, to find their meta-information.
     */
    private String UID;

    /**
     * The value of this field determines whether the {@link ModuleType}s can be used by anyone if they are
     * {@link Visibility#PUBLIC} or only by their creator if they are {@link Visibility#PRIVATE}.
     */
    private Visibility visibility = Visibility.PUBLIC;

    /**
     * Tags define categories that fit the particular {@link ModuleType} and which can serve as criteria for searching
     * or filtering of {@link ModuleType}s.
     */
    private Set<String> tags;

    /**
     * Short and accurate name of the {@link ModuleType}.
     */
    private String label;

    /**
     * Short and understandable description of that for what can be used the {@link ModuleType}.
     */
    private String description;

    protected Set<ConfigDescriptionParameter> configDescriptions;

    public enum Visibility {

        /**
         * The {@link ModuleType} is visible for anyone.
         */
        PUBLIC,

        /**
         * The {@link ModuleType} is visible only for its creator.
         */
        PRIVATE
    }

    /**
     * This constructor is responsible to initialize common base properties of the {@link ModuleType}s.
     * 
     * @param UID is an unique id of the {@link ModuleType}, used as reference from the {@link Module}s, to find their
     *            meta-information.
     * @param configDescriptions is a {@link Set} of meta-information configuration descriptions.
     */
    public ModuleType(String UID, Set<ConfigDescriptionParameter> configDescriptions) {
        this.UID = UID;
        this.configDescriptions = configDescriptions;
    }

    /**
     * This constructor is responsible to initialize all common properties of the {@link ModuleType}s.
     * 
     * @param UID unique id of the {@link ModuleType}.
     * @param configDescriptions is a {@link Set} of meta-information configuration descriptions.
     * @param label is a short and accurate name of the {@link ModuleType}.
     * @param description is a short and understandable description of which can be used the {@link ModuleType}.
     * @param tags defines categories that fit the {@link ModuleType} and which can serve as criteria for searching
     *            or filtering it.
     * @param visibility determines whether the {@link ModuleType} can be used by anyone if it is
     *            {@link Visibility#PUBLIC} or only by its creator if it is {@link Visibility#PRIVATE}.
     */
    public ModuleType(String UID, Set<ConfigDescriptionParameter> configDescriptions, String label, String description,
            Set<String> tags, Visibility visibility) {
        this(UID, configDescriptions);
        this.label = label;
        this.description = description;
        this.tags = tags;
        this.visibility = visibility;
    }

    /**
     * This method is used for getting the unique id (UID) of the {@link ModuleType}. It is unique in scope of
     * RuleEngine. The UID
     * can consists of segments separated by column. The first segment contains system {@link ModuleType}, which
     * corresponds to the {@link ModuleHandler} of the same type, and the rest are optional and contains UIDs of custom
     * the {@link ModuleType}s. It uses as reference from the {@link Module}s, to find their meta-information.
     * 
     * @return the unique id (UID) of the {@link ModuleType}, corresponding to the some type of {@link Module}s.
     */
    public String getUID() {
        return UID;
    }

    /**
     * This method is used for getting the Set of {@link ConfigDescriptionParameter}s defined by this {@link ModuleType}
     * .<br/>
     * 
     * @return a {@link Set} of meta-information configuration descriptions.
     */
    public Set<ConfigDescriptionParameter> getConfigurationDescription() {
        return configDescriptions;
    }

    /**
     * {@link ModuleType}s can have <li><code>tags</code> which are non-hierarchical keywords or terms for describing
     * them. The tags are used to filter the ModuleTypes. This method is used for getting the tags assign to this
     * {@link ModuleType}.
     * 
     * @return {@link #tags} assign to this {@link ModuleType}
     */
    public Set<String> getTags() {
        return tags;
    }

    /**
     * This method is used for getting the label of the {@link ModuleType}. The label is a
     * short, user friendly name of the {@link ModuleType}.
     * 
     * @return the {@link #label} of the {@link ModuleType}.
     */
    public String getLabel() {
        return label;
    }

    /**
     * This method is used for getting the description of the {@link ModuleType}. The description is a short and
     * understandable description of that for what can be used the {@link ModuleType}.
     * 
     * @return the {@link #description} of the ModuleType.
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method is used for getting visibility of the {@link ModuleType}. The visibility determines whether the
     * {@link ModuleType}s can be used by anyone if they are {@link Visibility#PUBLIC} or only by their creator if they
     * are {@link Visibility#PRIVATE}. The default visibility is {@link Visibility#PUBLIC}.
     * 
     * @return the {@link #visibility} of the {@link ModuleType}.
     */
    public Visibility getVisibility() {
        if (visibility == null) {
            return Visibility.PUBLIC;
        }
        return visibility;
    }

}

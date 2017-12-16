/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.template;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.core.common.registry.Identifiable;

/**
 * The templates define types of shared, ready to use definitions of automation objects, which
 * can be instantiated and configured to produce automation instances. Each Template has a unique id (UID).
 * <p>
 * The {@link Template}s can be used by any creator of automation objects, but they can be modified only by its owner.
 * <p>
 * Templates can have <code>tags</code> - non-hierarchical keywords or terms for describing them.
 *
 * @author Yordan Mihaylov - Initial Contribution
 * @author Ana Dimova - Initial Contribution
 * @author Vasil Ilchev - Initial Contribution
 */
@NonNullByDefault
public interface Template extends Identifiable<String> {

    /**
     * This method is used obtain the UID of a Template.
     *
     * @return the identifier of the Template.
     */
    @Override
    public String getUID();

    /**
     * Templates can have
     * <ul>
     * <li><code>tags</code> - non-hierarchical keywords or terms for describing them. The tags are used to filter the
     * templates. This method is used to obtain the assigned tags to a Template.</li>
     * </ul>
     *
     * @return tags of the template
     */
    public Set<String> getTags();

    /**
     * This method is used to obtain the label of a Template. The label is a short, human-readable label of the Template
     * defined by its creator.
     *
     * @return the label of the Template.
     */
    public @Nullable String getLabel();

    /**
     * This method is used to obtain the description of a Template. The description is a long, human-readable
     * description of the purpose of a Template, defined by its creator.
     *
     * @return the description of the Template.
     */
    public @Nullable String getDescription();

    /**
     * This method is used to show visibility of a Template.
     *
     * @return visibility of the Template.
     */
    public Visibility getVisibility();

}

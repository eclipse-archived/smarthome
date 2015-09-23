/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.icon;

import java.io.InputStream;
import java.util.Set;

import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.ui.icon.IconSet.Format;
import org.osgi.framework.BundleContext;

/**
 * This is an abstract base class for implementing icon providers that serve icons from file resources.
 * These files could be read from the file system, directly from the bundle itself or from somewhere else that can
 * provide an {@link InputStream}.
 *
 * The resources are expected to follow the naming convention "<category>[-<state>].<format>", e.g. "alarm.png" or
 * "alarm-on.svg".
 * Resource names must be all lower case. Whether an icon is provided or not is determined by the existence of a
 * resource without a state postfix.
 * If a specific resource for a state is available, it will be used. If not, the default icon without a state postfix is
 * used. If the state is a decimal number between 0 and 100, the implementation will look for a resource with the next
 * smaller state postfix available. Example: For category "DimmableLight" and state 84, it will check for the resources
 * dimmablelight-82.png, dimmablelight-81.png, dimmablelight-80.png and return the first one it can find.
 *
 * @author Kai Kreuzer
 *
 */
abstract public class AbstractResourceIconProvider implements IconProvider {

    /**
     * The OSGi bundle context
     */
    protected BundleContext context;

    /**
     * An I18nProvider service
     */
    protected I18nProvider i18nProvider;

    /**
     * When activating the service, we need to keep the bundle context.
     *
     * @param context the bundle context provided through OSGi DS.
     */
    protected void activate(BundleContext context) {
        this.context = context;
    }

    protected void setI18nProvider(I18nProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    protected void unsetI18nProvider(I18nProvider i18nProvider) {
        this.i18nProvider = null;
    }

    @Override
    public Set<IconSet> getIconSets() {
        return getIconSets(null);
    }

    @Override
    public Integer hasIcon(String category, String iconSetId, Format format) {
        return hasResource(iconSetId, category.toLowerCase() + "." + format.toString().toLowerCase()) ? getPriority()
                : null;
    }

    @Override
    public InputStream getIcon(String category, String iconSetId, String state, Format format) {
        String resourceWithState = category + ((state != null) ? "-" + state.toLowerCase() : "") + "."
                + format.toString().toLowerCase();
        if (hasResource(iconSetId, resourceWithState)) {
            return getResource(iconSetId, resourceWithState);
        } else {
            // let's treat all percentage-based categories
            try {
                Double stateAsDouble = Double.valueOf(state);
                if (stateAsDouble > 0 && stateAsDouble < 100) {
                    for (int i = stateAsDouble.intValue(); i >= 0; i--) {
                        String resourceWithNumberState = category + "-" + i + "." + format.toString().toLowerCase();
                        if (hasResource(iconSetId, resourceWithNumberState)) {
                            return getResource(iconSetId, resourceWithNumberState);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // does not seem to be a number, so ignore it
            }
            return getResource(iconSetId, category + "." + format.toString().toLowerCase());
        }
    }

    /**
     * Provides the priority of this provider. A higher value will give this provider a precedence over others.
     *
     * @return the priority as a positive integer
     */
    abstract protected Integer getPriority();

    /**
     * Provides the content of a resource for a certain icon set as a stream or null, if the resource does not exist.
     *
     * @param iconSetId the id of the icon set for which the resource is requested
     * @param resourceName the name of the resource
     * @return the content as a stream or null, if the resource does not exist
     */
    abstract protected InputStream getResource(String iconSetId, String resourceName);

    /**
     * Checks whether a certain resource exists for a given icon set.
     *
     * @param iconSetId the id of the icon set for which the resource is requested
     * @param resourceName the name of the resource
     * @return true, if the resource exists, false otherwise
     */
    abstract protected boolean hasResource(String iconSetId, String resourceName);

}
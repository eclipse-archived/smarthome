/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.provider.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.eclipse.smarthome.core.common.registry.Provider;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;

/**
 * This class is base for {@link MultipleModuleTypeFileProvider} and {@link MultipleTemplateFileProvider} which are
 * wrappers of multiple {@link ModuleTypeProvider}s and {@link TemplateProvider}s, responsible for importing the
 * automation objects from local file system.
 * <p>
 * It provides functionality for tracking {@link Parser} services and provides common functionality for notifying the
 * {@link ProviderChangeListener}s for adding, updating and removing the {@link ModuleType}s and {@link Template}s.
 *
 * @author Ana Dimova - Initial Contribution
 *
 */
public abstract class AbstractMultipleFileProvider<E, P extends AbstractFileProvider<E>>
        implements ProviderChangeListener<E>, Provider<E> {

    protected static final String ROOTS = "roots";

    protected Map<String, P> providers;
    protected String[] roots = new String[] { "automation" };
    protected Map<Parser<E>, Map<String, String>> parsers = new HashMap<Parser<E>, Map<String, String>>();
    protected List<ProviderChangeListener<E>> listeners = new ArrayList<ProviderChangeListener<E>>();

    public void deactivate() {
        roots = null;
        parsers.clear();
        listeners.clear();
        if (providers != null) {
            providers.clear();
        }
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<E> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<E> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public void added(Provider<E> provider, E element) {
        synchronized (listeners) {
            for (ProviderChangeListener<E> listener : listeners) {
                listener.added(this, element);
            }
        }
    }

    @Override
    public void removed(Provider<E> provider, E element) {
        synchronized (listeners) {
            for (ProviderChangeListener<E> listener : listeners) {
                listener.removed(this, element);
            }
        }
    }

    @Override
    public void updated(Provider<E> provider, E oldelement, E element) {
        synchronized (listeners) {
            for (ProviderChangeListener<E> listener : listeners) {
                listener.updated(this, oldelement, element);
            }
        }
    }

    public void addParser(Parser<E> parser, Map<String, String> properties) {
        parsers.put(parser, properties);
        if (providers != null) {
            for (AbstractFileProvider<E> provider : providers.values()) {
                provider.addParser(parser, properties);
            }
        }
    }

    public void removeParser(Parser<E> parser, Map<String, String> properties) {
        parsers.remove(parser);
        if (providers != null) {
            for (AbstractFileProvider<E> provider : providers.values()) {
                provider.removeParser(parser, properties);
            }
        }
    }

}

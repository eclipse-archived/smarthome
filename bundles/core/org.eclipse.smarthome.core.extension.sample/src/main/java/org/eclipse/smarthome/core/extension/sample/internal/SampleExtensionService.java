/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.extension.sample.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.extension.Extension;
import org.eclipse.smarthome.core.extension.ExtensionEventFactory;
import org.eclipse.smarthome.core.extension.ExtensionService;
import org.eclipse.smarthome.core.extension.ExtensionType;

/**
 * This is an implementation of an {@link ExtensionService} that can be used as a dummy service for testing the
 * functionality.
 * It is not meant to be used anywhere productively.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class SampleExtensionService implements ExtensionService {

    private EventPublisher eventPublisher;

    List<ExtensionType> types = new ArrayList<>(3);
    Map<String, Extension> extensions = new HashMap<>(30);

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    protected void activate() {
        types.add(new ExtensionType("binding", "Bindings"));
        types.add(new ExtensionType("ui", "User Interfaces"));
        types.add(new ExtensionType("persistence", "Persistence Services"));

        for (ExtensionType type : types) {
            for (int i = 0; i < 10; i++) {
                String id = type.getId() + Integer.toString(i);
                boolean installed = Math.random() > 0.5;
                String label = RandomStringUtils.randomAlphabetic(5) + " " + StringUtils.capitalize(type.getId());
                String typeId = type.getId();
                String version = "1.0";
                Extension extension = new Extension(id, typeId, label, version, installed);
                extensions.put(extension.getId(), extension);
            }
        }
    }

    protected void deactivate() {
        types.clear();
        extensions.clear();
    }

    @Override
    public void install(String id) {
        try {
            Thread.sleep((long) (Math.random() * 10000));
            Extension extension = getExtension(id, null);
            extension.setInstalled(true);
            postInstalledEvent(id);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void uninstall(String id) {
        try {
            Thread.sleep((long) (Math.random() * 5000));
            Extension extension = getExtension(id, null);
            extension.setInstalled(false);
            postUninstalledEvent(id);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public List<Extension> getExtensions(Locale locale) {
        return new ArrayList<>(extensions.values());
    }

    @Override
    public Extension getExtension(String id, Locale locale) {
        return extensions.get(id);
    }

    @Override
    public List<ExtensionType> getTypes(Locale locale) {
        return types;
    }

    private void postInstalledEvent(String extensionId) {
        if (eventPublisher != null) {
            Event event = ExtensionEventFactory.createExtensionInstalledEvent(extensionId);
            eventPublisher.post(event);
        }
    }

    private void postUninstalledEvent(String extensionId) {
        if (eventPublisher != null) {
            Event event = ExtensionEventFactory.createExtensionUninstalledEvent(extensionId);
            eventPublisher.post(event);
        }
    }
}

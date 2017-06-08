/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.notification.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.notification.EventFilterFactory;
import org.eclipse.smarthome.core.notification.NotificationService;
import org.eclipse.smarthome.model.core.EventType;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.core.ModelRepositoryChangeListener;
import org.eclipse.smarthome.model.notification.notification.ActionID;
import org.eclipse.smarthome.model.notification.notification.AllActions;
import org.eclipse.smarthome.model.notification.notification.AllEntities;
import org.eclipse.smarthome.model.notification.notification.AllNamespaces;
import org.eclipse.smarthome.model.notification.notification.AllTargets;
import org.eclipse.smarthome.model.notification.notification.EntityTypeID;
import org.eclipse.smarthome.model.notification.notification.EventConfiguration;
import org.eclipse.smarthome.model.notification.notification.Filter;
import org.eclipse.smarthome.model.notification.notification.NamespaceID;
import org.eclipse.smarthome.model.notification.notification.NotificationModel;
import org.eclipse.smarthome.model.notification.notification.Target;
import org.eclipse.smarthome.model.notification.notification.TargetID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * This class is the central part of the notification management and delegation logic. It reads the notification
 * models and manages the invocation of {@link NotificationService}s upon events.
 *
 * @author Karel Goderis - Initial contribution and API
 *
 */
public class NotificationManager implements ModelRepositoryChangeListener, EventSubscriber {

    private final Logger logger = LoggerFactory.getLogger(NotificationManager.class);

    private static NotificationManager instance;

    /* default */ModelRepository modelRepository;

    private final Map<String, EventFilterFactory> typedEventFilterFactories = new ConcurrentHashMap<String, EventFilterFactory>();

    /* default */Map<String, NotificationService> notificationServices = new HashMap<String, NotificationService>();

    /** keeps a list of configurations for each notification service */
    protected Map<String, List<EventConfiguration>> eventConfigurations = new ConcurrentHashMap<String, List<EventConfiguration>>();

    /** keeps a list of event filters for each notification service */
    protected Map<String, Map<String, EventFilter>> eventFilters = new ConcurrentHashMap<String, Map<String, EventFilter>>();

    public NotificationManager() {
        NotificationManager.instance = this;
    }

    static/* default */NotificationManager getInstance() {
        return instance;
    }

    public void activate() {
    }

    public void deactivate() {
    }

    public void setModelRepository(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
        modelRepository.addModelRepositoryChangeListener(this);
        for (String modelName : modelRepository.getAllModelNamesOfType("notify")) {
            String serviceName = modelName.substring(0, modelName.length() - ".notify".length());
            stopEventHandling(serviceName);
            startEventHandling(serviceName);
        }
    }

    public void unsetModelRepository(ModelRepository modelRepository) {
        modelRepository.removeModelRepositoryChangeListener(this);
        for (String modelName : modelRepository.getAllModelNamesOfType("notify")) {
            stopEventHandling(modelName);
        }
        this.modelRepository = null;
    }

    public void addNotificationService(NotificationService notificationService) {
        logger.debug("Initializing {} notification service.", notificationService.getName());
        notificationServices.put(notificationService.getName(), notificationService);
        stopEventHandling(notificationService.getName());
        startEventHandling(notificationService.getName());
    }

    public void removeNotificationService(NotificationService notificationService) {
        stopEventHandling(notificationService.getName());
        notificationServices.remove(notificationService.getName());
    }

    protected void addEventFilterFactory(EventFilterFactory eventFilterFactory) {
        Set<String> supportedEventTypes = eventFilterFactory.getSupportedEventFilterTypes();

        for (String supportedEventType : supportedEventTypes) {
            synchronized (this) {
                if (!typedEventFilterFactories.containsKey(supportedEventType)) {
                    typedEventFilterFactories.put(supportedEventType, eventFilterFactory);
                }
            }
        }
    }

    protected void removeEventFilterFactory(EventFilterFactory eventFactory) {
        Set<String> supportedEventFilterTypes = eventFactory.getSupportedEventFilterTypes();

        for (String supportedEventFilterType : supportedEventFilterTypes) {
            typedEventFilterFactories.remove(supportedEventFilterType);
        }
    }

    @Override
    public void modelChanged(String modelName, EventType type) {
        if (modelName.endsWith(".notify")) {
            String serviceName = modelName.substring(0, modelName.length() - ".notify".length());
            if (type == EventType.REMOVED || type == EventType.MODIFIED) {
                stopEventHandling(serviceName);
            }

            if (type == EventType.ADDED || type == EventType.MODIFIED) {
                if (notificationServices.containsKey(serviceName)) {
                    startEventHandling(serviceName);
                }
            }
        }
    }

    /**
     * Registers a notification model file with the notification manager, so that it becomes active.
     *
     * @param modelName the name of the notification model without file extension
     */
    private void startEventHandling(String modelName) {
        if (modelRepository != null) {
            NotificationModel model = (NotificationModel) modelRepository.getModel(modelName + ".notify");
            if (model != null) {
                // set up the filters
                for (Filter filter : model.getFilters()) {
                    if (filter.getFilterType() != null) {
                        EventFilterFactory eventFilterFactory = typedEventFilterFactories.get(filter.getFilterType());
                        if (eventFilterFactory != null) {
                            EventFilter eventFilter = null;
                            try {
                                eventFilter = eventFilterFactory.createEventFilter(filter.getFilterType(),
                                        filter.getOptions());
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            if (eventFilter != null) {
                                if (eventFilters.get(modelName) == null) {
                                    eventFilters.put(modelName, new HashMap<String, EventFilter>());
                                }
                                eventFilters.get(modelName).put(filter.getName(), eventFilter);
                            }
                        }
                    }
                }

                eventConfigurations.put(modelName, model.getConfigs());

            }
        }
    }

    /**
     * Unregisters a notification model file from the notification manager, so that it is not further regarded.
     *
     * @param modelName the name of the notification model without file extension
     */
    private void stopEventHandling(String modelName) {
        eventConfigurations.remove(modelName);
        eventFilters.remove(modelName);
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return ImmutableSet.of(ALL_EVENT_TYPES);
    }

    @Override
    public EventFilter getEventFilter() {
        // we want to received all Types of events as we do our own filtering
        return null;
    }

    @Override
    public void receive(Event event) {
        logger.debug("Received and event Type {} Topic {}", event.getType(), event.getTopic());
        synchronized (eventConfigurations) {
            for (Entry<String, List<EventConfiguration>> entry : eventConfigurations.entrySet()) {
                String serviceName = entry.getKey();
                if (notificationServices.containsKey(serviceName)) {
                    for (EventConfiguration config : entry.getValue()) {
                        if (matchTopic(event, config)) {
                            if (applyFilters(event, config.getFilters(), eventFilters.get(serviceName))) {
                                for (Target target : config.getTargets()) {
                                    notificationServices.get(serviceName).notify(target.getName(),
                                            target.getTargetOptions(), event);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean matchTopic(Event event, EventConfiguration config) {

        boolean namespaceMatch = false;
        boolean entityTypeMatch = false;
        boolean targetMatch = false;
        boolean actionMatch = false;

        String[] topicElements = event.getTopic().split("/");
        if (topicElements.length != 4)
            throw new IllegalArgumentException("Invalid topic: " + event.getTopic());

        if (config.getNamespacedetail() instanceof AllNamespaces) {
            namespaceMatch = true;
        }
        if (config.getNamespacedetail() instanceof NamespaceID) {
            NamespaceID namespaceDetail = (NamespaceID) config.getNamespacedetail();
            if (topicElements[0].equals(namespaceDetail.getNamespace())) {
                namespaceMatch = true;
            }
        }

        if (config.getEntitydetail() instanceof AllEntities) {
            entityTypeMatch = true;
        }
        if (config.getEntitydetail() instanceof EntityTypeID) {
            EntityTypeID entityTypeDetail = (EntityTypeID) config.getEntitydetail();
            if (topicElements[1].equals(entityTypeDetail.getEntity())) {
                entityTypeMatch = true;
            }
        }

        if (config.getTargetdetail() instanceof AllTargets) {
            targetMatch = true;
        }
        if (config.getTargetdetail() instanceof TargetID) {
            TargetID targetDetail = (TargetID) config.getTargetdetail();
            if (topicElements[2].equals(targetDetail.getTarget())) {
                targetMatch = true;
            }
        }

        if (config.getActiondetail() instanceof AllActions) {
            actionMatch = true;
        }
        if (config.getActiondetail() instanceof ActionID) {
            ActionID actionDetail = (ActionID) config.getActiondetail();
            if (topicElements[3].equals(actionDetail.getAction())) {
                actionMatch = true;
            }
        }

        return namespaceMatch && entityTypeMatch && targetMatch && actionMatch;

    }

    private boolean applyFilters(Event event, EList<Filter> configFilters, Map<String, EventFilter> eventFilters) {

        for (Filter filter : configFilters) {
            EventFilter eventFilter = eventFilters.get(filter.getName());

            if (eventFilter == null || !eventFilter.apply(event)) {
                return false;
            }
        }

        return true;
    }
}

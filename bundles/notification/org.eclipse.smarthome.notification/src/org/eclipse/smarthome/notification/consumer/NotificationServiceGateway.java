/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.notification.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.model.core.EventType;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.core.ModelRepositoryChangeListener;
import org.eclipse.smarthome.model.notification.notification.EventConfiguration;
import org.eclipse.smarthome.model.notification.notification.Filter;
import org.eclipse.smarthome.model.notification.notification.NotificationModel;
import org.eclipse.smarthome.model.notification.notification.Target;
import org.eclipse.smarthome.notification.Notification;
import org.eclipse.smarthome.notification.consumer.filter.EventFilterFactory;
import org.eclipse.smarthome.notification.events.NotificationAddedEvent;
import org.eclipse.smarthome.notification.events.NotificationRemovedEvent;
import org.eclipse.smarthome.notification.events.NotificationUpdatedEvent;
import org.eclipse.smarthome.notification.manager.NotificationManager;
import org.eclipse.smarthome.notification.producer.NotificationServiceBridge;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * This class is the central part of the notification service management and delegation logic. It reads the notification
 * models and manages the invocation of {@link NotificationService}s upon events.
 *
 * @author Karel Goderis - Initial contribution and API
 *
 */
public class NotificationServiceGateway implements ModelRepositoryChangeListener, EventSubscriber {

    private final Logger logger = LoggerFactory.getLogger(NotificationServiceGateway.class);

    private ModelRepository modelRepository;
    private BundleContext bundleContext;

    private Map<String, EventFilterFactory> typedEventFilterFactories = new ConcurrentHashMap<String, EventFilterFactory>();
    private Map<String, NotificationService> notificationServices = new HashMap<String, NotificationService>();
    private Map<String, NotificationServiceBridge> notificationServiceBridges = new HashMap<String, NotificationServiceBridge>();
    private Map<String, Target> serviceTargets = new HashMap<String, Target>();
    private NotificationManager notificationManager;

    private final Set<String> subscribedEventTypes = ImmutableSet.of(NotificationAddedEvent.TYPE,
            NotificationRemovedEvent.TYPE, NotificationUpdatedEvent.TYPE);

    public NotificationServiceGateway() {
    }

    public void activate(ComponentContext componentContext) {
        this.bundleContext = componentContext.getBundleContext();
    }

    public void deactivate() {
        this.bundleContext = null;
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

    public void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void unsetNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = null;
    }

    public void addNotificationService(NotificationService notificationService) {
        logger.debug("Initializing the '{}' notification service.", notificationService.getName());
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

    @Override
    public void receive(Event event) {
        if (event instanceof NotificationAddedEvent) {
            // logger.debug("Received an event Type {} Topic {}", event.getType(), event.getTopic());
            Notification notification = ((NotificationAddedEvent) event).getNotification();
            String source = StringUtils.split(notification.getType(), ":")[0];
            String serviceID = StringUtils.split(notification.getType(), ":")[1];
            String target = StringUtils.split(notification.getType(), ":")[2];
            if (source.equals(NotificationServiceBridge.class.getSimpleName()) && serviceID != null && target != null) {
                logger.debug("Received a notification for Service {} Target {}", serviceID, target);

                if (modelRepository != null) {
                    NotificationModel model = (NotificationModel) modelRepository.getModel(serviceID + ".notify");
                    if (model != null) {
                        List<Target> targets = model.getTargets();
                        for (Target serviceTarget : targets) {
                            if (serviceTarget.getName().equals(target)) {
                                NotificationService service = notificationServices.get(serviceID);
                                if (service != null) {
                                    service.notify(serviceTarget.getName(), serviceTarget.getTargetOptions(),
                                            notification);
                                } else {
                                    logger.warn(
                                            "The Notification Service Gateway does not support a service of type '{}'",
                                            serviceID);
                                }

                                break;
                            }
                        }

                        logger.debug("Removing the notification with ID '{}' after delivery", notification.getUID());
                        notificationManager.remove(notification.getUID());

                    }
                }
            }

        }
    }

    /**
     * Registers a notification model file with the notification service gateway, so that it becomes active.
     *
     * @param modelName the name of the notification model without file extension
     */
    private void startEventHandling(String modelName) {

        List<EventConfiguration> eventConfigurations = new ArrayList<EventConfiguration>();
        Map<String, EventFilter> eventFilters = new HashMap<String, EventFilter>();

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
                                eventFilters.put(filter.getName(), eventFilter);
                            }
                        }
                    }
                }

                for (EventConfiguration configuration : model.getConfigs()) {
                    eventConfigurations.add(configuration);
                }

                NotificationServiceBridge bridge = new NotificationServiceBridge(modelName, eventFilters,
                        eventConfigurations, bundleContext);
                notificationServiceBridges.put(modelName, bridge);
                bridge.activate();

            }
        }
    }

    /**
     * Unregisters a notification model file from the notification manager, so that it is not further regarded.
     *
     * @param modelName the name of the notification model without file extension
     */
    private void stopEventHandling(String modelName) {
        NotificationServiceBridge bridge = notificationServiceBridges.remove(modelName);
        if (bridge != null)
            bridge.deactivate();
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    @Override
    public EventFilter getEventFilter() {
        // we want to received all Types of events as we do our own filtering
        return null;
    }

}

/*******************************************************************************
 *
 * Copyright (c) 2016  Bosch Software Innovations GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * The Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Plamen Peev - Bosch Software Innovations GmbH - Please refer to git log
 *
 *******************************************************************************/
package org.eclipse.smarthome.automation.sample.moduletype.demo.internal.handlers;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.sample.moduletype.demo.internal.factory.HandlerFactory;
import org.eclipse.smarthome.config.core.Configuration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * This class is handler for 'ConsoleTrigger' {@link Trigger}s.
 *
 * <pre>
 * Example usage:
 *
 * "id":"RuleTrigger",
 * "type":"ConsoleTrigger",
 * "configuration":{
 *    "eventTopic":"demo/topic",
 *    "keyName":"key"
 * }
 * </pre>
 * 
 * @author Plamen Peev - Initial contribution
 */
public class ConsoleTrigger extends BaseModuleHandler<Trigger> implements TriggerHandler, EventHandler {

    /**
     * This constant is used by {@link HandlerFactory} to create a correct handler instance. It must be the same as in
     * JSON definition of the module type.
     */
    public final static String UID = "ConsoleTrigger";

    /**
     * This constant is used to get the value of the 'eventTopic' property from {@link Trigger}'s {@link Configuration}.
     */
    private static final String EVENT_TOPIC = "eventTopic";

    /**
     * This constant is used to get the name of the entry of the incoming event where should be the value for the output
     * of this trigger.
     */
    private static final String KEY = "keyName";

    /**
     * This constant defines the output name of this {@link Trigger} handler.
     */
    private static final String OUTPUT_NAME = "outputValue";

    /**
     * This field will contain the event topic with which this {@link Trigger} handler is subscribed for {@link Event}s.
     */
    private final String eventTopic;

    /**
     * A bundle's execution context within the Framework.
     */
    private final BundleContext context;

    /**
     * This field will contain the name of the entry that contains the value for trigger's output
     */
    private final String keyName;

    /**
     * This is a callback to RuleEngine which is used by the {@link TriggerHandler} to notify the RuleEngine about
     * firing of this {@link Trigger} handler.
     */
    private RuleEngineCallback ruleCallback;

    /**
     * This field stores the service registration of this {@link Trigger} handler as {@link EventHandler} in the
     * framework.
     */
    @SuppressWarnings("rawtypes")
    private ServiceRegistration registration;

    /**
     * Constructs a {@link ConsoleTrigger} instance.
     *
     * @param module - the {@link Trigger} for which the instance is created.
     * @param context - a bundle's execution context within the Framework.
     */
    public ConsoleTrigger(final Trigger module, final BundleContext context) {
        super(module);

        if (module == null) {
            throw new IllegalArgumentException("'module' can not be null.");
        }
        final Configuration configuration = module.getConfiguration();
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration can't be null.");
        }
        eventTopic = (String) configuration.get(EVENT_TOPIC);
        if (eventTopic == null) {
            throw new IllegalArgumentException("'eventTopic' can not be null.");
        }
        keyName = (String) configuration.get(KEY);
        if (keyName == null) {
            throw new IllegalArgumentException("'keyName' can not be null.");
        }
        this.context = context;
    }

    /**
     * This method is called from {@link EventAdmin} service.
     * It gets the value of 'value' event property, pass it to the output of this {@link Trigger} handler and notifies
     * the RuleEngine that this handler is fired.
     *
     * @param event - {@link Event} that is passed from {@link EventAdmin} service.
     */
    @Override
    public void handleEvent(final Event event) {
        final Integer outputValue = (Integer) event.getProperty(keyName);
        final Map<String, Object> outputProps = new HashMap<String, Object>();
        outputProps.put(OUTPUT_NAME, outputValue);
        ruleCallback.triggered(module, outputProps);
    }

    /**
     * This method is used to set a callback object to the RuleEngine
     *
     * @param ruleCallback a callback object to the RuleEngine.
     */
    @Override
    public void setRuleEngineCallback(final RuleEngineCallback ruleCallback) {
        this.ruleCallback = ruleCallback;
        final Dictionary<String, Object> registrationProperties = new Hashtable<String, Object>();
        registrationProperties.put(EventConstants.EVENT_TOPIC, eventTopic);
        registration = context.registerService(EventHandler.class, this, registrationProperties);
    }

    /**
     * This method is used to unregister this handler.
     */
    @Override
    public void dispose() {
        registration.unregister();
        registration = null;
        super.dispose();
    }

}

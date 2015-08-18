package org.eclipse.smarthome.automation.module.handler;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.AbstractModuleHandler;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.handler.TriggerHandler;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.events.TopicEventFilter;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class GenericEventTriggerHandler extends AbstractModuleHandler implements TriggerHandler, EventSubscriber {

	private RuleEngineCallback callback;
	private ModuleTypeRegistry moduleTypeRegistry;
	private Map<String, Object> config;
	private String source;
	private String topic;
	private Set<String> types;
	private EventFilter filter;
	private Trigger trigger;

	private static final String CFG_EVENT_TOPIC = "eventTopic";
	private static final String CFG_EVENT_SOURCE = "eventSource";
	private static final String CFG_EVENT_TYPES = "eventTypes";

	public GenericEventTriggerHandler(Module module, ModuleTypeRegistry moduleTypeRegistry) {
		super(module);
		this.trigger = (Trigger) module;
		this.moduleTypeRegistry = moduleTypeRegistry;
		this.config = getResolvedConfiguration(null);
		this.source = (String) config.get(CFG_EVENT_SOURCE);
		this.topic = (String) config.get(CFG_EVENT_TOPIC);
		this.types = Sets.newHashSet(((String) config.get(CFG_EVENT_TYPES)).split(","));
		this.filter = new TopicEventFilter(topic);
	}

	@Override
	public void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
		this.callback = ruleCallback;
	}

	@Override
	protected ModuleTypeRegistry getModuleTypeRegistry() {
		return this.moduleTypeRegistry;
	}

	@Override
	public Set<String> getSubscribedEventTypes() {
		return types;
	}

	@Override
	public EventFilter getEventFilter() {
		return filter;
	}

	@Override
	public void receive(Event event) {
		if (!this.source.equals(event.getSource())) {
			return;
		}
		Map<String, Object> values = Maps.newHashMap();
		values.put("source", event.getSource());
		values.put("payload", event.getPayload());
		values.put("type", event.getType());
		values.put("topic", event.getTopic());
		callback.triggered(this.trigger, getResolvedOutputs(config, null, values));
	}

}

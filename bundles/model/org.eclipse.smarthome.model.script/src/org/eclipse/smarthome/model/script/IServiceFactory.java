package org.eclipse.smarthome.model.script;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.model.core.ModelRepository;
import org.eclipse.smarthome.model.script.engine.ScriptEngine;
import org.eclipse.smarthome.model.script.engine.action.ActionService;

public interface IServiceFactory {

    void addActionService(ActionService actionService);

    void removeActionService(ActionService actionService);

    void setItemRegistry(ItemRegistry itemRegistry);

    void unsetItemRegistry(ItemRegistry itemRegistry);

    void setEventPublisher(EventPublisher eventPublisher);

    void unsetEventPublisher(EventPublisher eventPublisher);

    void setModelRepository(ModelRepository modelRepository);

    void unsetModelRepository(ModelRepository modelRepository);

    void setScriptEngine(ScriptEngine scriptEngine);

    void unsetScriptEngine(ScriptEngine scriptEngine);

}
package org.eclipse.smarthome.automation.core.internal.composite;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Connection;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.core.internal.RuleEngine;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.type.ModuleType;

public class AbstractCompositeModuleHandler<M extends Module, MT extends ModuleType, H extends ModuleHandler>
        implements ModuleHandler {

    protected LinkedHashMap<M, H> moduleHandlerMap;
    protected M module;
    protected MT moduleType;

    public AbstractCompositeModuleHandler(M module, MT moduleType, LinkedHashMap<M, H> mapModuleToHandler) {
        this.module = module;
        this.moduleType = moduleType;
        this.moduleHandlerMap = mapModuleToHandler;
    }

    protected Map<String, ?> getCompositeContext(Map<String, ?> context, Module module) {
        Map<String, Object> result = new HashMap<>(context);
        for (Entry<String, Object> config : module.getConfiguration().entrySet()) {
            result.put("$" + config.getKey(), config.getValue());
        }

        Set<Connection> connections = null;

        if (module instanceof Condition) {
            connections = ((Condition) module).getConnections();
        } else if (module instanceof Action) {
            connections = ((Action) module).getConnections();
        }

        if (connections != null) {
            for (Connection connection : connections) {
                Object o = context
                        .get(connection.getOuputModuleId() + RuleEngine.OUTPUT_SEPARATOR + connection.getOutputName());
                result.put("$" + connection.getInputName(), o);
            }
        }
        return result;
    }

    protected void updateChildConfig(Module child, Map<String, ?> compositeContext) {
        for (Entry<String, Object> config : child.getConfiguration().entrySet()) {
            Object o = config.getValue();
            if (o instanceof String) {
                String key = (String) o;
                if (isReference(key)) {
                    Object contextValue = compositeContext.get(key);
                    config.setValue(contextValue);
                }
            }
        }
    }

    protected boolean isReference(String ref) {
        return ref.startsWith("$") && ref.length() > 1;
    }

    @Override
    public void dispose() {
        if (moduleHandlerMap != null) {
            moduleHandlerMap.clear();
            moduleHandlerMap = null;
        }
    }

}

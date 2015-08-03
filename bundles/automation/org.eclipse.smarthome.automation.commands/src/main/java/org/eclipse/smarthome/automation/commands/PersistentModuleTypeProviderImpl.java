package org.eclipse.smarthome.automation.commands;

import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.provider.util.PersistableModuleType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.osgi.framework.BundleContext;

public class PersistentModuleTypeProviderImpl extends ModuleTypeProviderImpl<PersistableModuleType> {

    /**
     * This constructor extends the parent constructor functionality with initializing the version of persistence.
     * 
     * @param context is the {@code BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    public PersistentModuleTypeProviderImpl(BundleContext context) {
        super(context, PersistentModuleTypeProviderImpl.class);
    }

    @Override
    protected String getKey(ModuleType element) {
        return element.getUID();
    }

    @Override
    protected String getStorageName() {
        return "commands_module_types";
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }
    
    @Override
    protected ModuleType toElement(String key, PersistableModuleType persistableElement) {
     // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected PersistableModuleType toPersistableElement(ModuleType element) {
        // TODO Auto-generated method stub
        return null;
    }
}

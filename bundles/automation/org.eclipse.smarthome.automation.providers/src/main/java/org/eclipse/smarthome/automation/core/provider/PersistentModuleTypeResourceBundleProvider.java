package org.eclipse.smarthome.automation.core.provider;

import org.eclipse.smarthome.automation.provider.util.PersistableModuleType;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.osgi.framework.BundleContext;

public class PersistentModuleTypeResourceBundleProvider extends ModuleTypeResourceBundleProvider<PersistableModuleType> {

    public PersistentModuleTypeResourceBundleProvider(BundleContext context) {
        super(context, PersistentModuleTypeResourceBundleProvider.class);
        isReady = true;
    }

    @Override
    protected String getStorageName() {
        return "providers_module_types";
    }

    @Override
    protected String getKey(ModuleType element) {
        return element.getUID();
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
        return new PersistableModuleType();
    }

}

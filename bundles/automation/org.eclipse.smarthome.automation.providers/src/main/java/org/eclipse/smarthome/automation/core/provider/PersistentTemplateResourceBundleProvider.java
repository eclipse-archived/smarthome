package org.eclipse.smarthome.automation.core.provider;

import org.eclipse.smarthome.automation.provider.util.PersistableRuleTemplate;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.osgi.framework.BundleContext;

public class PersistentTemplateResourceBundleProvider extends TemplateResourceBundleProvider<PersistableRuleTemplate> {

    public PersistentTemplateResourceBundleProvider(BundleContext context) {
        super(context, PersistentTemplateResourceBundleProvider.class);
        isReady = true;
    }

    @Override
    protected String getKey(RuleTemplate element) {
        return element.getUID();
    }

    @Override
    protected String getStorageName() {
        return "providers_templates";
    }

    @Override
    protected String keyToString(String key) {
        return key;
    }

    @Override
    protected RuleTemplate toElement(String key, PersistableRuleTemplate persistableElement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected PersistableRuleTemplate toPersistableElement(RuleTemplate element) {
        return new PersistableRuleTemplate();
    }


}

package org.eclipse.smarthome.automation.commands;

import org.eclipse.smarthome.automation.provider.util.PersistableRuleTemplate;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.osgi.framework.BundleContext;

public class PersistentTemplateProviderImpl extends TemplateProviderImpl<PersistableRuleTemplate> {

    public PersistentTemplateProviderImpl(BundleContext context) {
        super(context, PersistentTemplateProviderImpl.class);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String getKey(RuleTemplate element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getStorageName() {
        return "commands_templates";
    }

    @Override
    protected String keyToString(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected RuleTemplate toElement(String key, PersistableRuleTemplate persistableElement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected PersistableRuleTemplate toPersistableElement(RuleTemplate element) {
        // TODO Auto-generated method stub
        return null;
    }
}
